package models

external interface MessageData {
    val id: Int?
    val user_message: String?
    val popup_message: String?
    val updated_at: String?
    val updated_by: String?
}

data class Message(
    val id: Int = 0,
    val userMessage: String = "",
    val popupMessage: String = "",
    val updatedAt: String = "",
    val updatedBy: String = ""
) {
    val isEmpty: Boolean get() = userMessage.isEmpty() && popupMessage.isEmpty()
    val isNotEmpty: Boolean get() = !isEmpty
    
    companion object {
        fun empty() = Message()
        fun fromJson(json: MessageData) = Message(
            id = json.id ?: 0,
            userMessage = json.user_message ?: "",
            popupMessage = json.popup_message ?: "",
            updatedAt = json.updated_at ?: "",
            updatedBy = json.updated_by ?: ""
        )
    }
}
