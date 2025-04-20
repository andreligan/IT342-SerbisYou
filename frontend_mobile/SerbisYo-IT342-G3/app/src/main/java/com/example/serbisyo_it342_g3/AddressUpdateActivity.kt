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
import com.example.serbisyo_it342_g3.api.Barangay
import com.example.serbisyo_it342_g3.api.City
import com.example.serbisyo_it342_g3.api.PSGCApiClient
import com.example.serbisyo_it342_g3.api.Province
import com.example.serbisyo_it342_g3.model.Address
import com.example.serbisyo_it342_g3.utils.PreferenceManager
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class AddressUpdateActivity : AppCompatActivity() {
    private val TAG = "AddressUpdateActivity"
    
    private lateinit var spinnerProvince: Spinner
    private lateinit var spinnerCity: Spinner
    private lateinit var spinnerBarangay: Spinner
    private lateinit var etStreetAddress: TextInputEditText
    private lateinit var etPostalCode: TextInputEditText
    private lateinit var btnUpdateAddress: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var addressApiClient: AddressApiClient
    private lateinit var psgcApiClient: PSGCApiClient
    private lateinit var sharedPrefs: SharedPreferences
    
    private var provinceList: List<Province> = emptyList()
    private var cityList: List<City> = emptyList()
    private var barangayList: List<Barangay> = emptyList()
    
    private var userId: Int = 0
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
        
        // Initialize API clients and preference manager
        preferenceManager = PreferenceManager(this)
        addressApiClient = AddressApiClient(this)
        psgcApiClient = PSGCApiClient(this)
        
        // Initialize shared preferences
        sharedPrefs = getSharedPreferences("serbisyo_prefs", MODE_PRIVATE)
        
        // Get user info from preferences
        userToken = preferenceManager.getString("token", "")
        userId = preferenceManager.getInt("user_id", 0)
        addressId = preferenceManager.getInt("address_id", 0)
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
        psgcApiClient.getProvinces(
            onSuccess = { provinces ->
                runOnUiThread {
                    provinceList = provinces
                    val provinceNames = provinces.map { it.name }
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, provinceNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerProvince.adapter = adapter
                    showLoading(false)
                }
            },
            onError = { errorMessage ->
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this, "Error loading provinces: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    private fun loadCities(provinceCode: String) {
        showLoading(true)
        psgcApiClient.getCitiesByProvince(
            provinceCode = provinceCode,
            onSuccess = { cities ->
                runOnUiThread {
                    cityList = cities
                    val cityNames = cities.map { it.name }
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cityNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCity.adapter = adapter
                    showLoading(false)
                }
            },
            onError = { errorMessage ->
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this, "Error loading cities: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    private fun loadBarangays(cityCode: String) {
        showLoading(true)
        psgcApiClient.getBarangaysByCity(
            cityCode = cityCode,
            onSuccess = { barangays ->
                runOnUiThread {
                    barangayList = barangays
                    val barangayNames = barangays.map { it.name }
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, barangayNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerBarangay.adapter = adapter
                    showLoading(false)
                }
            },
            onError = { errorMessage ->
                runOnUiThread {
                    showLoading(false)
                    Toast.makeText(this, "Error loading barangays: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    private fun fetchUserAddress() {
        if (addressId != null && addressId != 0) {
            showLoading(true)
            addressApiClient.getAddressById(
                addressId = addressId!!,
                token = userToken,
                callback = { address, error ->
                    if (address != null) {
                        runOnUiThread {
                            populateAddressFields(address)
                            showLoading(false)
                        }
                    } else {
                        runOnUiThread {
                            showLoading(false)
                            Toast.makeText(this, "Error loading address: $error", Toast.LENGTH_SHORT).show()
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
        
        // Province, city, and barangay will be populated when data is loaded
        // We'll need to match the values from the address with the loaded data
        // This is a bit complex since we need to wait for data to load
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
            province = selectedProvince.name,
            city = selectedCity.name,
            barangay = selectedBarangay.name,
            street = streetAddress,
            zipCode = postalCode
        )
        
        // Call API to update address
        val addrId = addressId ?: 0
        if (addrId > 0) {
            // Update existing address
            addressApiClient.updateAddress(
                addressId = addrId,
                address = address,
                token = userToken,
                callback = { success, errorMessage ->
                    runOnUiThread {
                        showLoading(false)
                        btnUpdateAddress.isEnabled = true
                        
                        if (success) {
                            Toast.makeText(this, "Address updated successfully", Toast.LENGTH_SHORT).show()
                            finish() // Close the activity
                        } else {
                            try {
                                val jsonError = errorMessage?.let { JSONObject(it) }
                                val message = jsonError?.optString("message", errorMessage) ?: errorMessage
                                Toast.makeText(this, "Error updating address: $message", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(this, "Error updating address: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                            
                            Log.e(TAG, "Error updating address: $errorMessage")
                        }
                    }
                }
            )
        } else {
            // Create new address
            addressApiClient.createAddress(
                address = address,
                token = userToken,
                callback = { newAddressId, errorMessage ->
                    runOnUiThread {
                        showLoading(false)
                        btnUpdateAddress.isEnabled = true
                        
                        if (newAddressId != null) {
                            // Save the address ID to preferences
                            preferenceManager.setInt("address_id", newAddressId)
                            
                            Toast.makeText(this, "Address created successfully", Toast.LENGTH_SHORT).show()
                            finish() // Close the activity
                        } else {
                            try {
                                val jsonError = errorMessage?.let { JSONObject(it) }
                                val message = jsonError?.optString("message", errorMessage) ?: errorMessage
                                Toast.makeText(this, "Error creating address: $message", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(this, "Error creating address: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                            
                            Log.e(TAG, "Error creating address: $errorMessage")
                        }
                    }
                }
            )
        }
    }
    
    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
} 