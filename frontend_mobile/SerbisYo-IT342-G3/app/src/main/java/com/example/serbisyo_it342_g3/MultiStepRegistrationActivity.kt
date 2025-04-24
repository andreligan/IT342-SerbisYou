package com.example.serbisyo_it342_g3

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread
import com.example.serbisyo_it342_g3.api.BaseApiClient

class MultiStepRegistrationActivity : AppCompatActivity() {

    // UI Components
    private lateinit var step1Circle: View
    private lateinit var step2Circle: View
    private lateinit var step3Circle: View
    private lateinit var step4Circle: View
    private lateinit var step1Text: TextView
    private lateinit var step2Text: TextView
    private lateinit var step3Text: TextView
    private lateinit var step4Text: TextView
    private lateinit var step1Label: TextView
    private lateinit var step2Label: TextView
    private lateinit var step3Label: TextView
    private lateinit var step4Label: TextView
    private lateinit var line1to2: View
    private lateinit var line2to3: View
    private lateinit var line3to4: View
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var tvTitle: TextView

    // Data
    private var currentStep = 1
    private var userType: String = "" // "customer" or "serviceProvider"
    private var firstName: String = ""
    private var lastName: String = ""
    private var phoneNumber: String = ""
    private var businessName: String = ""
    private var yearsExperience: String = "0"
    private var email: String = ""
    private var username: String = ""
    private var password: String = ""
    
    private val TAG = "MultiStepRegistration"

