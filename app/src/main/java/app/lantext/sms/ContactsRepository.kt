package app.lantext.sms

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
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
        return source.map {
            ContactDto(
                id = it.id,
                name = it.name,
                number = it.number,
                photoUrl = "/api/v1/contacts/photo?number=${Uri.encode(it.normalized.ifBlank { it.number })}",
                avatarColor = it.color,
            )
        }
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
