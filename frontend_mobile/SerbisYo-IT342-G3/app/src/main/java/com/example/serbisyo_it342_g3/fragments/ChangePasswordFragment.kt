package com.example.serbisyo_it342_g3.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.api.UserApiClient

class ChangePasswordFragment : Fragment() {
    private val TAG = "ChangePasswordFragment"
    
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnUpdatePassword: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var headerText: TextView
    private lateinit var subHeaderText: TextView
    
    private lateinit var userApiClient: UserApiClient
    private var token: String = ""
    private var userId: Long = 0
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_change_password, container, false)
        
        // Get arguments
        arguments?.let {
            userId = it.getLong("userId", 0)
            token = it.getString("token", "") ?: ""
        }
        
        // Initialize the API client
        userApiClient = UserApiClient(requireContext())
        
        // Initialize views
        etCurrentPassword = view.findViewById(R.id.etCurrentPassword)
        etNewPassword = view.findViewById(R.id.etNewPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnUpdatePassword = view.findViewById(R.id.btnUpdatePassword)
        progressBar = view.findViewById(R.id.progressBar)
        headerText = view.findViewById(R.id.headerText)
        subHeaderText = view.findViewById(R.id.subHeaderText)
        
        // Setup header
        headerText.text = "Change Password"
        subHeaderText.text = "Update your account password securely"
        
        // Set update button click listener
        btnUpdatePassword.setOnClickListener {
            if (validateInputs()) {
                updatePassword()
            }
        }
        
        return view
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        val currentPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        
        if (currentPassword.isEmpty()) {
            etCurrentPassword.error = "Current password cannot be empty"
            isValid = false
        }
        
        if (newPassword.isEmpty()) {
            etNewPassword.error = "New password cannot be empty"
            isValid = false
        } else if (newPassword.length < 6) {
            etNewPassword.error = "Password must be at least 6 characters"
            isValid = false
        }
        
        if (confirmPassword.isEmpty()) {
            etConfirmPassword.error = "Please confirm your new password"
            isValid = false
        } else if (newPassword != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            isValid = false
        }
        
        return isValid
    }
    
    private fun updatePassword() {
        progressBar.visibility = View.VISIBLE
        
        val currentPassword = etCurrentPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        
        userApiClient.changePassword(userId, currentPassword, newPassword, token) { success, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error changing password", error)
                    Toast.makeText(context, "Error changing password: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                if (success) {
                    Toast.makeText(context, "Password changed successfully", Toast.LENGTH_SHORT).show()
                    // Clear inputs
                    etCurrentPassword.text.clear()
                    etNewPassword.text.clear()
                    etConfirmPassword.text.clear()
                } else {
                    Toast.makeText(context, "Failed to change password. Please check your current password.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 