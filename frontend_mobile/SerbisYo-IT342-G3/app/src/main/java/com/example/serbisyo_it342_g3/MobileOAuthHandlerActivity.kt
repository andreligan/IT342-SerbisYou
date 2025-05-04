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
        
        // Enhanced logging
        Log.d(TAG, "MobileOAuthHandlerActivity created")
        Log.d(TAG, "Intent action: ${intent.action}")
        Log.d(TAG, "Intent data: ${intent.data}")
        
        // Process the incoming intent
        processIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        // Enhanced logging
        Log.d(TAG, "onNewIntent called with action: ${intent.action}")
        Log.d(TAG, "onNewIntent data: ${intent.data}")
        
        processIntent(intent)
    }
    
    private fun processIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri != null) {
                Log.d(TAG, "Processing URI: $uri")
                Log.d(TAG, "URI host: ${uri.host}, path: ${uri.path}, query: ${uri.query}")
                
                // Check if this is a redirect to the web frontend
                if (uri.host == "serbisyo.vercel.app" && 
                    (uri.path?.contains("oauth-role-selection") == true || 
                     uri.path?.contains("oauth2/redirect") == true)) {
                    Log.d(TAG, "Detected web frontend redirect, intercepting for mobile app")
                    handleWebFrontendRedirect(uri)
                } else {
                    handleOAuthResponse(uri)
                }
            } else {
                Log.e(TAG, "No URI data in intent")
                navigateToLogin(true)
            }
        } else {
            Log.e(TAG, "Unexpected intent action: ${intent.action}")
            navigateToLogin(true)
        }
    }
    
    private fun handleWebFrontendRedirect(uri: Uri) {
        try {
            // Extract parameters from the web frontend redirect
            var email = uri.getQueryParameter("email")
            var name = uri.getQueryParameter("name")
            var picture = uri.getQueryParameter("picture")
            
            // Also check for token/role parameters in case this is a login (not registration)
            val token = uri.getQueryParameter("token")
            val userId = uri.getQueryParameter("userId")
            val role = uri.getQueryParameter("role")
            
            Log.d(TAG, "Extracted from web frontend: email=$email, name=$name, picture=$picture")
            Log.d(TAG, "Token data: token=$token, userId=$userId, role=$role")
            
            // If we have token/role parameters, handle as an existing user flow
            if (token != null && role != null) {
                handleExistingUser(token, userId, role)
                return
            }
            
            // For new user flow with email/name
            if (email != null && name != null) {
                // Launch the mobile app's role selection activity
                val intent = Intent(this, OAuth2RoleSelectionActivity::class.java).apply {
                    putExtra("email", email)
                    putExtra("name", name) 
                    putExtra("picture", picture ?: "")
                    putExtra("isManualEntry", false)
                }
                Log.d(TAG, "Starting OAuth2RoleSelectionActivity with email=$email, name=$name")
                startActivity(intent)
                finish()
            } else {
                // Check fragment portion for parameters (some OAuth flows put data in fragments)
                val fragment = uri.fragment
                if (fragment != null && fragment.isNotEmpty()) {
                    Log.d(TAG, "Checking fragment for parameters: $fragment")
                    
                    // Parse fragment parameters
                    val fragmentParams = fragment.split("&").associate { param ->
                        val parts = param.split("=")
                        if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
                    }
                    
                    // Try to extract email/name from fragment
                    email = fragmentParams["email"] ?: email
                    name = fragmentParams["name"] ?: name
                    picture = fragmentParams["picture"] ?: picture
                    
                    // Try to extract token/role from fragment
                    val tokenFromFragment = fragmentParams["token"]
                    val userIdFromFragment = fragmentParams["userId"]
                    val roleFromFragment = fragmentParams["role"]
                    
                    if (tokenFromFragment != null && roleFromFragment != null) {
                        handleExistingUser(tokenFromFragment, userIdFromFragment, roleFromFragment)
                        return
                    }
                    
                    if (email != null && name != null) {
                        val intent = Intent(this, OAuth2RoleSelectionActivity::class.java).apply {
                            putExtra("email", email)
                            putExtra("name", name) 
                            putExtra("picture", picture ?: "")
                            putExtra("isManualEntry", false)
                        }
                        startActivity(intent)
                        finish()
                        return
                    }
                }
                
                Log.e(TAG, "Missing required parameters in web frontend redirect")
                Toast.makeText(this, "Missing user information in redirect", Toast.LENGTH_LONG).show()
                navigateToLogin(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling web frontend redirect", e)
            Toast.makeText(this, "Error processing authentication redirect: ${e.message}", Toast.LENGTH_LONG).show()
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
            
            Log.d(TAG, "Extracted params - token: $token, role: $role, email: $email, name: $name")
            
            if (token != null && role != null) {
                // This is a successful login for an existing user
                handleExistingUser(token, userId, role)
            } else if (email != null && name != null) {
                // This is a new user that needs to select a role
                handleNewUser(email, name, picture ?: "")
            } else {
                // Check fragment portion for parameters
                val fragment = uri.fragment
                if (fragment != null && fragment.isNotEmpty()) {
                    Log.d(TAG, "Checking fragment for parameters: $fragment")
                    
                    // Parse fragment parameters
                    val fragmentParams = fragment.split("&").associate { param ->
                        val parts = param.split("=")
                        if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
                    }
                    
                    // Try to extract token/role from fragment
                    val tokenFromFragment = fragmentParams["token"]
                    val userIdFromFragment = fragmentParams["userId"]
                    val roleFromFragment = fragmentParams["role"]
                    
                    // Try to extract email/name from fragment
                    val emailFromFragment = fragmentParams["email"]
                    val nameFromFragment = fragmentParams["name"]
                    val pictureFromFragment = fragmentParams["picture"]
                    
                    if (tokenFromFragment != null && roleFromFragment != null) {
                        handleExistingUser(tokenFromFragment, userIdFromFragment, roleFromFragment)
                        return
                    }
                    
                    if (emailFromFragment != null && nameFromFragment != null) {
                        handleNewUser(emailFromFragment, nameFromFragment, pictureFromFragment ?: "")
                        return
                    }
                }
                
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