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
) {
    companion object {
        fun fromJson(json: dynamic): FormConfig {
            val fieldsList = mutableListOf<FormField>()
            json.fields?.let { fieldsJson ->
                for (i in 0 until fieldsJson.length) {
                    val f = fieldsJson[i]
                    fieldsList += FormField(
                        name = f.name?.toString() ?: "",
                        type = f.type?.toString() ?: "text",
                        required = f.required == true,
                        options = f.options?.let { opts ->
                            List(opts.length) { j -> opts[j].toString() }
                        } ?: emptyList()
                    )
                }
            }
            return FormConfig(
                id = json.id?.unsafeCast<Int?>() ?: 0,
                heading = json.heading?.toString() ?: "",
                fields = fieldsList,
                isActive = json.is_active == true
            )
        }
    }
}
