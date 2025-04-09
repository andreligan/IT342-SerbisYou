package com.example.serbisyo_it342_g3

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.adapters.ServiceAdapter
import com.example.serbisyo_it342_g3.api.ServiceApiClient
import com.example.serbisyo_it342_g3.data.Service
import android.content.SharedPreferences
import android.util.Log
import android.view.View

class ServiceProviderDashboardActivity : AppCompatActivity() {
    private lateinit var tvWelcome: TextView
    private lateinit var btnAddService: Button
    private lateinit var rvServices: RecyclerView
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var sharedPreferences: SharedPreferences

    private val services = mutableListOf<Service>()
    private lateinit var serviceApiClient: ServiceApiClient
    private var providerId: Long = 0
    private var token: String = ""
    private val TAG = "ProviderDashboard"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_provider_dashboard)

        // Initialize ServiceApiClient with context
        serviceApiClient = ServiceApiClient(this)

        // Get SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        token = sharedPreferences.getString("token", "") ?: ""

        Log.d(TAG, "Retrieved token: $token")

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome)
        btnAddService = findViewById(R.id.btnAddService)
        rvServices = findViewById(R.id.rvServices)

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Service Provider Dashboard"

        // Get user data from shared preferences
        val username = sharedPreferences.getString("username", "Provider")
        val userIdStr = sharedPreferences.getString("userId", "0")

        providerId = userIdStr?.toLongOrNull() ?: 0

        if (providerId == 0L) {
            Toast.makeText(this, "Error: Provider ID not found", Toast.LENGTH_LONG).show()
        }

        // Set welcome message
        tvWelcome.text = "Welcome, $username!"

        // Setup RecyclerView
        serviceAdapter = ServiceAdapter(
            services,
            onEditClick = { service -> editService(service) },
            onDeleteClick = { service -> deleteService(service) }
        )
        rvServices.layoutManager = LinearLayoutManager(this)
        rvServices.adapter = serviceAdapter

        // Load services
        loadServices()

        // Add service button click
        btnAddService.setOnClickListener {
            val intent = Intent(this, AddServiceActivity::class.java)
            intent.putExtra("PROVIDER_ID", providerId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadServices() // Refresh services list when returning to this activity
    }

    private fun loadServices() {
        serviceApiClient.getServicesByProviderId(providerId, token) { servicesList, error ->
            if (error != null) {
                Log.e(TAG, "Error loading services", error)
                runOnUiThread {
                    Toast.makeText(this, "Error loading services: ${error.message}", Toast.LENGTH_SHORT).show()
                }
                return@getServicesByProviderId
            }

            runOnUiThread {
                services.clear()
                if (servicesList != null) {
                    services.addAll(servicesList)
                }
                serviceAdapter.notifyDataSetChanged()

                if (services.isEmpty()) {
                    Toast.makeText(this, "No services found. Add your first service!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun editService(service: Service) {
        val intent = Intent(this, EditServiceActivity::class.java)
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
            if (error != null) {
                Log.e(TAG, "Error deleting service", error)
                runOnUiThread {
                    Toast.makeText(this, "Error deleting service: ${error.message}", Toast.LENGTH_SHORT).show()
                }
                return@deleteService
            }

            runOnUiThread {
                if (success) {
                    services.remove(service)
                    serviceAdapter.notifyDataSetChanged()
                    Toast.makeText(this, "Service deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to delete service", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
                startActivity(Intent(this, ServiceProviderProfileActivity::class.java))
                true
            }
            R.id.action_delete_all_services -> {
                //showDeleteAllServicesConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        // Clear user session data
        with(sharedPreferences.edit()) {
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