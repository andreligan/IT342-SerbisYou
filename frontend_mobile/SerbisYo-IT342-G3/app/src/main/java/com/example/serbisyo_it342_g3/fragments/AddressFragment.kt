package com.example.serbisyo_it342_g3.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.api.AddressApiClient
import com.example.serbisyo_it342_g3.api.PSGCApiClient
import com.example.serbisyo_it342_g3.data.Address
import com.example.serbisyo_it342_g3.data.Barangay
import com.example.serbisyo_it342_g3.data.Municipality
import com.example.serbisyo_it342_g3.data.Province
import androidx.core.content.ContextCompat

class AddressFragment : Fragment() {
    private val TAG = "AddressFragment"
    
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
    private lateinit var headerText: TextView
    private lateinit var subHeaderText: TextView
    private lateinit var savedAddressesSection: LinearLayout
    
    private lateinit var addressApiClient: AddressApiClient
    private lateinit var psgcApiClient: PSGCApiClient
    private var token: String = ""
    private var userId: Long = 0
    private var addresses = mutableListOf<Address>()
    private lateinit var addressAdapter: AddressAdapter
    
    // Edit mode variables
    private var isEditMode = false
    private var editingAddressId: Long? = null
    
    // Lists for the spinners
    private val provinces = mutableListOf<Province>()
    private val municipalities = mutableListOf<Municipality>()
    private val barangays = mutableListOf<Barangay>()
    
    // Selected items
    private var selectedProvince: Province? = null
    private var selectedMunicipality: Municipality? = null
    private var selectedBarangay: Barangay? = null
    
