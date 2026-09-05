package app.lantext.sms

import kotlinx.serialization.Serializable

@Serializable
data class ConversationDto(
    val id: String,
    val address: String,
    val displayName: String,
    val photoUrl: String?,
    val snippet: String,
    val timestamp: Long,
    val unread: Int,
    val isGroup: Boolean,
    val recipients: List<String>,
    val avatarColor: String,
    val contactId: String? = null,
)

@Serializable
data class AttachmentDto(
    val id: String,
    val mimeType: String,
    val name: String?,
    val url: String,
)

@Serializable
data class MessageDto(
    val id: String,
    val threadId: String,
    val address: String,
    val displayName: String,
    val body: String,
    val timestamp: Long,
    val incoming: Boolean,
    val type: String,
    val status: String,
    val attachments: List<AttachmentDto> = emptyList(),
)

@Serializable
data class ContactDto(
    val id: String,
    val name: String,
    val number: String,
    val photoUrl: String?,
    val avatarColor: String,
)

@Serializable
data class LabeledValue(
    val label: String,
    val value: String,
)

@Serializable
data class ContactDetailsDto(
    val id: String?,
    val name: String,
    val photoUrl: String?,
    val avatarColor: String,
    val phones: List<LabeledValue> = emptyList(),
    val emails: List<LabeledValue> = emptyList(),
    val org: String? = null,
    val title: String? = null,
    val postal: String? = null,
)

@Serializable
data class SendRequest(
    val recipients: List<String> = emptyList(),
    val body: String = "",
    val threadId: String? = null,
    val subscriptionId: Int? = null,
    val imageBase64: String? = null,
    val imageMime: String? = null,
    val mediaUrl: String? = null,
)

@Serializable
data class GifHit(
    val id: String,
    val title: String,
    val url: String,
    val width: Int,
    val height: Int,
    val bytes: Int,
)

@Serializable
data class SearchHit(
    val conversation: ConversationDto,
    val message: MessageDto?,
)

@Serializable
data class CreateContactRequest(
    val name: String = "",
    val number: String = "",
)

@Serializable
data class AddPhoneRequest(
    val number: String = "",
)

@Serializable
data class AppearanceRequest(
    val palette: String = "fern",
    val mode: String = "auto",
)
