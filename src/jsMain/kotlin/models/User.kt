package models

data class User(
    val id: String = "",
    val empId: String = "",
    val name: String = "",
    val accessLevel: String = "crewcontroller",
    val createdAt: String = "",
    val lastLogin: String = ""
) {
    val isAdmin: Boolean get() = accessLevel == "admin"
    val isCrewController: Boolean get() = accessLevel == "crewcontroller"
    
    companion object {
        fun fromJson(json: dynamic): User {
            return User(
                id = json.id?.toString() ?: "",
                empId = json.emp_id?.toString() ?: "",
                name = json.name?.toString() ?: "",
                accessLevel = json.access_level?.toString() ?: "crewcontroller",
                createdAt = json.created_at?.toString() ?: "",
                lastLogin = json.last_login?.toString() ?: ""
            )
        }
    }
}

data class AuthResult(
    val success: Boolean,
    val user: User? = null,
    val error: String? = null
) {
    companion object {
        fun success(user: User) = AuthResult(true, user)
        fun failure(error: String) = AuthResult(false, error = error)
    }
}
