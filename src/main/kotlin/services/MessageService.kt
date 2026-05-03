package services

import config.SupabaseConfig
import models.Message
import kotlinx.browser.window
import kotlinx.coroutines.await

external interface MessageJson {
    val user_message: String?
    val popup_message: String?
}

class MessageService {
    private val baseUrl = SupabaseConfig.SUPABASE_URL
    
    suspend fun getMessages(): Message {
        return try {
            val response = window.fetch(
                "$baseUrl/rest/v1/messages?id=eq.1&select=*",
                org.w3c.fetch.RequestInit(
                    headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toJsObject())
                )
            ).await()
            
            val data = response.json().await().unsafeCast<Array<MessageJson>>()
            if (data.isNotEmpty()) Message.fromJson(data[0]) else Message.empty()
        } catch (e: Throwable) {
            Message.empty()
        }
    }
    
    suspend fun saveUserMessage(message: String, empId: String): Boolean {
        return try {
            val now = js("new Date().toISOString()").toString()
            val body = """{"user_message":"${message.replace("\"", "\\\"")}","updated_at":"$now","updated_by":"$empId"}"""
            
            window.fetch(
                "$baseUrl/rest/v1/messages?id=eq.1",
                org.w3c.fetch.RequestInit(
                    method = "PATCH",
                    headers = org.w3c.fetch.Headers(SupabaseConfig.headers().toJsObject()),
                    body = body
                )
            ).await()
            true
        } catch (e: Throwable) {
            false
        }
    }
    
    suspend fun savePopupMessage(message: String, empId: String): Boolean {
        return try {
            val now = js("new Date().toISOString()").toString()
            val body = """{"popup_message":"${message.replace("\"", "\\\"")}","updated_at":"$now","updated_by":"$empId"}"""
            
            window.fetch(
                "$baseUrl/rest/v1/messages?id=eq.1",
                org.w3c.fetch.RequestInit(
                    method = "PATCH",
                    headers = org.w3c.fetch.Headers(SupabaseConfig.headers().toJsObject()),
                    body = body
                )
            ).await()
            true
        } catch (e: Throwable) {
            false
        }
    }
}
