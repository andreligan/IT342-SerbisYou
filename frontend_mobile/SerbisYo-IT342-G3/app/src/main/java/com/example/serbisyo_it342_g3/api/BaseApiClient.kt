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
open class BaseApiClient(private val context: Context) {
    companion object {
        private const val TAG = "BaseApiClient"
        
        // For Android Emulator - Virtual Device
        private const val EMULATOR_URL = "http://10.0.2.2:8080"
        
        // For Physical Device - Default IP for server (should be updated to match the actual server)
        private const val PHYSICAL_DEVICE_URL = "http://192.168.200.136:8080"
        
        // For Physical Device - Additional server IP options
        private const val ALTERNATE_SERVER_IP_1 = "http://192.168.1.100:8080"
        private const val ALTERNATE_SERVER_IP_2 = "http://192.168.1.101:8080"
        private const val ALTERNATE_SERVER_IP_3 = "http://192.168.100.10:8080"

        // Production URL for released APK
        private const val PRODUCTION_URL = "https://serbisyo-backend.onrender.com"
        
        // STATIC PROPERTY TO CONFIGURE API URL - Set to production for release
        var BASE_URL = PRODUCTION_URL

        // Method to set base URL from anywhere in the app
        fun setBaseUrl(url: String) {
            BASE_URL = url
            Log.d(TAG, "Set base URL to: $BASE_URL")
        }
        
        // Initialize the URL on app start
        fun initializeUrl(connectivityManager: ConnectivityManager) {
            // For release version, we'll always use the production URL
            BASE_URL = PRODUCTION_URL
            Log.d(TAG, "Using production URL: $BASE_URL")

            /*
            // Commented out for release version
            val isEmulator = android.os.Build.FINGERPRINT.contains("generic") || 
                             android.os.Build.MODEL.contains("google_sdk")
            
            if (isEmulator) {
                BASE_URL = EMULATOR_URL
                Log.d(TAG, "Running on emulator, using URL: $BASE_URL")
            } else {
                BASE_URL = PHYSICAL_DEVICE_URL
                Log.d(TAG, "Running on physical device, using URL: $BASE_URL")
            }
            
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            
            Log.d(TAG, "Network active: ${network != null}")
            Log.d(TAG, "Network capabilities: ${capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}")
            */
        }
    }
    
    // Common HTTP client configured with appropriate timeouts
    val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)  // Increased timeout for Render.com free tier
        .readTimeout(120, TimeUnit.SECONDS)     // Increased timeout for Render.com free tier
        .writeTimeout(120, TimeUnit.SECONDS)    // Increased timeout for Render.com free tier
        .build()
        
    // Common Gson instance for JSON parsing
    val gson = Gson()
    
    init {
        // Log the current base URL when the client is initialized
        Log.d(TAG, "Initializing API client with base URL: $BASE_URL")
    }
    
    // Method to check if device has internet connectivity
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    // Method to get API base URL
    fun getBaseUrl(): String {
        // Add logging for debugging
        Log.d(TAG, "Using base URL: $BASE_URL")
        return BASE_URL
    }
    
    // Helper methods that match the backend's expected URLs
    fun getSuccessUrl(): String {
        // Return the deployed frontend URL for payment success
        return "https://serbisyo.vercel.app/payment-success"
    }
    
    fun getCancelUrl(): String {
        // Return the deployed frontend URL for payment cancel
        return "https://serbisyo.vercel.app/payment-cancel"
    }
} 