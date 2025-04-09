package com.example.serbisyo_it342_g3

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.serbisyo_it342_g3.api.ServiceApiClient
import com.example.serbisyo_it342_g3.data.ServiceCategory
import com.example.serbisyo_it342_g3.data.Service
import com.example.serbisyo_it342_g3.data.ServiceProvider
import com.example.serbisyo_it342_g3.data.Address
import android.content.SharedPreferences
import android.util.Log

class EditServiceActivity : AppCompatActivity() {
    private lateinit var etServiceName: EditText
    private lateinit var etServiceDescription: EditText
    private lateinit var etPriceRange: EditText
    private lateinit var etDurationEstimate: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnUpdateService: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var serviceApiClient: ServiceApiClient
    private var categories = listOf<ServiceCategory>()
    private val TAG = "EditServiceActivity"

    private var serviceId: Long = 0
    private var providerId: Long = 0
    private var categoryId: Long = 0
    private var token: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_service)

        // Initialize ServiceApiClient with context
        serviceApiClient = ServiceApiClient(this)
        
        // Get SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        token = sharedPreferences.getString("token", "") ?: ""
        
        Log.d(TAG, "Retrieved token: $token")

        // Get data from intent
        serviceId = intent.getLongExtra("SERVICE_ID", 0)
        providerId = intent.getLongExtra("PROVIDER_ID", 0)
        categoryId = intent.getLongExtra("CATEGORY_ID", 0)

        if (serviceId == 0L || providerId == 0L) {
            Toast.makeText(this, "Error: Missing required data", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize views
        etServiceName = findViewById(R.id.etServiceName)
        etServiceDescription = findViewById(R.id.etServiceDescription)
        etPriceRange = findViewById(R.id.etPriceRange)
        etDurationEstimate = findViewById(R.id.etDurationEstimate)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnUpdateService = findViewById(R.id.btnUpdateService)
        btnCancel = findViewById(R.id.btnCancel)
        progressBar = findViewById(R.id.progressBar)

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Edit Service"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Load categories for spinner
        loadCategories()

        // Fill in existing data
        etServiceName.setText(intent.getStringExtra("SERVICE_NAME"))
        etServiceDescription.setText(intent.getStringExtra("SERVICE_DESCRIPTION"))
        etPriceRange.setText(intent.getStringExtra("PRICE_RANGE"))
        etDurationEstimate.setText(intent.getStringExtra("DURATION_ESTIMATE"))

        // Update service button click
        btnUpdateService.setOnClickListener {
            if (validateInputs()) {
                updateService()
            }
        }

        // Cancel button click
        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun loadCategories() {
        progressBar.visibility = View.VISIBLE
        
        serviceApiClient.getServiceCategories(token) { categories, error ->
            if (error != null) {
                runOnUiThread {
                    Toast.makeText(this, "Error loading categories: ${error.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
                return@getServiceCategories
            }
            
            if (categories != null) {
                this.categories = categories
                runOnUiThread {
                    val categoryNames = categories.map { it.categoryName }
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCategory.adapter = adapter

                    // Select the current category
                    val selectedCategoryIndex = categories.indexOfFirst { it.categoryId == categoryId }
                    if (selectedCategoryIndex >= 0) {
                        spinnerCategory.setSelection(selectedCategoryIndex)
                    }

                    progressBar.visibility = View.GONE
                }
            }
        }
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

    private fun updateService() {
        val serviceName = etServiceName.text.toString()
        val serviceDescription = etServiceDescription.text.toString()
        val priceRange = etPriceRange.text.toString()
        val durationEstimate = etDurationEstimate.text.toString()
        val categoryPosition = spinnerCategory.selectedItemPosition

        if (categoryPosition < 0 || categoryPosition >= categories.size) {
            Toast.makeText(this, "Please select a valid category", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCategoryId = categories[categoryPosition].categoryId

        val updatedService = Service(
            serviceId = serviceId,
            serviceName = serviceName,
            serviceDescription = serviceDescription,
            priceRange = priceRange,
            durationEstimate = durationEstimate
        )

        progressBar.visibility = View.VISIBLE
        btnUpdateService.isEnabled = false

        // Log token for debugging
        Log.d(TAG, "Updating service with token: ${token.take(20)}...")

        serviceApiClient.updateService(serviceId, providerId, selectedCategoryId, updatedService, token) { result, error ->
            runOnUiThread {
                if (error != null) {
                    Toast.makeText(this, "Error updating service: ${error.message}", Toast.LENGTH_SHORT).show()
                    btnUpdateService.isEnabled = true
                    progressBar.visibility = View.GONE
                    return@runOnUiThread
                }
                
                if (result != null) {
                    Toast.makeText(this, "Service updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update service", Toast.LENGTH_SHORT).show()
                    btnUpdateService.isEnabled = true
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