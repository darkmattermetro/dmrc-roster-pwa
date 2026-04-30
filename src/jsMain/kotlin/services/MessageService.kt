package services

import config.SupabaseConfig
import models.Message
import kotlinx.browser.window
import kotlinx.coroutines.await

class MessageService {
    private val baseUrl = SupabaseConfig.SUPABASE_URL
    
    suspend fun getMessages(): Message {
        return try {
            val response = window.fetch(
                "$baseUrl/rest/v1/messages?id=eq.1&select=*",
                org.w3c.fetch.RequestInit(
                    headers = org.w3c.fetch.Headers(SupabaseConfig.selectHeaders().toPlainObject())
                )
            ).await()
            
            val data = (response as dynamic).json().await()
            if (data.length > 0) Message.fromJson(data[0]) else Message.empty()
        } catch (e: dynamic) {
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
                    headers = org.w3c.fetch.Headers(SupabaseConfig.headers().toPlainObject()),
                    body = body
                )
            ).await()
            true
        } catch (e: dynamic) {
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
                    headers = org.w3c.fetch.Headers(SupabaseConfig.headers().toPlainObject()),
                    body = body
                )
            ).await()
            true
        } catch (e: dynamic) {
            false
        }
    }
}
