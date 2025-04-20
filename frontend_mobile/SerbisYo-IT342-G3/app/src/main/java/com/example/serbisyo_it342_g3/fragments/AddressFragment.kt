package com.example.serbisyo_it342_g3.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.api.AddressApiClient
import com.example.serbisyo_it342_g3.data.Address

class AddressFragment : Fragment() {
    private val TAG = "AddressFragment"
    
    private lateinit var etStreet: EditText
    private lateinit var etBarangay: EditText
    private lateinit var etCity: EditText
    private lateinit var etProvince: EditText
    private lateinit var etZipCode: EditText
    private lateinit var btnSaveAddress: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var rvAddresses: RecyclerView
    private lateinit var tvNoAddresses: TextView
    private lateinit var headerText: TextView
    private lateinit var subHeaderText: TextView
    private lateinit var savedAddressesSection: LinearLayout
    
    private lateinit var addressApiClient: AddressApiClient
    private var token: String = ""
    private var userId: Long = 0
    private var addresses = mutableListOf<Address>()
    private lateinit var addressAdapter: AddressAdapter
    
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
        
        // Initialize the API client
        addressApiClient = AddressApiClient(requireContext())
        
        // Initialize views
        etStreet = view.findViewById(R.id.etStreet)
        etBarangay = view.findViewById(R.id.etBarangay)
        etCity = view.findViewById(R.id.etCity)
        etProvince = view.findViewById(R.id.etProvince)
        etZipCode = view.findViewById(R.id.etZipCode)
        btnSaveAddress = view.findViewById(R.id.btnSaveAddress)
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
        addressAdapter = AddressAdapter(addresses) { address ->
            deleteAddress(address.addressId?.toInt() ?: 0)
        }
        rvAddresses.adapter = addressAdapter
        
        // Load addresses
        loadAddresses()
        
        // Set save button click listener
        btnSaveAddress.setOnClickListener {
            if (validateInputs()) {
                saveAddress()
            }
        }
        
        return view
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
        
        if (etBarangay.text.toString().trim().isEmpty()) {
            etBarangay.error = "Barangay cannot be empty"
            isValid = false
        }
        
        if (etCity.text.toString().trim().isEmpty()) {
            etCity.error = "City cannot be empty"
            isValid = false
        }
        
        if (etProvince.text.toString().trim().isEmpty()) {
            etProvince.error = "Province cannot be empty"
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
        val barangay = etBarangay.text.toString().trim()
        val city = etCity.text.toString().trim()
        val province = etProvince.text.toString().trim()
        val zipCode = etZipCode.text.toString().trim()
        
        val newAddress = Address(
            street = street,
            barangay = barangay,
            city = city,
            province = province,
            postalCode = zipCode
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
                    etBarangay.text.clear()
                    etCity.text.clear()
                    etProvince.text.clear()
                    etZipCode.text.clear()
                    
                    // Reload addresses
                    loadAddresses()
                } else {
                    Toast.makeText(context, "Failed to save address", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
    
    // Adapter for displaying addresses
    inner class AddressAdapter(
        private val addresses: List<Address>,
        private val onDeleteClick: (Address) -> Unit
    ) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {
        
        inner class AddressViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvStreet: TextView = view.findViewById(R.id.tvStreet)
            val tvBarangay: TextView = view.findViewById(R.id.tvBarangay)
            val tvCity: TextView = view.findViewById(R.id.tvCity)
            val tvProvince: TextView = view.findViewById(R.id.tvProvince)
            val tvZipCode: TextView = view.findViewById(R.id.tvZipCode)
            val btnDelete: Button = view.findViewById(R.id.btnDelete)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_address, parent, false)
            return AddressViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
            val address = addresses[position]
            
            holder.tvStreet.text = address.street
            holder.tvBarangay.text = "Barangay: ${address.barangay}"
            holder.tvCity.text = "City: ${address.city}"
            holder.tvProvince.text = "Province: ${address.province}"
            holder.tvZipCode.text = "Zip Code: ${address.postalCode}"
            
            holder.btnDelete.setOnClickListener {
                onDeleteClick(address)
            }
        }
        
        override fun getItemCount() = addresses.size
    }
} 