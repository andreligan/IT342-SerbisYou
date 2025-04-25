package com.example.serbisyo_it342_g3

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.adapters.ServiceAdapter
import com.example.serbisyo_it342_g3.api.ServiceApiClient
import com.example.serbisyo_it342_g3.data.Service
import android.content.SharedPreferences
import android.widget.FrameLayout
import com.example.serbisyo_it342_g3.fragments.ChatsFragment
import com.example.serbisyo_it342_g3.fragments.NotificationsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.serbisyo_it342_g3.api.NotificationApiClient

class ServiceProviderDashboardActivity : AppCompatActivity() {
    private lateinit var tvProviderName: TextView
    private lateinit var btnAddService: Button
    private lateinit var btnAddServiceHero: Button
    private lateinit var btnManageServices: Button
    private lateinit var rvServices: RecyclerView
    private lateinit var serviceAdapter: ServiceAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var dashboardContent: androidx.core.widget.NestedScrollView
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var categoryContainer: LinearLayout
    private lateinit var tvServiceCount: TextView
    private lateinit var tvBookingCount: TextView
    private lateinit var tvRating: TextView
    private lateinit var servicesContainer: LinearLayout
    private lateinit var tvManageAll: TextView
    
    // Slideshow components
    private lateinit var slideshowImage1: ImageView
    private lateinit var slideshowImage2: ImageView
    private lateinit var slideshowImage3: ImageView
    private lateinit var slideshowHandler: Handler
    private lateinit var slideshowRunnable: Runnable
    private var currentSlideshowImage = 0

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
        initViews()

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
        tvProviderName.text = username ?: "Provider"

        // Hide the vertical RecyclerView - we'll only use the horizontal layout
        rvServices.visibility = View.GONE

        // Setup ServiceAdapter (needed to maintain compatibility, but not really used now)
        serviceAdapter = ServiceAdapter(
            services,
            onEditClick = { service -> editService(service) },
            onDeleteClick = { service -> deleteService(service) }
        )
        
        // Setup buttons
        setupButtonListeners()
        
        // Setup Bottom Navigation
        setupBottomNavigation()

        // Setup slideshow
        setupSlideshow()
        
        // Setup category chips
        setupCategories()

        // Load services - these will populate the horizontal layout
        loadServices()

        // Check provider ID
        checkProviderId()
        
