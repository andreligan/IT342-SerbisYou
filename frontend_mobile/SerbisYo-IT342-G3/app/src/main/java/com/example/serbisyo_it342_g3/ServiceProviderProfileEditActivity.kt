package com.example.serbisyo_it342_g3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.serbisyo_it342_g3.api.UserApiClient
import com.example.serbisyo_it342_g3.data.Address
import com.example.serbisyo_it342_g3.data.ServiceProvider

class ServiceProviderProfileEditActivity : AppCompatActivity() {
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etBusinessName: EditText
    private lateinit var etYearsExperience: EditText
    private lateinit var etAvailability: EditText
    private lateinit var etStreet: EditText
    private lateinit var etCity: EditText
    private lateinit var etProvince: EditText
    private lateinit var etZipCode: EditText
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var userApiClient: UserApiClient
    private var userId: Long = 0
    private var token: String = ""
    private val TAG = "ServiceProviderProfileEdit"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_provider_profile_edit)

        // Initialize ApiClient
        userApiClient = UserApiClient(this)
        
        // Get SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        token = sharedPref.getString("token", "") ?: ""
        val userIdStr = sharedPref.getString("userId", "0")
        userId = userIdStr?.toLongOrNull() ?: 0
        
        // Debug SharedPreferences
        val allPrefs = sharedPref.all
        Log.d(TAG, "All SharedPreferences: $allPrefs")
        Log.d(TAG, "Token from SharedPreferences: ${token.take(20)}...")
        Log.d(TAG, "Token length: ${token.length}")
        Log.d(TAG, "UserID from SharedPreferences: $userId")
        
        if (userId == 0L) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        if (token.isBlank()) {
            Toast.makeText(this, "Error: Authentication token not found", Toast.LENGTH_SHORT).show()
            // Redirect to login screen
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        
        Log.d(TAG, "User ID: $userId, Token: ${token.take(20)}...")

        // Initialize views
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etBusinessName = findViewById(R.id.etBusinessName)
        etYearsExperience = findViewById(R.id.etYearsExperience)
        etAvailability = findViewById(R.id.etAvailability)
        etStreet = findViewById(R.id.etStreet)
        etCity = findViewById(R.id.etCity)
        etProvince = findViewById(R.id.etProvince)
        etZipCode = findViewById(R.id.etZipCode)
        btnSave = findViewById(R.id.btnSave)
        progressBar = findViewById(R.id.progressBar)

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Edit Business Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Load current profile
        loadProfile()

        // Save button click
        btnSave.setOnClickListener {
            if (validateInputs()) {
                saveProfile()
            }
        }
    }
    
    private fun loadProfile() {
        progressBar.visibility = View.VISIBLE
        
        userApiClient.getServiceProviderProfile(userId, token) { provider, error ->
            if (error != null) {
                Log.e(TAG, "Error loading profile", error)
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error loading profile: ${error.message}", Toast.LENGTH_SHORT).show()
                }
                return@getServiceProviderProfile
            }
            
            runOnUiThread {
                progressBar.visibility = View.GONE
                if (provider != null) {
                    // Fill form with service provider data
                    etBusinessName.setText(provider.businessName)
                    etFirstName.setText(provider.firstName)
                    etLastName.setText(provider.lastName)
                    etPhoneNumber.setText(provider.phoneNumber)
                    etYearsExperience.setText(provider.yearsOfExperience.toString())
                    etAvailability.setText(provider.availabilitySchedule)
                    
                    // Address data
                    provider.address?.let { address ->
                        etStreet.setText(address.street)
                        etCity.setText(address.city)
                        etProvince.setText(address.province)
                        etZipCode.setText(address.postalCode)
                    }
                }
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        val businessName = etBusinessName.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val yearsExperience = etYearsExperience.text.toString().trim()
        val availability = etAvailability.text.toString().trim()
        val street = etStreet.text.toString().trim()
        val city = etCity.text.toString().trim()
        val province = etProvince.text.toString().trim()
        val zipCode = etZipCode.text.toString().trim()
        
        if (businessName.isEmpty()) {
            etBusinessName.error = "Business name is required"
            return false
        }
        
        if (firstName.isEmpty()) {
            etFirstName.error = "First name is required"
            return false
        }
        
        if (lastName.isEmpty()) {
            etLastName.error = "Last name is required"
            return false
        }
        
        if (phoneNumber.isEmpty()) {
            etPhoneNumber.error = "Phone number is required"
            return false
        }
        
        if (yearsExperience.isEmpty()) {
            etYearsExperience.error = "Years of experience is required"
            return false
        }
        
        if (availability.isEmpty()) {
            etAvailability.error = "Availability schedule is required"
            return false
        }
        
        if (street.isEmpty()) {
            etStreet.error = "Street address is required"
            return false
        }
        
        if (city.isEmpty()) {
            etCity.error = "City is required"
            return false
        }
        
        if (province.isEmpty()) {
            etProvince.error = "Province is required"
            return false
        }
        
        if (zipCode.isEmpty()) {
            etZipCode.error = "ZIP code is required"
            return false
        }
        
        return true
    }
    
    private fun saveProfile() {
        progressBar.visibility = View.VISIBLE
        btnSave.isEnabled = false
        
        val businessName = etBusinessName.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val yearsExperience = etYearsExperience.text.toString().toIntOrNull() ?: 0
        val availability = etAvailability.text.toString().trim()
        val street = etStreet.text.toString().trim()
        val city = etCity.text.toString().trim()
        val province = etProvince.text.toString().trim()
        val zipCode = etZipCode.text.toString().trim()
        
        val address = Address(
            addressId = 0, // Will be updated by backend
            street = street,
            city = city,
            province = province,
            postalCode = zipCode
        )
        
        val provider = ServiceProvider(
            providerId = userId,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            businessName = businessName,
            yearsOfExperience = yearsExperience,
            availabilitySchedule = availability,
            address = address
        )
        
        userApiClient.updateServiceProviderProfile(provider, token) { success, error ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                btnSave.isEnabled = true
                
                if (error != null) {
                    Toast.makeText(this, "Error updating profile: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                if (success) {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 