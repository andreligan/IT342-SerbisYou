package com.example.serbisyo_it342_g3

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.serbisyo_it342_g3.api.AddressApiClient
import com.example.serbisyo_it342_g3.api.PSGCApiClient
import com.example.serbisyo_it342_g3.data.Barangay
import com.example.serbisyo_it342_g3.data.Municipality
import com.example.serbisyo_it342_g3.data.Province
import com.example.serbisyo_it342_g3.model.Address
import com.google.android.material.textfield.TextInputEditText

class AddressUpdateActivity : AppCompatActivity() {
    private val TAG = "AddressUpdateActivity"
    
    private lateinit var spinnerProvince: Spinner
    private lateinit var spinnerCity: Spinner
    private lateinit var spinnerBarangay: Spinner
    private lateinit var etStreetAddress: TextInputEditText
    private lateinit var etPostalCode: TextInputEditText
    private lateinit var btnUpdateAddress: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var addressApiClient: AddressApiClient
    private lateinit var psgcApiClient: PSGCApiClient
    private lateinit var sharedPrefs: SharedPreferences
    
    private var provinceList: List<Province> = emptyList()
    private var cityList: List<Municipality> = emptyList()
    private var barangayList: List<Barangay> = emptyList()
    
    private var userId: Long = 0
    private var userToken: String = ""
    private var addressId: Int? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_update)
        
        // Initialize views
        spinnerProvince = findViewById(R.id.spinnerProvince)
        spinnerCity = findViewById(R.id.spinnerCity)
        spinnerBarangay = findViewById(R.id.spinnerBarangay)
        etStreetAddress = findViewById(R.id.etStreetAddress)
        etPostalCode = findViewById(R.id.etPostalCode)
        btnUpdateAddress = findViewById(R.id.btnUpdateAddress)
        progressBar = findViewById(R.id.progressBar)
        
        // Initialize API clients
        addressApiClient = AddressApiClient(this)
        psgcApiClient = PSGCApiClient(this)
        
        // Initialize shared preferences
        sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        
        // Get user info from preferences
        userToken = sharedPrefs.getString("token", "") ?: ""
        val userIdStr = sharedPrefs.getString("userId", "0") ?: "0"
        userId = userIdStr.toLongOrNull() ?: 0
        
        // Get address ID from intent if available
        addressId = intent.getIntExtra("address_id", 0)
        if (addressId == 0) addressId = null
        
        // Fetch existing address if available
        if (addressId != null && addressId != 0) {
            fetchUserAddress()
        }
        
        // Setup spinners and their adapters
        setupSpinners()
        
        // Load provinces from PSGC API
        loadProvinces()
        
        // Set up button click listener
        btnUpdateAddress.setOnClickListener {
            if (validateInputs()) {
                updateAddress()
            }
        }
    }
    
    private fun setupSpinners() {
        // Setup province spinner
        spinnerProvince.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && provinceList.isNotEmpty()) {
                    val selectedProvince = provinceList[position]
                    loadCities(selectedProvince.code)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
        
        // Setup city spinner
        spinnerCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && cityList.isNotEmpty()) {
                    val selectedCity = cityList[position]
                    loadBarangays(selectedCity.code)
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    private fun loadProvinces() {
        showLoading(true)
        psgcApiClient.getProvinces { provinces, error ->
            runOnUiThread {
                showLoading(false)
                
                if (error != null) {
                    Toast.makeText(this, "Error loading provinces: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                if (provinces != null) {
                    provinceList = provinces.sortedBy { it.name }
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, provinceList)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerProvince.adapter = adapter
                }
            }
        }
    }
    
    private fun loadCities(provinceCode: String) {
        showLoading(true)
        psgcApiClient.getMunicipalitiesByProvince(provinceCode) { municipalities, error ->
            runOnUiThread {
                showLoading(false)
                
                if (error != null) {
                    Toast.makeText(this, "Error loading cities: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                if (municipalities != null) {
                    cityList = municipalities.sortedBy { it.name }
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cityList)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCity.adapter = adapter
                    
                    // Clear barangay spinner
                    barangayList = emptyList()
                    spinnerBarangay.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, emptyList<String>())
                }
            }
        }
    }
    
    private fun loadBarangays(cityCode: String) {
        showLoading(true)
        psgcApiClient.getBarangaysByMunicipality(cityCode) { barangays, error ->
            runOnUiThread {
                showLoading(false)
                
                if (error != null) {
                    Toast.makeText(this, "Error loading barangays: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                if (barangays != null) {
                    barangayList = barangays.sortedBy { it.name }
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, barangayList)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerBarangay.adapter = adapter
                }
            }
        }
    }
    
    private fun fetchUserAddress() {
        if (addressId != null && addressId != 0) {
            showLoading(true)
            addressApiClient.getAddressById(
                addressId = addressId!!,
                token = userToken,
                callback = { address, error ->
                    runOnUiThread {
                        showLoading(false)
                        
                        if (error != null) {
                            Toast.makeText(this, "Error loading address: $error", Toast.LENGTH_SHORT).show()
                            return@runOnUiThread
                        }
                        
                        if (address != null) {
                            populateAddressFields(address)
                        }
                    }
                }
            )
        }
    }
    
    private fun populateAddressFields(address: Address) {
        // Set the street address and postal code
        etStreetAddress.setText(address.street)
        etPostalCode.setText(address.zipCode)
        
        // We'll need to select the matching province, city, and barangay in the spinners
        // This will happen after the data is loaded
    }
    
    private fun validateInputs(): Boolean {
        if (spinnerProvince.selectedItemPosition < 0 || provinceList.isEmpty()) {
            Toast.makeText(this, "Please select a province", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (spinnerCity.selectedItemPosition < 0 || cityList.isEmpty()) {
            Toast.makeText(this, "Please select a city/municipality", Toast.LENGTH_SHORT).show()
            return false
        }
        
        if (spinnerBarangay.selectedItemPosition < 0 || barangayList.isEmpty()) {
            Toast.makeText(this, "Please select a barangay", Toast.LENGTH_SHORT).show()
            return false
        }
        
        val streetAddress = etStreetAddress.text.toString().trim()
        if (streetAddress.isEmpty()) {
            etStreetAddress.error = "Street address is required"
            etStreetAddress.requestFocus()
            return false
        }
        
        val postalCode = etPostalCode.text.toString().trim()
        if (postalCode.isEmpty()) {
            etPostalCode.error = "Postal/ZIP code is required"
            etPostalCode.requestFocus()
            return false
        }
        
        if (userToken.isEmpty()) {
            Toast.makeText(this, "Authentication error. Please log in again.", Toast.LENGTH_SHORT).show()
            return false
        }
        
        return true
    }
    
    private fun updateAddress() {
        if (!validateInputs()) return
        
        showLoading(true)
        btnUpdateAddress.isEnabled = false
        
        val selectedProvince = provinceList[spinnerProvince.selectedItemPosition]
        val selectedCity = cityList[spinnerCity.selectedItemPosition]
        val selectedBarangay = barangayList[spinnerBarangay.selectedItemPosition]
        val streetAddress = etStreetAddress.text.toString().trim()
        val postalCode = etPostalCode.text.toString().trim()
        
        // Create address object
        val address = Address(
            id = addressId ?: 0,
            street = streetAddress,
            city = selectedCity.name,
            province = selectedProvince.name,
            barangay = selectedBarangay.name,
            zipCode = postalCode
        )
        
        // Call API to update address
        if (addressId != null && addressId!! > 0) {
            // Update existing address
            addressApiClient.updateAddress(addressId!!, address, userToken) { success, error ->
                runOnUiThread {
                    showLoading(false)
                    btnUpdateAddress.isEnabled = true
                    
                    if (error != null) {
                        Toast.makeText(this, "Error updating address: $error", Toast.LENGTH_LONG).show()
                        Log.e(TAG, "Error updating address: $error")
                        return@runOnUiThread
                    }
                    
                    if (success) {
                        Toast.makeText(this, "Address updated successfully", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity
                    } else {
                        Toast.makeText(this, "Failed to update address", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            // Create new address
            addressApiClient.createAddress(address, userId, userToken) { newAddressId, error ->
                runOnUiThread {
                    showLoading(false)
                    btnUpdateAddress.isEnabled = true
                    
                    if (error != null) {
                        Toast.makeText(this, "Error creating address: $error", Toast.LENGTH_LONG).show()
                        Log.e(TAG, "Error creating address: $error")
                        return@runOnUiThread
                    }
                    
                    if (newAddressId != null) {
                        Toast.makeText(this, "Address created successfully", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity
                    } else {
                        Toast.makeText(this, "Failed to create address", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnUpdateAddress.isEnabled = !isLoading
    }
}