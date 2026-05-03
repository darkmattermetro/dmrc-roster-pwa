package services

import config.SupabaseConfig
import models.AuthResult
import models.User
import kotlinx.browser.window
import kotlinx.coroutines.await

private const val ACCESS_CODE = "satvik"

external interface ProfileJson {
    val emp_id: String?
    val name: String?
    val password_hash: String?
    val access_level: String?
    val created_at: String?
    val last_login: String?
}

class AuthService {
    private val baseUrl = SupabaseConfig.SUPABASE_URL
    
    suspend fun login(empId: String, password: String): AuthResult {
        return try {
            val normalizedId = empId.trim().uppercase()
            val inputHash = hashPassword(password)
            
            val response = window.fetch(
                "$baseUrl/rest/v1/profiles?emp_id=eq.$normalizedId&select=*",
                org.w3c.fetch.RequestInit(
                    headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toJsObject())
                )
            ).await()
            
            val data = response.json().await().unsafeCast<Array<ProfileJson>>()
            
            if (data.isEmpty()) {
                return AuthResult.failure("Emp ID not found. Please register first.")
            }
            
            val profile = data[0]
            val storedHash = profile.password_hash ?: ""
            
            if (storedHash != inputHash) {
                return AuthResult.failure("Invalid Password!")
            }
            
            val now = js("new Date().toISOString()").toString()
            window.fetch(
                "$baseUrl/rest/v1/profiles?emp_id=eq.$normalizedId",
                org.w3c.fetch.RequestInit(
                    method = "PATCH",
                    headers = org.w3c.fetch.Headers(SupabaseConfig.headers().toJsObject()),
                    body = JSON.stringify(js("({last_login: '$now'})"))
                )
            ).await()
            
            AuthResult.success(User.fromJson(profile))
        } catch (e: Throwable) {
            AuthResult.failure("Login failed: ${e.message}")
        }
    }
    
    suspend fun register(empId: String, name: String, password: String, accessCode: String, accessLevel: String): AuthResult {
        return try {
            if (accessCode != ACCESS_CODE) {
                return AuthResult.failure("Invalid Access Code!")
            }
            
            val normalizedId = empId.trim().uppercase()
            val allowedTable = if (accessLevel == "admin") "allowed_admins" else "allowed_crew_controllers"
            
            val allowedResp = window.fetch(
                "$baseUrl/rest/v1/$allowedTable?emp_id=eq.$normalizedId&select=*",
                org.w3c.fetch.RequestInit(
                    headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toJsObject())
                )
            ).await()
            
            val allowedData = allowedResp.json().await().unsafeCast<Array<ProfileJson>>()
            if (allowedData.isEmpty()) {
                return AuthResult.failure("Emp ID not authorized for $accessLevel access!")
            }
            
            val existingResp = window.fetch(
                "$baseUrl/rest/v1/profiles?emp_id=eq.$normalizedId&select=*",
                org.w3c.fetch.RequestInit(
                    headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toJsObject())
                )
            ).await()
            
            val existingData = existingResp.json().await().unsafeCast<Array<ProfileJson>>()
            if (existingData.isNotEmpty()) {
                return AuthResult.failure("Emp ID already registered! Please login.")
            }
            
            val passwordHash = hashPassword(password)
            val now = js("new Date().toISOString()").toString()
            
            val insertBody = js("""({
                emp_id: "$normalizedId",
                name: "$name",
                password_hash: "$passwordHash",
                access_level: "$accessLevel",
                created_at: "$now"
            })""")
            
            window.fetch(
                "$baseUrl/rest/v1/profiles",
                org.w3c.fetch.RequestInit(
                    method = "POST",
                    headers = org.w3c.fetch.Headers(SupabaseConfig.headers().toJsObject()),
                    body = JSON.stringify(insertBody)
                )
            ).await()
            
            val newResp = window.fetch(
                "$baseUrl/rest/v1/profiles?emp_id=eq.$normalizedId&select=*",
                org.w3c.fetch.RequestInit(
                    headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toJsObject())
                )
            ).await()
            
            val newData = newResp.json().await().unsafeCast<Array<ProfileJson>>()
            if (newData.isNotEmpty()) {
                AuthResult.success(User.fromJson(newData[0]))
            } else {
                AuthResult.failure("Registration failed")
            }
        } catch (e: Throwable) {
            AuthResult.failure("Registration failed: ${e.message}")
        }
    }
    
    fun saveCurrentUser(user: User) {
        val json = js("""({id:"${user.id}",empId:"${user.empId}",name:"${user.name}",accessLevel:"${user.accessLevel}"})""")
        window.localStorage.setItem("dmrc_user", JSON.stringify(json))
    }
    
    fun getCurrentUser(): User? {
        val stored = window.localStorage.getItem("dmrc_user") ?: return null
        val data = JSON.parse<ProfileJson>(stored)
        return User(
            id = data.emp_id ?: "",
            empId = data.emp_id ?: "",
            name = data.name ?: "",
            accessLevel = data.access_level ?: "crewcontroller"
        )
    }
    
    fun clearCurrentUser() {
        window.localStorage.removeItem("dmrc_user")
    }
    
    private fun hashPassword(password: String): String {
        var hash = 0
        for (i in 0 until password.length) {
            val char = password[i].code
            hash = ((hash shl 5) - hash) + char
            hash = hash and hash
        }
        return hash.toString()
    }
}

fun Map<String, String>.toJsObject(): dynamic {
    val obj = js("{}")
    for ((k, v) in this) {
        obj[k] = v
    }
    return obj
}
