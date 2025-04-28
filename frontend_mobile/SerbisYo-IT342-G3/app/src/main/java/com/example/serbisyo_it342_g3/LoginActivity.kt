package com.example.serbisyo_it342_g3

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.serbisyo_it342_g3.api.BaseApiClient
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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
    private lateinit var btnGoogleSignIn: Button
    private lateinit var baseApiClient: BaseApiClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var sharedPreferences: SharedPreferences
    
    private val loginActivityTag = "LoginActivity"
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignUp = findViewById(R.id.tvSignUp)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Initialize BaseApiClient
        baseApiClient = BaseApiClient(this)
        
        // Set up ActivityResultLauncher for Google Sign-In
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                handleGoogleSignInResult(data)
            } else {
                Log.d(loginActivityTag, "Google Sign-In failed: ${result.resultCode}")
            }
        }
        
        // Check for OAuth error from browser redirect
        if (intent.getBooleanExtra("auth_error", false)) {
            Toast.makeText(this, "Google authentication failed. Please try again or use username/password.", Toast.LENGTH_LONG).show()
        }
        
        // Add long press listener to Google Sign-In button to check status manually
        btnGoogleSignIn.setOnLongClickListener {
            checkGoogleLoginStatus()
            true
        }
        
        // Network connectivity check
        if (!baseApiClient.isNetworkAvailable()) {
            Toast.makeText(this, "No network connection available", Toast.LENGTH_LONG).show()
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Debug message
            Log.d(loginActivityTag, "Attempting login with username: $username")
            Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show()
            
            loginUser(username, password)
        }

        tvSignUp.setOnClickListener {
            // Redirect to the new multi-step registration flow
            startActivity(Intent(this, MultiStepRegistrationActivity::class.java))
        }
        
        // Set up Google Sign In button
        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
        
        // Check if we should start Google Sign-In immediately (from registration fragment)
        if (intent.getBooleanExtra("startGoogleSignIn", false)) {
            signInWithGoogle()
        }
        
        // Check if we have Google Sign-In data passed from registration
        val googleSignInData = intent.getParcelableExtra("googleSignInData") as? Intent
        if (googleSignInData != null) {
            handleGoogleSignInResult(googleSignInData)
        }
    }
    
    private fun signInWithGoogle() {
        try {
            // Get the server base URL
            val baseUrl = baseApiClient.getBaseUrl()
            
            // Create direct URL to Google Sign-In endpoint
            val googleAuthUrl = "$baseUrl/oauth2/authorization/google"
            
            // Open in Chrome Custom Tabs (preferred by Google) or external browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleAuthUrl))
            
            // To customize Chrome Custom Tabs for better experience
            try {
                // Try to use Chrome Custom Tabs if available
                val customTabsIntent = androidx.browser.customtabs.CustomTabsIntent.Builder()
                    .setColorSchemeParams(
                        androidx.browser.customtabs.CustomTabsIntent.COLOR_SCHEME_LIGHT,
                        androidx.browser.customtabs.CustomTabColorSchemeParams.Builder()
                            .setToolbarColor(resources.getColor(R.color.colorPrimary, theme))
                            .build()
                    )
                    .setShowTitle(true)
                    .build()
                
                // Find Chrome or other browser that supports Custom Tabs
                val packageName = "com.android.chrome" // Default to Chrome
                customTabsIntent.intent.setPackage(packageName)
                customTabsIntent.launchUrl(this, Uri.parse(googleAuthUrl))
            } catch (e: Exception) {
                // Fallback to regular browser intent
                startActivity(intent)
                Log.e(loginActivityTag, "Error using CustomTabs: ${e.message}", e)
            }
            
            // Log for debugging
            Log.d(loginActivityTag, "Opening Google auth URL with Custom Tabs: $googleAuthUrl")
            
            // Register redirect handler activity in manifest
            Toast.makeText(
                this, 
                "Please sign in with Google. After signing in, you'll be redirected back to the app.",
                Toast.LENGTH_LONG
            ).show()
            
        } catch (e: Exception) {
            Log.e(loginActivityTag, "Error starting Google Sign-In: ${e.message}", e)
            Toast.makeText(this, "Error starting Google Sign-In: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun handleGoogleSignInResult(data: Intent) {
        try {
            // Extract email from the intent data
            val email = data.getStringExtra("email") ?: ""
            
            if (email.isNotEmpty()) {
                // We have email from Google authentication, check if user exists
                checkExistingUser(email, data.getStringExtra("name") ?: "", data.getStringExtra("picture") ?: "")
            } else {
                Log.e(loginActivityTag, "No email received from Google authentication")
                Toast.makeText(this, "Failed to get user information from Google", Toast.LENGTH_LONG).show()
            }
            
        } catch (e: Exception) {
            Log.e(loginActivityTag, "Error processing Google authentication result", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun checkExistingUser(email: String, name: String, picture: String) {
        thread {
            try {
                val client = baseApiClient.client
                
                // Create a request to check if user exists
                val jsonObj = JSONObject()
                jsonObj.put("email", email)
                
                val requestBody = jsonObj.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())
                
                val requestUrl = "${baseApiClient.getBaseUrl()}/api/user-auth/check-email"
                
                val request = Request.Builder()
                    .url(requestUrl)
                    .post(requestBody)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: ""
                        val exists = responseBody.contains("true")
                        
                        runOnUiThread {
                            if (exists) {
                                // User exists, try to login with Google credentials
                                loginWithGoogle(email)
                            } else {
                                // User doesn't exist, redirect to role selection
                                val intent = Intent(this, OAuth2RoleSelectionActivity::class.java).apply {
                                    putExtra("email", email)
                                    putExtra("name", name)
                                    putExtra("picture", picture)
                                }
                                startActivity(intent)
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "Error checking user: ${response.code}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(loginActivityTag, "Error checking existing user", e)
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun loginWithGoogle(email: String) {
        thread {
            try {
                val client = baseApiClient.client
                
                val jsonObj = JSONObject()
                jsonObj.put("email", email)
                jsonObj.put("isOAuth", true)
                
                val requestBody = jsonObj.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())
                
                val requestUrl = "${baseApiClient.getBaseUrl()}/api/user-auth/oauth-login"
                
                val request = Request.Builder()
                    .url(requestUrl)
                    .post(requestBody)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: ""
                        
                        try {
                            val jsonResponse = Gson().fromJson<Map<String, Any>>(responseBody, Map::class.java)
                            
                            // Store the username (email) before calling handleSuccessfulLogin
                            this.username = email
                            
                            // Use the centralized method to handle successful login
                            runOnUiThread {
                                handleSuccessfulLogin(jsonResponse)
                            }
                        } catch (e: Exception) {
                            Log.e(loginActivityTag, "Error parsing response", e)
                            runOnUiThread {
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "Login failed: ${response.code}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(loginActivityTag, "Error during Google login", e)
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun loginUser(username: String, password: String) {
        thread {
            try {
                Log.d(loginActivityTag, "Creating HTTP client")
                val client = baseApiClient.client
                
                val jsonObject = JSONObject()
                jsonObject.put("userName", username)
                jsonObject.put("password", password)
                
                val requestBody = jsonObject.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())
                
                val requestUrl = "${baseApiClient.getBaseUrl()}/api/user-auth/login"

                Log.d(loginActivityTag, "Sending login request to: $requestUrl")
                Log.d(loginActivityTag, "Request body: $jsonObject")
                
                val request = Request.Builder()
                    .url(requestUrl)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    Log.d(loginActivityTag, "Response code: ${response.code}")
                    
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: ""
                        Log.d(loginActivityTag, "Response body: $responseBody")
                        
                        try {
                            val jsonResponse = Gson().fromJson<Map<String, Any>>(responseBody, Map::class.java)
                            Log.d(loginActivityTag, "Parsed response: $jsonResponse")
                            
                            // Store the username in the class field before calling handleSuccessfulLogin
                            this.username = username
                            
                            // Use our new centralized method to handle the successful login
                            runOnUiThread {
                                handleSuccessfulLogin(jsonResponse)
                            }
                        } catch (e: Exception) {
                            Log.e(loginActivityTag, "Error parsing response", e)
                            runOnUiThread {
                                Toast.makeText(this, "Error parsing response: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        val errorBody = response.body?.string() ?: ""
                        Log.e(loginActivityTag, "Login failed: ${response.code} - $errorBody")
                        
                        runOnUiThread {
                            when (response.code) {
                                400 -> Toast.makeText(this, "Invalid username or password", Toast.LENGTH_LONG).show()
                                403 -> {
                                    // Try to parse the error message from the response
                                    try {
                                        val errorJson = if (errorBody.isNotEmpty()) {
                                            JSONObject(errorBody)
                                        } else {
                                            null
                                        }
                                        val message = errorJson?.optString("message") ?: "Access denied. Please check your credentials."
                                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        Log.e(loginActivityTag, "Error parsing error response", e)
                                        Toast.makeText(this, "Access denied. Please check your credentials.", Toast.LENGTH_LONG).show()
                                    }
                                }
                                401 -> Toast.makeText(this, "Unauthorized access", Toast.LENGTH_LONG).show()
                                else -> Toast.makeText(this, "Login failed (${response.code}): Please try again", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e(loginActivityTag, "Network error", e)
                runOnUiThread {
                    Toast.makeText(this, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e(loginActivityTag, "Error during login", e)
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Method to manually check if user is already logged in via Google
    // This helps users who completed Google authentication in browser
    // but the app didn't automatically capture the result
    private fun checkGoogleLoginStatus() {
        Toast.makeText(this, "Checking Google authentication status...", Toast.LENGTH_SHORT).show()
        
        thread {
            try {
                val client = baseApiClient.client
                val requestUrl = "${baseApiClient.getBaseUrl()}/api/user-auth/check-oauth-status"
                
                // Create an empty request to check if user has active session
                val requestBody = "{}".toRequestBody("application/json".toMediaTypeOrNull())
                
                val request = Request.Builder()
                    .url(requestUrl)
                    .post(requestBody)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: ""
                        Log.d(loginActivityTag, "OAuth status response: $responseBody")
                        
                        try {
                            val jsonResponse = Gson().fromJson<Map<String, Any>>(responseBody, Map::class.java)
                            val isAuthenticated = jsonResponse["authenticated"] as? Boolean ?: false
                            
                            if (isAuthenticated) {
                                // Get the email for username
                                val email = jsonResponse["email"] as? String ?: ""
                                
                                // Store the email as username
                                this.username = email
                                
                                // Use our centralized method to handle login
                                runOnUiThread {
                                    handleSuccessfulLogin(jsonResponse)
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this, "Not authenticated with Google. Please sign in.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(loginActivityTag, "Error parsing response", e)
                            runOnUiThread {
                                Toast.makeText(this, "Error checking status: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "Failed to check login status: ${response.code}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(loginActivityTag, "Error checking OAuth status", e)
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Handle successful login response
    private fun handleSuccessfulLogin(loginResponse: Map<String, Any>) {
        val token = loginResponse["token"] as? String ?: ""
        val userIdStr = loginResponse["userId"] as? String
        val userIdLong = userIdStr?.toLongOrNull() ?: 0
        val role = loginResponse["role"] as? String ?: ""

        Log.d(loginActivityTag, "Extracted data - Token: ${token.take(10)}..., Role: $role, UserId: $userIdLong")

        // Store user data in SharedPreferences
        with(sharedPreferences.edit()) {
            putString("token", token)
            putLong("userId", userIdLong)
            putString("username", username)  // from class field
            putString("role", role)
            
            // IMPORTANT: Don't set providerId here - will be retrieved by API later
            // Clear any existing providerId to avoid issues
            remove("providerId")
            apply()
        }
        
        // Also clear from user_prefs if it exists
        val userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(userPrefs.edit()) {
            remove("providerId")
            apply()
        }

        // Navigate to appropriate dashboard based on role
        if (role.equals("Service Provider", ignoreCase = true)) {
            Log.d(loginActivityTag, "Navigating to ServiceProviderDashboardActivity")
            val intent = Intent(this, ServiceProviderDashboardActivity::class.java)
            startActivity(intent)
            finish()
        } else if (role.equals("Customer", ignoreCase = true)) {
            Log.d(loginActivityTag, "Navigating to CustomerDashboardActivity")
            val intent = Intent(this, CustomerDashboardActivity::class.java)
            startActivity(intent)
            finish()
        } else if (role.equals("Admin", ignoreCase = true)) {
            // Redirect to admin dashboard (if implemented)
            Log.d(loginActivityTag, "Admin role detected but no Admin dashboard implemented")
            Toast.makeText(this, "Admin dashboard not implemented yet", Toast.LENGTH_SHORT).show()
        } else {
            Log.e(loginActivityTag, "Unknown role: $role")
            Toast.makeText(this, "Login successful but unknown role: $role", Toast.LENGTH_SHORT).show()
        }
    }
}
