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
data class SendRequest(
    val recipients: List<String> = emptyList(),
    val body: String = "",
    val threadId: String? = null,
    val subscriptionId: Int? = null,
    val imageBase64: String? = null,
    val imageMime: String? = null,
)

@Serializable
data class SearchHit(
    val conversation: ConversationDto,
    val message: MessageDto?,
)
