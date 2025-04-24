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
import com.example.serbisyo_it342_g3.data.Customer
import com.example.serbisyo_it342_g3.data.User
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {
    private val tag = "ProfileFragment"

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var btnUpdateProfile: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvErrorMessage: TextView
    private lateinit var profileImageView: ShapeableImageView
    private lateinit var fabCamera: FloatingActionButton

    private lateinit var userApiClient: UserApiClient
    private var token: String = ""
    private var userId: Long = 0
    private var selectedImageUri: Uri? = null

    // Activity result launcher for image picker
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            selectedImageUri = result.data?.data

            try {
                // Create a local copy of the image to avoid permission issues
                val localImageFile = createLocalCopyOfImage(selectedImageUri)
                if (localImageFile != null) {
                    // Save the local file path to preferences
                    val prefs = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("profile_image_${userId}", localImageFile.absolutePath).apply()
                    
                    // Display the selected image
                    profileImageView.setImageURI(Uri.fromFile(localImageFile))
                    selectedImageUri = Uri.fromFile(localImageFile)
                    
                    // Hide the "No Image" text if it exists
                    view?.findViewById<TextView>(R.id.tvNoImage)?.visibility = View.GONE
                } else {
                    // If local copy fails, still try to show the original URI
                    profileImageView.setImageURI(selectedImageUri)
                    
                    // Save the image URI to local storage for persistence
                    if (selectedImageUri != null) {
                        val prefs = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("profile_image_${userId}", selectedImageUri.toString()).apply()
                    }
                    
                    // Hide the "No Image" text if it exists
                    view?.findViewById<TextView>(R.id.tvNoImage)?.visibility = View.GONE
                }
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
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Get arguments
        arguments?.let {
            userId = it.getLong("userId", 0)
            token = it.getString("token", "") ?: ""
        }

        // Initialize the API client
        userApiClient = UserApiClient(requireContext())

        // Initialize views
        etUsername = view.findViewById(R.id.etUsername)
        etEmail = view.findViewById(R.id.etEmail)
        etFirstName = view.findViewById(R.id.etFirstName)
        etLastName = view.findViewById(R.id.etLastName)
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber)
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile)
        progressBar = view.findViewById(R.id.progressBar)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)

        // Initialize profile image views
        profileImageView = view.findViewById(R.id.profileImage)
        fabCamera = view.findViewById(R.id.fabCamera)

        // Enable editing of username and email fields
        etUsername.isEnabled = true
        etEmail.isEnabled = true

        // Set click listener for camera button
        fabCamera.setOnClickListener {
            openImagePicker()
        }

        // Load user profile
        loadUserProfile()

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

    private fun loadUserProfile() {
        progressBar.visibility = View.VISIBLE
        tvErrorMessage.visibility = View.GONE
        
        // Set default image first
        profileImageView.setImageResource(R.drawable.default_profile)
        
        // Try to load profile image from shared preferences if available
        loadProfileImageFromPrefs()
        
        userApiClient.getCustomerProfile(userId, token) { customer, error -> 
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(tag, "Error loading profile", error)
                    tvErrorMessage.setText(R.string.profile_not_found)
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
                    
                    // If the customer has a profile image URL from the server
                    if (!customer.profileImage.isNullOrEmpty()) {
                        Log.d(tag, "Server provided profile image URL: ${customer.profileImage}")
                        // We already set a default image, so we don't need to do anything else here
                        // The locally saved image (if any) already has priority
                    }
                } else {
                    tvErrorMessage.setText(R.string.profile_not_found)
                    tvErrorMessage.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private fun loadProfileImageFromPrefs() {
        try {
            val prefs = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
            val savedImagePath = prefs.getString("profile_image_${userId}", null)
            
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
                    // make a local copy of the image
                    val localImageFile = createLocalCopyOfImage(uri)
                    if (localImageFile != null) {
                        // Update the saved path to the local copy
                        prefs.edit().putString("profile_image_${userId}", localImageFile.absolutePath).apply()
                        
                        // Display the image from the local file
                        profileImageView.setImageURI(Uri.fromFile(localImageFile))
                        selectedImageUri = Uri.fromFile(localImageFile)
                        
                        // Hide the "No Image" text if it exists
                        view?.findViewById<TextView>(R.id.tvNoImage)?.visibility = View.GONE
                    } else {
                        // Direct URI access as fallback - may cause permission errors
                        try {
                            // Check if we can access this URI before trying to load it
                            val inputStream = requireContext().contentResolver.openInputStream(uri)
                            inputStream?.close()
                            
                            // If we get here, we can access the URI
                            profileImageView.setImageURI(uri)
                            selectedImageUri = uri
                            
                            // Hide the "No Image" text if it exists
                            view?.findViewById<TextView>(R.id.tvNoImage)?.visibility = View.GONE
                        } catch (e: Exception) {
                            // Failed to load the image, use default
                            Log.e(tag, "Error loading saved profile image, using default", e)
                            profileImageView.setImageResource(R.drawable.default_profile)
                            
                            // Since this URI is problematic, remove it from preferences
                            prefs.edit().remove("profile_image_${userId}").apply()
                        }
                    }
                } catch (e: Exception) {
                    // Failed to load the image, use default
                    Log.e(tag, "Error loading saved profile image, using default", e)
                    profileImageView.setImageResource(R.drawable.default_profile)
                    
                    // Since this path is problematic, remove it from preferences
                    prefs.edit().remove("profile_image_${userId}").apply()
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
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString().trim()).matches()) {
            etEmail.error = getString(R.string.error_email_invalid)
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

        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()

        // Create a User object for the userAuth field
        val userAuth = User(
            userId = userId,
            userName = username,
            email = email
        )

        val updatedCustomer = Customer(
            customerId = userId,
            firstName = firstName,
            lastName = lastName,
            phoneNumber = phoneNumber,
            username = username,
            email = email,
            userAuth = userAuth,
            profileImage = if (selectedImageUri != null) selectedImageUri.toString() else null
        )

        userApiClient.updateCustomerProfile(updatedCustomer, token) { success, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE

                if (error != null) {
                    Toast.makeText(context, getString(R.string.error_image_upload, error.message), Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                if (success) {
                    // If image was selected, upload it
                    if (selectedImageUri != null) {
                        uploadProfileImage(selectedImageUri!!)
                    } else {
                        Toast.makeText(context, R.string.profile_update_success, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, R.string.profile_update_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadProfileImage(imageUri: Uri) {
        try {
            // Create a local copy of the image
            val localImageFile = createLocalCopyOfImage(imageUri) ?: run {
                Toast.makeText(context, getString(R.string.error_image_access), Toast.LENGTH_SHORT).show()
                return
            }
            
            // Show a loading message
            Toast.makeText(context, getString(R.string.uploading_profile_image), Toast.LENGTH_SHORT).show()
            
            // Display the selected image directly
            profileImageView.setImageURI(Uri.fromFile(localImageFile))
            
            // Upload the image to the server
            userApiClient.uploadProfileImage(userId, localImageFile, token) { success, error ->
                requireActivity().runOnUiThread {
                    if (success) {
                        Toast.makeText(context, getString(R.string.profile_update_with_image), Toast.LENGTH_SHORT).show()
                        
                        // Save the image file path to shared preferences for persistence
                        val prefs = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("profile_image_${userId}", localImageFile.absolutePath).apply()
                        
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
                        
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        Log.e(tag, "Error uploading profile image", error)
                        
                        // Still save the image file path to shared preferences
                        // This lets users see their selected image even if server upload failed
                        val prefs = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("profile_image_${userId}", localImageFile.absolutePath).apply()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error preparing image for upload", e)
            Toast.makeText(context, getString(R.string.error_image_prep, e.message), Toast.LENGTH_SHORT).show()
        }
    }

    private fun createLocalCopyOfImage(uri: Uri?): File? {
        if (uri == null) return null
        
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val timestamp = System.currentTimeMillis()
            val filename = "profile_image_${userId}_$timestamp.jpg"
            val file = File(requireContext().cacheDir, filename)
            
            file.outputStream().use { outputStream ->
                inputStream.use { input ->
                    input.copyTo(outputStream)
                }
            }
            
            // Verify the file was created successfully and has content
            if (file.exists() && file.length() > 0) {
                Log.d(tag, "Local image copy created successfully at ${file.absolutePath} with size ${file.length()} bytes")
                return file
            } else {
                Log.e(tag, "Created file is empty or doesn't exist: ${file.absolutePath}")
                return null
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to create local copy of image", e)
            return null
        }
    }

    override fun onResume() {
        super.onResume()
        
        // Reload the profile image from shared preferences when the fragment is resumed
        loadProfileImageFromPrefs()
    }
}