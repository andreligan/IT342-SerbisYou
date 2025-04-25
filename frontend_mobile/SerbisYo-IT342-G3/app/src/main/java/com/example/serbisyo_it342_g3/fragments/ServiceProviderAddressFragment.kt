package com.example.serbisyo_it342_g3.fragments

import android.content.Context
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
import com.example.serbisyo_it342_g3.api.UserApiClient
import com.example.serbisyo_it342_g3.data.Address
import com.example.serbisyo_it342_g3.data.Barangay
import com.example.serbisyo_it342_g3.data.Municipality
import com.example.serbisyo_it342_g3.data.Province
import com.example.serbisyo_it342_g3.data.ServiceProvider

class ServiceProviderAddressFragment : Fragment() {
    private val TAG = "SPAddressFragment"
    
    // UI components
    private lateinit var etStreet: EditText
    private lateinit var spinnerProvince: Spinner
    private lateinit var spinnerCity: Spinner
    private lateinit var spinnerBarangay: Spinner
    private lateinit var etZipCode: EditText
    private lateinit var btnSaveAddress: Button
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
            providerId = it.getLong("providerId", 0)
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
        
        // Load addresses
        loadAddresses()
        
        // Load provinces for initial spinner
        loadProvinces()
        
        // Set save button click listener
        btnSaveAddress.setOnClickListener {
            if (validateInputs()) {
                saveAddress()
            }
        }
        
