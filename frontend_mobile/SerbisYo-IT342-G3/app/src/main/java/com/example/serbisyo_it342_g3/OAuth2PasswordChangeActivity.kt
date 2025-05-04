package com.example.serbisyo_it342_g3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.serbisyo_it342_g3.api.BaseApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.concurrent.thread

/**
 * Activity for changing password after OAuth registration
 * with option to skip and go straight to dashboard
 */
class OAuth2PasswordChangeActivity : AppCompatActivity() {
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSavePassword: Button
    private lateinit var btnSkip: Button
    
    private lateinit var baseApiClient: BaseApiClient
    private val TAG = "OAuth2PasswordChange"
    
    private var token: String = ""
    private var userId: Long = 0
    private var userRole: String = ""
    private var email: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth2_password_change)
        
        // Initialize views
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSavePassword = findViewById(R.id.btnSavePassword)
        btnSkip = findViewById(R.id.btnSkip)
        
        // Initialize API client
        baseApiClient = BaseApiClient(this)
        
        // Get data from intent
        token = intent.getStringExtra("token") ?: ""
        userId = intent.getLongExtra("userId", 0)
        userRole = intent.getStringExtra("role") ?: ""
        email = intent.getStringExtra("email") ?: ""
        
        if (token.isEmpty() || userId == 0L || userRole.isEmpty()) {
            Toast.makeText(this, "Error: Missing authentication information", Toast.LENGTH_LONG).show()
            navigateToDashboard()
            return
        }
        
        // Set up buttons
        btnSavePassword.setOnClickListener {
            if (validatePasswords()) {
                changePassword(etPassword.text.toString())
            }
        }
        
        btnSkip.setOnClickListener {
            // Skip password change and go to dashboard
            Toast.makeText(this, "You can change your password later in settings", Toast.LENGTH_SHORT).show()
            navigateToDashboard()
        }
    }
    
    private fun validatePasswords(): Boolean {
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()
        
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            return false
        }
        
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters"
            return false
        }
        
        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            return false
        }
        
        return true
    }
    
    private fun changePassword(newPassword: String) {
        Toast.makeText(this, "Updating password...", Toast.LENGTH_SHORT).show()
        
        thread {
            try {
                val client = baseApiClient.client
                
                val jsonObj = JSONObject()
                jsonObj.put("userId", userId)
                jsonObj.put("email", email)
                jsonObj.put("newPassword", newPassword)
                
                val requestBody = jsonObj.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())
                
                val requestUrl = "${baseApiClient.getBaseUrl()}/api/user-auth/set-password"
                
                Log.d(TAG, "Sending password change request to: $requestUrl")
                
                val request = Request.Builder()
                    .url(requestUrl)
                    .header("Authorization", "Bearer $token")
                    .post(requestBody)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    Log.d(TAG, "Response code: ${response.code}")
                    
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            navigateToDashboard()
                        } else {
                            val errorBody = response.body?.string() ?: ""
                            Log.e(TAG, "Password change failed: ${response.code} - $errorBody")
                            Toast.makeText(this, "Failed to update password. You can try again later in settings.", Toast.LENGTH_LONG).show()
                            
                            // Still navigate to dashboard even if password change failed
                            navigateToDashboard()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during password change", e)
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    navigateToDashboard()
                }
            }
        }
    }
    
    private fun navigateToDashboard() {
        // Navigate based on role
        val intent = if (userRole.contains("Customer", ignoreCase = true)) {
            Intent(this, CustomerDashboardActivity::class.java)
        } else {
            Intent(this, ServiceProviderDashboardActivity::class.java)
        }
        
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 