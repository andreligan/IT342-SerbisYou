package com.example.serbisyo_it342_g3.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.api.UserApiClient
import com.example.serbisyo_it342_g3.data.ServiceProvider
import com.example.serbisyo_it342_g3.data.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import android.os.Handler
import android.os.Looper
import com.example.serbisyo_it342_g3.api.BaseApiClient
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import androidx.annotation.RequiresApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody


class ServiceProviderProfileFragment : Fragment() {
    private val tag = "SPProfileFragment"

    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etPhoneNumber: TextInputEditText
    private lateinit var etExperience: TextInputEditText
    private lateinit var btnUpdateProfile: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvErrorMessage: TextView
    private lateinit var tvSuccessMessage: TextView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserType: TextView
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var fabCamera: FloatingActionButton

    private lateinit var userApiClient: UserApiClient
    private var token: String = ""
    private var userId: Long = 0
    private var providerId: Long = 0
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_service_provider_profile, container, false)

        // Get arguments
        arguments?.let {
            userId = it.getLong("userId", 0)
            token = it.getString("token", "") ?: ""
            providerId = it.getLong("providerId", 0)
        }

        // Initialize the API client
        userApiClient = UserApiClient(requireContext())

        // Initialize views
        etUsername = view.findViewById(R.id.etUsername)
        etEmail = view.findViewById(R.id.etEmail)
        etFirstName = view.findViewById(R.id.etFirstName)
        etLastName = view.findViewById(R.id.etLastName)
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber)
        etExperience = view.findViewById(R.id.etExperience)
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile)
        progressBar = view.findViewById(R.id.progressBar)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)
        tvSuccessMessage = view.findViewById(R.id.tvSuccessMessage)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserType = view.findViewById(R.id.tvUserType)

        // Initialize profile image views
        profileImageView = view.findViewById(R.id.profileImage)
        fabCamera = view.findViewById(R.id.fabCamera)

        // Set click listener for camera button
        fabCamera.setOnClickListener {
            openImagePicker()
        }

        // Load provider profile
        loadProviderProfile()

        // Set update button click listener
        btnUpdateProfile.setOnClickListener {
            if (validateInputs()) {
                updateProfile()
            }
        }

        return view
    }

    @Suppress("DEPRECATION")
    private fun openImagePicker() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        } catch (e: Exception) {
            Log.e(tag, "Error opening image picker", e)
            Toast.makeText(context, "Failed to open image selector: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProviderProfile() {
        progressBar.visibility = View.VISIBLE
        tvErrorMessage.visibility = View.GONE
        tvSuccessMessage.visibility = View.GONE
        
        // Set default image first
        profileImageView.setImageResource(R.drawable.default_profile)
        
        // Try to load profile image from shared preferences if available
        loadProfileImageFromPrefs()
        
        userApiClient.getServiceProviderProfile(userId, token) { provider, error -> 
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(tag, "Error loading profile", error)
                    tvErrorMessage.text = "Profile not found"
                    tvErrorMessage.visibility = View.VISIBLE
                    return@runOnUiThread
                }
                
                if (provider != null) {
                    Log.d(tag, "Provider data received: $provider")
                    
                    // Save providerId if it was not set before
                    val providerIdValue = provider.providerId ?: 0
                    if (providerId == 0L && providerIdValue > 0) {
                        providerId = providerIdValue
                        val prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        prefs.edit().putLong("providerId", providerId).apply()
                    }
                    
                    // Fill form with provider data
                    provider.userAuth?.let { user ->
                        Log.d(tag, "UserAuth data: $user")
                        
                        // Account Information
                        etUsername.setText(user.userName)
                        etEmail.setText(user.email)
                    }
                    
                    // Personal Information
                    etFirstName.setText(provider.firstName ?: "")
                    etLastName.setText(provider.lastName ?: "")
                    etPhoneNumber.setText(provider.phoneNumber ?: "")
                    etExperience.setText(provider.yearsOfExperience?.toString() ?: "0")
                    
                    // Update display name at the top
                    val fullName = "${provider.firstName ?: ""} ${provider.lastName ?: ""}"
                    tvUserName.text = fullName.trim().ifEmpty { "Service Provider" }
                    
                    // Set business type below user name
                    tvUserType.text = provider.businessName ?: "Service Provider"
                    
                    // Safe call for profile image check
                    val profileImage = provider.profileImage
                    if (profileImage != null && profileImage.isNotEmpty() && selectedImageUri == null) {
                        Log.d(tag, "Server provided profile image URL: $profileImage")
                        
                        try {
                            // Simple approach - just set the default image and log the server URL
                            // We don't try to load the remote image to avoid network issues
                            Log.d(tag, "Server image available at: $profileImage")
                            
                            // Keep using the default image or one from shared preferences
                            // This avoids network loading issues that could crash the app
                        } catch (e: Exception) {
                            Log.e(tag, "Error with profile image info", e)
                        }
                    }
                } else {
                    tvErrorMessage.text = "Profile not found"
                    tvErrorMessage.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private fun loadProfileImageFromPrefs() {
        try {
            if (providerId == 0L) {
                Log.e(tag, "Provider ID is 0 when loading profile image")
                return
            }
            
            val prefs = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
            val imagePath = prefs.getString("profile_image_$providerId", null)
            
            if (imagePath.isNullOrEmpty()) {
                Log.d(tag, "No profile image path found in preferences")
                return
            }
            
            Log.d(tag, "Loading profile image from: $imagePath")
            
            try {
                // Check if it's a file path
                val imageFile = File(imagePath)
                if (imageFile.exists()) {
                    // Load from file path using direct approach
                    profileImageView.setImageURI(Uri.fromFile(imageFile))
                    return
                }
                
                // Fallback to try as URI
                val imageUri = Uri.parse(imagePath)
                profileImageView.setImageURI(imageUri)
            } catch (e: Exception) {
                Log.e(tag, "Error loading profile image from path: $imagePath", e)
                // Set default image
                profileImageView.setImageResource(R.drawable.default_profile)
                
                // Clear the invalid path from preferences
                prefs.edit().remove("profile_image_$providerId").apply()
            }
        } catch (e: Exception) {
            Log.e(tag, "Error in loadProfileImageFromPrefs", e)
            // Set default image in case of any error
            profileImageView.setImageResource(R.drawable.default_profile)
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (etUsername.text.toString().trim().isEmpty()) {
            etUsername.error = "Username cannot be empty"
            isValid = false
        }

        if (etEmail.text.toString().trim().isEmpty()) {
            etEmail.error = "Email cannot be empty"
            isValid = false
        }

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
        tvErrorMessage.visibility = View.GONE 
        tvSuccessMessage.visibility = View.GONE

        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val yearsOfExperience = etExperience.text.toString().trim().toIntOrNull() ?: 0

        // Create a User object for the userAuth field
        val userAuth = User(
            userId = userId,
            userName = username,
            email = email
        )

        userApiClient.getServiceProviderProfile(userId, token) { provider, error ->
            if (error != null || provider == null) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    tvErrorMessage.text = "Failed to get current provider details"
                    tvErrorMessage.visibility = View.VISIBLE
                }
                return@getServiceProviderProfile
            }
            
            // Important: Use the provider ID from the server response, not the userId
            val actualProviderId = provider.providerId ?: 1L
            Log.d(tag, "Using provider ID for update: $actualProviderId (user ID: $userId)")
            
            val updatedProvider = ServiceProvider(
                providerId = actualProviderId,  // Use actual provider ID from server, not userId
                firstName = firstName,
                lastName = lastName,
                phoneNumber = phoneNumber,
                businessName = provider.businessName ?: "",
                yearsOfExperience = yearsOfExperience,
                availabilitySchedule = provider.availabilitySchedule ?: "",
                address = provider.address,  // This will now work even if address is null
                userAuth = userAuth,
                paymentMethod = provider.paymentMethod
            )

            // Separate variable for image URI to avoid ServiceProvider constructor issues
            val imageUri = if (selectedImageUri != null) selectedImageUri.toString() else null

            userApiClient.updateServiceProviderProfile(updatedProvider, token, imageUri) { success, updateError ->
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE

                    if (updateError != null) {
                        tvErrorMessage.text = "Failed to update profile: ${updateError.message}"
                        tvErrorMessage.visibility = View.VISIBLE
                        return@runOnUiThread
                    }

                    if (success) {
                        // Update display name at the top
                        tvUserName.text = "$firstName $lastName"
                        
                        // Show toast message for successful update
                        Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                        
                        // If image was selected, upload it
                        if (selectedImageUri != null) {
                            uploadProfileImage(selectedImageUri!!)
                        } else {
                            // Show success message
                            tvSuccessMessage.visibility = View.VISIBLE
                            // Hide success message after 3 seconds
                            Handler(Looper.getMainLooper()).postDelayed({
                                tvSuccessMessage.visibility = View.GONE
                            }, 3000)
                        }
                    } else {
                        tvErrorMessage.text = "Profile update failed"
                        tvErrorMessage.visibility = View.VISIBLE
                        // Show toast for failed update
                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun uploadProfileImage(imageUri: Uri) {
        try {
            if (providerId <= 0) {
                Log.e(tag, "Invalid provider ID for image upload")
                Toast.makeText(requireContext(), "Invalid provider ID", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                btnUpdateProfile.isEnabled = true
                return
            }

            Log.d(tag, "Starting image upload for provider $providerId")
            
            // Save a copy of the image to the app's private storage
            val imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
            }
            
            // Create app-specific directory if it doesn't exist
            val appDir = File(requireContext().filesDir, "profile_images")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            
            // Save a file with the provider ID
            val imageFile = File(appDir, "profile_${providerId}.jpg")
            FileOutputStream(imageFile).use { out ->
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            // Save the file path in shared preferences
            val prefs = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("profile_image_$providerId", imageFile.absolutePath).apply()
            Log.d(tag, "Saved local image copy at: ${imageFile.absolutePath}")
            
            // Create a temporary file for the API upload - use the imageFile directly
            
            // Upload the image with the correct parameter types:
            // uploadServiceProviderImage(providerId: Long, imageFile: File, token: String, callback: (Boolean, Exception?) -> Unit)
            userApiClient.uploadServiceProviderImage(
                providerId,
                imageFile,
                token
            ) { success, error ->
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnUpdateProfile.isEnabled = true
                    
                    if (success) {
                        // Show toast message for successful image upload
                        Toast.makeText(requireContext(), "Profile image uploaded successfully!", Toast.LENGTH_SHORT).show()
                        
                        // Show success message
                        tvSuccessMessage.visibility = View.VISIBLE
                        // Hide success message after 3 seconds
                        Handler(Looper.getMainLooper()).postDelayed({
                            tvSuccessMessage.visibility = View.GONE
                        }, 3000)
                    } else {
                        // Handle specific error cases
                        val errorMessage = when {
                            error?.message?.contains("too large") == true -> 
                                "Image is too large"
                            error?.message?.contains("Authentication") == true -> 
                                "Authentication error"
                            error?.message?.contains("permission") == true ->
                                "Permission denied"
                            else -> 
                                "Failed to upload image: ${error?.message}"
                        }
                        
                        // Show toast for failed image upload
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                        
                        tvErrorMessage.text = errorMessage
                        tvErrorMessage.visibility = View.VISIBLE
                        Log.e(tag, "Error uploading profile image to server", error)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error processing or uploading profile image", e)
            Toast.makeText(requireContext(), "Error uploading profile image: ${e.message}", Toast.LENGTH_LONG).show()
            tvErrorMessage.text = "Error uploading profile image: ${e.message}"
            tvErrorMessage.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            btnUpdateProfile.isEnabled = true
        }
    }

    private fun getBaseUrl(): String {
        return BaseApiClient.BASE_URL
    }
    
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            try {
                selectedImageUri = data.data
                profileImageView.setImageURI(selectedImageUri)
                
                // Hide the "No Image" text if it exists
                view?.findViewById<TextView>(R.id.tvNoImage)?.visibility = View.GONE
                
                Log.d(tag, "Image selected: $selectedImageUri")
            } catch (e: Exception) {
                Log.e(tag, "Error handling selected image", e)
                Toast.makeText(context, "Failed to load selected image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
        
        @JvmStatic
        fun newInstance(userId: Long, token: String, providerId: Long) =
            ServiceProviderProfileFragment().apply {
                arguments = Bundle().apply {
                    putLong("userId", userId)
                    putString("token", token)
                    putLong("providerId", providerId)
                }
            }
    }
}