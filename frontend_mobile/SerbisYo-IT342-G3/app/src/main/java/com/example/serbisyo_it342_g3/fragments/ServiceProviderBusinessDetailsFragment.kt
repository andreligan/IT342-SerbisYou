package com.example.serbisyo_it342_g3.fragments

import android.content.Context
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
import com.example.serbisyo_it342_g3.data.ServiceProvider

class ServiceProviderBusinessDetailsFragment : Fragment() {
    private val tag = "SPBusinessFragment"
    
    private lateinit var etBusinessName: EditText
    private lateinit var etYearsExperience: EditText
    private lateinit var etAvailabilitySchedule: EditText
    private lateinit var etPaymentMethod: EditText
    private lateinit var btnUpdateBusinessDetails: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvErrorMessage: TextView
    
    private lateinit var userApiClient: UserApiClient
    private var token: String = ""
    private var userId: Long = 0
    private var providerId: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_provider_business_details, container, false)
        
        // Get arguments
        arguments?.let {
            userId = it.getLong("userId", 0)
            token = it.getString("token", "") ?: ""
            providerId = it.getLong("providerId", 0)
        }

        // Initialize API client
        userApiClient = UserApiClient(requireContext())
        
        // Initialize views
        etBusinessName = view.findViewById(R.id.etBusinessName)
        etYearsExperience = view.findViewById(R.id.etYearsExperience)
        etAvailabilitySchedule = view.findViewById(R.id.etAvailabilitySchedule)
        etPaymentMethod = view.findViewById(R.id.etPaymentMethod)
        btnUpdateBusinessDetails = view.findViewById(R.id.btnUpdateBusinessDetails)
        progressBar = view.findViewById(R.id.progressBar)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)
        
        // Load business details
        loadBusinessDetails()
        
        // Set button click listener
        btnUpdateBusinessDetails.setOnClickListener {
            if (validateInputs()) {
                updateBusinessDetails()
            }
        }
        
        return view
    }
    
    private fun loadBusinessDetails() {
        progressBar.visibility = View.VISIBLE
        tvErrorMessage.visibility = View.GONE
        
        userApiClient.getServiceProviderProfile(userId, token) { provider, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(tag, "Error loading business details", error)
                    tvErrorMessage.setText(R.string.business_details_not_found)
                    tvErrorMessage.visibility = View.VISIBLE
                    return@runOnUiThread
                }
                
                if (provider == null) {
                    tvErrorMessage.setText(R.string.provider_not_found)
                    tvErrorMessage.visibility = View.VISIBLE
                    return@runOnUiThread
                }
                
                // Save providerId if it was not set before - Fixed null safety issue
                val providerIdValue = provider.providerId ?: 0
                if (providerId == 0L && providerIdValue > 0) {
                    providerId = providerIdValue
                    val prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("providerId", providerId.toString()).apply()
                }
                
                // Fill form with business details - Safely handle nullable values
                etBusinessName.setText(provider.businessName ?: "")
                etYearsExperience.setText(provider.yearsOfExperience?.toString() ?: "0")
                etAvailabilitySchedule.setText(provider.availabilitySchedule ?: "")
                etPaymentMethod.setText(provider.paymentMethod ?: "")
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (etBusinessName.text.toString().trim().isEmpty()) {
            etBusinessName.error = getString(R.string.error_business_name_empty)
            isValid = false
        }
        
        val yearsExperience = etYearsExperience.text.toString().trim()
        if (yearsExperience.isEmpty()) {
            etYearsExperience.error = getString(R.string.error_years_experience_empty)
            isValid = false
        } else {
            try {
                val years = yearsExperience.toInt()
                if (years < 0) {
                    etYearsExperience.error = getString(R.string.error_years_experience_negative)
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                etYearsExperience.error = getString(R.string.error_years_experience_invalid)
                isValid = false
            }
        }
        
        if (etAvailabilitySchedule.text.toString().trim().isEmpty()) {
            etAvailabilitySchedule.error = getString(R.string.error_availability_empty)
            isValid = false
        }
        
        return isValid
    }
    
    private fun updateBusinessDetails() {
        progressBar.visibility = View.VISIBLE
        
        val businessName = etBusinessName.text.toString().trim()
        val yearsExperience = etYearsExperience.text.toString().trim().toIntOrNull() ?: 0
        val availabilitySchedule = etAvailabilitySchedule.text.toString().trim()
        val paymentMethod = etPaymentMethod.text.toString().trim().takeIf { it.isNotEmpty() }
        
        userApiClient.getServiceProviderProfile(userId, token) { provider, error ->
            if (error != null || provider == null) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Failed to get current provider details", Toast.LENGTH_SHORT).show()
                }
                return@getServiceProviderProfile
            }
            
            // Create updated provider with new business details
            val updatedProvider = ServiceProvider(
                providerId = providerId,
                firstName = provider.firstName,
                lastName = provider.lastName,
                phoneNumber = provider.phoneNumber,
                businessName = businessName,
                yearsOfExperience = yearsExperience,
                availabilitySchedule = availabilitySchedule,
                paymentMethod = paymentMethod,
                address = provider.address,
                userAuth = provider.userAuth
            )
            
            userApiClient.updateServiceProviderProfile(updatedProvider, token) { success, updateError ->
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    
                    if (updateError != null) {
                        Toast.makeText(context, getString(R.string.error_business_update, updateError.message), Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }
                    
                    if (success) {
                        Toast.makeText(context, R.string.business_update_success, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, R.string.business_update_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    companion object {
        @JvmStatic
        fun newInstance(userId: Long, token: String, providerId: Long) =
            ServiceProviderBusinessDetailsFragment().apply {
                arguments = Bundle().apply {
                    putLong("userId", userId)
                    putString("token", token)
                    putLong("providerId", providerId)
                }
            }
    }
}