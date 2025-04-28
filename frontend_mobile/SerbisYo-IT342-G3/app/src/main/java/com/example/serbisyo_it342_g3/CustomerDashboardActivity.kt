package com.example.serbisyo_it342_g3

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.adapters.CustomerServiceAdapter
import com.example.serbisyo_it342_g3.api.BookingApiClient
import com.example.serbisyo_it342_g3.api.NotificationApiClient
import com.example.serbisyo_it342_g3.api.ScheduleApiClient
import com.example.serbisyo_it342_g3.api.ServiceApiClient
import com.example.serbisyo_it342_g3.api.UserApiClient
import com.example.serbisyo_it342_g3.data.Booking
import com.example.serbisyo_it342_g3.data.Service
import com.example.serbisyo_it342_g3.data.ServiceCategory
import com.example.serbisyo_it342_g3.fragments.BookingHistoryFragment
import com.example.serbisyo_it342_g3.fragments.ChatsFragment
import com.example.serbisyo_it342_g3.fragments.HomeFragment
import com.example.serbisyo_it342_g3.fragments.NotificationsFragment
import com.example.serbisyo_it342_g3.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.widget.FrameLayout
import android.widget.TextView
import android.net.ConnectivityManager

class CustomerDashboardActivity : AppCompatActivity(), NotificationsFragment.NotificationBadgeListener {
    // Existing view variables
    private lateinit var tvWelcome: TextView
    private lateinit var tvNoServices: TextView
    private lateinit var rvServices: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var spinnerCategories: Spinner
    private lateinit var categoriesContainer: LinearLayout
    private lateinit var serviceAdapter: CustomerServiceAdapter
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var dashboardContent: ScrollView
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var bookingApiClient: BookingApiClient
    private lateinit var ivProfileImage: ShapeableImageView
    
    // Hero section and slideshow elements
    private lateinit var btnHeroBookService: Button
    private lateinit var slideshowImage1: ImageView
    private lateinit var slideshowImage2: ImageView
    private lateinit var slideshowImage3: ImageView
    private var currentSlideshowImage = 0
    private val slideshowHandler = Handler(Looper.getMainLooper())
    private val slideshowRunnable = object : Runnable {
        override fun run() {
            changeSlideshowImage()
            slideshowHandler.postDelayed(this, SLIDESHOW_DELAY)
        }
    }
    
    private val services = mutableListOf<Service>()
    private val filteredServices = mutableListOf<Service>()
    private val allServices = mutableListOf<Service>()
    private val categories = mutableListOf<ServiceCategory>()
    private lateinit var serviceApiClient: ServiceApiClient
    private lateinit var userApiClient: UserApiClient
    private var token: String = ""
    private var userId: Long = 0
    private val TAG = "CustomerDashboard"
    
    // Slideshow constants
    private val SLIDESHOW_DELAY = 3000L // 3 seconds between slides
    private val FADE_DURATION = 500L // 0.5 second fade animation
    
    // Special category for "All Categories"
    private val ALL_CATEGORIES = ServiceCategory(0, "All Categories")

    // Add notification badge properties
    private lateinit var notificationApiClient: NotificationApiClient
    private var notificationBadge: TextView? = null
    private val notificationHandler = Handler(Looper.getMainLooper())
    private val notificationCheckRunnable = object : Runnable {
        override fun run() {
            checkForNotifications()
            notificationHandler.postDelayed(this, 60000) // Check every minute
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_dashboard)

        // Initialize ServiceApiClient with context
        serviceApiClient = ServiceApiClient(this)
        userApiClient = UserApiClient(this)
        notificationApiClient = NotificationApiClient(this)
        
        // Get SharedPreferences
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        token = sharedPref.getString("token", "") ?: ""
        
        // Fix userId retrieval using try-catch
        userId = try {
            // Try to get as Long first (new format)
            sharedPref.getLong("userId", 0)
        } catch (e: ClassCastException) {
            // If that fails, try the String format (old format) and convert
            val userIdStr = sharedPref.getString("userId", "0")
            userIdStr?.toLongOrNull() ?: 0
        }
        
        Log.d(TAG, "Retrieved token: $token")
        Log.d(TAG, "Retrieved user ID: $userId")

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome)
        tvNoServices = findViewById(R.id.tvNoServices)
        rvServices = findViewById(R.id.rvServices)
        progressBar = findViewById(R.id.progressBar)
        spinnerCategories = findViewById(R.id.spinnerCategories)
        bottomNavigation = findViewById(R.id.bottomNavigation)
        dashboardContent = findViewById(R.id.dashboardContent)
        fragmentContainer = findViewById(R.id.fragmentContainer)
        ivProfileImage = findViewById(R.id.ivProfileImage)

