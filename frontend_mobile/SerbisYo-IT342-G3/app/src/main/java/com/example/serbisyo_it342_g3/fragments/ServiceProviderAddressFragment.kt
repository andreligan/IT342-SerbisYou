package com.example.serbisyo_it342_g3.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.api.AddressApiClient
import com.example.serbisyo_it342_g3.api.PSGCApiClient
import com.example.serbisyo_it342_g3.api.UserApiClient
import com.example.serbisyo_it342_g3.data.Address
import com.example.serbisyo_it342_g3.data.Barangay
import com.example.serbisyo_it342_g3.data.Municipality
import com.example.serbisyo_it342_g3.data.Province
import com.example.serbisyo_it342_g3.data.ServiceProvider
import org.json.JSONObject

class ServiceProviderAddressFragment : Fragment() {
    private val TAG = "SPAddressFragment"
    
    // UI components
    private lateinit var etStreet: EditText
    private lateinit var spinnerProvince: Spinner
    private lateinit var spinnerCity: Spinner
    private lateinit var spinnerBarangay: Spinner
    private lateinit var etZipCode: EditText
    private lateinit var btnSaveAddress: Button
    private lateinit var btnCancelEdit: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var rvAddresses: RecyclerView
    private lateinit var tvNoAddresses: TextView
    private lateinit var savedAddressesSection: LinearLayout
    
    // API clients
    private lateinit var userApiClient: UserApiClient
    private lateinit var addressApiClient: AddressApiClient
    private lateinit var psgcApiClient: PSGCApiClient
    
    // User data
    private var token: String = ""
    private var userId: Long = 0
    private var providerId: Long = 0
    
    // Edit mode variables
    private var isEditMode = false
    private var editingAddressId: Long? = null
    
    // Address data
    private var addresses = mutableListOf<Address>()
    private lateinit var addressAdapter: AddressAdapter
    
    // PSGC data
    private val provinces = mutableListOf<Province>()
    private val municipalities = mutableListOf<Municipality>()
    private val barangays = mutableListOf<Barangay>()
    
    // Selected location data
    private var selectedProvince: Province? = null
    private var selectedMunicipality: Municipality? = null
    private var selectedBarangay: Barangay? = null
    
