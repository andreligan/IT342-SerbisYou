package com.example.serbisyo_it342_g3

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.AlertDialog
import android.content.Context
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
import com.example.serbisyo_it342_g3.api.UserApiClient
import com.example.serbisyo_it342_g3.utils.ImageUtils

class ServiceProviderDashboardActivity : AppCompatActivity() {
    private lateinit var tvProviderName: TextView
    private lateinit var btnAddService: Button
    private lateinit var btnAddServiceHero: Button
    private lateinit var btnManageServices: Button
    private lateinit var btnManageBookings: Button
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
    private lateinit var userApiClient: UserApiClient
    private var providerId: Long = 0
    private var token: String = ""
    private val TAG = "ProviderDashboard"
    private var unreadNotificationsCount = 0
    private var userId: Long = 0
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_provider_dashboard)

        // Initialize UI elements
        initViews()
        
        // Setup RecyclerView
        rvServices.layoutManager = LinearLayoutManager(this)
        
        // Initialize ServiceApiClient
        serviceApiClient = ServiceApiClient(this)
        
        // Initialize NotificationApiClient
        notificationApiClient = NotificationApiClient(this)
        
        // Initialize UserApiClient with context
        userApiClient = UserApiClient(this)
        
        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Service Provider Dashboard"
        
        // Get SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        
        // Get user data from SharedPreferences
        userId = try {
            sharedPreferences.getLong("userId", 0)
        } catch (e: ClassCastException) {
            val userIdStr = sharedPreferences.getString("userId", "0")
            userIdStr?.toLongOrNull() ?: 0
        }
        
        username = sharedPreferences.getString("username", "") ?: ""
        token = sharedPreferences.getString("token", "") ?: ""
        
        Log.d(TAG, "Retrieved token: $token")
        
        // Check both SharedPreferences for provider ID
        providerId = try {
            val id1 = sharedPreferences.getLong("providerId", 0)
            val id2 = userPrefs.getLong("providerId", 0)
            
            if (id1 > 0) id1 else if (id2 > 0) id2 else 0
        } catch (e: ClassCastException) {
            try {
                val idStr1 = sharedPreferences.getString("providerId", "0") ?: "0"
                val idStr2 = userPrefs.getString("providerId", "0") ?: "0"
                
                val id1 = idStr1.toLongOrNull() ?: 0
                val id2 = idStr2.toLongOrNull() ?: 0
                
                if (id1 > 0) id1 else if (id2 > 0) id2 else 0
            } catch (e: Exception) {
                0
            }
        }
        
        // DON'T set providerId = userId - this causes issues with service loading
        // Instead, we'll get the correct provider ID in checkProviderId()
        
        // Set welcome message
        tvProviderName.text = username

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

        // Check provider ID first, then load services in the callback when we have the correct ID
        checkProviderId()
        
        // Setup "Manage All" link
        tvManageAll.setOnClickListener {
            navigateToMyServices()
        }

        // Check for notifications
        checkForNotifications()
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
        btnManageBookings = findViewById(R.id.btnManageBookings)
        
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
            navigateToMyServices()
        }
        
        // Manage bookings button click
        btnManageBookings.setOnClickListener {
            navigateToBookings()
        }
    }
    
    // Helper function to navigate to the My Services tab
    private fun navigateToMyServices() {
        val intent = Intent(this, ServiceProviderProfileManagementActivity::class.java)
        intent.putExtra("SELECTED_TAB", ServiceProviderProfileManagementActivity.SERVICES_TAB)
        startActivity(intent)
    }
    
    // Helper function to navigate to the bookings management screen
    private fun navigateToBookings() {
        val intent = Intent(this, ProviderBookingsActivity::class.java)
        intent.putExtra("PROVIDER_ID", providerId)
        startActivity(intent)
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
        
        // Check provider ID again when resuming the activity
        // This ensures we pick up any provider ID set by fragments
        checkProviderId()
        
        // Also check for notifications
        checkForNotifications()
        
        // If the user returned from adding/editing a service, refresh the list
        loadServices()
        
        // Resume slideshow animation
        startSlideshow()
        
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
        // Check if we have a valid provider ID first
        if (providerId <= 0) {
            Log.e(TAG, "Cannot load services: Provider ID is not valid ($providerId)")
            runOnUiThread {
                // Removed confusing toast message - we'll just show the UI for "no services" instead
                tvServiceCount.text = "0"
                findViewById<TextView>(R.id.tvNoServices).visibility = View.VISIBLE
                findViewById<HorizontalScrollView>(R.id.servicesScrollView).visibility = View.GONE
            }
            return
        }
        
        Log.d(TAG, "Loading services for provider ID: $providerId")
        serviceApiClient.getServicesByProviderId(providerId, token) { servicesList, error -> 
            if (error != null) {
                Log.e(TAG, "Error loading services", error)
                runOnUiThread {
                    // Keep this toast for actual errors, but not 404 errors
                    if (error.message?.contains("404") != true) {
                        Toast.makeText(this, "Error loading services: ${error.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                    }
                    // Don't try to show services if there was an error
                    tvServiceCount.text = "0"
                    findViewById<TextView>(R.id.tvNoServices).visibility = View.VISIBLE
                    findViewById<HorizontalScrollView>(R.id.servicesScrollView).visibility = View.GONE
                }
                return@getServicesByProviderId
            }

            runOnUiThread {
                services.clear()
                if (servicesList != null) {
                    // Double check that these services actually belong to this provider
                    val filteredServices = servicesList.filter { 
                        it.provider?.providerId == providerId 
                    }
                    
                    // Only add services that match this provider ID
                    services.addAll(filteredServices)
                    
                    // Update service count in dashboard
                    tvServiceCount.text = filteredServices.size.toString()
                    
                    // Populate the horizontal services container
                    populateServicesHorizontally(filteredServices)
                } else {
                    // Set service count to 0 if servicesList is null
                    tvServiceCount.text = "0"
                }
                
                // Update the original RecyclerView (kept for backward compatibility)
                serviceAdapter.notifyDataSetChanged()

                if (services.isEmpty()) {
                    findViewById<TextView>(R.id.tvNoServices).visibility = View.VISIBLE
                    findViewById<HorizontalScrollView>(R.id.servicesScrollView).visibility = View.GONE
                    rvServices.visibility = View.GONE
                } else {
                    findViewById<TextView>(R.id.tvNoServices).visibility = View.GONE
                    findViewById<HorizontalScrollView>(R.id.servicesScrollView).visibility = View.VISIBLE
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
        
        // Add proper layout parameters for consistent display
        val layoutParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.service_card_width),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.marginEnd = resources.getDimensionPixelSize(R.dimen.service_card_margin)
        cardView.layoutParams = layoutParams
        
        // Find views in card
        val serviceImage = cardView.findViewById<ImageView>(R.id.ivServiceImage)
        val serviceName = cardView.findViewById<TextView>(R.id.tvServiceName)
        val serviceCategory = cardView.findViewById<TextView>(R.id.tvCategory)
        val servicePrice = cardView.findViewById<TextView>(R.id.tvPrice)
        val btnEdit = cardView.findViewById<ImageButton>(R.id.btnEdit)
        val btnDelete = cardView.findViewById<ImageButton>(R.id.btnDelete)
        val btnAddImage = cardView.findViewById<Button>(R.id.btnAddImage)
        val tvNoImage = cardView.findViewById<TextView>(R.id.tvNoImage)
        
        // Hide the "ADD IMAGE" button - we don't need it in dashboard
        btnAddImage.visibility = View.GONE
        
        // Set service data
        serviceName.text = service.serviceName
        serviceCategory.text = service.category?.categoryName ?: "Uncategorized"
        
        // Format price to have a currency symbol and show it more prominently
        val formattedPrice = if (service.effectivePrice.isNotEmpty()) {
            if (service.effectivePrice.contains("-")) {
                // For price ranges
                "₱${service.effectivePrice}"
            } else {
                // For single prices
                "₱${service.effectivePrice}"
            }
        } else {
            "₱0"
        }
        servicePrice.text = formattedPrice
        // Set price text color to a green color like in the frontend_web
        servicePrice.setTextColor(ContextCompat.getColor(this, R.color.primary_green))
        
        // Set the duration estimate
        val tvDuration = cardView.findViewById<TextView>(R.id.tvDuration)
        val formattedDuration = if (service.durationEstimate.isNotEmpty()) {
            if (service.durationEstimate.endsWith("hrs") || service.durationEstimate.endsWith("hours") || 
                service.durationEstimate.contains("hour") || service.durationEstimate.contains("hr")) {
                service.durationEstimate
            } else {
                "${service.durationEstimate} hours"
            }
        } else {
            "1 hour"
        }
        tvDuration.text = formattedDuration
        
        // Set the service description
        val tvDescription = cardView.findViewById<TextView>(R.id.tvDescription)
        tvDescription.text = service.serviceDescription
        
        // Always hide the "No Image" text
        tvNoImage.visibility = View.GONE
        serviceImage.visibility = View.VISIBLE
        
        // Check if service has an image URL and use ImageUtils to load it
        if (!service.imageUrl.isNullOrEmpty()) {
            Log.d(TAG, "Loading image from URL: ${service.imageUrl}")
            
            // Get the full image URL
            val fullImageUrl = ImageUtils.getFullImageUrl(service.imageUrl, this)
            if (fullImageUrl != null) {
                // Load image using ImageUtils
                ImageUtils.loadImageAsync(fullImageUrl, serviceImage)
            } else {
                // Fallback to category image if URL processing fails
                val imageResource = getCategoryImage(service.category?.categoryName)
                serviceImage.setImageResource(imageResource)
            }
        } else {
            // If no image URL, use a category-based placeholder
            Log.d(TAG, "No image URL, using placeholder for category: ${service.category?.categoryName}")
            val imageResource = getCategoryImage(service.category?.categoryName)
            serviceImage.setImageResource(imageResource)
        }
        
        // Set category color (applying to the category TextView background color)
        // Instead of replacing the background drawable, we're just tinting the existing background
        val drawable = serviceCategory.background.mutate()
        drawable.setTint(getCategoryColor(service.category?.categoryName))
        serviceCategory.background = drawable
        
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
        intent.putExtra("PRICE_RANGE", service.effectivePrice)
        intent.putExtra("DURATION_ESTIMATE", service.durationEstimate)
        startActivity(intent)
    }

    private fun deleteService(service: Service) {
        serviceApiClient.deleteService(service.serviceId, token) { success, error -> 
            if (error != null) {
                Log.e(TAG, "Error deleting service", error)
                runOnUiThread {
                    Toast.makeText(this, "Error deleting service: ${error.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
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
        
        if (token.isEmpty()) {
            Log.e(TAG, "Auth token is blank. Cannot check for notifications.")
            return // Skip if we don't have a valid token
        }
        
        Log.d(TAG, "Checking for notifications for user ID: $userId")
        
        notificationApiClient.getNotificationsByUserId(userId, token) { notifications, error -> 
            if (error != null) {
                Log.e(TAG, "Error checking notifications: ${error.message ?: ""}", error)
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

    private fun getUserId(): Long {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = try {
            // Try to get as Long first (new format)
            sharedPref.getLong("userId", 0)
        } catch (e: ClassCastException) {
            // If that fails, try the String format (old format) and convert
            sharedPref.getString("userId", "0")?.toLongOrNull() ?: 0
        }
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
        val userPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        
        // Fix the type mismatch by using getLong instead of getString for userId
        val userId = try {
            // Try to get as Long first (new format)
            sharedPref.getLong("userId", 0)
        } catch (e: ClassCastException) {
            // If that fails, try the String format (old format) and convert
            sharedPref.getString("userId", "0")?.toLongOrNull() ?: 0
        }
        
        val username = sharedPref.getString("username", "") ?: ""
        val role = sharedPref.getString("role", "") ?: ""
        
        // IMPORTANT: For debugging - clear any existing provider ID to ensure we get a fresh one from the API
        with(sharedPref.edit()) {
            remove("providerId")
            apply()
        }
        
        with(userPref.edit()) {
            remove("providerId")
            apply()
        }
        
        // After clearing, check if we somehow still have a providerId (from old SharedPreferences)
        var providerId = 0L

        Log.d(TAG, "User details - UserId: $userId, Username: $username, Role: $role, ProviderId: $providerId")
        
        // Always prioritize getting the provider ID from the API - more reliable
        if (userId > 0) {
            Log.d(TAG, "Retrieving provider ID from API for user ID: $userId")
            
            // Attempt to get provider profile from API using userId
            userApiClient.getServiceProviderByAuthId(userId, token) { provider, error -> 
                if (error != null) {
                    Log.e(TAG, "Error getting provider profile", error)
                    // Show error message instead of using fallback
                    runOnUiThread {
                        Toast.makeText(
                            this@ServiceProviderDashboardActivity, 
                            "Could not find your service provider account. Please contact support.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@getServiceProviderByAuthId
                }
                
                if (provider != null && provider.providerId != null) {
                    val newProviderId = provider.providerId
                    Log.d(TAG, "Retrieved provider ID from API: $newProviderId")
                    
                    // Save to both SharedPreferences
                    with(sharedPref.edit()) {
                        putLong("providerId", newProviderId)
                        apply()
                    }
                    
                    with(userPref.edit()) {
                        putLong("providerId", newProviderId)
                        apply()
                    }
                    
                    // Update the class-level providerId variable
                    runOnUiThread {
                        this.providerId = newProviderId
                        
                        // Reload services with the correct provider ID
                        loadServices()
                    }
                } else {
                    Log.e(TAG, "Could not retrieve provider ID from user profile API call")
                    runOnUiThread {
                        Toast.makeText(
                            this@ServiceProviderDashboardActivity, 
                            "No service provider account was found. Please contact support.",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Show empty dashboard
                        tvServiceCount.text = "0"
                        findViewById<TextView>(R.id.tvNoServices).visibility = View.VISIBLE
                        findViewById<HorizontalScrollView>(R.id.servicesScrollView).visibility = View.GONE
                    }
                    // Don't continue to loadServices() if we don't have a valid provider ID
                }
            }
        } else {
            Log.e(TAG, "UserId is invalid or missing")
            runOnUiThread {
                Toast.makeText(
                    this@ServiceProviderDashboardActivity, 
                    "User account information is missing. Please login again.",
                    Toast.LENGTH_LONG
                ).show()
                
                // Show empty dashboard
                tvServiceCount.text = "0"
                findViewById<TextView>(R.id.tvNoServices).visibility = View.VISIBLE
                findViewById<HorizontalScrollView>(R.id.servicesScrollView).visibility = View.GONE
            }
        }
    }
}