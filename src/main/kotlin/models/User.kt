package models

external interface ProfileData {
    val id: String?
    val emp_id: String?
    val name: String?
    val access_level: String?
    val created_at: String?
    val last_login: String?
}

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
        fun fromJson(json: ProfileData): User {
            return User(
                id = json.id ?: "",
                empId = json.emp_id ?: "",
                name = json.name ?: "",
                accessLevel = json.access_level ?: "crewcontroller",
                createdAt = json.created_at ?: "",
                lastLogin = json.last_login ?: ""
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
