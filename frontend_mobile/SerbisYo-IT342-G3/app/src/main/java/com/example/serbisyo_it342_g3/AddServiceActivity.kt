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
import android.net.Uri
import com.example.serbisyo_it342_g3.utils.ImageUtils
import android.app.AlertDialog

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
    private lateinit var ivServiceImage: ImageView
    private lateinit var tvImageStatus: TextView
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var serviceApiClient: ServiceApiClient
    private var categories = listOf<ServiceCategory>()
    private var providerId: Long = 0
    private var token: String = ""
    private val TAG = "AddServiceActivity"
    private var selectedImageBase64: String? = null
    private var selectedImageUri: Uri? = null

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
        ivServiceImage = findViewById(R.id.ivServiceImage)
        tvImageStatus = findViewById(R.id.tvImageStatus)

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Add New Service"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Load categories for spinner
        loadCategories()

        // Add service button click - add with or without image
        btnAddService.setOnClickListener {
            if (validateInputs()) {
                if (selectedImageBase64 != null) {
                    // If image is selected, use it
                    addServiceWithImage(selectedImageBase64)
                } else {
                    // Otherwise add without image
                    addService()
                }
            }
        }

        // Select image button click - just select the image, don't submit yet
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

        // Create a progress dialog to show user what's happening
        val progressDialog = AlertDialog.Builder(this)
            .setTitle("Adding Service")
            .setMessage("Please wait while we upload your service with image...")
            .setCancelable(false)
            .create()
        progressDialog.show()

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
            price = priceRange,
            durationEstimate = durationEstimate
        )

        serviceApiClient.createServiceWithImage(providerId, selectedCategory.categoryId, newService, base64Image, token) { createdService, exception ->
            runOnUiThread {
                progressDialog.dismiss()
                progressBar.visibility = View.GONE
                btnAddService.isEnabled = true

                if (exception != null) {
                    Log.e(TAG, "Error adding service with image", exception)
                    
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

                    // If endpoint not found, try adding without image as fallback
                    if (exception.message?.contains("404") == true || exception.message?.contains("Not Found") == true) {
                        AlertDialog.Builder(this)
                            .setTitle("Image Upload Failed")
                            .setMessage("We couldn't upload the image with the service. Would you like to add the service without an image?")
                            .setPositiveButton("Yes") { _, _ -> addService() }
                            .setNegativeButton("No", null)
                            .show()
                    } else {
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                        Log.e(TAG, "Add service with image error: ${exception.message}")
                    }
                    return@runOnUiThread
                }

                if (createdService != null) {
                    Toast.makeText(this, "Service added successfully with image", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("Image Upload Issue")
                        .setMessage("We had trouble adding the service with the image. Would you like to try adding the service without an image?")
                        .setPositiveButton("Yes") { _, _ -> addService() }
                        .setNegativeButton("No", null)
                        .show()
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
                
                // Store the selected image URI
                selectedImageUri = imageUri
                
                // Show processing status
                tvImageStatus.visibility = View.VISIBLE
                tvImageStatus.text = "Processing image..."
                
                // Show the selected image directly
                ivServiceImage.setImageURI(imageUri)
                
                // Process image in background to avoid UI blocking
                Thread {
                    try {
                        // Convert to bitmap and resize for upload
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                        val resizedBitmap = ImageUtils.resizeBitmap(bitmap, 800, 800)
                        
                        // Convert to Base64 for upload using our utility class
                        selectedImageBase64 = ImageUtils.bitmapToBase64(resizedBitmap)
                        
                        runOnUiThread {
                            if (selectedImageBase64 == null) {
                                tvImageStatus.text = "Image processing failed"
                                Toast.makeText(this, "Failed to process image. You can still add the service without an image.", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.d(TAG, "Successfully converted image to base64, length: ${selectedImageBase64!!.length}")
                                tvImageStatus.text = "Image ready for upload"
                                Toast.makeText(this, "Image selected successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing image: ${e.message}", e)
                        runOnUiThread {
                            tvImageStatus.text = "Image processing failed"
                            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
                            selectedImageBase64 = null
                            selectedImageUri = null
                        }
                    }
                }.start()
            } catch (e: Exception) {
                Log.e(TAG, "Error processing image: ${e.message}", e)
                tvImageStatus.text = "Image processing failed"
                Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
                selectedImageBase64 = null
                selectedImageUri = null
            }
        } else if (requestCode == PICK_IMAGE_REQUEST) {
            // User cancelled image selection
            Log.d(TAG, "Image selection cancelled or failed")
        }
    }

    private fun convertImageToBase64(imageUri: android.net.Uri?): String? {
        if (imageUri == null) return null

        try {
            // Use our utility class that properly resizes and encodes images
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            val resizedBitmap = ImageUtils.resizeBitmap(bitmap, 800, 800)
            return ImageUtils.bitmapToBase64(resizedBitmap)
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