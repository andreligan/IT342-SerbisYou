package com.example.serbisyo_it342_g3

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.adapters.ServiceAdapter
import com.example.serbisyo_it342_g3.api.ServiceApiClient
import com.example.serbisyo_it342_g3.data.Service

class ManageServicesActivity : AppCompatActivity() {
    private lateinit var rvServices: RecyclerView
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var tvNoServices: TextView
    private lateinit var progressBar: ProgressBar
    
    private val TAG = "ManageServices"
    private var providerId: Long = 0
    private var token: String = ""
    private val services = mutableListOf<Service>()
    private lateinit var serviceApiClient: ServiceApiClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_services)
        
        // Initialize ServiceApiClient
        serviceApiClient = ServiceApiClient(this)
        
        // Get providerId from intent
        providerId = intent.getLongExtra("PROVIDER_ID", 0)
        
        // Get token from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        token = sharedPreferences.getString("token", "") ?: ""
        
        // Set up back button in toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Manage Services"
        
        // Initialize views
        rvServices = findViewById(R.id.rvServices)
        tvNoServices = findViewById(R.id.tvNoServices)
        progressBar = findViewById(R.id.progressBar)
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Load services
        loadServices()
    }

    private fun setupRecyclerView() {
        serviceAdapter = ServiceAdapter(
            services,
            onEditClick = { service -> editService(service) },
            onDeleteClick = { service -> deleteService(service) }
        )
        
        rvServices.layoutManager = LinearLayoutManager(this)
        rvServices.adapter = serviceAdapter
    }

    private fun loadServices() {
        progressBar.visibility = View.VISIBLE
        tvNoServices.visibility = View.GONE
        
        serviceApiClient.getServicesByProviderId(providerId, token) { servicesList, error ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error loading services", error)
                    Toast.makeText(this, "Error loading services: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                services.clear()
                if (servicesList != null && servicesList.isNotEmpty()) {
                    services.addAll(servicesList)
                    rvServices.visibility = View.VISIBLE
                    tvNoServices.visibility = View.GONE
                } else {
                    rvServices.visibility = View.GONE
                    tvNoServices.visibility = View.VISIBLE
                }
                
                serviceAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun editService(service: Service) {
        // Navigate to edit service activity
        val intent = android.content.Intent(this, EditServiceActivity::class.java)
        intent.putExtra("SERVICE_ID", service.serviceId)
        intent.putExtra("PROVIDER_ID", providerId)
        intent.putExtra("CATEGORY_ID", service.category?.categoryId)
        intent.putExtra("SERVICE_NAME", service.serviceName)
        intent.putExtra("SERVICE_DESCRIPTION", service.serviceDescription)
        intent.putExtra("PRICE_RANGE", service.priceRange)
        intent.putExtra("DURATION_ESTIMATE", service.durationEstimate)
        startActivity(intent)
    }

    private fun deleteService(service: Service) {
        serviceApiClient.deleteService(service.serviceId, token) { success, error ->
            runOnUiThread {
                if (error != null) {
                    Log.e(TAG, "Error deleting service", error)
                    Toast.makeText(this, "Error deleting service: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                if (success) {
                    services.remove(service)
                    serviceAdapter.notifyDataSetChanged()
                    
                    // Show empty state if no services left
                    if (services.isEmpty()) {
                        rvServices.visibility = View.GONE
                        tvNoServices.visibility = View.VISIBLE
                    }
                    
                    Toast.makeText(this, "Service deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to delete service", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        // Reload services when returning to this activity
        loadServices()
    }
}