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
        val ops = ArrayList<ContentProviderOperation>()
        ops += ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build()
        ops += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, display)
            .build()
        ops += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
            .build()
        try {
            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException(e.message ?: "Could not save the contact")
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
        val rawId = rawContactId(id)
            ?: throw IllegalStateException("Could not find that contact")
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
            throw IllegalStateException(e.message ?: "Could not add that number")
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

    private fun rawContactId(contactId: Long): Long? {
        context.contentResolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            arrayOf(ContactsContract.RawContacts._ID),
            "${ContactsContract.RawContacts.CONTACT_ID}=?",
            arrayOf(contactId.toString()),
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) return cursor.getLong(0)
        }
        return null
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
