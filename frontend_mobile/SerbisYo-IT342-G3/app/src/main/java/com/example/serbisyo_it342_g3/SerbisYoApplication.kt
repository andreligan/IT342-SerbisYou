package com.example.serbisyo_it342_g3

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import android.widget.Toast
import com.example.serbisyo_it342_g3.api.BaseApiClient

class SerbisYoApplication : Application() {

    companion object {
        private const val TAG = "SerbisYoApplication"
        private const val DEFAULT_BASE_URL = "https://serbisyo-api.example.com"
        private const val PREFS_NAME = "SerbisYoPrefs"
        private const val KEY_BASE_URL = "base_url"
        
        fun getBaseUrl(context: Context): String {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        }
        
        fun setBaseUrl(context: Context, url: String) {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_BASE_URL, url).apply()
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize the ConnectivityManager
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // Initialize BaseApiClient with proper URL based on device type and network
        BaseApiClient.initializeUrl(connectivityManager)
        
        // If the device is on a WiFi network, override with the current network's info
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        if (network != null && capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            try {
                // For this specific app, we know the server should be at this IP and port
                val serverIpAddress = "192.168.1.102" // Current server IP
                Log.d(TAG, "Connected to WiFi, setting server to: $serverIpAddress")
                BaseApiClient.setBaseUrl("http://$serverIpAddress:8080")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting server IP address", e)
            }
        }
        
        // Set up network monitoring
        setupNetworkMonitoring()
    }
    
    private fun setupNetworkMonitoring() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val networkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d(TAG, "Network connected")
            }
            
            override fun onLost(network: Network) {
                super.onLost(network)
                Log.d(TAG, "Network disconnected")
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val hasWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                
                Log.d(TAG, "Network capabilities changed - Has internet: $hasInternet, Is WiFi: $hasWifi")
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
            
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
} 