package app.lantext.sms

import android.Manifest
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import app.lantext.util.PhoneNumbers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(private val context: Context) {
    data class Contact(
        val id: String,
        val name: String,
        val number: String,
        val normalized: String,
        val photoUri: Uri?,
        val color: String,
    )

    private val region = PhoneNumbers.defaultRegion(context)
    @Volatile private var cache: List<Contact> = emptyList()
    @Volatile private var byNumber: Map<String, Contact> = emptyMap()

    suspend fun refresh() = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val list = mutableListOf<Contact>()
        val map = HashMap<String, Contact>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
        )
        resolver.query(uri, projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC")
            ?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.string(0)
                    val name = cursor.string(1).ifBlank { cursor.string(2) }
                    val number = cursor.string(2)
                    val normalized = PhoneNumbers.normalize(number, region)
                    val photoId = cursor.getLong(3)
                    val photo = if (photoId > 0) {
                        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id.toLongOrNull() ?: 0)
                            .buildUpon()
                            .appendPath(ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
                            .build()
                    } else {
                        null
                    }
                    val contact = Contact(id, name, number, normalized, photo, avatarColor(name.ifBlank { number }))
                    list += contact
                    if (normalized.isNotBlank()) map.putIfAbsent(normalized, contact)
                    val digits = PhoneNumbers.digitsKeepPlus(number)
                    if (digits.isNotBlank()) map.putIfAbsent(digits, contact)
                }
            }
        cache = list.distinctBy { it.id + it.normalized }
        byNumber = map
    }

    fun lookup(address: String?): Contact? {
        val raw = address.orEmpty()
        if (raw.isBlank()) return null
        val normalized = PhoneNumbers.normalize(raw, region)
        return byNumber[normalized] ?: byNumber[PhoneNumbers.digitsKeepPlus(raw)]
    }

    fun search(query: String): List<ContactDto> {
        val q = query.trim()
        val source = if (q.isEmpty()) cache.take(40) else cache.filter {
            it.name.contains(q, ignoreCase = true) || it.number.contains(q) || it.normalized.contains(q)
        }.take(40)
        return source.map { toDto(it) }
    }

    suspend fun createContact(name: String, number: String): ContactDto = withContext(Dispatchers.IO) {
        val display = name.trim()
        val phone = number.trim()
        require(display.isNotEmpty()) { "Name is required" }
        require(phone.isNotEmpty()) { "Number is required" }
        requireWrite()
        val account = preferredAccount()
        val given = display.substringBefore(' ').ifBlank { display }
        val family = display.substringAfter(' ', "").trim()
        val ops = ArrayList<ContentProviderOperation>()
        ops += ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
            .build()
        ops += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, display)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, given)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, family.ifBlank { null })
            .build()
        ops += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
            .withValue(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY, 1)
            .withValue(ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY, 1)
            .build()
        try {
            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException(contactWriteError(e, "Could not save the contact"))
        }
        refresh()
        lookup(phone)?.let { toDto(it) } ?: ContactDto(
            id = "",
            name = display,
            number = phone,
            photoUrl = null,
            avatarColor = avatarColor(display),
        )
    }

    suspend fun addPhoneToContact(contactId: String, number: String): ContactDto = withContext(Dispatchers.IO) {
        val phone = number.trim()
        require(phone.isNotEmpty()) { "Number is required" }
        val id = contactId.toLongOrNull() ?: throw IllegalArgumentException("Invalid contact")
        requireWrite()
        val rawId = writableRawContactId(id)
            ?: throw IllegalStateException("Could not find a writable copy of that contact")
        val values = ContentValues().apply {
            put(ContactsContract.Data.RAW_CONTACT_ID, rawId)
            put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
            put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
        }
        val inserted = try {
            context.contentResolver.insert(ContactsContract.Data.CONTENT_URI, values)
        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException(contactWriteError(e, "Could not add that number"))
        }
        if (inserted == null) {
            throw IllegalStateException("Could not add that number to the contact")
        }
        refresh()
        cache.firstOrNull { it.id == contactId }?.let { toDto(it) }
            ?: lookup(phone)?.let { toDto(it) }
            ?: throw IllegalStateException("Saved, but could not reload the contact")
    }

    fun photoBytes(number: String): ByteArray? {
        val contact = lookup(number) ?: return null
        val uri = contact.photoUri ?: return null
        return try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (_: Exception) {
            null
        }
    }

    fun displayName(address: String?): String {
        lookup(address)?.name?.let { return it }
        return PhoneNumbers.display(address, region).ifBlank { address.orEmpty() }
    }

    fun detailsForNumbers(numbers: List<String>): List<ContactDetailsDto> {
        val seen = LinkedHashSet<String>()
        val out = mutableListOf<ContactDetailsDto>()
        for (raw in numbers) {
            val number = raw.trim()
            if (number.isBlank()) continue
            val key = PhoneNumbers.normalize(number, region).ifBlank { PhoneNumbers.digitsKeepPlus(number) }
            if (!seen.add(key.ifBlank { number })) continue
            val contact = lookup(number)
            out += if (contact == null) {
                ContactDetailsDto(
                    id = null,
                    name = PhoneNumbers.display(number, region).ifBlank { number },
                    photoUrl = null,
                    avatarColor = avatarColor(number),
                    phones = listOf(LabeledValue("Phone", number)),
                )
            } else {
                loadDetails(contact)
            }
        }
        return out
    }

    private fun loadDetails(contact: Contact): ContactDetailsDto {
        val phones = mutableListOf<LabeledValue>()
        val emails = mutableListOf<LabeledValue>()
        var org: String? = null
        var title: String? = null
        var postal: String? = null
        context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.Data.DATA1,
                ContactsContract.Data.DATA2,
                ContactsContract.Data.DATA3,
                ContactsContract.Data.DATA4,
            ),
            "${ContactsContract.Data.CONTACT_ID}=?",
            arrayOf(contact.id),
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val mime = cursor.getString(0).orEmpty()
                val data1 = cursor.getString(1).orEmpty()
                if (data1.isBlank()) continue
                when (mime) {
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE ->
                        phones += LabeledValue(phoneLabel(cursor.getInt(2), cursor.getString(3)), data1)
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE ->
                        emails += LabeledValue(emailLabel(cursor.getInt(2), cursor.getString(3)), data1)
                    ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE -> {
                        if (org.isNullOrBlank()) org = data1
                        val job = cursor.getString(4)
                        if (title.isNullOrBlank() && !job.isNullOrBlank()) title = job
                    }
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE ->
                        if (postal.isNullOrBlank()) postal = data1
                }
            }
        }
        if (phones.none { it.value == contact.number }) {
            phones.add(0, LabeledValue("Phone", contact.number))
        }
        return ContactDetailsDto(
            id = contact.id,
            name = contact.name,
            photoUrl = photoUrl(contact.number),
            avatarColor = contact.color,
            phones = phones.distinctBy { it.value },
            emails = emails.distinctBy { it.value },
            org = org,
            title = title,
            postal = postal,
        )
    }

    private fun phoneLabel(type: Int, custom: String?): String = when (type) {
        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "Mobile"
        ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "Home"
        ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "Work"
        ContactsContract.CommonDataKinds.Phone.TYPE_MAIN -> "Main"
        ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK -> "Work fax"
        ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM -> custom?.ifBlank { "Phone" } ?: "Phone"
        else -> "Phone"
    }

    private fun emailLabel(type: Int, custom: String?): String = when (type) {
        ContactsContract.CommonDataKinds.Email.TYPE_HOME -> "Home"
        ContactsContract.CommonDataKinds.Email.TYPE_WORK -> "Work"
        ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM -> custom?.ifBlank { "Email" } ?: "Email"
        else -> "Email"
    }

    fun photoUrl(address: String?): String? {
        val contact = lookup(address) ?: return null
        if (contact.photoUri == null) return null
        val key = contact.normalized.ifBlank { contact.number }
        return "/api/v1/contacts/photo?number=${Uri.encode(key)}"
    }

    private fun toDto(contact: Contact): ContactDto = ContactDto(
        id = contact.id,
        name = contact.name,
        number = contact.number,
        photoUrl = "/api/v1/contacts/photo?number=${Uri.encode(contact.normalized.ifBlank { contact.number })}",
        avatarColor = contact.color,
    )

    private fun requireWrite() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            throw IllegalStateException("Allow contacts access in the LanText app so it can save names and numbers.")
        }
    }

    private data class ContactAccount(val type: String?, val name: String?)

    private fun preferredAccount(): ContactAccount {
        val counts = LinkedHashMap<ContactAccount, Int>()
        context.contentResolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(
                ContactsContract.RawContacts.ACCOUNT_TYPE,
                ContactsContract.RawContacts.ACCOUNT_NAME,
            ),
            "${ContactsContract.RawContacts.DELETED}=0",
            null,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val type = cursor.getString(0)
                val name = cursor.getString(1)
                if (type.isNullOrBlank() || name.isNullOrBlank()) continue
                if (isReadOnlyAccount(type)) continue
                val key = ContactAccount(type, name)
                counts[key] = (counts[key] ?: 0) + 1
            }
        }
        counts.keys.firstOrNull { it.type == "com.google" }?.let { return it }
        return counts.maxByOrNull { it.value }?.key ?: ContactAccount(null, null)
    }

    private fun writableRawContactId(contactId: Long): Long? {
        val rows = mutableListOf<Triple<Long, String?, String?>>()
        context.contentResolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(
                ContactsContract.RawContacts._ID,
                ContactsContract.RawContacts.ACCOUNT_TYPE,
                ContactsContract.RawContacts.ACCOUNT_NAME,
            ),
            "${ContactsContract.RawContacts.CONTACT_ID}=? AND ${ContactsContract.RawContacts.DELETED}=0",
            arrayOf(contactId.toString()),
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                rows += Triple(cursor.getLong(0), cursor.getString(1), cursor.getString(2))
            }
        }
        val writable = rows.filter { !isReadOnlyAccount(it.second) }
        val pool = writable.ifEmpty { rows }
        return pool.firstOrNull { it.second == "com.google" }?.first
            ?: pool.firstOrNull { !it.second.isNullOrBlank() }?.first
            ?: pool.firstOrNull()?.first
    }

    private fun isReadOnlyAccount(type: String?): Boolean {
        val t = type.orEmpty().lowercase()
        if (t.isEmpty()) return false
        return t.contains("sim") || t.contains("profile") || t == "com.android.contacts.sim"
    }

    private fun contactWriteError(e: Exception, fallback: String): String {
        val detail = e.message?.takeIf { it.isNotBlank() }
        return if (detail != null) "$fallback ($detail)" else fallback
    }

    companion object {
        private val COLORS = listOf(
            "#1F8A70", "#2E7D32", "#1565C0", "#6A1B9A", "#C62828",
            "#EF6C00", "#00838F", "#AD1457", "#4527A0", "#33691E",
        )

        fun avatarColor(seed: String): String {
            val idx = (seed.hashCode() and 0x7fffffff) % COLORS.size
            return COLORS[idx]
        }
    }
}

private fun Cursor.string(index: Int): String =
    if (isNull(index)) "" else getString(index).orEmpty()
