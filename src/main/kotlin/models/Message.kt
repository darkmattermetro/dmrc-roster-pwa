package models

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
        fun fromJson(json: dynamic) = Message(
            id = json.id?.unsafeCast<Int?>() ?: 0,
            userMessage = json.user_message?.toString() ?: "",
            popupMessage = json.popup_message?.toString() ?: "",
            updatedAt = json.updated_at?.toString() ?: "",
            updatedBy = json.updated_by?.toString() ?: ""
        )
    }
}
