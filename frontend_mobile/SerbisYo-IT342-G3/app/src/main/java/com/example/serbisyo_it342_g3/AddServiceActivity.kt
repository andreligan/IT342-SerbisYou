package com.example.serbisyo_it342_g3

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
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
    private lateinit var ivServiceImage: ImageView
    private lateinit var btnSelectImage: Button

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
        ivServiceImage = findViewById(R.id.ivServiceImage)
        btnSelectImage = findViewById(R.id.btnSelectImage)

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
        
        // Select image button click
        btnSelectImage.setOnClickListener {
            checkStoragePermission()
        }
    }

    private fun checkStoragePermission() {
        // For Android 13+ (API 33+), we need READ_MEDIA_IMAGES
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED) {
                
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    )) {
                    Toast.makeText(this, 
                        "We need permission to access your gallery for selecting a service image", 
                        Toast.LENGTH_LONG).show()
                }
                
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                return
            }
            // Permission already granted
            openImagePicker()
        } 
        // For Android 10-12, use READ_EXTERNAL_STORAGE
        else {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )) {
                    Toast.makeText(this, 
                        "We need permission to access your gallery for selecting a service image", 
                        Toast.LENGTH_LONG).show()
                }
                
                // Request the permission
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                return
            }
            // Permission already granted
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            // Add these flags to help with Xiaomi MIUI restrictions
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            pickImageLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening image picker", e)
            Toast.makeText(this, "Error opening gallery: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertImageToBase64(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Resize the bitmap to reduce file size
            val maxDimension = 800 // Set a reasonable max dimension
            val originalWidth = bitmap.width
            val originalHeight = bitmap.height
            var newWidth = originalWidth
            var newHeight = originalHeight
            
            if (originalWidth > maxDimension || originalHeight > maxDimension) {
                if (originalWidth > originalHeight) {
                    newWidth = maxDimension
                    newHeight = (originalHeight * maxDimension) / originalWidth
                } else {
                    newHeight = maxDimension
                    newWidth = (originalWidth * maxDimension) / originalHeight
                }
                
                Log.d(TAG, "Resizing image from ${originalWidth}x${originalHeight} to ${newWidth}x${newHeight}")
            }
            
            val resizedBitmap = if (newWidth != originalWidth || newHeight != originalHeight) {
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }

            val byteArrayOutputStream = ByteArrayOutputStream()
            // Compress the image much more aggressively (reduce quality)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            
            Log.d(TAG, "Image byte array size before encoding: ${byteArray.size} bytes")
            
            imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
            
            Log.d(TAG, "Base64 string length: ${imageBase64.length} characters")
            
            // Log the first and last 20 characters of the Base64 string to verify it looks correct
            if (imageBase64.length > 40) {
                Log.d(TAG, "Base64 string starts with: ${imageBase64.substring(0, 20)}...")
                Log.d(TAG, "Base64 string ends with: ...${imageBase64.substring(imageBase64.length - 20)}")
            }
            
            if (resizedBitmap != bitmap) {
                resizedBitmap.recycle()
            }
            bitmap.recycle()
        } catch (e: Exception) {
            Log.e(TAG, "Error converting image to Base64", e)
            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
            imageBase64 = "" // Clear the image data on error
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

        // Create service with image URL (if available)
        val service = Service(
            serviceName = serviceName,
            serviceDescription = serviceDescription,
            priceRange = priceRange,
            durationEstimate = durationEstimate,
            imageUrl = imageBase64 // Set the Base64 encoded image
        )

        // Log token for debugging
        Log.d(TAG, "Adding service with token: ${token.take(20)}...")
        // Log image info
        if (imageBase64.isNotEmpty()) {
            Log.d(TAG, "Image size: ${imageBase64.length} characters")
        } else {
            Log.d(TAG, "No image selected")
        }

        // Use the updated method that takes providerId, categoryId, service and the image
        serviceApiClient.createServiceWithImage(
            providerId,
            selectedCategory.categoryId,
            service,
            token
        ) { createdService, exception ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                btnAddService.isEnabled = true
                
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
                }
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

    private fun showAuthenticationErrorDialog() {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Authentication Error")
            .setMessage("Your session has expired or is invalid. Please log in again.")
            .setPositiveButton("Log in") { _, _ ->
                // Clear token from SharedPreferences
                val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                sharedPreferences.edit().remove("token").apply()
                
                // Navigate to login screen
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .create()
        
        alertDialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}