package services

import config.SupabaseConfig
import models.AuthResult
import models.User
import kotlinx.browser.window
import kotlinx.coroutines.await

private const val ACCESS_CODE = "satvik"

class AuthService {
    private val baseUrl = SupabaseConfig.SUPABASE_URL
    
    suspend fun login(empId: String, password: String): AuthResult {
        return try {
            val normalizedId = empId.trim().uppercase()
            val inputHash = hashPassword(password)
            
            val response = window.fetch(
                "$baseUrl/rest/v1/profiles?emp_id=eq.$normalizedId&select=*",
                org.w3c.fetch.RequestInit(
                    headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toPlainObject())
                )
            ).await()
            
            val data = (response as dynamic).json().await()
            
            if (data.length == 0) {
                return AuthResult.failure("Emp ID not found. Please register first.")
            }
            
            val profile = data[0]
            val storedHash = profile.password_hash?.toString() ?: ""
            
            if (storedHash != inputHash) {
                return AuthResult.failure("Invalid Password!")
            }
            
            val now = js("new Date().toISOString()").toString()
            window.fetch(
                "$baseUrl/rest/v1/profiles?emp_id=eq.$normalizedId",
                org.w3c.fetch.RequestInit(
                    method = "PATCH",
                    headers = org.w3c.fetch.Headers(SupabaseConfig.headers().toPlainObject()),
                    body = JSON.stringify(js("{last_login: '$now'}"))
                )
            ).await()
            
            AuthResult.success(User.fromJson(profile))
        } catch (e: dynamic) {
            AuthResult.failure("Login failed: ${e.toString()}")
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
                    headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toPlainObject())
                )
            ).await()
            
            val allowedData = (allowedResp as dynamic).json().await()
            if (allowedData.length == 0) {
                return AuthResult.failure("Emp ID not authorized for $accessLevel access!")
            }
            
            val existingResp = window.fetch(
                "$baseUrl/rest/v1/profiles?emp_id=eq.$normalizedId&select=*",
                org.w3c.fetch.RequestInit(
                    headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toPlainObject())
                )
            ).await()
            
            val existingData = (existingResp as dynamic).json().await()
            if (existingData.length > 0) {
                return AuthResult.failure("Emp ID already registered! Please login.")
            }
            
            val passwordHash = hashPassword(password)
            val now = js("new Date().toISOString()").toString()
            
            val insertBody = JSON.stringify(js("""
                {
                    "emp_id": "$normalizedId",
                    "name": "$name",
                    "password_hash": "$passwordHash",
                    "access_level": "$accessLevel",
                    "created_at": "$now"
                }
            """.trimIndent()))
            
            window.fetch(
                "$baseUrl/rest/v1/profiles",
                org.w3c.fetch.RequestInit(
                    method = "POST",
                    headers = org.w3c.fetch.Headers(SupabaseConfig.headers().toPlainObject()),
                    body = insertBody
                )
            ).await()
            
            val newResp = window.fetch(
                "$baseUrl/rest/v1/profiles?emp_id=eq.$normalizedId&select=*",
                org.w3c.fetch.RequestInit(
                    headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toPlainObject())
                )
            ).await()
            
            val newData = (newResp as dynamic).json().await()
            if (newData.length > 0) {
                AuthResult.success(User.fromJson(newData[0]))
            } else {
                AuthResult.failure("Registration failed")
            }
        } catch (e: dynamic) {
            AuthResult.failure("Registration failed: ${e.toString()}")
        }
    }
    
    fun saveCurrentUser(user: User) {
        val json = JSON.stringify(js("""
            {"id":"${user.id}","empId":"${user.empId}","name":"${user.name}","accessLevel":"${user.accessLevel}"}
        """.trimIndent()))
        window.localStorage.setItem("dmrc_user", json)
    }
    
    fun getCurrentUser(): User? {
        val stored = window.localStorage.getItem("dmrc_user") ?: return null
        val data = JSON.parse(stored)
        return User(
            id = data.id?.toString() ?: "",
            empId = data.empId?.toString() ?: "",
            name = data.name?.toString() ?: "",
            accessLevel = data.accessLevel?.toString() ?: "crewcontroller"
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

fun Map<String, String>.toPlainObject(): dynamic {
    val obj = js("{}")
    for ((k, v) in this) {
        obj[k] = v
    }
    return obj
}
