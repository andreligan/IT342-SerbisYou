package com.example.serbisyo_it342_g3

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.serbisyo_it342_g3.api.BaseApiClient
import com.google.gson.Gson
import kotlin.concurrent.thread

class OAuth2RedirectHandlerActivity : AppCompatActivity() {
    private val TAG = "OAuth2Redirect"
    private lateinit var baseApiClient: BaseApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth2_redirect_handler)
        
        baseApiClient = BaseApiClient(this)
        
        // Enhanced logging
        Log.d(TAG, "OAuth2RedirectHandlerActivity created")
        Log.d(TAG, "Base URL: ${baseApiClient.getBaseUrl()}")
        
        // Log the intent information
        if (intent != null) {
            Log.d(TAG, "Intent Action: ${intent.action}")
            Log.d(TAG, "Intent Data: ${intent.data}")
            if (intent.data != null) {
                Log.d(TAG, "Intent data host: ${intent.data?.host}")
                Log.d(TAG, "Intent data path: ${intent.data?.path}")
                Log.d(TAG, "Intent data query: ${intent.data?.query}")
            }
        } else {
            Log.e(TAG, "Intent is null")
        }
        
        try {
            handleIntent(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling redirect: ${e.message}", e)
            Log.e(TAG, "Stack trace: ", e)
            Toast.makeText(this, "Error handling authentication", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        // Enhanced logging for new intent
        Log.d(TAG, "Received new intent")
        Log.d(TAG, "Intent Action: ${intent.action}")
        Log.d(TAG, "Intent Data: ${intent.data}")
        
        try {
            handleIntent(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling new intent: ${e.message}", e)
            Log.e(TAG, "Stack trace: ", e)
            Toast.makeText(this, "Error handling authentication", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }
    
    private fun handleIntent(intent: Intent) {
        // Check if this is a redirect to the web frontend
        if (intent.data?.toString()?.contains("serbisyo.vercel.app/oauth-role-selection") == true) {
            // This is a redirect to the web frontend that should be handled by the mobile app
            val uri = intent.data
            Log.d(TAG, "Intercepted redirect to web frontend: $uri")
            
            // Extract parameters from the URL
            val email = uri?.getQueryParameter("email")
            val name = uri?.getQueryParameter("name")
            val picture = uri?.getQueryParameter("picture")
            
            Log.d(TAG, "Extracted from web frontend - email: $email, name: $name, picture: $picture")
            
            if (email != null && name != null) {
                navigateToRoleSelection(email, name, picture ?: "")
                return
            } else {
                Log.e(TAG, "Missing required parameters in web frontend redirect")
                Toast.makeText(this, "Missing required parameters in redirect", Toast.LENGTH_LONG).show()
                navigateToLogin()
                return
            }
        }
        
        // Check if intent data contains localhost (which might cause redirection issues)
        if (intent.data?.toString()?.contains("localhost") == true) {
            Log.w(TAG, "Intent contains web URL which may cause issues: ${intent.data}")
            // Try to modify the URL to use production domain and handle it
            var modifiedUrl = intent.data.toString()
            
            // Convert localhost to production URL if needed
            if (modifiedUrl.contains("localhost:8080")) {
                modifiedUrl = modifiedUrl.replace("localhost:8080", "serbisyo-backend.onrender.com")
            }
            
            // Check if this is a redirect to web frontend that we need to intercept
            if (modifiedUrl.contains("serbisyo.vercel.app")) {
                Log.d(TAG, "Intercepting redirect to web frontend: $modifiedUrl")
                
                // Extract query parameters regardless of the path
                val uri = Uri.parse(modifiedUrl)
                val token = uri.getQueryParameter("token")
                val userId = uri.getQueryParameter("userId")
                val role = uri.getQueryParameter("role")
                val email = uri.getQueryParameter("email")
                val name = uri.getQueryParameter("name")
                val picture = uri.getQueryParameter("picture")
                
                Log.d(TAG, "Extracted from web redirect - token: $token, role: $role, email: $email, name: $name")
                
                if (token != null && role != null) {
                    // Existing user flow
                    handleExistingUserRedirect(token, userId, role)
                    return
                } else if (email != null && name != null) {
                    // New user flow
                    navigateToRoleSelection(email, name, picture ?: "")
                    return
                }
                
                // If no useful parameters found, try to extract from path components
                if (modifiedUrl.contains("/oauth2/redirect") || modifiedUrl.contains("/oauth-role-selection")) {
                    // This is likely a redirect with parameters in the fragment or path
                    // Try to handle it by checking for fragments
                    val fragment = uri.fragment
                    if (fragment != null) {
                        val fragmentParams = fragment.split("&").associate { 
                            val parts = it.split("=")
                            if (parts.size == 2) parts[0] to parts[1] else "" to ""
                        }
                        
                        val tokenFromFragment = fragmentParams["token"]
                        val roleFromFragment = fragmentParams["role"]
                        val emailFromFragment = fragmentParams["email"]
                        val nameFromFragment = fragmentParams["name"]
                        
                        if (tokenFromFragment != null && roleFromFragment != null) {
                            handleExistingUserRedirect(tokenFromFragment, fragmentParams["userId"], roleFromFragment)
                            return
                        } else if (emailFromFragment != null && nameFromFragment != null) {
                            navigateToRoleSelection(emailFromFragment, nameFromFragment, fragmentParams["picture"] ?: "")
                            return
                        }
                    }
                }
            } else {
                // Not a web frontend URL, just a production backend URL
                val productionUri = android.net.Uri.parse(modifiedUrl)
                
                // Extract parameters from the fixed URI
                val token = productionUri.getQueryParameter("token")
                val userId = productionUri.getQueryParameter("userId")
                val role = productionUri.getQueryParameter("role")
                val email = productionUri.getQueryParameter("email")
                val name = productionUri.getQueryParameter("name")
                val picture = productionUri.getQueryParameter("picture")
                
                Log.d(TAG, "Extracted from fixed URI - token: $token, role: $role, email: $email, name: $name")
                
                if (token != null && role != null) {
                    handleExistingUserRedirect(token, userId, role)
                    return
                } else if (email != null && name != null) {
                    navigateToRoleSelection(email, name, picture ?: "")
                    return
                }
            }
            // Otherwise, continue with normal processing
        }
        
        // Get token and user info from intent
        val token = intent.getStringExtra("token")
        val userId = intent.getStringExtra("userId")
        val role = intent.getStringExtra("role")
        
        // Enhanced logging
        Log.d(TAG, "Processing intent extras - token: ${token != null}, userId: $userId, role: $role")
        
        // If token exists, we're handling a successful OAuth login
        if (token != null && role != null) {
            handleExistingUserRedirect(token, userId, role)
        } else {
            // Otherwise, we need to check if this is a new user who needs to select a role
            // or if there was an error
            val error = intent.getStringExtra("error")
            
            if (error != null) {
                Log.e(TAG, "Authentication error received: $error")
                Toast.makeText(this, "Authentication error: $error", Toast.LENGTH_LONG).show()
                navigateToLogin()
                return
            }
            
            // Handle Google redirect with user info for new user
            val email = intent.getStringExtra("email")
            val name = intent.getStringExtra("name")
            val picture = intent.getStringExtra("picture")
            
            Log.d(TAG, "New user info - email: $email, name: $name")
            
            if (email != null && name != null) {
                navigateToRoleSelection(email, name, picture ?: "")
            } else {
                Log.e(TAG, "Missing user information in intent")
                Toast.makeText(this, "Missing user information", Toast.LENGTH_LONG).show()
                navigateToLogin()
            }
        }
    }
    
    private fun handleExistingUserRedirect(token: String, userId: String?, role: String) {
        // Save authentication data
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("token", token)
            
            // Convert userId to Long if present
            val userIdLong = userId?.toLongOrNull() ?: 0L
            putLong("userId", userIdLong)
            
            putString("role", role)
            apply()
        }
        
        // Navigate based on role
        if (role.lowercase().contains("customer")) {
            startActivity(Intent(this, CustomerDashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        } else {
            startActivity(Intent(this, ServiceProviderDashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
        
        finish()
    }
    
    private fun navigateToRoleSelection(email: String, name: String, picture: String) {
        val intent = Intent(this, OAuth2RoleSelectionActivity::class.java).apply {
            putExtra("email", email)
            putExtra("name", name)
            putExtra("picture", picture)
        }
        startActivity(intent)
        finish()
    }
    
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
} 