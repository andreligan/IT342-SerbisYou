package com.example.serbisyo_it342_g3

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.serbisyo_it342_g3.api.ServiceApiClient
import com.example.serbisyo_it342_g3.data.ServiceCategory
import com.example.serbisyo_it342_g3.data.Service
import com.example.serbisyo_it342_g3.data.ServiceProvider
import com.example.serbisyo_it342_g3.data.Address
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.InputStream
import android.app.AlertDialog
import android.content.Context

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
    private lateinit var ivServiceImage: ImageView
    private lateinit var btnSelectImage: Button

    private lateinit var serviceApiClient: ServiceApiClient
    private var categories = listOf<ServiceCategory>()
    private val TAG = "EditServiceActivity"

    private var serviceId: Long = 0
    private var providerId: Long = 0
    private var categoryId: Long = 0
    private var token: String = ""
    private var currentImageUrl: String = ""
    
    private var selectedImageUri: Uri? = null
    private var imageBase64: String = ""

    // Register for activity result for image picking
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                ivServiceImage.setImageURI(uri)
                convertImageToBase64(uri)
            }
        }
    }

    // Permission request launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Permission denied to read external storage", Toast.LENGTH_SHORT).show()
        }
    }

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
        currentImageUrl = intent.getStringExtra("IMAGE_URL") ?: ""

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
        ivServiceImage = findViewById(R.id.ivServiceImage)
        btnSelectImage = findViewById(R.id.btnSelectImage)

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
        
        // If there's an existing image, try to load it
        if (currentImageUrl.isNotEmpty()) {
            try {
                // If the image is a Base64 string, decode it
                if (currentImageUrl.startsWith("data:image") || 
                    currentImageUrl.startsWith("/9j/") || 
                    currentImageUrl.length > 100) {
                    
                    val imageBytes = Base64.decode(currentImageUrl, Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ivServiceImage.setImageBitmap(decodedImage)
                    
                    // Keep the existing image if no new one is selected
                    imageBase64 = currentImageUrl
                }
                // If it's a URL, you would need to load it with an image library like Glide or Picasso
                // For now, just keep the placeholder
            } catch (e: Exception) {
                Log.e(TAG, "Error loading existing image", e)
            }
        }

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
            durationEstimate = durationEstimate,
            imageUrl = imageBase64.ifEmpty { currentImageUrl } // Use new image if selected, otherwise keep existing
        )

        progressBar.visibility = View.VISIBLE
        btnUpdateService.isEnabled = false

        // Log token for debugging
        Log.d(TAG, "Updating service with token: ${token.take(20)}...")
        // Log image info
        if (imageBase64.isNotEmpty()) {
            Log.d(TAG, "Image size: ${imageBase64.length} characters")
        } else {
            Log.d(TAG, "Using existing image URL")
        }

        serviceApiClient.updateServiceWithImage(
            serviceId, 
            providerId, 
            selectedCategoryId, 
            updatedService, 
            token
        ) { result, error ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                btnUpdateService.isEnabled = true
                
                if (error != null) {
                    Log.e(TAG, "Error updating service", error)
                    
                    // Handle different error cases
                    when {
                        error.message?.contains("Authentication failed") == true || 
                        error.message?.contains("token") == true -> {
                            showAuthenticationErrorDialog()
                        }
                        error.message?.contains("too large") == true -> {
                            Toast.makeText(this, "Image is too large. Please select a smaller image.", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                    return@runOnUiThread
                }
                
                if (result != null) {
                    Toast.makeText(this, "Service updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to update service", Toast.LENGTH_SHORT).show()
                }
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