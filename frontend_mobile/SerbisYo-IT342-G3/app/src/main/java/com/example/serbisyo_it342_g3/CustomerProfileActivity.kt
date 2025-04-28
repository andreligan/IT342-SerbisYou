package com.example.serbisyo_it342_g3

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.serbisyo_it342_g3.api.UserApiClient
import com.example.serbisyo_it342_g3.data.Customer

class CustomerProfileActivity : AppCompatActivity() {
    private val TAG = "CustomerProfileActivity"

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var btnUpdateProfile: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvErrorMessage: TextView
    
    private lateinit var userApiClient: UserApiClient
    private var token: String = ""
    private var userId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_profile)
        
        // Initialize the API client
        userApiClient = UserApiClient(this)
        
        // Initialize views
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile)
        progressBar = findViewById(R.id.progressBar)
        tvErrorMessage = findViewById(R.id.tvErrorMessage)
        
        // Get user data from SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        token = sharedPref.getString("token", "") ?: ""
        
        // Fix userId retrieval using try-catch
        userId = try {
            // Try to get as Long first (new format)
            sharedPref.getLong("userId", 0)
        } catch (e: ClassCastException) {
            // If that fails, try the String format (old format) and convert
            val userIdStr = sharedPref.getString("userId", "0")
            userIdStr?.toLongOrNull() ?: 0
        }
        
        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "My Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Load user profile
        loadUserProfile()
        
        // Set update button click listener
        btnUpdateProfile.setOnClickListener {
            if (validateInputs()) {
                updateProfile()
            }
        }
    }
    
    private fun loadUserProfile() {
        progressBar.visibility = View.VISIBLE
        tvErrorMessage.visibility = View.GONE
        
        userApiClient.getCustomerProfile(userId, token) { customer, error ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error loading profile", error)
                    tvErrorMessage.text = "Customer profile not found. Please contact support."
                    tvErrorMessage.visibility = View.VISIBLE
                    return@runOnUiThread
                }
                
                if (customer != null) {
                    // Fill form with customer data
                    etUsername.setText(customer.username)
                    etEmail.setText(customer.email)
                    etFirstName.setText(customer.firstName)
                    etLastName.setText(customer.lastName)
                    etPhoneNumber.setText(customer.phoneNumber)
                } else {
                    tvErrorMessage.text = "Customer profile not found. Please contact support."
                    tvErrorMessage.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (etFirstName.text.toString().trim().isEmpty()) {
            etFirstName.error = "First name cannot be empty"
            isValid = false
        }
        
        if (etLastName.text.toString().trim().isEmpty()) {
            etLastName.error = "Last name cannot be empty"
            isValid = false
        }
        
        if (etPhoneNumber.text.toString().trim().isEmpty()) {
            etPhoneNumber.error = "Phone number cannot be empty"
            isValid = false
        }
        
        return isValid
    }
    
    private fun updateProfile() {
        progressBar.visibility = View.VISIBLE
        
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        
        val updatedCustomer = Customer(
            customerId = userId,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber
        )
        
        userApiClient.updateCustomerProfile(updatedCustomer, token) { success, error ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Toast.makeText(this, "Error updating profile: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                if (success) {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}