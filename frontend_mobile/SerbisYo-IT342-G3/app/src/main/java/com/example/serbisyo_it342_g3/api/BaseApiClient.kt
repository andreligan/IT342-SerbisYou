package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.gson.Gson
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * BaseApiClient provides common functionality for all API clients
 * including base URL configuration and network utility methods.
 */
class BaseApiClient(private val context: Context) {
    companion object {
        // For Android Emulator - Virtual Device (default)
        private const val EMULATOR_URL = "http://10.0.2.2:8080"
        
        // For Physical Device - Use your computer's actual IP address
        private const val PHYSICAL_DEVICE_URL = "http://172.20.10.2:8080"
        
        // Local device loopback
        private const val LOCALHOST_URL = "http://localhost:8080"
        
        // For production use, provide actual API server URL
        private const val PRODUCTION_URL = "https://api.serbisyo.com"
        
        // STATIC PROPERTY TO CONFIGURE API URL
        var BASE_URL = PHYSICAL_DEVICE_URL

        // Method to set base URL from anywhere in the app
        fun setBaseUrl(url: String) {
            BASE_URL = url
            Log.d("BaseApiClient", "Set base URL to: $BASE_URL")
        }
    }
    
    // Common HTTP client configured with appropriate timeouts
    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
        
    // Common Gson instance for JSON parsing
    val gson = Gson()
    
    // Method to check if device has internet connectivity
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    // Method to get API base URL
    fun getBaseUrl(): String {
        return BASE_URL
    }
} 