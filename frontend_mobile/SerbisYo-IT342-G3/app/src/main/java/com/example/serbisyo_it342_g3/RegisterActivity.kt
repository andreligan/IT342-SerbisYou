package com.example.serbisyo_it342_g3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import kotlin.concurrent.thread
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class RegisterActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize views
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)

        // Get data from intent
        val role = intent.getStringExtra("ROLE") ?: ""
        val firstName = intent.getStringExtra("FIRST_NAME") ?: ""
        val lastName = intent.getStringExtra("LAST_NAME") ?: ""
        val phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: ""
        val street = intent.getStringExtra("STREET") ?: ""
        val city = intent.getStringExtra("CITY") ?: ""
        val province = intent.getStringExtra("PROVINCE") ?: ""
        val postalCode = intent.getStringExtra("POSTAL_CODE") ?: ""
        val barangay = intent.getStringExtra("BARANGAY") ?: ""

        // For service provider
        val businessName = intent.getStringExtra("BUSINESS_NAME") ?: ""

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (validateInputs(username, email, password, confirmPassword)) {
                registerUser(
                    username,
                    email,
                    password,
                    role,
                    firstName,
                    lastName,
                    phoneNumber,
                    street,
                    city,
                    province,
                    postalCode,
                    barangay,
                    businessName
                )
            }
        }
    }

    private fun validateInputs(username: String, email: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun registerUser(
        username: String,
        email: String,
        password: String,
        role: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        street: String,
        city: String,
        province: String,
        postalCode: String,
        barangay: String,
        businessName: String
    ) {
        thread {
            try {
                val client = OkHttpClient()

                // Create JSON objects for registration
                val userAuthJson = JSONObject().apply {
                    put("userName", username)
                    put("password", password)
                    put("email", email)
                    // Standardize role format to match backend expectations
                    put("role", if (role.equals("SERVICE_PROVIDER", ignoreCase = true)) 
                               "Service Provider" else "Customer")
                }

                val addressJson = JSONObject().apply {
                    put("streetName", street)
                    put("city", city)
                    put("barangay", barangay)
                    put("province", province)
                    put("zipCode", postalCode)
                }

                val profileJson = JSONObject().apply {
                    put("firstName", firstName)
                    put("lastName", lastName)
                    put("phoneNumber", phoneNumber)
                }

                // Add business-specific fields for service providers
                if (role.equals("SERVICE_PROVIDER", ignoreCase = true)) {
                    profileJson.put("businessName", businessName)
                    profileJson.put("yearsOfExperience", 0)
                    profileJson.put("availabilitySchedule", "Monday-Friday, 9AM-5PM")
                }

                // Create the main request body
                val requestBodyJson = JSONObject().apply {
                    put("userAuth", userAuthJson)
                    
                    // Important: Address needs to be handled separately to fix transient entity issues
                    if (role.equals("CUSTOMER", ignoreCase = true)) {
                        put("customer", profileJson)
                        // Directly attach address to the customer
                        profileJson.put("address", addressJson)
                    } else if (role.equals("SERVICE_PROVIDER", ignoreCase = true)) {
                        put("serviceProvider", profileJson)
                        // Directly attach address to the service provider
                        profileJson.put("address", addressJson)
                    }
                }

                val requestBody = requestBodyJson.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/api/user-auth/register") // Using 10.0.2.2 for emulator
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    if (response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                            // Navigate to login screen
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "Registration failed: ${response.code} - ${responseBody ?: response.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Network error: ${e.message}\nMake sure the backend server is running.", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}