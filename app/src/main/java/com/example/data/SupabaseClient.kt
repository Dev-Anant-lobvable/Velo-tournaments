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
    val client = createSupabaseClient(
        supabaseUrl = if (BuildConfig.url.startsWith("http")) BuildConfig.url else "https://xyz.supabase.co",
        supabaseKey = BuildConfig.key
    ) {
        install(Postgrest)
        install(Auth)
    }
}
