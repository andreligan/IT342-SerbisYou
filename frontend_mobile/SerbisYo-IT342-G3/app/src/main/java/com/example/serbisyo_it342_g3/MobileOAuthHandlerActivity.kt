package com.example.serbisyo_it342_g3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * This activity handles redirects from browser-based OAuth flow.
 * It should be registered in the AndroidManifest.xml with proper intent filters
 * to capture redirects from the OAuth provider.
 */
class MobileOAuthHandlerActivity : AppCompatActivity() {
    
    private val TAG = "MobileOAuthHandler"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Process the incoming intent
        processIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntent(intent)
    }
    
    private fun processIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri != null) {
                handleOAuthResponse(uri)
            } else {
                Log.e(TAG, "No URI data in intent")
                navigateToLogin(true)
            }
        } else {
            Log.e(TAG, "Unexpected intent action: ${intent.action}")
            navigateToLogin(true)
        }
    }
    
    private fun handleOAuthResponse(uri: Uri) {
        Log.d(TAG, "Handling OAuth response: $uri")
        try {
            // Extract token information if this is an existing user flow
            val token = uri.getQueryParameter("token")
            val userId = uri.getQueryParameter("userId")
            val role = uri.getQueryParameter("role")
            
            // Extract user info if this is a new user flow
            val email = uri.getQueryParameter("email")
            val name = uri.getQueryParameter("name")
            val picture = uri.getQueryParameter("picture")
            
            if (token != null && role != null) {
                // This is a successful login for an existing user
                handleExistingUser(token, userId, role)
            } else if (email != null && name != null) {
                // This is a new user that needs to select a role
                handleNewUser(email, name, picture ?: "")
            } else {
                // We couldn't extract the expected parameters
                val error = uri.getQueryParameter("error")
                if (error != null) {
                    Log.e(TAG, "Authentication error: $error")
                    Toast.makeText(this, "Authentication error: $error", Toast.LENGTH_LONG).show()
                } else {
                    Log.e(TAG, "Invalid redirect URI format: $uri")
                    Toast.makeText(this, "Invalid authentication response", Toast.LENGTH_LONG).show()
                }
                navigateToLogin(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling OAuth response", e)
            Toast.makeText(this, "Error processing authentication: ${e.message}", Toast.LENGTH_LONG).show()
            navigateToLogin(true)
        }
    }
    
    private fun handleExistingUser(token: String, userId: String?, role: String) {
        Log.d(TAG, "Handling existing user with role: $role")
        
        // Save authentication data
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("token", token)
            // Convert userId if present
            val userIdLong = userId?.toLongOrNull() ?: 0L
            putLong("userId", userIdLong)
            putString("role", role)
            putString("username", "Google User") // Could be improved with actual data
            apply()
        }
        
        // Navigate based on role
        if (role.contains("Customer", ignoreCase = true)) {
            startActivity(Intent(this, CustomerDashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        } else if (role.contains("Service Provider", ignoreCase = true)) {
            startActivity(Intent(this, ServiceProviderDashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        } else {
            Log.w(TAG, "Unknown role: $role, redirecting to login")
            navigateToLogin(false)
        }
        
        finish()
    }
    
    private fun handleNewUser(email: String, name: String, picture: String) {
        Log.d(TAG, "Handling new user with email: $email, name: $name")
        
        // Navigate to role selection
        val intent = Intent(this, OAuth2RoleSelectionActivity::class.java).apply {
            putExtra("email", email)
            putExtra("name", name) 
            putExtra("picture", picture)
            putExtra("isManualEntry", false) // From real OAuth
        }
        startActivity(intent)
        finish()
    }
    
    private fun navigateToLogin(showError: Boolean) {
        val intent = Intent(this, LoginActivity::class.java)
        if (showError) {
            intent.putExtra("auth_error", true)
        }
        startActivity(intent)
        finish()
    }
} 