package com.example.data

import com.example.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Singleton client for interacting with Supabase.
 * It uses the 'url' and 'key' secrets provided in the AI Studio environment.
 */
object SupabaseClient {
    val client by lazy {
        createSupabaseClient(
            supabaseUrl = if (BuildConfig.url.isNotBlank() && BuildConfig.url.startsWith("http")) BuildConfig.url else "https://xyz.supabase.co",
            supabaseKey = if (BuildConfig.key.isNotBlank()) BuildConfig.key else "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        ) {
            install(Postgrest)
            install(Auth)
        }
    }
}
