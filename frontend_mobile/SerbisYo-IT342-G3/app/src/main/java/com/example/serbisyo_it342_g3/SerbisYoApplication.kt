package com.example.serbisyo_it342_g3

import android.app.Application
import android.content.Context
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
    }

    override fun onCreate() {
        super.onCreate()
        
        // Set the base URL for API clients
        val ipAddress = getWifiIPAddress()
        if (ipAddress.isNotEmpty()) {
            Log.d(TAG, "Using device IP address: $ipAddress")
            // Use the detected IP address directly
            BaseApiClient.setBaseUrl("http://$ipAddress:8080")
        } else {
            Log.d(TAG, "No WiFi IP address found, using default settings")
        }
        
        // Set up network monitoring
        setupNetworkMonitoring()
    }
    
    private fun getWifiIPAddress(): String {
        try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return ""
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ""
            
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                // Based on the logs, the device's IP address is 192.168.1.30
                return "192.168.1.102"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting WiFi IP address", e)
        }
        return ""
    }
    
    private fun setupNetworkMonitoring() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        val networkCallback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d(TAG, "Network available")
            }
            
            override fun onLost(network: Network) {
                super.onLost(network)
                Log.d(TAG, "Network lost")
            }
            
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                
                val hasInternet = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                )
                
                Log.d(TAG, "Network capabilities changed - Has internet: $hasInternet")
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering network callback", e)
        }
    }
} 