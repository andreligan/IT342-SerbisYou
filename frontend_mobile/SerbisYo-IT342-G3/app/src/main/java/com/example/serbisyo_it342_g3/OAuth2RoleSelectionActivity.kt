package com.example.serbisyo_it342_g3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.serbisyo_it342_g3.api.BaseApiClient
import com.example.serbisyo_it342_g3.utils.ImageUtils
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.concurrent.thread

class OAuth2RoleSelectionActivity : AppCompatActivity() {
    private lateinit var btnCustomer: Button
    private lateinit var btnServiceProvider: Button
    private lateinit var imgUserProfile: ImageView
    private lateinit var tvWelcomeUser: TextView
    private lateinit var manualEntryLayout: LinearLayout
    private lateinit var etEmail: EditText
    private lateinit var etName: EditText
    private lateinit var btnContinue: Button
    
    private lateinit var baseApiClient: BaseApiClient
    private val TAG = "OAuth2RoleSelection"
    
    // User data from Google authentication
    private var email: String = ""
    private var name: String = ""
    private var picture: String = ""
    private var isManualEntry: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth2_role_selection)
        
        // Initialize views
        btnCustomer = findViewById(R.id.btnCustomer)
        btnServiceProvider = findViewById(R.id.btnServiceProvider)
        imgUserProfile = findViewById(R.id.imgUserProfile)
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser)
        
        // Find manual entry layout elements
        manualEntryLayout = findViewById(R.id.manualEntryLayout)
        etEmail = findViewById(R.id.etEmail)
        etName = findViewById(R.id.etName)
        btnContinue = findViewById(R.id.btnContinue)
        
        // Initialize API client
        baseApiClient = BaseApiClient(this)
        
        // Get user data from intent
        email = intent.getStringExtra("email") ?: ""
        name = intent.getStringExtra("name") ?: ""
        picture = intent.getStringExtra("picture") ?: ""
        isManualEntry = intent.getBooleanExtra("isManualEntry", false)
        
        if (isManualEntry) {
            // Show manual entry form
            showManualEntryForm()
        } else if (email.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Error: Missing user information", Toast.LENGTH_LONG).show()
            finish()
            return
        } else {
            // Show role selection screen with prefilled data
            showRoleSelection()
        }
        
        // Setup buttons
        btnContinue.setOnClickListener {
            validateAndContinue()
        }
        
        // Role selection handlers
        btnCustomer.setOnClickListener {
            registerOAuthUser("Customer")
        }
        
        btnServiceProvider.setOnClickListener {
            registerOAuthUser("Service Provider")
        }
    }
    
    private fun showManualEntryForm() {
        manualEntryLayout.visibility = View.VISIBLE
        btnCustomer.visibility = View.GONE
        btnServiceProvider.visibility = View.GONE
        tvWelcomeUser.text = "Enter your Google account details"
        imgUserProfile.setImageResource(R.drawable.ic_google)
        
        // Update the subtitle text
        findViewById<TextView>(R.id.tvCompleteRegistration).text = "Please enter your Google account information below"
        
        // Show back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }
    
    private fun showRoleSelection() {
        manualEntryLayout.visibility = View.GONE
        btnCustomer.visibility = View.VISIBLE
        btnServiceProvider.visibility = View.VISIBLE
        
        // Set welcome message and profile image
        tvWelcomeUser.text = "Welcome, $name!"
        if (picture.isNotEmpty()) {
            ImageUtils.loadImageAsync(picture, imgUserProfile)
        } else {
            imgUserProfile.setImageResource(R.drawable.ic_person)
        }
        
        // Update the subtitle text
        findViewById<TextView>(R.id.tvCompleteRegistration).text = "Complete your registration by selecting your role"
    }
    
    private fun validateAndContinue() {
        val enteredEmail = etEmail.text.toString().trim()
        val enteredName = etName.text.toString().trim()
        
        if (enteredEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()) {
            etEmail.error = "Valid email is required"
            return
        }
        
        if (enteredName.isEmpty()) {
            etName.error = "Name is required"
            return
        }
        
        // Update values
        email = enteredEmail
        name = enteredName
        
        // Now show role selection
        showRoleSelection()
    }
    
    private fun registerOAuthUser(role: String) {
        // Show loading
        Toast.makeText(this, "Processing...", Toast.LENGTH_SHORT).show()
        
        // Extract first and last name
        val nameParts = name.split(" ")
        val firstName = nameParts.firstOrNull() ?: ""
        val lastName = if (nameParts.size > 1) nameParts.subList(1, nameParts.size).joinToString(" ") else ""
        
        thread {
            try {
                val client = baseApiClient.client
                
                // Create request body based on role
                val jsonObj = JSONObject()
                
                // Common user auth data
                val userAuthObj = JSONObject()
                userAuthObj.put("userName", email)
                userAuthObj.put("email", email)
                userAuthObj.put("role", role)
                userAuthObj.put("platform", "android") // Add platform flag to specify Android
                jsonObj.put("userAuth", userAuthObj)
                
                // Default address (will be updated later by user)
                val addressObj = JSONObject()
                addressObj.put("streetName", "")
                addressObj.put("barangay", "")
                addressObj.put("city", "")
                addressObj.put("province", "")
                addressObj.put("zipCode", "")
                jsonObj.put("address", addressObj)
                
                // Role-specific data
                if (role == "Customer") {
                    val customerObj = JSONObject()
                    customerObj.put("firstName", firstName)
                    customerObj.put("lastName", lastName)
                    customerObj.put("phoneNumber", "")
                    jsonObj.put("customer", customerObj)
                } else {
                    val providerObj = JSONObject()
                    providerObj.put("firstName", firstName)
                    providerObj.put("lastName", lastName)
                    providerObj.put("phoneNumber", "")
                    providerObj.put("businessName", "${firstName}'s Services")
                    jsonObj.put("serviceProvider", providerObj)
                }
                
                val requestBody = jsonObj.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())
                
                // Add mobile app redirect flag to URL
                val requestUrl = "${baseApiClient.getBaseUrl()}/api/oauth/register?redirectToMobile=true"
                
                Log.d(TAG, "Sending OAuth registration to: $requestUrl")
                Log.d(TAG, "Request body: $jsonObj")
                
                val request = okhttp3.Request.Builder()
                    .url(requestUrl)
                    .post(requestBody)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    Log.d(TAG, "Response code: ${response.code}")
                    
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: ""
                        Log.d(TAG, "Response: $responseBody")
                        
                        try {
                            val jsonResponse = Gson().fromJson(responseBody, Map::class.java)
                            
                            // Extract data
                            val token = jsonResponse["token"] as? String
                            val userId = jsonResponse["userId"] as? String
                            val userRole = jsonResponse["role"] as? String
                            
                            if (token != null && userRole != null) {
                                // Save authentication data
                                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    putString("token", token)
                                    // Convert userId if present
                                    val userIdLong = userId?.toLongOrNull() ?: 0L
                                    putLong("userId", userIdLong)
                                    putString("username", email)
                                    putString("role", userRole)
                                    apply()
                                }
                                
                                runOnUiThread {
                                    // Navigate to password change activity instead of dashboard
                                    val intent = Intent(this, OAuth2PasswordChangeActivity::class.java).apply {
                                        putExtra("token", token)
                                        putExtra("userId", userId?.toLongOrNull() ?: 0L)
                                        putExtra("role", userRole)
                                        putExtra("email", email)
                                    }
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this, "Registration error: Invalid response", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing registration response", e)
                            runOnUiThread {
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        // Handle error response
                        val errorBody = response.body?.string() ?: "Unknown error"
                        Log.e(TAG, "Error response: $errorBody")
                        runOnUiThread {
                            Toast.makeText(this, "Registration failed: ${response.code}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during registration", e)
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    override fun onBackPressed() {
        // If we're showing the manual entry form, we can just finish the activity
        if (manualEntryLayout.visibility == View.VISIBLE) {
            finish()
            return
        }
        
        // Otherwise, if we're at the role selection screen, go back to manual entry
        if (isManualEntry && manualEntryLayout.visibility == View.GONE) {
            showManualEntryForm()
            return
        }
        
        // In all other cases, just finish the activity
        super.onBackPressed()
    }
} 