package com.example.serbisyo_it342_g3

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.adapters.CustomerServiceAdapter
import com.example.serbisyo_it342_g3.api.ServiceApiClient
import com.example.serbisyo_it342_g3.data.Service
import com.example.serbisyo_it342_g3.data.ServiceCategory

class CustomerDashboardActivity : AppCompatActivity() {
    private lateinit var tvWelcome: TextView
    private lateinit var tvNoServices: TextView
    private lateinit var rvServices: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var spinnerCategories: Spinner
    private lateinit var serviceAdapter: CustomerServiceAdapter
    
    private val services = mutableListOf<Service>()
    private val filteredServices = mutableListOf<Service>()
    private val allServices = mutableListOf<Service>()
    private val categories = mutableListOf<ServiceCategory>()
    private lateinit var serviceApiClient: ServiceApiClient
    private var token: String = ""
    private val TAG = "CustomerDashboard"
    
    // Special category for "All Categories"
    private val ALL_CATEGORIES = ServiceCategory(0, "All Categories")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_dashboard)

        // Initialize ServiceApiClient with context
        serviceApiClient = ServiceApiClient(this)
        
        // Get SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        token = sharedPref.getString("token", "") ?: ""
        
        Log.d(TAG, "Retrieved token: $token")

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome)
        tvNoServices = findViewById(R.id.tvNoServices)
        rvServices = findViewById(R.id.rvServices)
        progressBar = findViewById(R.id.progressBar)
        spinnerCategories = findViewById(R.id.spinnerCategories)

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Customer Dashboard"

        // Get username from shared preferences
        val username = sharedPref.getString("username", "Customer")

        // Set welcome message
        tvWelcome.text = "Welcome, $username!"

        // Setup RecyclerView
        serviceAdapter = CustomerServiceAdapter(
            filteredServices,
            onServiceClick = { service -> viewServiceDetails(service) }
        )
        rvServices.layoutManager = LinearLayoutManager(this)
        rvServices.adapter = serviceAdapter
        
        // Setup category spinner
        setupCategorySpinner()

        // Load services and categories
        loadCategories()
        loadServices()
    }
    
    private fun setupCategorySpinner() {
        // Add listener to spinner
        spinnerCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterServicesByCategory(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    private fun loadCategories() {
        progressBar.visibility = View.VISIBLE
        
        serviceApiClient.getServiceCategories(token) { categoryList, error ->
            if (error != null) {
                Log.e(TAG, "Error loading categories", error)
                return@getServiceCategories
            }
            
            runOnUiThread {
                categories.clear()
                // Add "All Categories" as first option
                categories.add(ALL_CATEGORIES)
                
                if (categoryList != null) {
                    categories.addAll(categoryList)
                }
                
                // Setup spinner with categories
                val categoryNames = categories.map { it.categoryName }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerCategories.adapter = adapter
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadServices() // Refresh services list when returning to this activity
    }

    private fun loadServices() {
        progressBar.visibility = View.VISIBLE
        tvNoServices.visibility = View.GONE
        
        serviceApiClient.getAllServices(token) { servicesList, error ->
            if (error != null) {
                Log.e(TAG, "Error loading services", error)
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    tvNoServices.visibility = View.VISIBLE
                    Toast.makeText(this, "Error loading services: ${error.message}", Toast.LENGTH_SHORT).show()
                }
                return@getAllServices
            }
            
            runOnUiThread {
                progressBar.visibility = View.GONE
                allServices.clear()
                if (servicesList != null && servicesList.isNotEmpty()) {
                    allServices.addAll(servicesList)
                    // Apply current filter
                    filterServicesByCategory(spinnerCategories.selectedItemPosition)
                } else {
                    tvNoServices.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private fun filterServicesByCategory(position: Int) {
        filteredServices.clear()
        
        if (position == 0 || position >= categories.size) {
            // "All Categories" selected or invalid position
            filteredServices.addAll(allServices)
        } else {
            // Filter by selected category
            val selectedCategory = categories[position]
            filteredServices.addAll(allServices.filter { 
                it.category?.categoryId == selectedCategory.categoryId 
            })
        }
        
        // Update UI
        serviceAdapter.notifyDataSetChanged()
        if (filteredServices.isEmpty()) {
            tvNoServices.visibility = View.VISIBLE
            tvNoServices.text = "No services available for the selected category"
        } else {
            tvNoServices.visibility = View.GONE
        }
    }

    private fun viewServiceDetails(service: Service) {
        // This would navigate to a service details activity
        Toast.makeText(this, "Selected service: ${service.serviceName}", Toast.LENGTH_SHORT).show()
        
        // TODO: Create a ServiceDetailsActivity and navigate to it
        // val intent = Intent(this, ServiceDetailsActivity::class.java)
        // intent.putExtra("SERVICE_ID", service.serviceId)
        // startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            R.id.action_profile -> {
                // Navigate to profile edit screen
                val intent = Intent(this, CustomerProfileEditActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        // Clear user session data
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }

        // Navigate to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}