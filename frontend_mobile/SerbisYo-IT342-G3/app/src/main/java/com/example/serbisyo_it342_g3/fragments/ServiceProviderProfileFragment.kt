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
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
    
    // Activity result launcher for image picker
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            selectedImageUri = result.data?.data
            try {
                profileImageView.setImageURI(selectedImageUri)
                
                // Save the selected image URI to shared preferences
                selectedImageUri?.let {
                    val prefs = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("profile_image_$providerId", it.toString()).apply()
                }
                
                // Hide the "No Image" text if it exists
                view?.findViewById<TextView>(R.id.tvNoImage)?.visibility = View.GONE
            } catch (e: Exception) {
                Log.e(tag, "Error setting selected image", e)
                // Set default image if there's an error
                profileImageView.setImageResource(R.drawable.default_profile)
            }
        }
    }

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

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
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
                    tvErrorMessage.text = getString(R.string.profile_not_found)
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
                    tvErrorMessage.text = getString(R.string.profile_not_found)
                    tvErrorMessage.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private fun loadProfileImageFromPrefs() {
        try {
            val prefs = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
            val savedImagePath = prefs.getString("profile_image_$providerId", null)
            
            if (savedImagePath != null) {
                try {
                    // Check if this is a file path or content URI
                    if (savedImagePath.startsWith("/")) {
                        // It's a file path, use File directly
                        val file = File(savedImagePath)
                        if (file.exists()) {
                            profileImageView.setImageURI(Uri.fromFile(file))
                            selectedImageUri = Uri.fromFile(file)
                            
                            // Hide the "No Image" text if it exists
                            view?.findViewById<TextView>(R.id.tvNoImage)?.visibility = View.GONE
                            return
                        }
                    }
                    
                    // If not a file or file doesn't exist, try as content URI
                    val uri = Uri.parse(savedImagePath)
                    
                    // Instead of trying to access the original URI (which may cause permission errors),
                    // just store it for when we need to update the profile
                    selectedImageUri = uri
                    profileImageView.setImageURI(uri)
                    
                    // Hide the "No Image" text if it exists
                    view?.findViewById<TextView>(R.id.tvNoImage)?.visibility = View.GONE
                } catch (e: Exception) {
                    Log.e(tag, "Error loading saved image", e)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error in loadProfileImageFromPrefs", e)
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (etUsername.text.toString().trim().isEmpty()) {
            etUsername.error = getString(R.string.error_username_empty)
            isValid = false
        }

        if (etEmail.text.toString().trim().isEmpty()) {
            etEmail.error = getString(R.string.error_email_empty)
            isValid = false
        }

        if (etFirstName.text.toString().trim().isEmpty()) {
            etFirstName.error = getString(R.string.error_firstname_empty)
            isValid = false
        }

        if (etLastName.text.toString().trim().isEmpty()) {
            etLastName.error = getString(R.string.error_lastname_empty)
            isValid = false
        }

        if (etPhoneNumber.text.toString().trim().isEmpty()) {
            etPhoneNumber.error = getString(R.string.error_phone_empty)
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
                        tvErrorMessage.text = getString(R.string.error_profile_update, updateError.message)
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
                        tvErrorMessage.text = getString(R.string.profile_update_failed)
                        tvErrorMessage.visibility = View.VISIBLE
                        // Show toast for failed update
                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun uploadProfileImage(imageUri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            
            if (inputStream == null) {
                tvErrorMessage.text = getString(R.string.error_reading_image)
                tvErrorMessage.visibility = View.VISIBLE
                return
            }
            
            // Get the current provider ID one more time to ensure we have the right one
            userApiClient.getServiceProviderProfile(userId, token) { provider, error ->
                if (error != null || provider == null) {
                    requireActivity().runOnUiThread {
                        tvErrorMessage.text = "Failed to get provider ID for image upload"
                        tvErrorMessage.visibility = View.VISIBLE
                    }
                    return@getServiceProviderProfile
                }
                
                // Use the provider ID from the API, not the user ID
                val actualProviderId = provider.providerId ?: 1L
                Log.d(tag, "Using provider ID for image upload: $actualProviderId")
                
                // Create a local file to store the image
                val localImageFile = File(requireContext().cacheDir, "temp_profile_image_${System.currentTimeMillis()}.jpg")
                val outputStream = FileOutputStream(localImageFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
                
                // Upload the image
                userApiClient.uploadServiceProviderImage(actualProviderId, localImageFile, token) { success, error ->
                    requireActivity().runOnUiThread {
                        if (success) {
                            // Show toast message for successful image upload
                            Toast.makeText(requireContext(), "Profile image uploaded successfully!", Toast.LENGTH_SHORT).show()
                            
                            // Show success message
                            tvSuccessMessage.visibility = View.VISIBLE
                            // Hide success message after 3 seconds
                            Handler(Looper.getMainLooper()).postDelayed({
                                tvSuccessMessage.visibility = View.GONE
                            }, 3000)
                            
                            // Save the image file path to shared preferences
                            val prefs = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                            prefs.edit().putString("profile_image_${actualProviderId}", localImageFile.absolutePath).apply()
                            
                        } else {
                            // Handle specific error cases
                            val errorMessage = when {
                                error?.message?.contains("too large") == true -> 
                                    getString(R.string.error_image_too_large)
                                error?.message?.contains("Authentication") == true -> 
                                    getString(R.string.error_auth)
                                error?.message?.contains("permission") == true ->
                                    getString(R.string.error_permission)
                                else -> 
                                    getString(R.string.error_image_upload, error?.message)
                            }
                            
                            // Show toast for failed image upload
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                            
                            tvErrorMessage.text = errorMessage
                            tvErrorMessage.visibility = View.VISIBLE
                            Log.e(tag, "Error uploading profile image", error)
                            
                            // Still save the image file path to shared preferences
                            // This lets users see their selected image even if server upload failed
                            val prefs = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                            prefs.edit().putString("profile_image_${actualProviderId}", localImageFile.absolutePath).apply()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            tvErrorMessage.text = getString(R.string.error_processing_image, e.message)
            tvErrorMessage.visibility = View.VISIBLE
            Log.e(tag, "Error processing image", e)
        }
    }

    private fun getBaseUrl(): String {
        return BaseApiClient.BASE_URL
    }

    companion object {
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