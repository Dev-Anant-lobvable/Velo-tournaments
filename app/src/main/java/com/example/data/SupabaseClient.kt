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
        val supabaseUrl = if (BuildConfig.url.isNotBlank() && BuildConfig.url.startsWith("http")) BuildConfig.url else ""
        val supabaseKey = BuildConfig.key
        
        if (supabaseUrl.isBlank() || supabaseKey.isBlank()) {
            android.util.Log.e("SupabaseClient", "API Keys are missing! Set 'url' and 'key' secrets in the Secrets Panel.")
        }
        
        createSupabaseClient(
            supabaseUrl = supabaseUrl.ifEmpty { "https://placeholder.supabase.co" },
            supabaseKey = supabaseKey.ifEmpty { "placeholder_key" }
        ) {
            install(Postgrest)
            install(Auth)
        }
    }
}
