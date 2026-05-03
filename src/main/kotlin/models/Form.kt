package models

data class FormField(
    val name: String = "",
    val type: String = "text",
    val required: Boolean = false,
    val options: List<String> = emptyList()
)

data class FormConfig(
    val id: Int = 0,
    val heading: String = "",
    val fields: List<FormField> = emptyList(),
    val isActive: Boolean = false
)
