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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class ProfileFragment : Fragment() {
    private val TAG = "ProfileFragment"

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
            
            // Display the selected image
            profileImageView.setImageURI(selectedImageUri)
            
            // Hide the "No Image" text if it exists
            view?.findViewById<TextView>(R.id.tvNoImage)?.visibility = View.GONE
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
        
        // Load profile image from shared preferences if available
        val prefs = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val savedImageUri = prefs.getString("profile_image_${userId}", null)
        if (savedImageUri != null) {
            try {
                val uri = Uri.parse(savedImageUri)
                profileImageView.setImageURI(uri)
                selectedImageUri = uri
                // Hide the "No Image" text if it exists
                view?.findViewById<TextView>(R.id.tvNoImage)?.visibility = View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Error loading saved profile image", e)
            }
        }
        
        userApiClient.getCustomerProfile(userId, token) { customer, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error loading profile", error)
                    tvErrorMessage.text = "Customer profile not found. Please contact support."
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
                    if (customer.profileImage != null && customer.profileImage.isNotEmpty()) {
                        // Use Glide to load the image from the URL
                        Log.d(TAG, "Loading profile image URL: ${customer.profileImage}")
                        try {
                            context?.let { ctx ->
                                Glide.with(ctx)
                                    .load(customer.profileImage)
                                    .apply(RequestOptions()
                                        .placeholder(R.drawable.service_provider_image)
                                        .error(R.drawable.service_provider_image)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                                    .into(profileImageView)
                                
                                // Hide the "No Image" text
                                view?.findViewById<TextView>(R.id.tvNoImage)?.visibility = View.GONE
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error loading profile image with Glide", e)
                        }
                    }
                } else {
                    tvErrorMessage.text = "Customer profile not found. Please contact support."
                    tvErrorMessage.visibility = View.VISIBLE
                }
            }
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
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString().trim()).matches()) {
            etEmail.error = "Please enter a valid email address"
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
                    Toast.makeText(context, "Error updating profile: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                if (success) {
                    // If image was selected, upload it
                    if (selectedImageUri != null) {
                        uploadProfileImage(selectedImageUri!!)
                    } else {
                        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun uploadProfileImage(imageUri: Uri) {
        try {
            // Create a temporary file in the cache directory
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Toast.makeText(context, "Could not access the selected image", Toast.LENGTH_SHORT).show()
                return
            }
            
            val file = File(requireContext().cacheDir, "profile_image_${userId}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            
            // Show a loading message
            Toast.makeText(context, "Uploading profile image...", Toast.LENGTH_SHORT).show()
            
            // Upload the image to the server
            userApiClient.uploadProfileImage(userId, file, token) { success, error ->
                requireActivity().runOnUiThread {
                    if (success) {
                        Toast.makeText(context, "Profile updated successfully with new image", Toast.LENGTH_SHORT).show()
                        
                        // Save the image URI to shared preferences for persistence
                        val prefs = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("profile_image_${userId}", imageUri.toString()).apply()
                        
                    } else {
                        // Handle specific error cases
                        val errorMessage = when {
                            error?.message?.contains("too large") == true -> 
                                "Image is too large to upload. Please choose a smaller image."
                            error?.message?.contains("Authentication") == true -> 
                                "Authentication error. Please log in again."
                            error?.message?.contains("permission") == true ->
                                "You don't have permission to upload images."
                            else -> 
                                "Profile updated successfully but image could not be uploaded to server: ${error?.message}"
                        }
                        
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        Log.e(TAG, "Error uploading profile image", error)
                        
                        // Still save the image URI to shared preferences so we can show it on app restart
                        // This lets users see their selected image even if server upload failed
                        val prefs = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putString("profile_image_${userId}", imageUri.toString()).apply()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing image for upload", e)
            Toast.makeText(context, "Error preparing image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        
        // Reload the profile image from shared preferences when the fragment is resumed
        val prefs = requireActivity().getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val savedImageUri = prefs.getString("profile_image_${userId}", null)
        if (savedImageUri != null) {
            try {
                val uri = Uri.parse(savedImageUri)
                // Use Glide to load the local URI
                context?.let { ctx ->
                    Glide.with(ctx)
                        .load(uri)
                        .apply(RequestOptions()
                            .placeholder(R.drawable.service_provider_image)
                            .error(R.drawable.service_provider_image))
                        .into(profileImageView)
                }
                selectedImageUri = uri
                // Hide the "No Image" text if it exists
                view?.findViewById<TextView>(R.id.tvNoImage)?.visibility = View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Error loading saved profile image in onResume", e)
            }
        }
    }
} 