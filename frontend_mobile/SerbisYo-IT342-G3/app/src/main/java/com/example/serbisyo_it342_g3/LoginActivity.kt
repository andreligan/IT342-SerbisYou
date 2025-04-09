package com.example.serbisyo_it342_g3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView
    
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Debug message
            Log.d(TAG, "Attempting login with username: $username")
            Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
            
            loginUser(username, password)
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, RoleSelection::class.java))
        }
    }

    private fun loginUser(username: String, password: String) {
        thread {
            try {
                Log.d(TAG, "Creating HTTP client")
                val client = OkHttpClient()
                
                val jsonObject = JSONObject()
                jsonObject.put("userName", username)
                jsonObject.put("password", password)
                
                val requestBody = jsonObject.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())
                
                val requestUrl = "http://10.0.2.2:8080/api/user-auth/login"
                Log.d(TAG, "Sending login request to: $requestUrl")
                Log.d(TAG, "Request body: $jsonObject")
                
                val request = Request.Builder()
                    .url(requestUrl)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    Log.d(TAG, "Response code: ${response.code}")
                    
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: ""
                        Log.d(TAG, "Response body: $responseBody")
                        
                        try {
                            val jsonResponse = Gson().fromJson(responseBody, Map::class.java)
                            Log.d(TAG, "Parsed response: $jsonResponse")
                            
                            // Extract data from response
                            val token = jsonResponse["token"] as? String
                            val role = jsonResponse["role"] as? String
                            val userId = jsonResponse["userId"] as? String
                            val userName = jsonResponse["userName"] as? String
                            
                            Log.d(TAG, "Extracted data - Token: $token, Role: $role, UserId: $userId, UserName: $userName")
                            
                            if (role == null) {
                                runOnUiThread {
                                    Toast.makeText(this, "Error: Role not found in response", Toast.LENGTH_LONG).show()
                                }
                                return@use
                            }
                            
                            // Save to shared preferences
                            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("token", token)
                                putString("userId", userId)
                                putString("username", userName ?: username)
                                putString("role", role)
                                apply()
                            }
                            
                            runOnUiThread {
                                // Navigate based on role - case insensitive check
                                if (role.contains("customer", ignoreCase = true)) {
                                    Log.d(TAG, "Navigating to CustomerDashboardActivity")
                                    Toast.makeText(this, "Login successful as Customer", Toast.LENGTH_SHORT).show()
                                    
                                    val intent = Intent(this, CustomerDashboardActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                } 
                                else if (role.contains("service", ignoreCase = true)) {
                                    Log.d(TAG, "Navigating to ServiceProviderDashboardActivity")
                                    Toast.makeText(this, "Login successful as Service Provider", Toast.LENGTH_SHORT).show()
                                    
                                    val intent = Intent(this, ServiceProviderDashboardActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                } 
                                else {
                                    Log.d(TAG, "Unknown role: $role")
                                    Toast.makeText(this, "Unknown role: $role", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing response", e)
                            runOnUiThread {
                                Toast.makeText(this, "Error parsing response: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        val errorBody = response.body?.string() ?: ""
                        Log.e(TAG, "Login failed: ${response.code} - $errorBody")
                        
                        runOnUiThread {
                            Toast.makeText(this, "Login failed: Invalid credentials", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error", e)
                runOnUiThread {
                    Toast.makeText(this, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during login", e)
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
