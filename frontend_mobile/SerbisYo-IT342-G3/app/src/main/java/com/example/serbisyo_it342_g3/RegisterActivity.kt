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
        
        // Optional address fields
        val street = intent.getStringExtra("STREET")
        val city = intent.getStringExtra("CITY")
        val province = intent.getStringExtra("PROVINCE")
        val postalCode = intent.getStringExtra("POSTAL_CODE")
        
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
                    businessName,
                    street,
                    city,
                    province,
                    postalCode
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
        businessName: String,
        street: String?,
        city: String?,
        province: String?,
        postalCode: String?
    ) {
        thread {
            try {
                val client = OkHttpClient()

                // Step 1: Create user auth
                val userAuthJson = JSONObject().apply {
                    put("userName", username)
                    put("password", password)
                    put("email", email)
                    put("role", if (role.equals("SERVICE_PROVIDER", ignoreCase = true)) 
                               "Service Provider" else "Customer")
                }
                
                // Create the address first - critical because the database requires it
                // Create address JSON with all available fields
                val addressJson = JSONObject().apply {
                    if (!street.isNullOrEmpty()) put("streetName", street)
                    if (!city.isNullOrEmpty()) put("city", city)
                    if (!province.isNullOrEmpty()) put("province", province)
                    if (!postalCode.isNullOrEmpty()) put("zipCode", postalCode)
                    put("main", true)
                }
                
                // Create the customer or service provider JSON WITHOUT address_id
                // The entity doesn't have this field, so we remove it to avoid the Unrecognized field error
                val customerJson = if (role.equals("CUSTOMER", ignoreCase = true)) {
                    JSONObject().apply {
                        put("firstName", firstName)
                        put("lastName", lastName)
                        put("phoneNumber", phoneNumber)
                        // Removed address_id as it causes "Unrecognized field" error in CustomerEntity
                    }
                } else {
                    JSONObject()
                }

                val serviceProviderJson = if (role.equals("SERVICE_PROVIDER", ignoreCase = true)) {
                    JSONObject().apply {
                        put("firstName", firstName)
                        put("lastName", lastName)
                        put("phoneNumber", phoneNumber)
                        put("businessName", businessName)
                        put("yearsOfExperience", 0)
                        put("availabilitySchedule", "Monday-Friday, 9AM-5PM")
                        put("status", "Active")
                        put("paymentMethod", "Cash")
                        // Removed address_id as it causes "Unrecognized field" error in ServiceProviderEntity
                    }
                } else {
                    JSONObject()
                }

                // Create the main request body
                val requestBodyJson = JSONObject().apply {
                    put("userAuth", userAuthJson)
                    
                    // Always include address - this is critical for the backend
                    // The address must be created first and linked to the customer/provider
                    put("address", addressJson)
                    
                    if (role.equals("CUSTOMER", ignoreCase = true)) {
                        put("customer", customerJson)
                    } else {
                        put("serviceProvider", serviceProviderJson)
                    }
                }

                val requestBody = requestBodyJson.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())

                // Log the request for debugging
                println("Registration request: $requestBodyJson")

                val request = Request.Builder()

                    //.url("http://192.168.254.103:8080/api/user-auth/register")

                     //Replace lng if mag kinaunsa
                    //.url(com.example.serbisyo_it342_g3.utils.Constants.BASE_URL + "user-auth/register")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    println("Registration response: $responseBody")
                    
                    if (response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                            // Navigate to login screen
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    } else {
                        // Safe handling of error response
                        val errorMessage = try {
                            when (response.code) {
                                400 -> {
                                    // Safely extract error message if it exists
                                    val errorBody = responseBody ?: ""
                                    if (errorBody.contains("Unrecognized field")) {
                                        "Registration failed: Backend entity-field mismatch (address_id). Contact backend developer."
                                    } else if (errorBody.contains("doesn't have a default value")) {
                                        "Registration failed: Database requires address. Contact backend developer to fix schema."
                                    } else {
                                        "Registration failed: ${if (errorBody.isNotEmpty()) errorBody else "Invalid data or user already exists"}"
                                    }
                                }
                                500 -> "Server error: Address_id required in database but entity doesn't support it. Contact backend developer."
                                else -> "Registration failed: ${responseBody ?: response.message}"
                            }
                        } catch (e: Exception) {
                            // If we encounter any exception while parsing the error response
                            e.printStackTrace()
                            "Registration failed: ${e.message ?: "Unknown error"}"
                        }
                        
                        runOnUiThread {
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
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