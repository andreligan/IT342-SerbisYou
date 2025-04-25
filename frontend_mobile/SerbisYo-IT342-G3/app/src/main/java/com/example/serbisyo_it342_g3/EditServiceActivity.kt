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
import android.content.Intent
import android.provider.MediaStore
import android.Manifest
import android.os.Build
import androidx.core.app.ActivityCompat
import org.json.JSONObject

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

    // Constants for permissions and image picking
    private val PICK_IMAGE_REQUEST = 1
    private val STORAGE_PERMISSION_CODE = 2

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

        // Add image picker button functionality (make sure this button exists in your layout)
        findViewById<Button>(R.id.btnAddImage)?.setOnClickListener {
            openImagePicker()
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
            price = priceRange,
            durationEstimate = durationEstimate
        )

        progressBar.visibility = View.VISIBLE
        btnUpdateService.isEnabled = false

        // Log token for debugging
        Log.d(TAG, "Updating service with token: ${token.take(20)}...")

        serviceApiClient.updateService(serviceId, providerId, selectedCategoryId, updatedService, token) { result, error ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                btnUpdateService.isEnabled = true

                if (error != null) {
                    when {
                        error.message?.contains("403") == true -> {
                            var errorMessage = "Authorization error: You don't have permission to update this service"
                            try {
                                // Try to parse the error message from the JSON response
                                val errorBody = error.message?.substringAfter("{")?.let { "{$it" }
                                val jsonObject = JSONObject(errorBody ?: "{}")
                                if (jsonObject.has("message")) {
                                    errorMessage = jsonObject.getString("message")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing error response: ${e.message}")
                            }
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                            Log.e(TAG, "403 Forbidden error: ${error.message}")
                        }
                        error.message?.contains("401") == true -> {
                            var errorMessage = "Authentication error: Please log in again"
                            try {
                                val errorBody = error.message?.substringAfter("{")?.let { "{$it" }
                                val jsonObject = JSONObject(errorBody ?: "{}")
                                if (jsonObject.has("message")) {
                                    errorMessage = jsonObject.getString("message")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing error response: ${e.message}")
                            }
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                            Log.e(TAG, "401 Unauthorized error: ${error.message}")
                            // Consider redirecting to login screen here
                        }
                        error.message?.contains("413") == true -> {
                            Toast.makeText(this, "Error: The image is too large to upload", Toast.LENGTH_LONG).show()
                            Log.e(TAG, "413 Payload Too Large: ${error.message}")
                        }
                        else -> {
                            var errorMessage = "Error updating service: ${error.message}"
                            try {
                                val errorBody = error.message?.substringAfter("{")?.let { "{$it" }
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
                            Log.e(TAG, "Update service error: ${error.message}")
                        }
                    }
                    return@runOnUiThread
                }

                if (result != null) {
                    Toast.makeText(this, "Service updated successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update service. Please try again.", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Update service returned null result without error")
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

    private fun updateServiceWithImage(base64Image: String? = null) {
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
            price = priceRange,
            durationEstimate = durationEstimate
        )

        progressBar.visibility = View.VISIBLE
        btnUpdateService.isEnabled = false

        // Log for debugging
        Log.d(TAG, "Updating service with image. Token length: ${token.length}, Image provided: ${base64Image != null}")
        if (base64Image != null) {
            Log.d(TAG, "Image base64 length: ${base64Image.length}")
        }

        serviceApiClient.updateServiceWithImage(serviceId, providerId, selectedCategoryId, updatedService, base64Image, token) { result, error ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                btnUpdateService.isEnabled = true

                if (error != null) {
                    when {
                        error.message?.contains("403") == true -> {
                            var errorMessage = "Authorization error: You don't have permission to update this service"
                            try {
                                // Try to parse the error message from the JSON response
                                val errorBody = error.message?.substringAfter("{")?.let { "{$it" }
                                val jsonObject = JSONObject(errorBody ?: "{}")
                                if (jsonObject.has("message")) {
                                    errorMessage = jsonObject.getString("message")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing error response: ${e.message}")
                            }
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                            Log.e(TAG, "403 Forbidden error: ${error.message}")
                        }
                        error.message?.contains("401") == true -> {
                            var errorMessage = "Authentication error: Please log in again"
                            try {
                                val errorBody = error.message?.substringAfter("{")?.let { "{$it" }
                                val jsonObject = JSONObject(errorBody ?: "{}")
                                if (jsonObject.has("message")) {
                                    errorMessage = jsonObject.getString("message")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing error response: ${e.message}")
                            }
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                            Log.e(TAG, "401 Unauthorized error: ${error.message}")
                            // Consider redirecting to login screen here
                        }
                        error.message?.contains("413") == true -> {
                            Toast.makeText(this, "Error: The image is too large to upload", Toast.LENGTH_LONG).show()
                            Log.e(TAG, "413 Payload Too Large: ${error.message}")
                        }
                        else -> {
                            var errorMessage = "Error updating service: ${error.message}"
                            try {
                                val errorBody = error.message?.substringAfter("{")?.let { "{$it" }
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
                            Log.e(TAG, "Update service with image error: ${error.message}")
                        }
                    }
                    return@runOnUiThread
                }

                if (result != null) {
                    Toast.makeText(this, "Service updated successfully with image", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update service with image. Please try again.", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Update service with image returned null result without error")
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri = data.data
            try {
                Log.d(TAG, "Image selected: $imageUri")
                val base64Image = convertImageToBase64(imageUri)
                if (base64Image != null) {
                    updateServiceWithImage(base64Image)
                } else {
                    Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
                    updateService() // Fall back to updating without image
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image: ${e.message}", e)
                Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
                updateService() // Fall back to updating without image
            }
        }
    }

    private fun convertImageToBase64(imageUri: android.net.Uri?): String? {
        if (imageUri == null) return null

        try {
            // Use same image processing approach as other parts of the app
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            
            // Resize the image to reduce file size using our utility class
            val resizedBitmap = com.example.serbisyo_it342_g3.utils.ImageUtils.resizeBitmap(bitmap, 800, 800)
            
            // Convert to Base64 using our utility class
            return com.example.serbisyo_it342_g3.utils.ImageUtils.bitmapToBase64(resizedBitmap)
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
}