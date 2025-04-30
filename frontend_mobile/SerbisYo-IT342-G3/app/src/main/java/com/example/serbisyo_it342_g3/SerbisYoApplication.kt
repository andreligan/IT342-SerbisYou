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
import android.os.Build
import android.os.StrictMode
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class SerbisYoApplication : Application() {

    companion object {
        private const val TAG = "SerbisYoApplication"
        private const val DEFAULT_BASE_URL = "https://serbisyo-backend.onrender.com"
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
        
        // Configure hardware acceleration settings
        configureHardwareAcceleration()
        
        // Initialize the ConnectivityManager
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // Initialize BaseApiClient with proper URL based on device type and network
        BaseApiClient.initializeUrl(connectivityManager)
        
        // Show a toast warning about potential slow response times from the backend
        Toast.makeText(
            applicationContext, 
            "This app connects to a free-tier server which may be slow to respond initially. Please be patient during first use.",
            Toast.LENGTH_LONG
        ).show()
        
        // For release version, we don't want to override with local IP
        // Commenting out this code for the release APK

        /*
        // If the device is on a WiFi network, override with the current network's info
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        
        if (network != null && capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            try {
                // For this specific app, we know the server should be at this IP and port
                val serverIpAddress = "192.168.200.136" // Current server IP
                Log.d(TAG, "Connected to WiFi, setting server to: $serverIpAddress")
                BaseApiClient.setBaseUrl("http://$serverIpAddress:8080")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting server IP address", e)
            }
        }
        */
        
        // Set up network monitoring
        setupNetworkMonitoring()
    }
    
    private fun configureHardwareAcceleration() {
        try {
            // Apply some StrictMode policies to help with OpenGL problems
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                StrictMode.setThreadPolicy(
                    StrictMode.ThreadPolicy.Builder()
                        .permitCustomSlowCalls()
                        .permitDiskReads()
                        .permitDiskWrites()
                        .build()
                )
            }
            
            // Log that we've configured hardware acceleration
            Log.d(TAG, "Hardware acceleration configured")
        } catch (e: Exception) {
            Log.e(TAG, "Error configuring hardware acceleration", e)
        }
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