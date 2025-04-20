package com.example.serbisyo_it342_g3

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.serbisyo_it342_g3.api.ServiceApiClient
import com.example.serbisyo_it342_g3.data.ServiceCategory
import com.example.serbisyo_it342_g3.data.Service
import android.content.SharedPreferences
import org.json.JSONObject

class AddServiceActivity : AppCompatActivity() {
    private lateinit var etServiceName: EditText
    private lateinit var etServiceDescription: EditText
    private lateinit var etPriceRange: EditText
    private lateinit var etDurationEstimate: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnAddService: Button
    private lateinit var btnSelectImage: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var serviceApiClient: ServiceApiClient
    private var categories = listOf<ServiceCategory>()
    private var providerId: Long = 0
    private var token: String = ""
    private val TAG = "AddServiceActivity"
    
    // Constants for image picker
    private val PICK_IMAGE_REQUEST = 1
    private val STORAGE_PERMISSION_CODE = 100

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
        btnSelectImage = findViewById(R.id.btnSelectImage)
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
        
        // Select image button click
        btnSelectImage.setOnClickListener {
            openImagePicker()
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
                    Toast.makeText(this, "Error loading categories: ${exception.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                    return@runOnUiThread
                }
                
                if (categoriesList != null) {
                    categories = categoriesList
                    val categoryNames = categories.map { it.categoryName }
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCategory.adapter = adapter
                } else {
                    Log.e(TAG, "Categories list is null")
                    Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show()
                }
                
                progressBar.visibility = View.GONE
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
    
    private fun addServiceWithImage(base64Image: String? = null) {
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

        // Log for debugging
        Log.d(TAG, "Adding service with image. Token length: ${token.length}, Image provided: ${base64Image != null}")
        if (base64Image != null) {
            Log.d(TAG, "Image base64 length: ${base64Image.length}")
        }

        val newService = Service(
            serviceName = serviceName,
            serviceDescription = serviceDescription,
            priceRange = priceRange,
            durationEstimate = durationEstimate
        )

        serviceApiClient.createServiceWithImage(providerId, selectedCategory.categoryId, newService, base64Image, token) { createdService, exception ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                btnAddService.isEnabled = true
                
                if (exception != null) {
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
                    
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Add service with image error: ${exception.message}")
                    return@runOnUiThread
                }
                
                if (createdService != null) {
                    Toast.makeText(this, "Service added successfully with image", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to add service with image. Please try again.", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Add service with image returned null result without error")
                }
            }
        }
    }

    private fun openImagePicker() {
        if (checkStoragePermission()) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            Log.d(TAG, "Opening image picker")
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        } else {
            Log.e(TAG, "Storage permission not granted")
            ActivityCompat.requestPermissions(
                this,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
                } else {
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                },
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            // For Android 12 and below
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            try {
                Log.d(TAG, "Image selected: $imageUri")
                val base64Image = convertImageToBase64(imageUri)
                if (base64Image != null) {
                    addServiceWithImage(base64Image)
                } else {
                    Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
                    addService() // Fall back to adding without image
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image: ${e.message}", e)
                Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
                addService() // Fall back to adding without image
            }
        }
    }

    private fun convertImageToBase64(imageUri: android.net.Uri?): String? {
        if (imageUri == null) return null
        
        try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            
            if (bytes != null) {
                val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                Log.d(TAG, "Converted image to base64 string (length: ${base64.length})")
                return base64
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting image to base64: ${e.message}", e)
        }
        
        return null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openImagePicker()
            } else {
                Toast.makeText(this, "Permission denied. Cannot select image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}