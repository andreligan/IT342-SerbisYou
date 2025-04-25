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
import com.example.serbisyo_it342_g3.data.Address
import com.example.serbisyo_it342_g3.data.ServiceProvider

class ServiceProviderAddressFragment : Fragment() {
    private val tag = "SPAddressFragment"
    
    private lateinit var etStreet: EditText
    private lateinit var etCity: EditText
    private lateinit var etProvince: EditText
    private lateinit var etPostalCode: EditText
    private lateinit var btnUpdateAddress: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvErrorMessage: TextView
    
    private lateinit var userApiClient: UserApiClient
    private var token: String = ""
    private var userId: Long = 0
    private var providerId: Long = 0
    private var addressId: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_provider_address, container, false)
        
        // Get arguments
        arguments?.let {
            userId = it.getLong("userId", 0)
            token = it.getString("token", "") ?: ""
            providerId = it.getLong("providerId", 0)
        }

        // Initialize API client
        userApiClient = UserApiClient(requireContext())
        
        // Initialize views
        etStreet = view.findViewById(R.id.etStreet)
        etCity = view.findViewById(R.id.etCity)
        etProvince = view.findViewById(R.id.etProvince)
        etPostalCode = view.findViewById(R.id.etPostalCode)
        btnUpdateAddress = view.findViewById(R.id.btnUpdateAddress)
        progressBar = view.findViewById(R.id.progressBar)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)
        
        // Load address data
        loadAddress()
        
        // Set button click listener
        btnUpdateAddress.setOnClickListener {
            if (validateInputs()) {
                updateAddress()
            }
        }
        
        return view
    }
    
    private fun loadAddress() {
        progressBar.visibility = View.VISIBLE
        tvErrorMessage.visibility = View.GONE
        
        userApiClient.getServiceProviderProfile(userId, token) { provider, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(tag, "Error loading address", error)
                    tvErrorMessage.setText(R.string.address_not_found)
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
                
                // Fill form with address data - Fixed nullable check
                provider.address?.let { address ->
                    addressId = address.addressId ?: 0
                    etStreet.setText(address.street)
                    etCity.setText(address.city)
                    etProvince.setText(address.province)
                    etPostalCode.setText(address.postalCode)
                }
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (etStreet.text.toString().trim().isEmpty()) {
            etStreet.error = getString(R.string.error_street_empty)
            isValid = false
        }
        
        if (etCity.text.toString().trim().isEmpty()) {
            etCity.error = getString(R.string.error_city_empty)
            isValid = false
        }
        
        if (etProvince.text.toString().trim().isEmpty()) {
            etProvince.error = getString(R.string.error_province_empty)
            isValid = false
        }
        
        if (etPostalCode.text.toString().trim().isEmpty()) {
            etPostalCode.error = getString(R.string.error_postal_empty)
            isValid = false
        }
        
        return isValid
    }
    
    private fun updateAddress() {
        progressBar.visibility = View.VISIBLE
        
        val street = etStreet.text.toString().trim()
        val city = etCity.text.toString().trim()
        val province = etProvince.text.toString().trim()
        val postalCode = etPostalCode.text.toString().trim()
        
        // Create address object
        val address = Address(
            addressId = addressId,
            street = street,
            city = city,
            province = province,
            postalCode = postalCode
        )
        
        userApiClient.getServiceProviderProfile(userId, token) { provider, error ->
            if (error != null || provider == null) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Failed to get current provider details", Toast.LENGTH_SHORT).show()
                }
                return@getServiceProviderProfile
            }
            
            // Create updated provider with new address
            val updatedProvider = ServiceProvider(
                providerId = providerId,
                firstName = provider.firstName,
                lastName = provider.lastName,
                phoneNumber = provider.phoneNumber,
                businessName = provider.businessName,
                yearsOfExperience = provider.yearsOfExperience,
                availabilitySchedule = provider.availabilitySchedule,
                address = address,
                userAuth = provider.userAuth
            )
            
            userApiClient.updateServiceProviderProfile(updatedProvider, token) { success, updateError ->
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    
                    if (updateError != null) {
                        Toast.makeText(context, getString(R.string.error_address_update, updateError.message), Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }
                    
                    if (success) {
                        Toast.makeText(context, R.string.address_update_success, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, R.string.address_update_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    companion object {
        @JvmStatic
        fun newInstance(userId: Long, token: String, providerId: Long) =
            ServiceProviderAddressFragment().apply {
                arguments = Bundle().apply {
                    putLong("userId", userId)
                    putString("token", token)
                    putLong("providerId", providerId)
                }
            }
    }
}