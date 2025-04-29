package com.example.serbisyo_it342_g3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.serbisyo_it342_g3.api.ServiceApiClient
import com.example.serbisyo_it342_g3.data.ServiceCategory
import com.example.serbisyo_it342_g3.data.Service
import android.content.SharedPreferences
import org.json.JSONObject
import android.app.AlertDialog

class AddServiceActivity : AppCompatActivity() {
    private lateinit var etServiceName: EditText
    private lateinit var etServiceDescription: EditText
    private lateinit var etPriceRange: EditText
    private lateinit var etDurationEstimate: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnAddService: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var serviceApiClient: ServiceApiClient
    private var categories = listOf<ServiceCategory>()
    private var providerId: Long = 0
    private var token: String = ""
    private val TAG = "AddServiceActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)

        // Initialize ServiceApiClient with context
        serviceApiClient = ServiceApiClient(this)

        // Get SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        token = sharedPreferences.getString("token", "") ?: ""

        Log.d(TAG, "Token from SharedPreferences: $token")

        // Get provider ID from intent
        providerId = intent.getLongExtra("PROVIDER_ID", 0)
        if (providerId == 0L) {
            Toast.makeText(this, "Error: Provider ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        etServiceName = findViewById(R.id.etServiceName)
        etServiceDescription = findViewById(R.id.etServiceDescription)
        etPriceRange = findViewById(R.id.etPriceRange)
        etDurationEstimate = findViewById(R.id.etDurationEstimate)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnAddService = findViewById(R.id.btnAddService)
        btnCancel = findViewById(R.id.btnCancel)
        progressBar = findViewById(R.id.progressBar)

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Add New Service"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Load categories for spinner
        loadCategories()

        // Add service button click
        btnAddService.setOnClickListener {
            if (validateInputs()) {
                addService()
            }
        }

        // Cancel button click
        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun loadCategories() {
        progressBar.visibility = View.VISIBLE

        serviceApiClient.getServiceCategories(token) { categoriesList, exception ->
            runOnUiThread {
                if (exception != null) {
                    Log.e(TAG, "Error loading categories", exception)
                    // Try to continue with mock categories if API call fails
                    setupMockCategories()
                    Toast.makeText(this, "Using default categories due to connection issue", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    return@runOnUiThread
                }

                if (categoriesList != null && categoriesList.isNotEmpty()) {
                    categories = categoriesList
                    val categoryNames = categories.map { it.categoryName }
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCategory.adapter = adapter
                    
                    Log.d(TAG, "Loaded ${categories.size} categories successfully")
                } else {
                    Log.e(TAG, "Categories list is empty or null")
                    // Use mock categories as fallback
                    setupMockCategories()
                    Toast.makeText(this, "No categories found, using defaults", Toast.LENGTH_SHORT).show()
                }

                progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupMockCategories() {
        // Create mock categories to allow the app to function without API
        categories = listOf(
            ServiceCategory(categoryId = 1, categoryName = "Home Repair"),
            ServiceCategory(categoryId = 2, categoryName = "Cleaning"),
            ServiceCategory(categoryId = 3, categoryName = "Food Delivery"),
            ServiceCategory(categoryId = 4, categoryName = "Transportation")
        )
        
        val categoryNames = categories.map { it.categoryName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
        
        Log.d(TAG, "Set up mock categories: ${categories.size}")
    }

    private fun validateInputs(): Boolean {
        val serviceName = etServiceName.text.toString()
        val serviceDescription = etServiceDescription.text.toString()
        val priceRange = etPriceRange.text.toString()
        val durationEstimate = etDurationEstimate.text.toString()

        if (serviceName.isEmpty() || serviceDescription.isEmpty() ||
            priceRange.isEmpty() || durationEstimate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (categories.isEmpty() || spinnerCategory.selectedItemPosition < 0) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return false
        }

        if (token.isEmpty()) {
            Toast.makeText(this, "Authentication token not found. Please log in again.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun addService() {
        val serviceName = etServiceName.text.toString()
        val serviceDescription = etServiceDescription.text.toString()
        val priceRange = etPriceRange.text.toString()
        val durationEstimate = etDurationEstimate.text.toString()
        val categoryPosition = spinnerCategory.selectedItemPosition

        if (categoryPosition < 0 || categoryPosition >= categories.size) {
            Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCategory = categories[categoryPosition]

        progressBar.visibility = View.VISIBLE
        btnAddService.isEnabled = false

        // Use the alternative method that takes providerId and categoryId directly
        serviceApiClient.createService(providerId, selectedCategory.categoryId,
            Service(
                serviceName = serviceName,
                serviceDescription = serviceDescription,
                priceRange = priceRange,
                price = priceRange,
                durationEstimate = durationEstimate
            ),
            token  // Pass the token
        ) { createdService, exception ->
            runOnUiThread {
                if (exception != null) {
                    Log.e(TAG, "Error adding service", exception)

                    var errorMessage = "Error adding service: ${exception.message}"
                    try {
                        val errorBody = exception.message?.substringAfter("{")?.let { "{$it" }
                        if (errorBody != null && errorBody.startsWith("{")) {
                            val jsonObject = JSONObject(errorBody)
                            if (jsonObject.has("message")) {
                                errorMessage = jsonObject.getString("message")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing error response: ${e.message}")
                    }

                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    btnAddService.isEnabled = true
                    progressBar.visibility = View.GONE
                    return@runOnUiThread
                }

                if (createdService != null) {
                    Toast.makeText(this, "Service added successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e(TAG, "Created service is null")
                    Toast.makeText(this, "Failed to add service", Toast.LENGTH_SHORT).show()
                    btnAddService.isEnabled = true
                }

                progressBar.visibility = View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}