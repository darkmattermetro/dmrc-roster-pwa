package config

object SupabaseConfig {
    const val SUPABASE_URL = "https://pmjiopmuvkkwsaurtzjo.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBtamlvcG11dmtrd3NhdXJ0empvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzY0MTE2MDQsImV4cCI6MjA5MTk4NzYwNH0.br5RGBnFW3E2D16qKxRrvPhcwL3wStZtf21ARzAf_NA"
    
    fun headers(): Map<String, String> = mapOf(
        "apikey" to SUPABASE_ANON_KEY,
        "Authorization" to "Bearer $SUPABASE_ANON_KEY",
        "Content-Type" to "application/json",
        "Prefer" to "return=representation"
    )
    
    fun selectHeaders(): Map<String, String> = mapOf(
        "apikey" to SUPABASE_ANON_KEY,
        "Authorization" to "Bearer $SUPABASE_ANON_KEY",
        "Content-Type" to "application/json"
    )
}