    // Placeholders for empty spinner selections
    private val selectProvinceText = "Select Province"
    private val selectCityText = "Select City/Municipality"
    private val selectBarangayText = "Select Barangay"
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_address, container, false)
        
        // Get arguments
        arguments?.let {
            userId = it.getLong("userId", 0)
            token = it.getString("token", "") ?: ""
        }
        
        // Initialize the API clients
        addressApiClient = AddressApiClient(requireContext())
        psgcApiClient = PSGCApiClient(requireContext())
        
        // Initialize views
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
        headerText = view.findViewById(R.id.headerText)
        subHeaderText = view.findViewById(R.id.subHeaderText)
        savedAddressesSection = view.findViewById(R.id.savedAddressesSection)
        
        // Setup header
        headerText.text = "My Addresses"
        subHeaderText.text = "Manage your saved addresses"
        
        // Setup recycler view
        rvAddresses.layoutManager = LinearLayoutManager(context)
        addressAdapter = AddressAdapter(
            addresses,
            onDeleteClick = { address -> deleteAddress(address.addressId?.toInt() ?: 0) },
            onSetMainClick = { address -> setAddressAsMain(address) },
            onEditClick = { address -> editAddress(address) }
        )
        rvAddresses.adapter = addressAdapter
        
        // Setup empty spinners initially
        setupEmptySpinners()
        
        // Setup spinner listeners
        setupSpinnerListeners()
        
        // Load addresses
        loadAddresses()
        
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
        // Province spinner listener
        spinnerProvince.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Clear subsequent dropdowns when province changes
                municipalities.clear()
                barangays.clear()
                updateMunicipalitySpinner()
                updateBarangaySpinner()
                
                // Disable subsequent dropdowns until data is loaded
                spinnerCity.isEnabled = false
                spinnerBarangay.isEnabled = false
                
                // Only load municipalities if a real province is selected (not the placeholder)
                if (position > 0) {
                    selectedProvince = provinces[position - 1] // -1 because of placeholder
                    selectedProvince?.let {
                        loadMunicipalities(it.code)
                    }
                } else {
                    selectedProvince = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedProvince = null
                municipalities.clear()
                barangays.clear()
                updateMunicipalitySpinner()
                updateBarangaySpinner()
                spinnerCity.isEnabled = false
                spinnerBarangay.isEnabled = false
            }
        }
        
        // Municipality spinner listener
        spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Clear barangay dropdown when city changes
                barangays.clear()
                updateBarangaySpinner()
                
                // Disable barangay dropdown until data is loaded
                spinnerBarangay.isEnabled = false
                
                // Only load barangays if a real municipality is selected (not the placeholder)
                if (position > 0 && municipalities.isNotEmpty()) {
                    selectedMunicipality = municipalities[position - 1] // -1 because of placeholder
                    selectedMunicipality?.let {
                        // Log the municipality code to verify it's correct
                        Log.d(TAG, "Selected municipality: ${it.name} with code: ${it.code}")
                        loadBarangays(it.code)
                    }
                } else {
                    selectedMunicipality = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedMunicipality = null
                barangays.clear()
                updateBarangaySpinner()
                spinnerBarangay.isEnabled = false
            }
        }
        
        // Barangay spinner listener
        spinnerBarangay.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Only set selected barangay if a real barangay is selected (not the placeholder)
                if (position > 0 && barangays.isNotEmpty()) {
                    selectedBarangay = barangays[position - 1] // -1 because of placeholder
                } else {
                    selectedBarangay = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedBarangay = null
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
                } else {
                    Log.e(TAG, "No provinces returned")
                    Toast.makeText(context, "No provinces available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun loadMunicipalities(provinceCode: String) {
        progressBar.visibility = View.VISIBLE
        
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
                    updateMunicipalitySpinner()
                    
                    // Enable the city spinner now that we have data
                    spinnerCity.isEnabled = true
                    
                    Log.d(TAG, "Loaded ${municipalities.size} municipalities for province $provinceCode")
                } else {
                    Log.e(TAG, "No municipalities returned for province $provinceCode")
                    Toast.makeText(context, "No cities/municipalities available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun loadBarangays(municipalityCode: String) {
        progressBar.visibility = View.VISIBLE
        
        Log.d(TAG, "Loading barangays for municipality: $municipalityCode")
        
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
                } else {
                    Log.e(TAG, "No barangays returned for municipality $municipalityCode")
                    Toast.makeText(context, "No barangays available for this city/municipality", Toast.LENGTH_SHORT).show()
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
    
    private fun updateMunicipalitySpinner() {
        val displayItems = mutableListOf(selectCityText)
        
        if (municipalities.isNotEmpty()) {
            displayItems.addAll(municipalities.map { it.name })
        }
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, displayItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCity.adapter = adapter
        spinnerCity.setSelection(0) // Reset to placeholder
    }
    
    private fun updateBarangaySpinner() {
        val displayItems = mutableListOf(selectBarangayText)
        
        if (barangays.isNotEmpty()) {
            displayItems.addAll(barangays.map { it.name })
        }
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, displayItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBarangay.adapter = adapter
        spinnerBarangay.setSelection(0) // Reset to placeholder
    }
    
    private fun loadAddresses() {
        progressBar.visibility = View.VISIBLE
        
        addressApiClient.getAddressesByUserId(userId, token) { addressList, error -> 
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error loading addresses", error)
                    Toast.makeText(context, "Error loading addresses: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                addresses.clear()
                if (addressList != null && addressList.isNotEmpty()) {
                    addresses.addAll(addressList)
                    tvNoAddresses.visibility = View.GONE
                    rvAddresses.visibility = View.VISIBLE
                } else {
                    tvNoAddresses.visibility = View.VISIBLE
                    rvAddresses.visibility = View.GONE
                }
                
                addressAdapter.notifyDataSetChanged()
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (etStreet.text.toString().trim().isEmpty()) {
            etStreet.error = "Street cannot be empty"
            isValid = false
        }
        
        if (selectedProvince == null) {
            Toast.makeText(context, "Please select a province", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        if (selectedMunicipality == null) {
            Toast.makeText(context, "Please select a city/municipality", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        if (selectedBarangay == null) {
            Toast.makeText(context, "Please select a barangay", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        if (etZipCode.text.toString().trim().isEmpty()) {
            etZipCode.error = "Zip Code cannot be empty"
            isValid = false
        }
        
        return isValid
    }
    
    private fun saveAddress() {
        progressBar.visibility = View.VISIBLE
        
        val street = etStreet.text.toString().trim()
        val barangay = selectedBarangay?.name ?: ""
        val city = selectedMunicipality?.name ?: ""
        val province = selectedProvince?.name ?: ""
        val zipCode = etZipCode.text.toString().trim()
        
        // Log the address being saved
        Log.d(TAG, "Saving new address - street=$street, barangay=$barangay, city=$city, province=$province, zipCode=$zipCode")
        
        val newAddress = Address(
            street = street,
            barangay = barangay,
            city = city,
            province = province,
            postalCode = zipCode,
            // Also set zipCode field to ensure it's available
            zipCode = zipCode
        )
        
        addressApiClient.addAddress(userId, newAddress, token) { success, error -> 
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    // Show detailed error message
                    Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Address save error: ${error.message}")
                    return@runOnUiThread
                }
                
                if (success) {
                    Toast.makeText(context, "Address saved successfully", Toast.LENGTH_SHORT).show()
                    // Clear inputs
                    etStreet.text.clear()
                    etZipCode.text.clear()
                    
                    // Reset spinners
                    resetSpinners()
                    
                    // Reload addresses
                    loadAddresses()
                } else {
                    Toast.makeText(context, "Failed to save address", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun resetSpinners() {
        // Reset all selections
        spinnerProvince.setSelection(0)
        
        // Clear municipalities and barangays
        municipalities.clear()
        barangays.clear()
        
        // Update and disable dependent spinners
        updateMunicipalitySpinner()
        updateBarangaySpinner()
        spinnerCity.isEnabled = false
        spinnerBarangay.isEnabled = false
        
        // Clear selections
        selectedProvince = null
        selectedMunicipality = null
        selectedBarangay = null
    }
    
    private fun deleteAddress(addressId: Int) {
        progressBar.visibility = View.VISIBLE
        
        addressApiClient.deleteAddress(addressId, token) { success, error -> 
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Toast.makeText(context, "Error deleting address: ${error.message}", Toast.LENGTH_SHORT).show()
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
        progressBar.visibility = View.VISIBLE

        // Create a copy of the address with main set to true
        val updatedAddress = address.copy(main = true)

        addressApiClient.updateAddressMainStatus(updatedAddress.addressId?.toInt() ?: 0, true, token) { success, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE

                if (error != null) {
                    Toast.makeText(context, "Error setting as main address: ${error}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                if (success) {
                    Toast.makeText(context, "Main address updated successfully", Toast.LENGTH_SHORT).show()
                    // Reload addresses to reflect changes
                    loadAddresses()
                } else {
                    Toast.makeText(context, "Failed to set as main address", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun editAddress(address: Address) {
        // Get the address ID
        val addressId = address.addressId?.toInt() ?: 0
        if (addressId == 0) {
            Toast.makeText(context, "Cannot edit this address", Toast.LENGTH_SHORT).show()
            return
        }
        
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
                    Toast.makeText(context, "Failed to load cities for editing", Toast.LENGTH_SHORT).show()
                }
                return@getMunicipalitiesByProvince
            }
            
            municipalities.clear()
            municipalities.addAll(municipalityList.sortedBy { it.name })
            spinnerCity.isEnabled = true
            
            // Find matching city - try exact match first, then fuzzy match
            var matchingCity = municipalities.find { it.name.equals(address.city, ignoreCase = true) }
            
            // If no exact match, try to find by contains
            if (matchingCity == null) {
                matchingCity = municipalities.find { 
                    it.name.contains(address.city, ignoreCase = true) || 
                    address.city.contains(it.name, ignoreCase = true) 
                }
                
                if (matchingCity != null) {
                    Log.d(TAG, "Found city by fuzzy match: ${matchingCity.name} for ${address.city}")
                }
            }
            
            if (matchingCity == null) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Could not find matching city", Toast.LENGTH_SHORT).show()
                    updateMunicipalitySpinner()
                }
                return@getMunicipalitiesByProvince
            }
            
            selectedMunicipality = matchingCity
            
            // Update UI on main thread
            requireActivity().runOnUiThread {
                updateMunicipalitySpinner()
                
                // Select the city in the spinner (add 1 for placeholder)
                val cityIndex = findMunicipalityIndex(matchingCity)
                if (cityIndex > 0) {
                    spinnerCity.setSelection(cityIndex)
                    
                    // Now load barangays for the selected city
                    loadBarangaysForEdit(matchingCity.code, address)
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Could not select city in dropdown", Toast.LENGTH_SHORT).show()
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
            spinnerBarangay.isEnabled = true
            
            // If barangay is empty, we can't match it
            if (address.barangay.isNullOrEmpty()) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    updateBarangaySpinner()
                    // Just enable the spinner for selection
                    spinnerBarangay.setSelection(0)
                }
                return@getBarangaysByMunicipality
            }
            
            // Find matching barangay - try exact match first, then fuzzy match
            var matchingBarangay = barangays.find { it.name.equals(address.barangay, ignoreCase = true) }
            
            // If no exact match, try to find by contains
            if (matchingBarangay == null) {
                matchingBarangay = barangays.find { 
                    it.name.contains(address.barangay, ignoreCase = true) || 
                    address.barangay.contains(it.name, ignoreCase = true) 
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
                }
                return@getBarangaysByMunicipality
            }
            
            selectedBarangay = matchingBarangay
            
            // Update UI on main thread
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                updateBarangaySpinner()
                
                // Select the barangay in the spinner (add 1 for placeholder)
                val barangayIndex = findBarangayIndex(matchingBarangay)
                if (barangayIndex > 0) {
                    spinnerBarangay.setSelection(barangayIndex)
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Could not select barangay in dropdown", Toast.LENGTH_SHORT).show()
                }
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

    private fun updateAddress() {
        progressBar.visibility = View.VISIBLE
        
        val street = etStreet.text.toString().trim()
        val barangay = selectedBarangay?.name ?: ""
        val city = selectedMunicipality?.name ?: ""
        val province = selectedProvince?.name ?: ""
        val zipCode = etZipCode.text.toString().trim()
        
        // Log the address being updated
        Log.d(TAG, "Updating address - street=$street, barangay=$barangay, city=$city, province=$province, zipCode=$zipCode")
        
        val updatedAddress = Address(
            addressId = editingAddressId,
            street = street,
            barangay = barangay,
            city = city,
            province = province,
            postalCode = zipCode,
            zipCode = zipCode
        )
        
        addressApiClient.updateAddress(updatedAddress, token) { success, error -> 
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    // Show detailed error message
                    Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Address update error: ${error.message}")
                    return@runOnUiThread
                }
                
                if (success) {
                    Toast.makeText(context, "Address updated successfully", Toast.LENGTH_SHORT).show()
                    // Clear inputs
                    etStreet.text.clear()
                    etZipCode.text.clear()
                    
                    // Reset spinners
                    resetSpinners()
                    
                    // Reload addresses
                    loadAddresses()
                } else {
                    Toast.makeText(context, "Failed to update address", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun resetForm() {
        // Reset all form fields
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
                "postalCode='${address.postalCode}', " +
                "zipCode='${address.zipCode}', " +
                "street='${address.street}', " +
                "streetName='${address.streetName}', " +
                "barangay='${address.barangay}', " +
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
            var displayZipCode = ""
            
            if (!address.postalCode.isNullOrBlank()) {
                displayZipCode = address.postalCode
                Log.d(TAG, "Using postalCode: ${address.postalCode}")
            } else if (!address.zipCode.isNullOrBlank()) {
                displayZipCode = address.zipCode
                Log.d(TAG, "Using zipCode: ${address.zipCode}")
            } else {
                displayZipCode = "No ZIP code specified"
                // Debug log the raw values
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
}