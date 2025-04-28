package com.example.serbisyo_it342_g3

import android.content.Intent
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
        
        try {
            handleIntent(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling redirect: ${e.message}", e)
            Toast.makeText(this, "Error handling authentication", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        try {
            handleIntent(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling new intent: ${e.message}", e)
            Toast.makeText(this, "Error handling authentication", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }
    
    private fun handleIntent(intent: Intent) {
        // Get token and user info from intent
        val token = intent.getStringExtra("token")
        val userId = intent.getStringExtra("userId")
        val role = intent.getStringExtra("role")
        
        // If token exists, we're handling a successful OAuth login
        if (token != null && role != null) {
            handleExistingUserRedirect(token, userId, role)
        } else {
            // Otherwise, we need to check if this is a new user who needs to select a role
            // or if there was an error
            val error = intent.getStringExtra("error")
            
            if (error != null) {
                Toast.makeText(this, "Authentication error: $error", Toast.LENGTH_LONG).show()
                navigateToLogin()
                return
            }
            
            // Handle Google redirect with user info for new user
            val email = intent.getStringExtra("email")
            val name = intent.getStringExtra("name")
            val picture = intent.getStringExtra("picture")
            
            if (email != null && name != null) {
                navigateToRoleSelection(email, name, picture ?: "")
            } else {
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