    // Add BaseApiClient field
    private lateinit var baseApiClient: BaseApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_step_registration)

        // Initialize BaseApiClient
        baseApiClient = BaseApiClient(this)

        // Initialize UI components
        initViews()

        // Set the initial fragment
        showFragment(RegistrationTypeFragment())
    }

    private fun initViews() {
        // Step indicators
        step1Circle = findViewById(R.id.step1Circle)
        step2Circle = findViewById(R.id.step2Circle)
        step3Circle = findViewById(R.id.step3Circle)
        step4Circle = findViewById(R.id.step4Circle)
        
        // Step texts
        step1Text = findViewById(R.id.step1Text)
        step2Text = findViewById(R.id.step2Text)
        step3Text = findViewById(R.id.step3Text)
        step4Text = findViewById(R.id.step4Text)
        
        // Step labels
        step1Label = findViewById(R.id.step1Label)
        step2Label = findViewById(R.id.step2Label)
        step3Label = findViewById(R.id.step3Label)
        step4Label = findViewById(R.id.step4Label)
        
        // Lines between steps
        line1to2 = findViewById(R.id.line1to2)
        line2to3 = findViewById(R.id.line2to3)
        line3to4 = findViewById(R.id.line3to4)
        
        // Fragment container
        fragmentContainer = findViewById(R.id.fragmentContainer)
        
        // Title
        tvTitle = findViewById(R.id.tvTitle)
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun goToNextStep() {
        currentStep++
        updateStepIndicators()
        
        when (currentStep) {
            2 -> {
                tvTitle.text = "Personal Details"
                showFragment(RegistrationDetailsFragment())
            }
            3 -> {
                tvTitle.text = "Create Account"
                showFragment(RegistrationCredentialsFragment())
            }
            4 -> {
                tvTitle.text = "Confirmation"
                showFragment(RegistrationConfirmationFragment())
            }
        }
    }

    fun goToPreviousStep() {
        if (currentStep > 1) {
            currentStep--
            updateStepIndicators()
            
            when (currentStep) {
                1 -> {
                    tvTitle.text = "Get Started"
                    showFragment(RegistrationTypeFragment())
                }
                2 -> {
                    tvTitle.text = "Personal Details"
                    showFragment(RegistrationDetailsFragment())
                }
                3 -> {
                    tvTitle.text = "Create Account"
                    showFragment(RegistrationCredentialsFragment())
                }
            }
        }
    }

    private fun updateStepIndicators() {
        // Reset all indicators to inactive
        val inactiveColor = resources.getColor(R.color.light_gray)
        val activeColor = resources.getColor(R.color.primary_green)
        
        // Reset all steps
        step1Circle.setBackgroundResource(R.drawable.circle_inactive)
        step2Circle.setBackgroundResource(R.drawable.circle_inactive)
        step3Circle.setBackgroundResource(R.drawable.circle_inactive)
        step4Circle.setBackgroundResource(R.drawable.circle_inactive)
        
        step1Text.setTextColor(inactiveColor)
        step2Text.setTextColor(inactiveColor)
        step3Text.setTextColor(inactiveColor)
        step4Text.setTextColor(inactiveColor)
        
        step1Label.setTextColor(inactiveColor)
        step2Label.setTextColor(inactiveColor)
        step3Label.setTextColor(inactiveColor)
        step4Label.setTextColor(inactiveColor)
        
        line1to2.setBackgroundColor(inactiveColor)
        line2to3.setBackgroundColor(inactiveColor)
        line3to4.setBackgroundColor(inactiveColor)
        
        // Activate current step and all completed steps
        when (currentStep) {
            4 -> {
                step4Circle.setBackgroundResource(R.drawable.circle_active)
                step4Text.setTextColor(activeColor)
                step4Label.setTextColor(activeColor)
                line3to4.setBackgroundColor(activeColor)
                step3Circle.setBackgroundResource(R.drawable.circle_active)
                step3Text.setTextColor(activeColor)
                step3Label.setTextColor(activeColor)
                line2to3.setBackgroundColor(activeColor)
                step2Circle.setBackgroundResource(R.drawable.circle_active)
                step2Text.setTextColor(activeColor)
                step2Label.setTextColor(activeColor)
                line1to2.setBackgroundColor(activeColor)
                step1Circle.setBackgroundResource(R.drawable.circle_active)
                step1Text.setTextColor(activeColor)
                step1Label.setTextColor(activeColor)
            }
            3 -> {
                step3Circle.setBackgroundResource(R.drawable.circle_active)
                step3Text.setTextColor(activeColor)
                step3Label.setTextColor(activeColor)
                line2to3.setBackgroundColor(activeColor)
                step2Circle.setBackgroundResource(R.drawable.circle_active)
                step2Text.setTextColor(activeColor)
                step2Label.setTextColor(activeColor)
                line1to2.setBackgroundColor(activeColor)
                step1Circle.setBackgroundResource(R.drawable.circle_active)
                step1Text.setTextColor(activeColor)
                step1Label.setTextColor(activeColor)
            }
            2 -> {
                step2Circle.setBackgroundResource(R.drawable.circle_active)
                step2Text.setTextColor(activeColor)
                step2Label.setTextColor(activeColor)
                line1to2.setBackgroundColor(activeColor)
                step1Circle.setBackgroundResource(R.drawable.circle_active)
                step1Text.setTextColor(activeColor)
                step1Label.setTextColor(activeColor)
            }
            1 -> {
                step1Circle.setBackgroundResource(R.drawable.circle_active)
                step1Text.setTextColor(activeColor)
                step1Label.setTextColor(activeColor)
            }
        }
    }

    // Data setters
    fun setUserType(type: String) {
        userType = type
    }

    fun setPersonalDetails(
        firstName: String, 
        lastName: String, 
        phoneNumber: String,
        businessName: String = "",
        yearsExperience: String = "0"
    ) {
        this.firstName = firstName
        this.lastName = lastName
        this.phoneNumber = phoneNumber
        this.businessName = businessName
        this.yearsExperience = yearsExperience
    }

    fun setCredentials(username: String, email: String, password: String) {
        this.username = username
        this.email = email
        this.password = password
    }

    // Data getters
    fun getUserType(): String = userType
    fun getFirstName(): String = firstName
    fun getLastName(): String = lastName
    fun getPhoneNumber(): String = phoneNumber
    fun getBusinessName(): String = businessName
    fun getYearsExperience(): String = yearsExperience
    fun getUsername(): String = username
    fun getEmail(): String = email
    fun getPassword(): String = password

    fun completeRegistration() {
        // Show processing message
        Toast.makeText(this, "Processing registration...", Toast.LENGTH_SHORT).show()
        
        thread {
            try {
                // Use baseApiClient's client instead
                val client = baseApiClient.client

                // Step 1: Create user auth
                val userAuthJson = JSONObject().apply {
                    put("userName", username)
                    put("password", password)
                    put("email", email)
                    put("role", if (userType == "serviceProvider") "Service Provider" else "Customer")
                }
                
                // Create address JSON with default values
                val addressJson = JSONObject().apply {
                    put("streetName", "Default Street")
                    put("city", "Default City")
                    put("province", "Default Province")
                    put("zipCode", "0000")
                    put("main", true)
                }
                
                // Create customer JSON WITHOUT addressId field
                val customerJson = if (userType == "customer") {
                    JSONObject().apply {
                        put("firstName", firstName)
                        put("lastName", lastName)
                        put("phoneNumber", phoneNumber)
                        // Don't include addressId here - it causes the "Unrecognized field" error
                    }
                } else {
                    JSONObject()
                }

                val serviceProviderJson = if (userType == "serviceProvider") {
                    JSONObject().apply {
                        put("firstName", firstName)
                        put("lastName", lastName)
                        put("phoneNumber", phoneNumber)
                        put("businessName", businessName)
                        
                        // Convert years of experience to integer
                        val years = try {
                            yearsExperience.toInt()
                        } catch (e: NumberFormatException) {
                            0
                        }
                        put("yearsOfExperience", years)
                        
                        put("availabilitySchedule", "Monday-Friday, 9AM-5PM")
                        put("status", "Active")
                        put("paymentMethod", "Cash")
                        // Don't include addressId here - it causes the "Unrecognized field" error
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
                    
                    if (userType == "customer") {
                        put("customer", customerJson)
                    } else {
                        put("serviceProvider", serviceProviderJson)
                    }
                }

                val requestBody = requestBodyJson.toString()
                    .toRequestBody("application/json".toMediaTypeOrNull())

                // Log the request for debugging
                Log.d(TAG, "Registration request: $requestBodyJson")

                val request = Request.Builder()
                    .url("${baseApiClient.getBaseUrl()}/api/user-auth/register")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    Log.d(TAG, "Registration response: $responseBody")
                    
                    if (response.isSuccessful) {
                        runOnUiThread {
                            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                            // Navigate to login screen
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                    } else {
                        // Try to parse error message - show the specific error from backend
                        val errorMessage = try {
                            responseBody?.let {
                                if (it.isNotEmpty()) {
                                    try {
                                        val jsonObject = JSONObject(it)
                                        jsonObject.optString("message", it)
                                    } catch (e: Exception) {
                                        it
                                    }
                                } else {
                                    "Registration failed: ${response.code}"
                                }
                            } ?: "Registration failed: ${response.code}"
                        } catch (e: Exception) {
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