        // Setup "Manage All" link
        tvManageAll.setOnClickListener {
            val intent = Intent(this, ManageServicesActivity::class.java)
            intent.putExtra("PROVIDER_ID", providerId)
            startActivity(intent)
        }
    }
    
    private fun initViews() {
        // Find slideshow images
        slideshowImage1 = findViewById(R.id.slideshowImage1)
        slideshowImage2 = findViewById(R.id.slideshowImage2)
        slideshowImage3 = findViewById(R.id.slideshowImage3)
        
        // Find other views
        tvProviderName = findViewById(R.id.tvProviderName)
        tvServiceCount = findViewById(R.id.tvServiceCount)
        tvBookingCount = findViewById(R.id.tvBookingCount)
        tvRating = findViewById(R.id.tvRating)
        
        // Buttons
        btnAddService = findViewById(R.id.btnAddService)
        btnAddServiceHero = findViewById(R.id.btnAddServiceHero)
        btnManageServices = findViewById(R.id.btnManageServices)
        
        // Services container for horizontal layout
        servicesContainer = findViewById(R.id.servicesContainer)
        tvManageAll = findViewById(R.id.tvManageAll)
        
        // Core elements
        rvServices = findViewById(R.id.rvServices)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        dashboardContent = findViewById(R.id.dashboardContent)
        fragmentContainer = findViewById(R.id.fragmentContainer)
        
        // Category container
        categoryContainer = findViewById(R.id.categoryContainer)
    }
    
    private fun setupButtonListeners() {
        // Add service button click
        btnAddService.setOnClickListener {
            val intent = Intent(this, AddServiceActivity::class.java)
            intent.putExtra("PROVIDER_ID", providerId)
            startActivity(intent)
        }
        
        // Add service hero button click
        btnAddServiceHero.setOnClickListener {
            val intent = Intent(this, AddServiceActivity::class.java)
            intent.putExtra("PROVIDER_ID", providerId)
            startActivity(intent)
        }
        
        // Manage services button click
        btnManageServices.setOnClickListener {
            val intent = Intent(this, ManageServicesActivity::class.java)
            intent.putExtra("PROVIDER_ID", providerId)
            startActivity(intent)
        }
    }
    
    private fun setupSlideshow() {
        slideshowHandler = Handler(Looper.getMainLooper())
        slideshowRunnable = object : Runnable {
            override fun run() {
                changeSlideshowImage()
                slideshowHandler.postDelayed(this, 5000) // Change image every 5 seconds
            }
        }
        
        // Setup slideshow images with actual resources
        setupSlideshowImages()
        
        // Start the slideshow
        startSlideshow()
    }

    private fun setupSlideshowImages() {
        try {
            // Set the slideshow images from the drawable resources
            // Use the same resources as CustomerDashboardActivity
            slideshowImage1.setImageResource(R.drawable.cleaning)
            slideshowImage2.setImageResource(R.drawable.appliance_repair)
            slideshowImage3.setImageResource(R.drawable.carpentry)
            
            // Make sure the first image is visible and others are invisible
            slideshowImage1.visibility = View.VISIBLE
            slideshowImage1.alpha = 1f
            slideshowImage2.visibility = View.INVISIBLE
            slideshowImage3.visibility = View.INVISIBLE
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up slideshow images", e)
        }
    }

    private fun changeSlideshowImage() {
        try {
            // Get the current and next image views
            val currentImageView = when (currentSlideshowImage) {
                0 -> slideshowImage1
                1 -> slideshowImage2
                else -> slideshowImage3
            }
            
            // Update current image index
            currentSlideshowImage = (currentSlideshowImage + 1) % 3
            
            // Get the next image view
            val nextImageView = when (currentSlideshowImage) {
                0 -> slideshowImage1
                1 -> slideshowImage2
                else -> slideshowImage3
            }
            
            // Make next image visible but transparent
            nextImageView.alpha = 0f
            nextImageView.visibility = View.VISIBLE
            
            // Cross fade the images
            crossFadeImages(currentImageView, nextImageView)
        } catch (e: Exception) {
            Log.e(TAG, "Error changing slideshow image", e)
        }
    }
    
    private fun crossFadeImages(currentView: ImageView?, nextView: ImageView?) {
        if (currentView == null || nextView == null) return
        
        // Set the next view to 0% opacity and visible
        nextView.alpha = 0f
        nextView.visibility = View.VISIBLE
        
        // Animate the next view to 100% opacity
        nextView.animate()
            .alpha(1f)
            .setDuration(500)
            .setListener(null)
        
        // Animate the current view to 0% opacity
        currentView.animate()
            .alpha(0f)
            .setDuration(500)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentView.visibility = View.INVISIBLE
                }
            })
    }
    
    private fun setupCategories() {
        // Create sample category chips
        val categories = arrayOf("All", "Plumbing", "Electrical", "Cleaning", "Carpentry", "Painting")
        
        categoryContainer.removeAllViews() // Clear any existing views
        
        for (category in categories) {
            val categoryChip = createCategoryChip(category)
            categoryContainer.addView(categoryChip)
        }
    }
    
    private fun createCategoryChip(category: String): TextView {
        val chip = TextView(this)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(8, 0, 8, 0)
        chip.layoutParams = params
        
        chip.text = category
        chip.setPadding(24, 12, 24, 12)
        chip.setTextColor(ContextCompat.getColor(this, R.color.primary_green))
        chip.background = ContextCompat.getDrawable(this, R.drawable.category_chip_background)
        
        chip.setOnClickListener {
            // Handle category selection - filter services
            // filterServicesByCategory(category)
        }
        
        return chip
    }
    
    // ... existing methods ...
    
    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Show the dashboard content and hide fragment container
                    dashboardContent.visibility = View.VISIBLE
                    fragmentContainer.visibility = View.GONE
                    supportActionBar?.title = "Service Provider Dashboard"
                    true
                }
                R.id.navigation_chat -> {
                    // Load ChatsFragment and hide dashboard content
                    loadFragment(ChatsFragment.newInstance())
                    dashboardContent.visibility = View.GONE
                    fragmentContainer.visibility = View.VISIBLE
                    supportActionBar?.title = "Chats"
                    true
                }
                R.id.navigation_notifications -> {
                    // Load NotificationsFragment and hide dashboard content
                    loadFragment(NotificationsFragment.newInstance())
                    dashboardContent.visibility = View.GONE
                    fragmentContainer.visibility = View.VISIBLE
                    supportActionBar?.title = "Notifications"
                    true
                }
                R.id.navigation_profile -> {
                    // Launch ServiceProviderProfileManagementActivity instead of ServiceProviderProfileActivity
                    val intent = Intent(this, ServiceProviderProfileManagementActivity::class.java)
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
        startSlideshow() // Resume slideshow animation
        // Ensure home tab is selected when returning to this activity
        bottomNavigation.selectedItemId = R.id.navigation_home
    }
    
    override fun onPause() {
        super.onPause()
        stopSlideshow() // Pause slideshow when activity is not visible
    }
    
    private fun startSlideshow() {
        slideshowHandler.postDelayed(slideshowRunnable, 5000)
    }
    
    private fun stopSlideshow() {
        slideshowHandler.removeCallbacks(slideshowRunnable)
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
                    // Update service count in dashboard
                    tvServiceCount.text = servicesList.size.toString()
                    
                    // Populate the horizontal services container
                    populateServicesHorizontally(servicesList)
                }
                
                // Update the original RecyclerView (kept for backward compatibility)
                serviceAdapter.notifyDataSetChanged()

                if (services.isEmpty()) {
                    findViewById<TextView>(R.id.tvNoServices).visibility = View.VISIBLE
                    rvServices.visibility = View.GONE
                } else {
                    findViewById<TextView>(R.id.tvNoServices).visibility = View.GONE
                    rvServices.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun populateServicesHorizontally(servicesList: List<Service>) {
        runOnUiThread {
            // Always hide vertical RecyclerView
            rvServices.visibility = View.GONE
            
            // Clear existing views in services container
            servicesContainer.removeAllViews()
            
            if (servicesList.isEmpty()) {
                findViewById<TextView>(R.id.tvNoServices).visibility = View.VISIBLE
                findViewById<HorizontalScrollView>(R.id.servicesScrollView).visibility = View.GONE
            } else {
                findViewById<TextView>(R.id.tvNoServices).visibility = View.GONE
                findViewById<HorizontalScrollView>(R.id.servicesScrollView).visibility = View.VISIBLE
                
                // For each service, create a card and add to container
                for (service in servicesList) {
                    val serviceCard = createServiceCard(service)
                    servicesContainer.addView(serviceCard)
                }
            }
        }
    }

    private fun createServiceCard(service: Service): View {
        val inflater = LayoutInflater.from(this)
        val cardView = inflater.inflate(R.layout.item_service_card, servicesContainer, false)
        
        // Find views in card
        val serviceImage = cardView.findViewById<ImageView>(R.id.ivServiceImage)
        val serviceName = cardView.findViewById<TextView>(R.id.tvServiceName)
        val serviceCategory = cardView.findViewById<TextView>(R.id.tvServiceCategory)
        val servicePrice = cardView.findViewById<TextView>(R.id.tvServicePrice)
        val btnEdit = cardView.findViewById<ImageButton>(R.id.btnEdit)
        val btnDelete = cardView.findViewById<ImageButton>(R.id.btnDelete)
        val categoryColor = cardView.findViewById<View>(R.id.categoryColorIndicator)
        
        // Set service data
        serviceName.text = service.serviceName
        serviceCategory.text = service.category?.categoryName ?: "Uncategorized"
        servicePrice.text = service.priceRange
        
        // Set service image based on category
        val imageResource = getCategoryImage(service.category?.categoryName)
        serviceImage.setImageResource(imageResource)
        
        // Set category color
        categoryColor.setBackgroundColor(getCategoryColor(service.category?.categoryName))
        
        // Set click listeners for edit and delete
        btnEdit.setOnClickListener {
            editService(service)
        }
        
        btnDelete.setOnClickListener {
            showDeleteConfirmation(service)
        }
        
        return cardView
    }

    private fun showDeleteConfirmation(service: Service) {
        AlertDialog.Builder(this)
            .setTitle("Delete Service")
            .setMessage("Are you sure you want to delete ${service.serviceName}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteService(service)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getCategoryImage(categoryName: String?): Int {
        return when (categoryName) {
            "Cleaning" -> R.drawable.cleaning
            "Appliance Repair" -> R.drawable.appliance_repair
            "Carpentry" -> R.drawable.carpentry
            else -> R.drawable.cleaning // Default image
        }
    }

    private fun getCategoryColor(categoryName: String?): Int {
        return when (categoryName) {
            "Cleaning" -> ContextCompat.getColor(this, R.color.primary_green)
            "Appliance Repair" -> ContextCompat.getColor(this, R.color.primary_yellow)
            "Carpentry" -> ContextCompat.getColor(this, R.color.colorAccent)
            "Electrical" -> ContextCompat.getColor(this, R.color.teal_700)
            "Plumbing" -> ContextCompat.getColor(this, R.color.purple_700)
            else -> ContextCompat.getColor(this, R.color.primary_green) // Default color
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
                    // Remove service from the list
                    services.remove(service)
                    
                    // Update the horizontal services display
                    populateServicesHorizontally(services)
                    
                    // Update service count display
                    tvServiceCount.text = services.size.toString()
                    
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