        return view
    }
    
    private fun initializeViews(view: View) {
        etStreet = view.findViewById(R.id.etStreet)
        spinnerProvince = view.findViewById(R.id.spinnerProvince)
        spinnerCity = view.findViewById(R.id.spinnerCity)
        spinnerBarangay = view.findViewById(R.id.spinnerBarangay)
        etZipCode = view.findViewById(R.id.etZipCode)
        btnSaveAddress = view.findViewById(R.id.btnSaveAddress)
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
                } else {
                    tvNoAddresses.visibility = View.VISIBLE
                    rvAddresses.visibility = View.GONE
                }
                
                addressAdapter.notifyDataSetChanged()
            }
        }
    }
    
    private fun saveAddress() {
        progressBar.visibility = View.VISIBLE
        
        val street = etStreet.text.toString().trim()
        val barangay = selectedBarangay?.name ?: ""
        val city = selectedMunicipality?.name ?: ""
        val province = selectedProvince?.name ?: ""
        val zipCode = etZipCode.text.toString().trim()
        
        val newAddress = Address(
            street = street,
            barangay = barangay,
            city = city,
            province = province,
            postalCode = zipCode
        )
        
        addressApiClient.addServiceProviderAddress(providerId, newAddress, token) { success, error -> 
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
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
        spinnerProvince.setSelection(0)
        spinnerCity.setSelection(0)
        spinnerBarangay.setSelection(0)
        spinnerCity.isEnabled = false
        spinnerBarangay.isEnabled = false
        selectedProvince = null
        selectedMunicipality = null
        selectedBarangay = null
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
        // Populate the form with address data
        etStreet.setText(address.street)
        etZipCode.setText(address.postalCode)
        
        // We need to find and select the matching province, city, and barangay
        // This requires loading the data in sequence
        
        // Start by loading provinces
        psgcApiClient.getProvinces { provinceList, error ->
            if (error != null || provinceList == null) {
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Failed to load provinces for editing", Toast.LENGTH_SHORT).show()
                }
                return@getProvinces
            }
            
            provinces.clear()
            provinces.addAll(provinceList.sortedBy { it.name })
            
            // Find matching province
            val matchingProvince = provinces.find { it.name == address.province }
            if (matchingProvince == null) {
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Could not find matching province", Toast.LENGTH_SHORT).show()
                    updateProvinceSpinner()
                }
                return@getProvinces
            }
            
            selectedProvince = matchingProvince
            
            // Update UI on main thread
            requireActivity().runOnUiThread {
                updateProvinceSpinner()
                
                // Select the province in the spinner
                val provinceIndex = provinces.indexOf(matchingProvince) + 1 // +1 for the placeholder
                spinnerProvince.setSelection(provinceIndex)
                
                // Now load municipalities for the selected province
                loadMunicipalitiesForEdit(matchingProvince.code, address)
            }
        }
    }
    
    private fun loadMunicipalitiesForEdit(provinceCode: String, address: Address) {
        psgcApiClient.getMunicipalitiesByProvince(provinceCode) { municipalityList, error ->
            if (error != null || municipalityList == null) {
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Failed to load cities for editing", Toast.LENGTH_SHORT).show()
                }
                return@getMunicipalitiesByProvince
            }
            
            municipalities.clear()
            municipalities.addAll(municipalityList.sortedBy { it.name })
            
            // Find matching city
            val matchingCity = municipalities.find { it.name == address.city }
            if (matchingCity == null) {
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Could not find matching city", Toast.LENGTH_SHORT).show()
                    updateCitySpinner()
                    spinnerCity.isEnabled = true
                }
                return@getMunicipalitiesByProvince
            }
            
            selectedMunicipality = matchingCity
            
            // Update UI on main thread
            requireActivity().runOnUiThread {
                updateCitySpinner()
                spinnerCity.isEnabled = true
                
                // Select the city in the spinner
                val cityIndex = municipalities.indexOf(matchingCity) + 1 // +1 for the placeholder
                spinnerCity.setSelection(cityIndex)
                
                // Now load barangays for the selected city
                loadBarangaysForEdit(matchingCity.code, address)
            }
        }
    }
    
    private fun loadBarangaysForEdit(municipalityCode: String, address: Address) {
        psgcApiClient.getBarangaysByMunicipality(municipalityCode) { barangayList, error ->
            if (error != null || barangayList == null) {
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Failed to load barangays for editing", Toast.LENGTH_SHORT).show()
                }
                return@getBarangaysByMunicipality
            }
            
            barangays.clear()
            barangays.addAll(barangayList.sortedBy { it.name })
            
            // Find matching barangay
            val matchingBarangay = barangays.find { it.name == address.barangay }
            if (matchingBarangay == null) {
                requireActivity().runOnUiThread {
                    Toast.makeText(context, "Could not find matching barangay", Toast.LENGTH_SHORT).show()
                    updateBarangaySpinner()
                    spinnerBarangay.isEnabled = true
                }
                return@getBarangaysByMunicipality
            }
            
            selectedBarangay = matchingBarangay
            
            // Update UI on main thread
            requireActivity().runOnUiThread {
                updateBarangaySpinner()
                spinnerBarangay.isEnabled = true
                
                // Select the barangay in the spinner
                val barangayIndex = barangays.indexOf(matchingBarangay) + 1 // +1 for the placeholder
                spinnerBarangay.setSelection(barangayIndex)
                
                // Change button text to indicate editing
                btnSaveAddress.text = "Update Address"
            }
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
            
            // Use the correct field depending on which one is populated
            val displayStreet = when {
                !address.streetName.isNullOrEmpty() -> address.streetName
                !address.street.isBlank() -> address.street
                else -> "No street specified"
            }
            
            // Use the correct zipcode field depending on which one is populated
            val displayZipCode = when {
                address.postalCode.isNotEmpty() -> address.postalCode
                address.zipCode != null && address.zipCode.isNotEmpty() -> address.zipCode
                else -> "No ZIP code specified"
            }
            
            holder.tvStreet.text = displayStreet
            holder.tvCityProvince.text = "${address.city}, ${address.province} $displayZipCode"
            
            // Show/hide the main address indicator
            if (address.main) {
                holder.mainAddressIndicator.visibility = View.VISIBLE
                holder.btnSetAsMain.visibility = View.GONE
            } else {
                holder.mainAddressIndicator.visibility = View.GONE
                holder.btnSetAsMain.visibility = View.VISIBLE
            }
            
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