        // Initialize hero section and slideshow views
        btnHeroBookService = findViewById(R.id.btnHeroBookService)
        slideshowImage1 = findViewById(R.id.slideshowImage1)
        slideshowImage2 = findViewById(R.id.slideshowImage2)
        slideshowImage3 = findViewById(R.id.slideshowImage3)

        // Set up slideshow images
        setupSlideshowImages()

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "SerbisYo"

        // Get username from shared preferences
        val username = sharedPref.getString("username", "Customer")

        // Set welcome message
        tvWelcome.text = "Welcome, $username!"
        
        // Load profile image
        loadProfileImage()

        // Setup Book Service button in hero section
        setupBookServiceButtons()

        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup category spinner
        setupCategorySpinner()

        // Setup Bottom Navigation
        setupBottomNavigation()

        // Initialize BookingApiClient
        bookingApiClient = BookingApiClient(this)

        // Start slideshow
        startSlideshow()
        
        // Load data after views are set up
        loadData()

        // Start notification checking
        startNotificationChecking()
    }
    
    private fun loadProfileImage() {
        try {
            val prefs = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
            val savedImagePath = prefs.getString("profile_image_$userId", null)
            
            if (savedImagePath != null) {
                try {
                    // Check if this is a file path or content URI
                    if (savedImagePath.startsWith("/")) {
                        // It's a file path, use File directly
                        val file = File(savedImagePath)
                        if (file.exists() && file.length() > 0) {
                            ivProfileImage.setImageURI(Uri.fromFile(file))
                            return
                        }
                    }
                    
                    // If not a file or file doesn't exist, try as content URI
                    val uri = Uri.parse(savedImagePath)
                    ivProfileImage.setImageURI(uri)
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading saved profile image, using default", e)
                    ivProfileImage.setImageResource(R.drawable.default_profile)
                }
            } else {
                // Load customer data to check if they have a profile image
                if (userId > 0) {
                    userApiClient.getCustomerProfile(userId, token) { customer, error -> 
                        if (error != null || customer == null) {
                            Log.e(TAG, "Error getting customer profile for image", error)
                            return@getCustomerProfile
                        }
                        
                        if (!customer.profileImage.isNullOrEmpty()) {
                            try {
                                val profileImageFile = File(customer.profileImage)
                                if (profileImageFile.exists() && profileImageFile.length() > 0) {
                                    runOnUiThread {
                                        ivProfileImage.setImageURI(Uri.fromFile(profileImageFile))
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error loading profile image from customer data", e)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile image", e)
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Show the dashboard content and hide fragment container
                    dashboardContent.visibility = View.VISIBLE
                    fragmentContainer.visibility = View.GONE
                    supportActionBar?.title = "SerbisYo"
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
                    // Launch ProfileManagementActivity instead of just loading ProfileFragment
                    val intent = Intent(this, ProfileManagementActivity::class.java)
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
    
    private fun getUserId(): Long {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userIdStr = sharedPref.getString("userId", "0")
        return userIdStr?.toLongOrNull() ?: 0
    }
    
    private fun setupCategorySpinner() {
        // Add listener to spinner
        spinnerCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.d(TAG, "Category spinner selection changed to position $position")
                if (position == 0) {
                    // All Categories selected
                    showAllServices()
                } else {
                    filterServicesByCategory(position)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Default to showing all services
                showAllServices()
            }
        }
    }
    
    private fun applyServiceFilters() {
        // Apply current filter (if any spinner item is selected)
        val position = spinnerCategories.selectedItemPosition
        Log.d(TAG, "Applying service filters - current spinner position: $position")
        
        if (position > 0) {
            filterServicesByCategory(position)
        } else {
            // Otherwise show all services
            showAllServices()
        }
    }
    
    private fun loadCategories() {
        progressBar.visibility = View.VISIBLE
        
        serviceApiClient.getServiceCategories(token) { categoryList, error -> 
            runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error loading categories", error)
                    // Load services anyway even if categories fail
                    loadServices()
                    return@runOnUiThread
                }
                
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
                
                // Now load services after categories are loaded
                loadServices()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadServices() // Refresh services list when returning to this activity
        loadProfileImage() // Refresh profile image when returning to this activity
    }

    private fun loadData() {
        // First load categories, then load services
        loadCategories()
    }

    private fun loadServices() {
        progressBar.visibility = View.VISIBLE
        tvNoServices.visibility = View.GONE
        
        serviceApiClient.getAllServices(token) { servicesList, error -> 
            runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error loading services: ${error.message}", error)
                    Toast.makeText(this, "Error loading services: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                if (servicesList != null && servicesList.isNotEmpty()) {
                    // Store all services
                    allServices.clear()
                    allServices.addAll(servicesList)
                    
                    // Log services with images for debugging
                    Log.d(TAG, "Loaded ${servicesList.size} services")
                    servicesList.forEach { service ->
                        if (!service.imageUrl.isNullOrEmpty()) {
                            Log.d(TAG, "Service ${service.serviceName} has image: ${service.imageUrl}")
                        } else {
                            Log.d(TAG, "Service ${service.serviceName} has no image")
                        }
                    }
                    
                    // Display all services
                    filteredServices.clear()
                    filteredServices.addAll(allServices)
                    
                    // Update the UI
                    serviceAdapter.notifyDataSetChanged()
                    rvServices.visibility = View.VISIBLE
                    tvNoServices.visibility = View.GONE
                    
                    Toast.makeText(this, "Loaded ${servicesList.size} services", Toast.LENGTH_SHORT).show()
                } else {
                    filteredServices.clear()
                    serviceAdapter.notifyDataSetChanged()
                    
                    rvServices.visibility = View.GONE
                    tvNoServices.visibility = View.VISIBLE
                    tvNoServices.text = "No services available"
                }
            }
        }
    }
    
    private fun showAllServices() {
        filteredServices.clear()
        filteredServices.addAll(allServices)
        Log.d(TAG, "Showing ALL services: count = ${filteredServices.size}")
        
        // Log each service for debugging
        filteredServices.forEachIndexed { index, service ->
            Log.d(TAG, "Filtered service $index: ${service.serviceName} (ID: ${service.serviceId})")
        }
        
        serviceAdapter.notifyDataSetChanged()
        
        if (filteredServices.isEmpty()) {
            tvNoServices.visibility = View.VISIBLE
            rvServices.visibility = View.GONE
            tvNoServices.text = "No services available"
        } else {
            tvNoServices.visibility = View.GONE
            rvServices.visibility = View.VISIBLE
        }
    }
    
    private fun filterServicesByCategory(position: Int) {
        // Check if we have categories loaded
        if (categories.isEmpty() || position < 0 || position >= categories.size) {
            showAllServices()
            return
        }
        
        // Get the selected category
        val selectedCategory = categories[position]
        Log.d(TAG, "Filtering by category: ${selectedCategory.categoryName}")
        
        // Filter services by category
        filteredServices.clear()
        
        if (selectedCategory.categoryId == 0L) {
            // "All Categories" selected
            filteredServices.addAll(allServices)
            Log.d(TAG, "All categories selected, showing all ${allServices.size} services")
        } else {
            // Filter by specific category
            val filtered = allServices.filter { service -> 
                service.category?.categoryId == selectedCategory.categoryId
            }
            filteredServices.addAll(filtered)
            Log.d(TAG, "Category filter applied: found ${filteredServices.size} services in ${selectedCategory.categoryName}")
        }
        
        serviceAdapter.notifyDataSetChanged()
        
        if (filteredServices.isEmpty()) {
            tvNoServices.visibility = View.VISIBLE
            rvServices.visibility = View.GONE
            tvNoServices.text = "No services found in category: ${selectedCategory.categoryName}"
        } else {
            tvNoServices.visibility = View.GONE
            rvServices.visibility = View.VISIBLE
        }
    }
    
    private fun viewServiceDetails(service: Service) {
        // Ensure API URL is correctly initialized
        ensureApiUrlIsSet()
        
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_service_details)
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        
        // Provider information
        val tvProviderName = dialog.findViewById<TextView>(R.id.tvProviderName)
        val tvProviderUsername = dialog.findViewById<TextView>(R.id.tvProviderUsername)
        val tvRatingInfo = dialog.findViewById<TextView>(R.id.tvRatingInfo)
        val tvPhoneNumber = dialog.findViewById<TextView>(R.id.tvPhoneNumber)
        val tvAvailability = dialog.findViewById<TextView>(R.id.tvAvailability)
        val tvExperience = dialog.findViewById<TextView>(R.id.tvExperience)
        val ivProviderProfile = dialog.findViewById<ImageView>(R.id.ivProviderProfile)
        
        // Service information
        val tvServiceHeader = dialog.findViewById<TextView>(R.id.tvServiceHeader)
        val tvServiceDescription = dialog.findViewById<TextView>(R.id.tvServiceDescription)
        val tvCategory = dialog.findViewById<TextView>(R.id.tvCategory)
        val tvDuration = dialog.findViewById<TextView>(R.id.tvDuration)
        val tvPrice = dialog.findViewById<TextView>(R.id.tvPrice)
        val ivServiceImage = dialog.findViewById<ImageView>(R.id.ivServiceImage)
        
        // Buttons
        val btnCloseDialog = dialog.findViewById<ImageButton>(R.id.btnCloseDialog)
        val bookButton = dialog.findViewById<Button>(R.id.btnBookService)
        
        // Set provider information
        val providerName = service.provider?.businessName ?: 
                          "${service.provider?.firstName} ${service.provider?.lastName}"
        tvProviderName.text = providerName
        // Use userAuth's userName if available, or default to Business
        val username = if (service.provider?.userAuth != null) {
            service.provider.userAuth.userName ?: "Business"
        } else {
            "Business"
        }
        tvProviderUsername.text = username
        tvRatingInfo.text = "No reviews yet" // Use proper rating info field
        tvPhoneNumber.text = service.provider?.phoneNumber ?: "Not provided"
        tvExperience.text = "${service.provider?.yearsOfExperience ?: "N/A"} years experience"
        
        // Load provider profile image with improved debugging
        if (service.provider?.profileImage != null && service.provider.profileImage.isNotEmpty()) {
            try {
                Log.d(TAG, "Raw profile image path: ${service.provider.profileImage}")
                
                // Force load from network and avoid cache issues
                val profileImageUrl = service.provider.profileImage
                val fullUrl = if (profileImageUrl.startsWith("http")) {
                    profileImageUrl  // Already a full URL
                } else {
                    // Explicitly construct URL with base URL
                    "${com.example.serbisyo_it342_g3.api.BaseApiClient.BASE_URL}${
                        if (!profileImageUrl.startsWith("/")) "/$profileImageUrl" else profileImageUrl
                    }"
                }
                
                Log.d(TAG, "Final profile image URL: $fullUrl")
                
                // Use ImageUtils to load the image
                com.example.serbisyo_it342_g3.utils.ImageUtils.loadImageAsync(
                    fullUrl, ivProviderProfile)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading provider profile image", e)
                ivProviderProfile.setImageResource(R.drawable.ic_person)
            }
        } else {
            Log.d(TAG, "No profile image path available: ${service.provider?.profileImage}")
            ivProviderProfile.setImageResource(R.drawable.ic_person)
        }
        
        // Fetch provider schedule with provider ID
        if (service.provider?.providerId != null) {
            Log.d(TAG, "Provider ID for schedule: ${service.provider.providerId}")
            fetchProviderScheduleDirectly(service.provider.providerId, tvAvailability)
        } else {
            Log.d(TAG, "No provider ID available for schedules")
            tvAvailability.text = "Schedule information unavailable"
        }
        
        // Set service information
        tvServiceHeader.text = service.serviceName
        tvServiceDescription.text = service.serviceDescription
        tvCategory.text = service.category?.categoryName ?: "Uncategorized"
        tvDuration.text = service.durationEstimate
        tvPrice.text = service.priceRange
        
        // Load service image with improved handling
        if (!service.imageUrl.isNullOrEmpty()) {
            try {
                Log.d(TAG, "Raw service image path: ${service.imageUrl}")
                
                // Similar URL construction as profile image
                val serviceImageUrl = service.imageUrl
                val fullUrl = if (serviceImageUrl.startsWith("http")) {
                    serviceImageUrl  // Already a full URL
                } else {
                    // Explicitly construct URL with base URL
                    "${com.example.serbisyo_it342_g3.api.BaseApiClient.BASE_URL}${
                        if (!serviceImageUrl.startsWith("/")) "/$serviceImageUrl" else serviceImageUrl
                    }"
                }
                
                Log.d(TAG, "Final service image URL: $fullUrl")
                
                // Use ImageUtils to load the image
                com.example.serbisyo_it342_g3.utils.ImageUtils.loadImageAsync(
                    fullUrl, ivServiceImage)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading service image", e)
                ivServiceImage.setImageResource(R.drawable.ic_image_placeholder)
            }
        } else {
            Log.d(TAG, "No service image path available")
            ivServiceImage.setImageResource(R.drawable.ic_image_placeholder)
        }
        
        // Set button listeners
        btnCloseDialog.setOnClickListener {
            dialog.dismiss()
        }
        
        bookButton.setOnClickListener {
            showDateTimePicker(service)
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    // Direct approach to fetch provider schedules
    private fun fetchProviderScheduleDirectly(providerId: Long, tvAvailability: TextView) {
        val scheduleApiClient = ScheduleApiClient(this)
        
        Log.d(TAG, "Starting direct schedule fetch for provider ID: $providerId with token: ${token.take(10)}...")
        progressBar.visibility = View.VISIBLE
        
        // Show loading message
        tvAvailability.text = "Loading schedule..."
        
        // Make the API call to get schedules
        scheduleApiClient.getProviderSchedules(providerId, token) { schedules, error ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error fetching schedules: ${error.message}", error)
                    tvAvailability.text = "Could not load schedule information"
                    return@runOnUiThread
                }
                
                Log.d(TAG, "Received ${schedules?.size ?: 0} schedules")
                
                if (schedules.isNullOrEmpty()) {
                    tvAvailability.text = "No schedule information available"
                    return@runOnUiThread
                }
                
                // Debug log every schedule
                schedules.forEach { schedule ->
                    Log.d(TAG, "Schedule: Day=${schedule.dayOfWeek}, " +
                        "Time=${schedule.startTime}-${schedule.endTime}, " +
                        "Available=${schedule.isAvailable}")
                }
                
                // Filter to only available schedules
                val availableSchedules = schedules.filter { it.isAvailable }
                
                if (availableSchedules.isEmpty()) {
                    tvAvailability.text = "No available schedules"
                    return@runOnUiThread
                }
                
                // Group by day
                val schedulesByDay = availableSchedules.groupBy { it.dayOfWeek }
                
                // Format the schedule information
                val formattedSchedule = StringBuilder()
                
                schedulesByDay.forEach { (day, daySchedules) ->
                    formattedSchedule.append("$day: ")
                    
                    val timeRanges = daySchedules.map { "${it.startTime} - ${it.endTime}" }
                    formattedSchedule.append(timeRanges.joinToString(", "))
                    
                    formattedSchedule.append("\n")
                }
                
                // Set the text, removing trailing newline if present
                val scheduleText = formattedSchedule.toString().trimEnd()
                Log.d(TAG, "Setting schedule text: $scheduleText")
                tvAvailability.text = if (scheduleText.isNotEmpty()) scheduleText else "No schedule information"
            }
        }
    }
    
    private fun showDateTimePicker(service: Service) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_datetime_picker, null)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        
        // Set minimum date to tomorrow
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        datePicker.minDate = calendar.timeInMillis
        
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
            
        // Set button listeners
        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            alertDialog.dismiss()
        }
        
        dialogView.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            // Get selected date and time
            val year = datePicker.year
            val month = datePicker.month
            val day = datePicker.dayOfMonth
            val hour = timePicker.hour
            val minute = timePicker.minute
            
            // Create calendar instance with selected date/time
            val bookingCalendar = Calendar.getInstance()
            bookingCalendar.set(year, month, day, hour, minute, 0)
            
            // Format as ISO date string
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val bookingDateTime = sdf.format(bookingCalendar.time)
            
            // Create booking
            createBooking(service.serviceId, bookingDateTime)
            
            alertDialog.dismiss()
        }
        
        alertDialog.show()
    }
    
    private fun createBooking(serviceId: Long, bookingDateTime: String) {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = sharedPref.getString("userId", "0")?.toLongOrNull() ?: 0
        val customerName = sharedPref.getString("username", "Customer") ?: "Customer"
        
        if (userId == 0L) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_LONG).show()
            return
        }
        
        val bookingDate = bookingDateTime.split("T")[0] // Extract date part only
        
        progressBar.visibility = View.VISIBLE
        
        // Get the actual customerId instead of using userId directly
        val userApiClient = UserApiClient(this)
        userApiClient.getCustomerIdByUserId(userId, token) { customerId, error ->
            if (error != null || customerId == null) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error: Customer profile not found. Please set up your profile first.", Toast.LENGTH_LONG).show()
                }
                return@getCustomerIdByUserId
            }
            
            // Now use the correct customerId to create the booking
            bookingApiClient.createBooking(serviceId, customerId, bookingDate, token) { booking, bookingError ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    
                    if (bookingError != null) {
                        Log.e(TAG, "Error creating booking", bookingError)
                        Toast.makeText(this, "Error creating booking: ${bookingError.message}", Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }
                    
                    if (booking != null) {
                        Toast.makeText(this, "Booking created successfully! Status: ${booking.status}", Toast.LENGTH_LONG).show()
                        
                        // Send notification to service provider
                        sendBookingNotificationToProvider(booking, customerName)
                        
                        // Log that we sent the notification
                        Log.d(TAG, "Booking successful, notification sent to provider for booking ID: ${booking.bookingId}")
                    } else {
                        Toast.makeText(this, "Unknown error occurred while creating booking", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    
    private fun sendBookingNotificationToProvider(booking: Booking, customerName: String) {
        // Get the service provider from the booking
        val provider = booking.service?.provider
        
        // Log all the information we have about the provider
        Log.d(TAG, "Provider info: ${provider?.toString()}")
        
        // Try to get the provider's user ID in multiple ways
        val providerId = provider?.providerId
        val providerUserId = provider?.userAuth?.userId
        
        // Log what we found
        Log.d(TAG, "Provider ID: $providerId")
        Log.d(TAG, "Provider User ID: $providerUserId")
        
        // Check if we have a user ID to send notification to
        val targetUserId = providerUserId ?: providerId
        
        if (targetUserId == null) {
            Log.e(TAG, "Error: Provider User ID and Provider ID are both null. Cannot send notification.")
            Log.e(TAG, "Service details: ${booking.service}")
            Log.e(TAG, "Provider details: ${booking.service?.provider}")
            return
        }
        
        val serviceName = booking.service?.serviceName ?: "your service"
        
        Log.d(TAG, "Sending notification to user ID: $targetUserId")
        Log.d(TAG, "Service: $serviceName, Date: ${booking.bookingDate}")
        Log.d(TAG, "Customer name: $customerName")
        
        // Create notification message
        val message = "$customerName has booked $serviceName for ${booking.bookingDate}"
        
        // Create notification
        val notificationApiClient = NotificationApiClient(this)
        notificationApiClient.createNotification(
            targetUserId,
            "booking",
            message,
            token
        ) { notification, error -> 
            if (error != null) {
                Log.e(TAG, "Error sending notification to provider: ${error.message}", error)
                // Try to log details of the error for debugging
                if (error.message?.contains("response") == true) {
                    Log.e(TAG, "API Error response: ${error.message}")
                }
            } else {
                Log.d(TAG, "Notification sent to provider: $notification")
                if (notification != null) {
                    Log.d(TAG, "Notification ID: ${notification.notificationId}, User ID: ${notification.userId}")
                    Log.d(TAG, "Notification message: ${notification.message}")
                } else {
                    Log.e(TAG, "Notification object is null even though no error was reported")
                }
            }
        }
    }

    private fun testSendNotification() {
        // Get first service provider
        if (allServices.isEmpty()) {
            Toast.makeText(this, "No services available to test notifications", Toast.LENGTH_SHORT).show()
            return
        }
        
        val firstService = allServices.first()
        val provider = firstService.provider
        
        if (provider == null) {
            Toast.makeText(this, "Service has no provider information", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Log all information about the provider and service
        Log.d(TAG, "Testing notification with service: ${firstService.serviceName}")
        Log.d(TAG, "Provider info: $provider")
        Log.d(TAG, "Provider ID: ${provider.providerId}")
        Log.d(TAG, "Provider User Auth: ${provider.userAuth}")
        Log.d(TAG, "Provider User ID: ${provider.userAuth?.userId}")
        
        // Get the user ID from the provider
        val providerUserId = provider.userAuth?.userId
        val providerId = provider.providerId
        
        // Use userId if available, otherwise use providerId
        val targetUserId = providerUserId ?: providerId
        
        if (targetUserId == null) {
            Toast.makeText(this, "Cannot determine provider's user ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Create a test notification
        val message = "This is a test notification for ${firstService.serviceName}"
        
        // Show progress
        progressBar.visibility = View.VISIBLE
        
        // Create and send the notification
        val notificationApiClient = NotificationApiClient(this)
        notificationApiClient.createNotification(
            targetUserId,
            "test",
            message,
            token
        ) { notification, error -> 
            runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error sending test notification: ${error.message}", error)
                    Toast.makeText(this, "Failed to send test notification: ${error.message}", Toast.LENGTH_LONG).show()
                } else {
                    Log.d(TAG, "Test notification sent successfully: $notification")
                    Toast.makeText(this, "Test notification sent successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun showBookingHistory() {
        // Show booking history fragment
        val fragment = BookingHistoryFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
            
        // Hide dashboard content when showing fragment
        dashboardContent.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.customer_menu, menu)
        menu.add(Menu.NONE, 1001, Menu.NONE, "Test Notification")
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.menu_profile -> {
                // Set the profile tab as selected
                bottomNavigation.selectedItemId = R.id.navigation_profile
                true
            }
            R.id.menu_logout -> {
                // Clear shared preferences and go to login
                val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                sharedPrefs.edit().clear().apply()
                
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                true
            }
            1001 -> { // Test notification
                testSendNotification()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop slideshow when activity is paused
        slideshowHandler.removeCallbacks(slideshowRunnable)
        // Stop notification checking when activity is paused
        stopNotificationChecking()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop slideshow when activity is destroyed
        slideshowHandler.removeCallbacks(slideshowRunnable)
        // Stop notification checking when activity is destroyed
        stopNotificationChecking()
    }

    private fun startSlideshow() {
        // Start the slideshow
        slideshowHandler.postDelayed(slideshowRunnable, SLIDESHOW_DELAY)
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
            
            // Fade out current image
            val fadeOut = ObjectAnimator.ofFloat(currentImageView, "alpha", 1f, 0f)
            fadeOut.duration = FADE_DURATION
            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentImageView.visibility = View.INVISIBLE
                }
            })
            
            // Fade in next image
            val fadeIn = ObjectAnimator.ofFloat(nextImageView, "alpha", 0f, 1f)
            fadeIn.duration = FADE_DURATION
            
            // Start animations
            fadeOut.start()
            fadeIn.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error changing slideshow image", e)
        }
    }
    
    private fun setupSlideshowImages() {
        try {
            // Set the slideshow images from the drawable resources
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

    private fun setupBookServiceButtons() {
        // Setup only the hero button since we removed the others
        btnHeroBookService.setOnClickListener {
            // Navigate to BrowseServicesActivity
            val intent = Intent(this, BrowseServicesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        // Create and set the adapter with an empty list initially
        serviceAdapter = CustomerServiceAdapter(
            filteredServices,
            onServiceClick = { service -> viewServiceDetails(service) }
        )
        rvServices.adapter = serviceAdapter
    }

    private fun setupListeners() {
        // ... existing code ...
    }

    private fun onSuccessLoadProfile(firstName: String, lastName: String, imageUrl: String?) {
        // If we can't find the ID, create a temporary fallback solution
        // that doesn't crash but logs the issue
        try {
            val fullNameTextView = findViewById<TextView>(R.id.tvWelcome) // Using tvWelcome as fallback
            val userProfileImage = findViewById<ShapeableImageView>(R.id.ivProfileImage) // Using ivProfileImage as fallback
            
            fullNameTextView.text = "$firstName $lastName"
            
            if (!imageUrl.isNullOrEmpty()) {
                // Load profile image from URL code would go here
                Log.d(TAG, "Would load image from URL: $imageUrl")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onSuccessLoadProfile", e)
        }
    }

    private fun loadProfileData() {
        // ... existing code ...
    }

    // Add a method to directly set the BaseApiClient URL if needed
    private fun ensureApiUrlIsSet() {
        // Set the BaseApiClient URL based on device (you can customize this based on your environment)
        // This ensures the URL is set correctly for image and API calls
        try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            com.example.serbisyo_it342_g3.api.BaseApiClient.initializeUrl(connectivityManager)
            
            // Log the base URL for debugging
            val baseUrl = com.example.serbisyo_it342_g3.api.BaseApiClient.BASE_URL
            Log.d(TAG, "Using API base URL: $baseUrl")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing API URL", e)
        }
    }

    // Override the method from NotificationBadgeListener
    override fun updateNotificationBadge(count: Int) {
        runOnUiThread {
            val menuItem = bottomNavigation.menu.findItem(R.id.navigation_notifications)
            if (count > 0) {
                if (notificationBadge == null) {
                    // Initialize badge if not already done
                    val actionView = LayoutInflater.from(this).inflate(R.layout.notification_badge_layout, null)
                    notificationBadge = actionView.findViewById(R.id.notificationBadgeCount)
                    menuItem.actionView = actionView
                    
                    // Make the action view clickable to navigate to notifications
                    actionView.setOnClickListener {
                        bottomNavigation.selectedItemId = R.id.navigation_notifications
                    }
                }
                
                notificationBadge?.text = if (count > 99) "99+" else count.toString()
                notificationBadge?.visibility = View.VISIBLE
            } else {
                notificationBadge?.visibility = View.GONE
            }
        }
    }
    
    private fun checkForNotifications() {
        if (userId > 0 && token.isNotEmpty()) {
            notificationApiClient.getUnreadNotificationCount(userId, token) { count, error ->
                if (error != null) {
                    Log.e(TAG, "Error checking notifications", error)
                    return@getUnreadNotificationCount
                }
                updateNotificationBadge(count)
            }
        }
    }

    private fun startNotificationChecking() {
        // Start periodic notification checking
        notificationHandler.postDelayed(notificationCheckRunnable, 5000) // First check after 5 seconds
    }

    private fun stopNotificationChecking() {
        // Remove callbacks to stop the periodic checking
        notificationHandler.removeCallbacks(notificationCheckRunnable)
    }
}