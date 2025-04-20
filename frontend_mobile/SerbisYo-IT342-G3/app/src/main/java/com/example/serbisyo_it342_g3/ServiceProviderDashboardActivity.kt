package com.example.serbisyo_it342_g3

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.adapters.ServiceAdapter
import com.example.serbisyo_it342_g3.api.ServiceApiClient
import com.example.serbisyo_it342_g3.data.Service
import android.content.SharedPreferences
import android.util.Log
import android.widget.FrameLayout
import com.example.serbisyo_it342_g3.fragments.ChatsFragment
import com.example.serbisyo_it342_g3.fragments.NotificationsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.serbisyo_it342_g3.api.NotificationApiClient

class ServiceProviderDashboardActivity : AppCompatActivity() {
    private lateinit var tvWelcome: TextView
    private lateinit var btnAddService: Button
    private lateinit var rvServices: RecyclerView
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var dashboardContent: androidx.core.widget.NestedScrollView
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var fabAddService: FloatingActionButton

    private val services = mutableListOf<Service>()
    private lateinit var serviceApiClient: ServiceApiClient
    private lateinit var notificationApiClient: NotificationApiClient
    private var providerId: Long = 0
    private var token: String = ""
    private val TAG = "ProviderDashboard"
    private var unreadNotificationsCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_provider_dashboard)

        // Initialize ServiceApiClient with context
        serviceApiClient = ServiceApiClient(this)

        // Initialize NotificationApiClient
        notificationApiClient = NotificationApiClient(this)

        // Get SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        token = sharedPreferences.getString("token", "") ?: ""

        Log.d(TAG, "Retrieved token: $token")

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome)
        btnAddService = findViewById(R.id.btnAddService)
        rvServices = findViewById(R.id.rvServices)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        dashboardContent = findViewById(R.id.dashboardContent)
        fragmentContainer = findViewById(R.id.fragmentContainer)
        fabAddService = findViewById(R.id.fabAddService)

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

        // Check for notifications
        checkForNotifications()

        // Add service button click
        btnAddService.setOnClickListener {
            val intent = Intent(this, AddServiceActivity::class.java)
            intent.putExtra("PROVIDER_ID", providerId)
            startActivity(intent)
        }
        
        // Add service FAB click
        fabAddService.setOnClickListener {
            val intent = Intent(this, AddServiceActivity::class.java)
            intent.putExtra("PROVIDER_ID", providerId)
            startActivity(intent)
        }
        
        // Setup Bottom Navigation
        setupBottomNavigation()

        // Add after onCreate method, just before any existing methods
        checkProviderId()
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Show the dashboard content and hide fragment container
                    dashboardContent.visibility = View.VISIBLE
                    fragmentContainer.visibility = View.GONE
                    fabAddService.visibility = View.VISIBLE
                    supportActionBar?.title = "Service Provider Dashboard"
                    true
                }
                R.id.navigation_chat -> {
                    // Load ChatsFragment and hide dashboard content
                    loadFragment(ChatsFragment.newInstance())
                    dashboardContent.visibility = View.GONE
                    fragmentContainer.visibility = View.VISIBLE
                    fabAddService.visibility = View.GONE
                    supportActionBar?.title = "Chats"
                    true
                }
                R.id.navigation_notifications -> {
                    // Load NotificationsFragment and hide dashboard content
                    loadFragment(NotificationsFragment.newInstance())
                    dashboardContent.visibility = View.GONE
                    fragmentContainer.visibility = View.VISIBLE
                    fabAddService.visibility = View.GONE
                    supportActionBar?.title = "Notifications"
                    true
                }
                R.id.navigation_profile -> {
                    // Launch ServiceProviderProfileActivity
                    val intent = Intent(this, ServiceProviderProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        loadServices() // Refresh services list when returning to this activity
        checkForNotifications() // Check for new notifications
        // Ensure home tab is selected when returning to this activity
        bottomNavigation.selectedItemId = R.id.navigation_home
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

    private fun checkForNotifications() {
        val userId = getUserId() // Get the provider's user ID, not the provider ID
        
        if (userId <= 0) {
            Log.e(TAG, "Invalid user ID: $userId. Cannot check for notifications.")
            return // Skip if we don't have a valid user ID
        }
        
        if (token.isBlank()) {
            Log.e(TAG, "Auth token is blank. Cannot check for notifications.")
            return // Skip if we don't have a valid token
        }
        
        Log.d(TAG, "Checking for notifications for user ID: $userId")
        
        notificationApiClient.getNotificationsByUserId(userId, token) { notifications, error ->
            if (error != null) {
                Log.e(TAG, "Error checking notifications: ${error.message}", error)
                return@getNotificationsByUserId
            }
            
            if (notifications != null) {
                Log.d(TAG, "Retrieved ${notifications.size} notifications")
                
                // Count unread notifications
                unreadNotificationsCount = notifications.count { !it.isRead }
                Log.d(TAG, "Unread notifications: $unreadNotificationsCount")
                
                runOnUiThread {
                    // Update badge if needed
                    if (unreadNotificationsCount > 0) {
                        // Find the notifications menu item and add a badge
                        val badge = bottomNavigation.getOrCreateBadge(R.id.navigation_notifications)
                        badge.isVisible = true
                        badge.number = unreadNotificationsCount
                        Log.d(TAG, "Updated notification badge: $unreadNotificationsCount")
                    } else {
                        // Remove badge if there are no unread notifications
                        bottomNavigation.removeBadge(R.id.navigation_notifications)
                        Log.d(TAG, "Removed notification badge (no unread notifications)")
                    }
                }
            } else {
                Log.d(TAG, "No notifications returned (null)")
            }
        }
    }

    // Add a helper method to get the user ID (not provider ID)
    private fun getUserId(): Long {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPref.getString("userId", "0")?.toLongOrNull() ?: 0
        Log.d(TAG, "Retrieving user ID: $userId")
        return userId
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
                bottomNavigation.selectedItemId = R.id.navigation_profile
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

    private fun checkProviderId() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPref.getString("userId", "0")?.toLongOrNull() ?: 0
        val username = sharedPref.getString("username", "")
        val role = sharedPref.getString("role", "")
        val providerId = sharedPref.getString("providerId", "0")?.toLongOrNull() ?: 0
        
        Log.d(TAG, "User details - UserId: $userId, Username: $username, Role: $role, ProviderId: $providerId")
        
        // Make sure the providerId is stored in SharedPreferences for later use
        if (providerId == 0L && userId > 0) {
            Log.w(TAG, "Provider ID is not set. Attempting to retrieve it from user profile.")
            // Here you could make an API call to get the provider profile if needed
        }
    }
}