    // Selection placeholders
    private val selectProvinceText = "Select Province"
    private val selectCityText = "Select City/Municipality" 
    private val selectBarangayText = "Select Barangay"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_provider_address, container, false)
        
        // Get arguments
        arguments?.let {
            userId = it.getLong("userId", 0)
            token = it.getString("token", "") ?: ""
        }

        // Initialize API clients
        userApiClient = UserApiClient(requireContext())
        addressApiClient = AddressApiClient(requireContext())
        psgcApiClient = PSGCApiClient(requireContext())
        
        // Initialize views
        initializeViews(view)
        
        // Setup recycler view for addresses
        setupRecyclerView()
        
        // Setup empty spinners initially
        setupEmptySpinners()
        
        // Setup spinner listeners
        setupSpinnerListeners()
        
        // Get correct provider ID
        getProviderIdFromUserProfile()
        
        // Load provinces for initial spinner
        loadProvinces()
        
        // Set save button click listener
        btnSaveAddress.setOnClickListener {
            if (validateInputs()) {
                if (isEditMode) {
                    updateAddress()
                } else {
                    saveAddress()
                }
            }
        }
        
        // Set cancel button click listener
        btnCancelEdit.setOnClickListener {
            // Reset form and exit edit mode
            resetForm()
        }
        
        // Initially hide cancel button
        btnCancelEdit.visibility = View.GONE
        
        return view
    }
    
    private fun initializeViews(view: View) {
        etStreet = view.findViewById(R.id.etStreet)
        spinnerProvince = view.findViewById(R.id.spinnerProvince)
        spinnerCity = view.findViewById(R.id.spinnerCity)
        spinnerBarangay = view.findViewById(R.id.spinnerBarangay)
        etZipCode = view.findViewById(R.id.etZipCode)
        btnSaveAddress = view.findViewById(R.id.btnSaveAddress)
        btnCancelEdit = view.findViewById(R.id.btnCancelEdit)
        progressBar = view.findViewById(R.id.progressBar)
        rvAddresses = view.findViewById(R.id.rvAddresses)
        tvNoAddresses = view.findViewById(R.id.tvNoAddresses)
        savedAddressesSection = view.findViewById(R.id.savedAddressesSection)
    }
    
    private fun setupRecyclerView() {
        rvAddresses.layoutManager = LinearLayoutManager(context)
        addressAdapter = AddressAdapter(addresses, 
            onDeleteClick = { address -> deleteAddress(address.addressId) },
            onSetMainClick = { address -> setAddressAsMain(address) },
            onEditClick = { address -> editAddress(address) }
        )
        rvAddresses.adapter = addressAdapter
    }
    
    private fun setupEmptySpinners() {
        // Set up empty adapters initially for all spinners
        val provinceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf(selectProvinceText))
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProvince.adapter = provinceAdapter
        
        val cityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf(selectCityText))
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCity.adapter = cityAdapter
        spinnerCity.isEnabled = false
        
        val barangayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf(selectBarangayText))
        barangayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBarangay.adapter = barangayAdapter
        spinnerBarangay.isEnabled = false
    }
    
    private fun setupSpinnerListeners() {
        spinnerProvince.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0 && provinces.isNotEmpty()) {
                    // Subtract 1 because first item is the placeholder
                    selectedProvince = provinces[position - 1]
                    
                    // Clear city and barangay selections when province changes
                    selectedMunicipality = null
                    selectedBarangay = null
                    
                    // Reset city and barangay spinners
                    val emptyCityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf(selectCityText))
                    emptyCityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCity.adapter = emptyCityAdapter
                    
                    val emptyBarangayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf(selectBarangayText))
                    emptyBarangayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerBarangay.adapter = emptyBarangayAdapter
                    spinnerBarangay.isEnabled = false
                    
                    // Load municipalities for selected province
                    loadMunicipalities(selectedProvince!!.code)
                    
                    Log.d(TAG, "Selected province: ${selectedProvince?.name}")
                } else {
                    selectedProvince = null
                    selectedMunicipality = null
                    selectedBarangay = null
                    spinnerCity.isEnabled = false
                    spinnerBarangay.isEnabled = false
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedProvince = null
            }
        }
        
        spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0 && municipalities.isNotEmpty()) {
                    // Subtract 1 because first item is the placeholder
                    selectedMunicipality = municipalities[position - 1]
                    
                    // Clear barangay selection when city changes
                    selectedBarangay = null
                    
                    // Reset barangay spinner
                    val emptyAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listOf(selectBarangayText))
                    emptyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerBarangay.adapter = emptyAdapter
                    
                    // Load barangays for selected municipality
                    loadBarangays(selectedMunicipality!!.code)
                    
                    Log.d(TAG, "Selected city: ${selectedMunicipality?.name}")
                } else {
                    selectedMunicipality = null
                    selectedBarangay = null
                    spinnerBarangay.isEnabled = false
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedMunicipality = null
            }
        }
        
        spinnerBarangay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0 && barangays.isNotEmpty()) {
                    // Subtract 1 because first item is the placeholder
                    selectedBarangay = barangays[position - 1]
                    Log.d(TAG, "Selected barangay: ${selectedBarangay?.name}")
                } else {
                    selectedBarangay = null
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedBarangay = null
            }
        }
    }
    
    private fun getProviderIdFromUserProfile() {
        userApiClient.getServiceProviderByAuthId(userId, token) { serviceProvider, error ->
            try {
                // First check if fragment is still attached
                if (!isAdded) {
                    Log.w(TAG, "Fragment detached when receiving provider data")
                    return@getServiceProviderByAuthId
                }
                
                // Use activity safely
                val activity = activity ?: run {
                    Log.w(TAG, "Activity null when receiving provider data")
                    return@getServiceProviderByAuthId
                }
                
                activity.runOnUiThread {
                    try {
                        if (error != null) {
                            Log.e(TAG, "Error getting provider profile", error)
                            // Don't fall back to a default provider ID, just show empty state
                            if (isAdded) {
                                tvNoAddresses.visibility = View.VISIBLE
                                rvAddresses.visibility = View.GONE
                                tvNoAddresses.text = "No address information available for new account. Please add your first address."
                                progressBar.visibility = View.GONE
                            }
                        } else if (serviceProvider != null) {
                            providerId = serviceProvider.providerId ?: 0L
                            // Save provider ID to SharedPreferences safely
                            if (isAdded) {
                                saveProviderIdToPrefs(providerId)
                                Log.d(TAG, "Retrieved provider ID: $providerId")
                                
                                if (providerId > 0) {
                                    loadAddresses()
                                } else {
                                    tvNoAddresses.visibility = View.VISIBLE
                                    rvAddresses.visibility = View.GONE
                                    tvNoAddresses.text = "No address information available for new account. Please add your first address."
                                    progressBar.visibility = View.GONE
                                }
                            }
                        } else {
                            // No provider found - this is a new account
                            if (isAdded) {
                                tvNoAddresses.visibility = View.VISIBLE
                                rvAddresses.visibility = View.GONE
                                tvNoAddresses.text = "No address information available for new account. Please add your first address." 
                                progressBar.visibility = View.GONE
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating UI with provider data", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in provider callback", e)
            }
        }
    }
    
    private fun saveProviderIdToPrefs(id: Long) {
        if (id <= 0) return // Don't save invalid IDs
        
        try {
            if (!isAdded) return
            
            val activity = activity ?: return
            
            val sharedPreferences = activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putLong("providerId", id).apply()
            
            val userPrefs = activity.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            userPrefs.edit().putLong("providerId", id).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving provider ID to preferences", e)
        }
    }
    
    private fun getProviderIdFromAllProviders() {
        userApiClient.getAllServiceProviders(token) { providers, error ->
            try {
                if (!isAdded) {
                    Log.w(TAG, "Fragment detached when receiving all providers data")
                    return@getAllServiceProviders
                }
                
                val activity = activity ?: run {
                    Log.w(TAG, "Activity null when receiving all providers data")
                    return@getAllServiceProviders
                }
                
                activity.runOnUiThread {
                    try {
                        progressBar.visibility = View.GONE
                        
                        if (error != null) {
                            Log.e(TAG, "Error getting providers", error)
                            // Show empty state instead of using default provider
                            tvNoAddresses.visibility = View.VISIBLE
                            rvAddresses.visibility = View.GONE
                            tvNoAddresses.text = "No address information available. Please add your first address."
                            return@runOnUiThread
                        }
                        
                        if (providers != null) {
                            val matchingProvider = providers.find { it.userAuth?.userId == userId }
                            if (matchingProvider != null) {
                                providerId = matchingProvider.providerId ?: 0L
                                Log.d(TAG, "Found provider ID from list: $providerId")
                                
                                // Only save and load if we found a valid provider ID
                                if (providerId > 0) {
                                    saveProviderIdToPrefs(providerId)
                                    loadAddresses()
                                } else {
                                    tvNoAddresses.visibility = View.VISIBLE
                                    rvAddresses.visibility = View.GONE
                                    tvNoAddresses.text = "No address information available for your account. Please add your first address."
                                }
                            } else {
                                // No matching provider - new account
                                tvNoAddresses.visibility = View.VISIBLE
                                rvAddresses.visibility = View.GONE
                                tvNoAddresses.text = "No address information available for new account. Please add your first address."
                            }
                        } else {
                            // No providers returned from API
                            tvNoAddresses.visibility = View.VISIBLE
                            rvAddresses.visibility = View.GONE
                            tvNoAddresses.text = "No address information available. Please add your first address."
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating UI with all providers data", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in all providers callback", e)
            }
        }
    }
    
    private fun loadProvinces() {
        progressBar.visibility = View.VISIBLE
        
        psgcApiClient.getProvinces { provinceList, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error loading provinces", error)
                    Toast.makeText(context, "Failed to load provinces: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                if (provinceList != null) {
                    provinces.clear()
                    provinces.addAll(provinceList.sortedBy { it.name })
                    updateProvinceSpinner()
                    Log.d(TAG, "Loaded ${provinces.size} provinces")
                }
            }
        }
    }
    
    private fun loadMunicipalities(provinceCode: String) {
        progressBar.visibility = View.VISIBLE
        spinnerCity.isEnabled = false
        
        psgcApiClient.getMunicipalitiesByProvince(provinceCode) { municipalityList, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error loading municipalities", error)
                    Toast.makeText(context, "Failed to load cities/municipalities: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                if (municipalityList != null) {
                    municipalities.clear()
                    municipalities.addAll(municipalityList.sortedBy { it.name })
                    updateCitySpinner()
                    
                    // Enable the city spinner now that we have data
                    spinnerCity.isEnabled = true
                    
                    Log.d(TAG, "Loaded ${municipalities.size} municipalities for province $provinceCode")
                }
            }
        }
    }
    
    private fun loadBarangays(municipalityCode: String) {
        progressBar.visibility = View.VISIBLE
        spinnerBarangay.isEnabled = false
        
        psgcApiClient.getBarangaysByMunicipality(municipalityCode) { barangayList, error -> 
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error loading barangays", error)
                    Toast.makeText(context, "Failed to load barangays: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                if (barangayList != null) {
                    barangays.clear()
                    barangays.addAll(barangayList.sortedBy { it.name })
                    updateBarangaySpinner()
                    
                    // Enable the barangay spinner now that we have data
                    spinnerBarangay.isEnabled = true
                    
                    Log.d(TAG, "Loaded ${barangays.size} barangays for municipality $municipalityCode")
                }
            }
        }
    }
    
    private fun updateProvinceSpinner() {
        val displayItems = mutableListOf(selectProvinceText)
        displayItems.addAll(provinces.map { it.name })
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, displayItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProvince.adapter = adapter
    }
    
    private fun updateCitySpinner() {
        val displayItems = mutableListOf(selectCityText)
        displayItems.addAll(municipalities.map { it.name })
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, displayItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCity.adapter = adapter
    }
    
    private fun updateBarangaySpinner() {
        val displayItems = mutableListOf(selectBarangayText)
        displayItems.addAll(barangays.map { it.name })
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, displayItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBarangay.adapter = adapter
    }
    
    private fun validateInputs(): Boolean {
        if (spinnerProvince.selectedItemPosition <= 0) {
            Toast.makeText(context, "Please select a province", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (spinnerCity.selectedItemPosition <= 0) {
            Toast.makeText(context, "Please select a city/municipality", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (spinnerBarangay.selectedItemPosition <= 0) {
            Toast.makeText(context, "Please select a barangay", Toast.LENGTH_SHORT).show()
            return false
        }
        
        val street = etStreet.text.toString().trim()
        if (street.isEmpty()) {
            etStreet.error = "Street name is required"
            etStreet.requestFocus()
            return false
        }
        
        val zipCode = etZipCode.text.toString().trim()
        if (zipCode.isEmpty()) {
            etZipCode.error = "ZIP code is required"
            etZipCode.requestFocus()
            return false
        }
        
        return true
    }
    
    private fun loadAddresses() {
        progressBar.visibility = View.VISIBLE
        
        // Don't use default provider ID for new accounts
        if (providerId <= 0) {
            progressBar.visibility = View.GONE
            tvNoAddresses.visibility = View.VISIBLE
            rvAddresses.visibility = View.GONE
            tvNoAddresses.text = "No address information available. Please add your first address."
            return
        }
        
        Log.d(TAG, "Loading addresses for provider ID: $providerId")
        
        // Fetch service provider's addresses
        userApiClient.getServiceProviderAddresses(providerId, token) { addressList, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error loading addresses", error)
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                addresses.clear()
                if (addressList != null && addressList.isNotEmpty()) {
                    addresses.addAll(addressList)
                    tvNoAddresses.visibility = View.GONE
                    rvAddresses.visibility = View.VISIBLE
                    Log.d(TAG, "Loaded ${addresses.size} addresses")
                } else {
                    tvNoAddresses.visibility = View.VISIBLE
                    rvAddresses.visibility = View.GONE
                    Log.d(TAG, "No addresses found")
                }
                
                addressAdapter.notifyDataSetChanged()
            }
        }
    }
    
    private fun saveAddress() {
        if (selectedProvince == null) {
            Toast.makeText(context, "Please select a province", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedMunicipality == null) {
            Toast.makeText(context, "Please select a municipality/city", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedBarangay == null) {
            Toast.makeText(context, "Please select a barangay", Toast.LENGTH_SHORT).show()
            return
        }
        
        val street = etStreet.text.toString().trim()
        
        if (street.isEmpty()) {
            Toast.makeText(context, "Please enter street details", Toast.LENGTH_SHORT).show()
            return
        }
        
        val zipCode = etZipCode.text.toString().trim()
        
        if (zipCode.isEmpty()) {
            Toast.makeText(context, "Please enter ZIP code", Toast.LENGTH_SHORT).show()
            return
        }
        
        progressBar.visibility = View.VISIBLE
        
        // Create address object
        val address = Address(
            street = street,
            barangay = selectedBarangay?.name ?: "",
            city = selectedMunicipality?.name ?: "",
            province = selectedProvince?.name ?: "",
            zipCode = zipCode
        )
        
        if (isEditMode) {
            updateAddress()
        } else {
            createNewAddress(address)
        }
    }
    
    private fun createNewAddress(address: Address) {
        progressBar.visibility = View.VISIBLE
        btnSaveAddress.isEnabled = false

        addressApiClient.addServiceProviderAddress(providerId, address, token) { success, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                btnSaveAddress.isEnabled = true

                if (success) {
                    Toast.makeText(requireContext(), "Address added successfully", Toast.LENGTH_SHORT).show()
                    resetForm()
                    loadAddresses()
                } else {
                    Toast.makeText(requireContext(), "Failed to add address: ${error?.message}", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Add address error: ${error?.message}")
                }
            }
        }
    }
    
    private fun updateAddress() {
        if (editingAddressId == null) {
            Toast.makeText(context, "Invalid address ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        progressBar.visibility = View.VISIBLE
        
        val street = etStreet.text.toString().trim()
        val barangay = selectedBarangay?.name ?: ""
        val city = selectedMunicipality?.name ?: ""
        val province = selectedProvince?.name ?: ""
        val zipCode = etZipCode.text.toString().trim()
        
        val address = Address(
            addressId = editingAddressId,
            street = street,
            barangay = barangay,
            city = city,
            province = province,
            zipCode = zipCode,
            main = false
        )
        
        // Using updateAddress method from AddressApiClient
        addressApiClient.updateAddress(address, token) { success, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Toast.makeText(context, "Error updating address: ${error.message}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Update address error", error)
                    return@runOnUiThread
                }
                
                if (success) {
                    Toast.makeText(context, "Address updated successfully", Toast.LENGTH_SHORT).show()
                    resetForm()
                    loadAddresses()
                } else {
                    Toast.makeText(context, "Failed to update address", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updateExistingAddress(addressData: JSONObject) {
        progressBar.visibility = View.VISIBLE
        btnSaveAddress.isEnabled = false

        try {
            // Extract values from the JSON object
            val street = addressData.optString("street", "")
            val province = addressData.optString("province", "")
            val city = addressData.optString("city", "")
            val barangay = addressData.optString("barangay", "")
            val zipCode = addressData.optString("zipCode", "")

            // Create Address object
            val address = Address(
                addressId = editingAddressId,
                street = street,
                province = province,
                city = city,
                barangay = barangay,
                zipCode = zipCode,
                main = false
            )

            // Update the address using the token from class property
            addressApiClient.updateAddress(address, token) { success, error ->
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnSaveAddress.isEnabled = true

                    if (success) {
                        Toast.makeText(requireContext(), "Address updated successfully", Toast.LENGTH_SHORT).show()
                        resetForm()
                        loadAddresses()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update address: ${error?.message}", Toast.LENGTH_LONG).show()
                        Log.e(TAG, "Update address error: ${error?.message}")
                    }
                }
            }
        } catch (e: Exception) {
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                btnSaveAddress.isEnabled = true
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Exception during address update", e)
            }
        }
    }
    
    private fun cancelEdit() {
        // Reset the form and return to add mode
        isEditMode = false
        editingAddressId = -1
        
        // Reset UI
        btnSaveAddress.text = "Add Address"
        btnCancelEdit.visibility = View.GONE
        
        // Clear fields
        spinnerProvince.setSelection(0)
        resetMunicipalityAndBarangay()
        etStreet.setText("")
    }
    
    private fun deleteAddress(addressId: Long?) {
        if (addressId == null) {
            Toast.makeText(context, "Invalid address ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        progressBar.visibility = View.VISIBLE
        
        addressApiClient.deleteServiceProviderAddress(addressId, token) { success, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                if (success) {
                    Toast.makeText(context, "Address deleted successfully", Toast.LENGTH_SHORT).show()
                    
                    // Reload addresses
                    loadAddresses()
                } else {
                    Toast.makeText(context, "Failed to delete address", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun setAddressAsMain(address: Address) {
        if (address.addressId == null) {
            Toast.makeText(context, "Invalid address ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        progressBar.visibility = View.VISIBLE
        
        addressApiClient.setServiceProviderMainAddress(providerId, address.addressId, token) { success, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                if (success) {
                    Toast.makeText(context, "Main address updated successfully", Toast.LENGTH_SHORT).show()
                    
                    // Reload addresses
                    loadAddresses()
                } else {
                    Toast.makeText(context, "Failed to update main address", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun editAddress(address: Address) {
        // Set edit mode state
        isEditMode = true
        editingAddressId = address.addressId
        
        // Change UI for edit mode
        btnSaveAddress.text = "Update Address"
        btnCancelEdit.visibility = View.VISIBLE
        
        // Show loading indicator
        progressBar.visibility = View.VISIBLE
        
        // Immediately populate the simple fields
        etStreet.setText(address.street ?: address.streetName)
        etZipCode.setText(address.postalCode ?: address.zipCode)
        
        // Log the address details for debugging
        Log.d(TAG, "Editing address: ${address.addressId}")
        Log.d(TAG, "Province: ${address.province}, City: ${address.city}, Barangay: ${address.barangay}")
        Log.d(TAG, "Street: ${address.street}, ZipCode: ${address.postalCode ?: address.zipCode}")
        
        // We need to load provinces, municipalities, and barangays in sequence to populate the spinners
        loadProvincesForEdit(address)
    }
    
    private fun loadProvincesForEdit(address: Address) {
        psgcApiClient.getProvinces { provinceList, error ->
            if (error != null || provinceList == null) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Failed to load provinces for editing", Toast.LENGTH_SHORT).show()
                }
                return@getProvinces
            }
            
            provinces.clear()
            provinces.addAll(provinceList.sortedBy { it.name })
            
            // Find matching province - try exact match first, then fuzzy match
            var matchingProvince = provinces.find { it.name.equals(address.province, ignoreCase = true) }
            
            // If no exact match, try to find by contains
            if (matchingProvince == null) {
                matchingProvince = provinces.find { 
                    it.name.contains(address.province, ignoreCase = true) || 
                    address.province.contains(it.name, ignoreCase = true) 
                }
                
                if (matchingProvince != null) {
                    Log.d(TAG, "Found province by fuzzy match: ${matchingProvince.name} for ${address.province}")
                }
            }
            
            if (matchingProvince == null) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Could not find matching province", Toast.LENGTH_SHORT).show()
                    updateProvinceSpinner()
                }
                return@getProvinces
            }
            
            selectedProvince = matchingProvince
            
            // Update UI on main thread
            requireActivity().runOnUiThread {
                updateProvinceSpinner()
                
                // Select the province in the spinner (add 1 for placeholder)
                val provinceIndex = findProvinceIndex(matchingProvince)
                if (provinceIndex > 0) {
                    spinnerProvince.setSelection(provinceIndex)
                    
                    // Now load municipalities for the selected province
                    loadMunicipalitiesForEdit(matchingProvince.code, address)
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Could not select province in dropdown", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun findProvinceIndex(province: Province): Int {
        // Add 1 because the first item is the placeholder
        val index = provinces.indexOf(province) + 1
        // Debug log
        Log.d(TAG, "Province ${province.name} found at index $index (list size: ${provinces.size})")
        return index
    }
    
    private fun loadMunicipalitiesForEdit(provinceCode: String, address: Address) {
        psgcApiClient.getMunicipalitiesByProvince(provinceCode) { municipalityList, error ->
            if (error != null || municipalityList == null) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    // Don't show the error toast for municipalities
                    // Toast.makeText(context, "Failed to load municipalities for editing", Toast.LENGTH_SHORT).show()
                    
                    // Instead, just log the error and continue with empty list
                    Log.e(TAG, "Failed to load municipalities for editing: ${error?.message}")
                    
                    // Still proceed with the UI update
                    updateCitySpinner()
                    
                    // Try to load barangays directly with an empty municipality selection
                    if (address.barangay?.isNotEmpty() == true) {
                        // Just try to find the barangay without municipality context
                        loadBarangaysDirectly(address)
                    }
                }
                return@getMunicipalitiesByProvince
            }
            
            municipalities.clear()
            municipalities.addAll(municipalityList.sortedBy { it.name })
            
            // Find matching municipality - try exact match first, then fuzzy match
            var matchingMunicipality = municipalities.find { it.name.equals(address.city, ignoreCase = true) }
            
            // If no exact match, try to find by contains
            if (matchingMunicipality == null) {
                matchingMunicipality = municipalities.find { 
                    it.name.contains(address.city ?: "", ignoreCase = true) || 
                    (address.city?.contains(it.name, ignoreCase = true) ?: false)
                }
                
                if (matchingMunicipality != null) {
                    Log.d(TAG, "Found municipality by fuzzy match: ${matchingMunicipality.name} for ${address.city}")
                }
            }
            
            if (matchingMunicipality == null) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    // Don't show this error toast
                    // Toast.makeText(context, "Could not find matching municipality", Toast.LENGTH_SHORT).show()
                    updateCitySpinner()
                    
                    // Try to load barangays directly
                    if (address.barangay?.isNotEmpty() == true) {
                        loadBarangaysDirectly(address)
                    }
                }
                return@getMunicipalitiesByProvince
            }
            
            selectedMunicipality = matchingMunicipality
            
            // Update UI on main thread
            requireActivity().runOnUiThread {
                updateCitySpinner()
                
                // Select the municipality in the spinner (add 1 for placeholder)
                val municipalityIndex = findMunicipalityIndex(matchingMunicipality)
                if (municipalityIndex > 0) {
                    spinnerCity.setSelection(municipalityIndex)
                    
                    // Now load barangays for the selected municipality
                    loadBarangaysForEdit(matchingMunicipality.code, address)
                } else {
                    progressBar.visibility = View.GONE
                    // Don't show this error toast
                    // Toast.makeText(context, "Could not select municipality in dropdown", Toast.LENGTH_SHORT).show()
                    
                    // Try to load barangays directly
                    if (address.barangay?.isNotEmpty() == true) {
                        loadBarangaysDirectly(address)
                    }
                }
            }
        }
    }
    
    private fun findMunicipalityIndex(municipality: Municipality): Int {
        // Add 1 because the first item is the placeholder
        val index = municipalities.indexOf(municipality) + 1
        // Debug log
        Log.d(TAG, "Municipality ${municipality.name} found at index $index (list size: ${municipalities.size})")
        return index
    }
    
    private fun loadBarangaysForEdit(municipalityCode: String, address: Address) {
        psgcApiClient.getBarangaysByMunicipality(municipalityCode) { barangayList, error ->
            if (error != null || barangayList == null) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Failed to load barangays for editing", Toast.LENGTH_SHORT).show()
                }
                return@getBarangaysByMunicipality
            }
            
            barangays.clear()
            barangays.addAll(barangayList.sortedBy { it.name })
            
            // Find matching barangay - try exact match first, then fuzzy match
            var matchingBarangay = barangays.find { it.name.equals(address.barangay, ignoreCase = true) }
            
            // If no exact match, try to find by contains
            if (matchingBarangay == null) {
                matchingBarangay = barangays.find { 
                    it.name.contains(address.barangay ?: "", ignoreCase = true) || 
                    (address.barangay?.contains(it.name, ignoreCase = true) ?: false)
                }
                
                if (matchingBarangay != null) {
                    Log.d(TAG, "Found barangay by fuzzy match: ${matchingBarangay.name} for ${address.barangay}")
                }
            }
            
            if (matchingBarangay == null) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Could not find matching barangay", Toast.LENGTH_SHORT).show()
                    updateBarangaySpinner()
                    // We still completed the loading process
                }
                return@getBarangaysByMunicipality
            }
            
            selectedBarangay = matchingBarangay
            
            // Update UI on main thread
            requireActivity().runOnUiThread {
                updateBarangaySpinner()
                
                // Select the barangay in the spinner (add 1 for placeholder)
                val barangayIndex = findBarangayIndex(matchingBarangay)
                if (barangayIndex > 0) {
                    spinnerBarangay.setSelection(barangayIndex)
                } else {
                    Toast.makeText(context, "Could not select barangay in dropdown", Toast.LENGTH_SHORT).show()
                }
                
                // Loading complete
                progressBar.visibility = View.GONE
            }
        }
    }
    
    private fun findBarangayIndex(barangay: Barangay): Int {
        // Add 1 because the first item is the placeholder
        val index = barangays.indexOf(barangay) + 1
        // Debug log
        Log.d(TAG, "Barangay ${barangay.name} found at index $index (list size: ${barangays.size})")
        return index
    }
    
    private fun resetForm() {
        // Clear inputs
        etStreet.text.clear()
        etZipCode.text.clear()
        
        // Reset spinners
        resetSpinners()
        
        // Exit edit mode
        isEditMode = false
        editingAddressId = null
        
        // Update UI for add mode
        btnSaveAddress.text = "Save Address"
        btnCancelEdit.visibility = View.GONE
    }
    
    private fun resetSpinners() {
        spinnerProvince.setSelection(0)
        spinnerCity.setSelection(0)
        spinnerBarangay.setSelection(0)
        spinnerCity.isEnabled = false
        spinnerBarangay.isEnabled = false
        selectedProvince = null
        selectedMunicipality = null
        selectedBarangay = null
    }
    
    private fun resetMunicipalityAndBarangay() {
        selectedMunicipality = null
        selectedBarangay = null
        spinnerCity.isEnabled = false
        spinnerBarangay.isEnabled = false
    }
    
    // Helper method to try loading barangays directly when we can't find the right municipality
    private fun loadBarangaysDirectly(address: Address) {
        // Enable the city spinner anyway
        spinnerCity.isEnabled = true
        
        // We'll set a semi-selected state for city spinner if possible
        if (address.city?.isNotEmpty() == true) {
            val adapter = spinnerCity.adapter as? ArrayAdapter<String>
            if (adapter != null) {
                val cityList = mutableListOf<String>()
                cityList.add(selectCityText)
                cityList.add(address.city)
                
                val newAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cityList)
                newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCity.adapter = newAdapter
                spinnerCity.setSelection(1)
            }
        }
        
        // Just populate spinner with the barangay we know about
        val barangayList = mutableListOf<String>()
        barangayList.add(selectBarangayText)
        
        if (address.barangay?.isNotEmpty() == true) {
            barangayList.add(address.barangay)
            
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, barangayList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerBarangay.adapter = adapter
            spinnerBarangay.isEnabled = true
            spinnerBarangay.setSelection(1)
            
            // Create a simple Barangay object for the selected barangay
            selectedBarangay = Barangay(
                code = "unknown",
                name = address.barangay,
                municipalityCode = "unknown"
            )
        }
    }
    
    // Adapter for displaying addresses
    inner class AddressAdapter(
        private val addresses: List<Address>,
        private val onDeleteClick: (Address) -> Unit,
        private val onSetMainClick: (Address) -> Unit,
        private val onEditClick: (Address) -> Unit
    ) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {
        
        inner class AddressViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvStreet: TextView = view.findViewById(R.id.tvStreet)
            val tvCityProvince: TextView = view.findViewById(R.id.tvCityProvince)
            val mainAddressIndicator: LinearLayout = view.findViewById(R.id.mainAddressIndicator)
            val btnSetAsMain: Button = view.findViewById(R.id.btnSetAsMain)
            val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
            val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_address, parent, false)
            return AddressViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
            val address = addresses[position]
            
            // Log all address data for debugging
            Log.d(TAG, "Address #${position}: ID=${address.addressId}, " +
                "postalCode=${address.postalCode}, " +
                "zipCode=${address.zipCode}, " +
                "street=${address.street}, " +
                "streetName=${address.streetName}, " +
                "barangay=${address.barangay}, " +
                "main=${address.main}")
            
            // Use the correct field depending on which one is populated
            val displayStreet = when {
                !address.streetName.isNullOrEmpty() -> address.streetName
                !address.street.isBlank() -> address.street
                else -> "No street specified"
            }
            
            // Add barangay to the street display if available
            val streetWithBarangay = if (!address.barangay.isNullOrEmpty()) {
                "$displayStreet, ${address.barangay}"
            } else {
                displayStreet
            }
            
            // Direct approach to get ZIP code
            var displayZipCode = "No ZIP code specified"
            
            if (!address.postalCode.isNullOrBlank()) {
                displayZipCode = address.postalCode
                Log.d(TAG, "Using postalCode: ${address.postalCode}")
            } else if (!address.zipCode.isNullOrBlank()) {
                displayZipCode = address.zipCode
                Log.d(TAG, "Using zipCode: ${address.zipCode}")
            } else {
                // Check raw object values
                try {
                    val rawPostal = address.postalCode
                    val rawZip = address.zipCode
                    Log.d(TAG, "Raw postalCode='$rawPostal', rawZip='$rawZip'")
                } catch (e: Exception) {
                    Log.e(TAG, "Error accessing zip fields", e)
                }
            }
            
            holder.tvStreet.text = streetWithBarangay
            holder.tvCityProvince.text = "${address.city}, ${address.province}, $displayZipCode"
            
            // Show/hide the main address indicator and apply yellow border
            if (address.main) {
                holder.mainAddressIndicator.visibility = View.VISIBLE
                holder.btnSetAsMain.visibility = View.GONE
                // Change the background of the card to have a yellow border
                (holder.itemView as androidx.cardview.widget.CardView).setCardBackgroundColor(android.graphics.Color.WHITE)
                holder.itemView.background = ContextCompat.getDrawable(requireContext(), R.drawable.main_address_card_background)
                // Hide delete button for main address
                holder.btnDelete.visibility = View.GONE
            } else {
                holder.mainAddressIndicator.visibility = View.GONE
                holder.btnSetAsMain.visibility = View.VISIBLE
                // Reset the background
                (holder.itemView as androidx.cardview.widget.CardView).setCardBackgroundColor(android.graphics.Color.WHITE)
                holder.itemView.background = null
                // Show delete button for non-main addresses
                holder.btnDelete.visibility = View.VISIBLE
            }
            
            // Use custom edit and delete icons
            holder.btnEdit.setImageResource(R.drawable.ic_edit)
            holder.btnDelete.setImageResource(R.drawable.ic_delete)
            
            // Set as main button click listener
            holder.btnSetAsMain.setOnClickListener {
                onSetMainClick(address)
            }
            
            // Edit button click listener
            holder.btnEdit.setOnClickListener {
                onEditClick(address)
            }
            
            // Delete button click listener
            holder.btnDelete.setOnClickListener {
                onDeleteClick(address)
            }
        }
        
        override fun getItemCount() = addresses.size
    }
    
    companion object {
        @JvmStatic
        fun newInstance(userId: Long, token: String) =
            ServiceProviderAddressFragment().apply {
                arguments = Bundle().apply {
                    putLong("userId", userId)
                    putString("token", token)
                }
            }
        
        // Add backward compatibility method with providerId parameter
        @JvmStatic
        fun newInstance(userId: Long, token: String, providerId: Long) =
            ServiceProviderAddressFragment().apply {
                arguments = Bundle().apply {
                    putLong("userId", userId)
                    putString("token", token)
                    // providerId will be determined dynamically now
                }
            }
    }
}