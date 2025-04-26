package com.example.serbisyo_it342_g3

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.adapters.BrowseServiceAdapter
import com.example.serbisyo_it342_g3.api.AddressApiClient
import com.example.serbisyo_it342_g3.api.ApiClient
import com.example.serbisyo_it342_g3.api.BookingApiClient
import com.example.serbisyo_it342_g3.api.ScheduleApiClient
import com.example.serbisyo_it342_g3.api.ServiceApiClient
import com.example.serbisyo_it342_g3.api.UserApiClient
import com.example.serbisyo_it342_g3.data.Address
import com.example.serbisyo_it342_g3.data.Customer
import com.example.serbisyo_it342_g3.data.Schedule
import com.example.serbisyo_it342_g3.data.Service
import com.example.serbisyo_it342_g3.data.ServiceCategory
import com.example.serbisyo_it342_g3.utils.Constants
import com.example.serbisyo_it342_g3.utils.ImageUtils
import com.example.serbisyo_it342_g3.utils.NetworkUtils
import com.google.android.material.slider.RangeSlider
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import com.example.serbisyo_it342_g3.MainActivity
import com.example.serbisyo_it342_g3.api.PaymentApiClient
import com.example.serbisyo_it342_g3.data.Booking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BrowseServicesActivity : AppCompatActivity() {
    private lateinit var rvServices: RecyclerView
    private lateinit var tvServicesCount: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var serviceAdapter: BrowseServiceAdapter
    private lateinit var serviceApiClient: ServiceApiClient
    private lateinit var filterLayout: LinearLayout
    private lateinit var filterCardView: CardView
    private lateinit var btnToggleFilters: Button
    private lateinit var categorySpinner: Spinner
    private lateinit var priceRangeSlider: RangeSlider
    private lateinit var tvMinPrice: TextView
    private lateinit var tvMaxPrice: TextView
    private lateinit var ratingSlider: RangeSlider
    private lateinit var experienceSlider: RangeSlider
    private lateinit var btnApplyFilters: Button
    private lateinit var btnResetFilters: Button
    private lateinit var sortGroup: RadioGroup
    
    private val allServices = mutableListOf<Service>()
    private val filteredServices = mutableListOf<Service>()
    private val categories = mutableListOf<ServiceCategory>()
    private var selectedCategory: String? = null
    
    private var minPrice = 10f
    private var maxPrice = 2000f
    private var dynamicMinPrice = 10f
    private var dynamicMaxPrice = 2000f
    private var minRating = 0f
    private var maxRating = 5f
    private var minExperience = 0f
    private var maxExperience = 10f
    private var dynamicMaxExperience = 10f
    private var sortOption = "recommended"
    private var isFilterVisible = false
    
    private val tag = "BrowseServicesActivity"
    private var token: String = ""

    // Add constants at class level, add this near the top before onCreate
    companion object {
        private const val ADDRESS_UPDATE_REQUEST_CODE = 100
        private const val PAYMENT_REQUEST_CODE = 1001
    }

    // Add a reference to the current active booking dialog
    private var activeBookingDialog: Dialog? = null
    private var activeAddressTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse_services)
        
        // Initialize ServiceApiClient
        serviceApiClient = ServiceApiClient(this)
        
        // Get token from SharedPreferences
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        token = sharedPrefs.getString("token", "") ?: ""
        
        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Browse Services"
        
        // Initialize views
        rvServices = findViewById(R.id.rvServices)
        tvServicesCount = findViewById(R.id.tvServicesCount)
        progressBar = findViewById(R.id.progressBar)
        filterLayout = findViewById(R.id.filterLayout)
        filterCardView = findViewById(R.id.filterCardView)
        btnToggleFilters = findViewById(R.id.btnToggleFilters)
        categorySpinner = findViewById(R.id.categorySpinner)
        priceRangeSlider = findViewById(R.id.priceRangeSlider)
        tvMinPrice = findViewById(R.id.tvMinPrice)
        tvMaxPrice = findViewById(R.id.tvMaxPrice)
        ratingSlider = findViewById(R.id.ratingSlider)
        experienceSlider = findViewById(R.id.experienceSlider)
        btnApplyFilters = findViewById(R.id.btnApplyFilters)
        btnResetFilters = findViewById(R.id.btnResetFilters)
        sortGroup = findViewById(R.id.sortGroup)
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Initialize filter sliders with default values
        initializeSliders()
        
        // Setup toggle button for filters
        setupFilterToggle()
        
        // Load data
        loadData()
    }
    
    private fun initializeSliders() {
        // Initialize price range slider
        try {
            priceRangeSlider.valueFrom = minPrice
            priceRangeSlider.valueTo = maxPrice
            priceRangeSlider.setValues(minPrice, maxPrice)
            tvMinPrice.text = "₱${minPrice.toInt()}"
            tvMaxPrice.text = "₱${maxPrice.toInt()}"
            
            priceRangeSlider.addOnChangeListener { slider, _, _ ->
                val values = slider.values
                minPrice = values[0]
                maxPrice = values[1]
                
                // Update text views
                tvMinPrice.text = "₱${minPrice.toInt()}"
                tvMaxPrice.text = "₱${maxPrice.toInt()}"
            }
        } catch (e: Exception) {
            Log.e(tag, "Error setting up price range slider: ${e.message}")
        }
        
        // Initialize rating slider
        try {
            ratingSlider.valueFrom = 0f
            ratingSlider.valueTo = 5f
            ratingSlider.setValues(minRating)
            
            ratingSlider.addOnChangeListener { slider, _, _ ->
                minRating = slider.values[0]
            }
        } catch (e: Exception) {
            Log.e(tag, "Error setting up rating slider: ${e.message}")
        }
        
        // Initialize experience slider
        try {
            experienceSlider.valueFrom = 0f
            experienceSlider.valueTo = maxExperience
            experienceSlider.setValues(minExperience)
            
            experienceSlider.addOnChangeListener { slider, _, _ ->
                minExperience = slider.values[0]
            }
        } catch (e: Exception) {
            Log.e(tag, "Error setting up experience slider: ${e.message}")
        }
        
        // Set up filter buttons
        btnApplyFilters.setOnClickListener {
            applyFilters()
            // Hide filters after applying on mobile
            toggleFilters()
        }
        
        btnResetFilters.setOnClickListener {
            resetFilters()
        }
        
        // Sort radio group
        sortGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbRecommended -> sortOption = "recommended"
                R.id.rbPriceLowToHigh -> sortOption = "price_asc"
                R.id.rbPriceHighToLow -> sortOption = "price_desc"
                R.id.rbHighestRating -> sortOption = "rating"
                R.id.rbMostExperienced -> sortOption = "experience"
            }
        }
    }
    
    private fun setupRecyclerView() {
        // Set up with a 2-column grid layout
        val layoutManager = GridLayoutManager(this, 2)
        rvServices.layoutManager = layoutManager
        
        // Create and set adapter
        serviceAdapter = BrowseServiceAdapter(
            filteredServices,
            onServiceClick = { service ->
                // Show service details dialog
                showServiceDetailsDialog(service)
            }
        )
        rvServices.adapter = serviceAdapter
    }
    
    private fun setupFilterToggle() {
        btnToggleFilters.setOnClickListener {
            toggleFilters()
        }
    }
    
    private fun toggleFilters() {
        isFilterVisible = !isFilterVisible
        
        if (isFilterVisible) {
            filterCardView.visibility = View.VISIBLE
            btnToggleFilters.text = "Hide Filters"
        } else {
            filterCardView.visibility = View.GONE
            btnToggleFilters.text = "Show Filters"
        }
    }
    
    private fun loadData() {
        loadCategories()
        loadServices()
    }
    
    private fun loadCategories() {
        progressBar.visibility = View.VISIBLE
        
        serviceApiClient.getServiceCategories(token) { categoryList, error ->
            runOnUiThread {
                if (error != null) {
                    Log.e(tag, "Error loading categories", error)
                    return@runOnUiThread
                }
                
                if (categoryList != null) {
                    categories.clear()
                    categories.addAll(categoryList)
                    
                    // Setup category spinner
                    setupCategorySpinner()
                }
            }
        }
    }
    
    private fun setupCategorySpinner() {
        // Create a list with "All" as the first option followed by all category names
        val categoryNames = mutableListOf<String>("All")
        categories.forEach { category ->
            categoryNames.add(category.categoryName)
        }
        
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categoryNames
        )
        
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        
        // Apply the adapter to the spinner
        categorySpinner.adapter = adapter
        
        // Set listener for item selection
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategory = if (position == 0) null else categoryNames[position]
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedCategory = null
            }
        }
    }
    
    private fun loadServices() {
        progressBar.visibility = View.VISIBLE
        
        serviceApiClient.getAllServices(token) { servicesList, error ->
            runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(tag, "Error loading services", error)
                    Toast.makeText(this, "Error loading services: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                
                if (servicesList != null && servicesList.isNotEmpty()) {
                    allServices.clear()
                    allServices.addAll(servicesList)
                    
                    // Update filters based on loaded data
                    updateDynamicFilters(servicesList)
                    
                    // Display all services initially
                    applyFilters()
                    
                    Toast.makeText(this, "Loaded ${servicesList.size} services", Toast.LENGTH_SHORT).show()
                } else {
                    tvServicesCount.text = "0 services found"
                }
            }
        }
    }
    
    private fun updateDynamicFilters(services: List<Service>) {
        if (services.isEmpty()) return
        
        // Find the highest price in all services
        var highestPrice = 0f
        var highestExperience = 0f
        
        services.forEach { service ->
            // Update price range
            val price = parsePrice(service.effectivePrice)
            if (price > highestPrice) {
                highestPrice = price
            }
            
            // For future implementation - experience years
            // Assuming there will be a yearsOfExperience field in the service provider
            // service.serviceProvider?.yearsOfExperience?.let { years ->
            //     if (years > highestExperience) {
            //         highestExperience = years.toFloat()
            //     }
            // }
        }
        
        // Update dynamic price range (add a little buffer)
        if (highestPrice > 0) {
            dynamicMaxPrice = (highestPrice * 1.1f).coerceAtLeast(2000f)
            
            // Update the slider
            try {
                priceRangeSlider.valueTo = dynamicMaxPrice
                
                // Only set max value if it's the first load or reset
                if (maxPrice == 2000f) {
                    maxPrice = dynamicMaxPrice
                    priceRangeSlider.setValues(minPrice, maxPrice)
                    tvMaxPrice.text = "₱${maxPrice.toInt()}"
                }
            } catch (e: Exception) {
                Log.e(tag, "Error updating price slider range: ${e.message}")
            }
        }
        
        // Update dynamic experience range
        if (highestExperience > 0) {
            dynamicMaxExperience = (highestExperience * 1.2f).coerceAtLeast(10f)
            
            // Update the slider
            try {
                experienceSlider.valueTo = dynamicMaxExperience
                
                // Only set max value if it's the first load or reset
                if (maxExperience == 10f) {
                    maxExperience = dynamicMaxExperience
                    experienceSlider.setValues(minExperience)
                }
            } catch (e: Exception) {
                Log.e(tag, "Error updating experience slider range: ${e.message}")
            }
        }
    }
    
    private fun applyFilters() {
        filteredServices.clear()
        
        // Apply category filter
        var tempList = if (selectedCategory == null) {
            allServices
        } else {
            allServices.filter { service ->
                service.category?.categoryName == selectedCategory
            }
        }
        
        // Apply price filter
        tempList = tempList.filter { service ->
            val price = parsePrice(service.effectivePrice)
            price in minPrice..maxPrice
        }
        
        // Apply rating filter (future)
        
        // Apply experience filter (future)
        
        // Apply sorting
        when (sortOption) {
            "price_asc" -> tempList = tempList.sortedBy { parsePrice(it.effectivePrice) }
            "price_desc" -> tempList = tempList.sortedByDescending { parsePrice(it.effectivePrice) }
            "rating" -> {} // For future use
            "experience" -> {} // For future use
            else -> {} // Keep original order
        }
        
        // Update UI
        filteredServices.addAll(tempList)
        serviceAdapter.notifyDataSetChanged()
        
        tvServicesCount.text = "${filteredServices.size} services found"
    }
    
    private fun resetFilters() {
        // Reset category selection to "All"
        categorySpinner.setSelection(0)
        selectedCategory = null
        
        // Reset price range to dynamic values
        minPrice = dynamicMinPrice
        maxPrice = dynamicMaxPrice
        try {
            priceRangeSlider.setValues(minPrice, maxPrice)
            tvMinPrice.text = "₱${minPrice.toInt()}"
            tvMaxPrice.text = "₱${maxPrice.toInt()}"
        } catch (e: Exception) {
            Log.e(tag, "Error resetting price slider: ${e.message}")
        }
        
        // Reset rating
        minRating = 0f
        try {
            ratingSlider.setValues(minRating)
        } catch (e: Exception) {
            Log.e(tag, "Error resetting rating slider: ${e.message}")
        }
        
        // Reset experience to dynamic value
        minExperience = 0f
        try {
            experienceSlider.setValues(minExperience)
        } catch (e: Exception) {
            Log.e(tag, "Error resetting experience slider: ${e.message}")
        }
        
        // Reset sort option
        sortGroup.check(R.id.rbRecommended)
        sortOption = "recommended"
        
        // Apply filters (this will show all services)
        applyFilters()
    }
    
    private fun parsePrice(priceString: String): Float {
        return try {
            // Try to extract numeric value from price string
            priceString.replace("[^0-9.]".toRegex(), "").toFloatOrNull() ?: 0f
        } catch (e: Exception) {
            0f
        }
    }
    
    /**
     * Shows a dialog with detailed information about the selected service
     */
    private fun showServiceDetailsDialog(service: Service) {
        val dialog = Dialog(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_service_details)
        
        // Make dialog full width with rounded corners
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        
        // Set service details
        with(dialog) {
            // Header info
            findViewById<TextView>(R.id.tvServiceHeader).text = service.serviceName
            
            // Close button
            findViewById<ImageButton>(R.id.btnCloseDialog).setOnClickListener {
                dialog.dismiss()
            }
            
            // Service description
            findViewById<TextView>(R.id.tvServiceDescription).text = service.serviceDescription
            
            // Provider details
            service.provider?.let { provider ->
                findViewById<TextView>(R.id.tvProviderName).text = "${provider.firstName} ${provider.lastName}"
                findViewById<TextView>(R.id.tvProviderUsername).text = provider.userAuth?.userName ?: "User"
                findViewById<TextView>(R.id.tvExperience).text = "${provider.yearsOfExperience ?: 0} years experience"
                findViewById<TextView>(R.id.tvPhoneNumber).text = provider.phoneNumber ?: "No phone number"
                findViewById<TextView>(R.id.tvAvailability).text = provider.availabilitySchedule ?: "Contact provider for availability"
                
                // Provider profile image
                provider.profileImage?.let { imageUrl ->
                    val profileImageView = findViewById<ImageView>(R.id.ivProviderProfile)
                    ImageUtils.loadImageAsync(
                        ImageUtils.getFullImageUrl(imageUrl, this@BrowseServicesActivity),
                        profileImageView
                    )
                }
            }
            
            // Service category and duration
            findViewById<TextView>(R.id.tvCategory).text = service.category?.categoryName ?: "General"
            findViewById<TextView>(R.id.tvDuration).text = service.durationEstimate
            
            // Service rating (placeholder for future functionality)
            findViewById<RatingBar>(R.id.ratingBar).rating = 0f
            findViewById<TextView>(R.id.tvRatingInfo).text = getString(R.string.no_reviews_yet)
            
            // Price
            findViewById<TextView>(R.id.tvPrice).text = getString(R.string.service_price_format, parsePrice(service.effectivePrice).roundToInt())
            
            // Service image
            val serviceImageView = findViewById<ImageView>(R.id.ivServiceImage)
            if (!service.imageUrl.isNullOrEmpty()) {
                ImageUtils.loadImageAsync(
                    ImageUtils.getFullImageUrl(service.imageUrl, this@BrowseServicesActivity),
                    serviceImageView
                )
            }
            
            // Book button action
            findViewById<Button>(R.id.btnBookService).setOnClickListener {
                // Show the new booking dialog with schedule selection
                showServiceBookingDialog(service)
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
    
    /**
     * Shows a dialog for booking a service with calendar date selection and time slots
     */
    private fun showServiceBookingDialog(service: Service) {
        val dialog = Dialog(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_service_booking)
        
        // Store reference to active dialog
        activeBookingDialog = dialog
        
        // Make dialog full width with rounded corners
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        
        // Initialize API clients
        val scheduleApiClient = ScheduleApiClient(this)
        val addressApiClient = AddressApiClient(this)
        val userApiClient = UserApiClient(this)
        val bookingApiClient = BookingApiClient(this)
        
        // Get user data
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = try {
            sharedPrefs.getLong("userId", 0)
        } catch (e: ClassCastException) {
            val userIdStr = sharedPrefs.getString("userId", "0")
            userIdStr?.toLongOrNull() ?: 0
        }
        
        // Variables to store selected date and time
        var selectedDate: Calendar? = null
        var selectedTimeSlot: String? = null
        var customerAddress: Address? = null
        
        // Set up dialog views
        with(dialog) {
            // Header
            findViewById<TextView>(R.id.tvServiceHeader).text = "Book ${service.serviceName}"
            
            // Close button
            findViewById<ImageButton>(R.id.btnCloseDialog).setOnClickListener {
                activeBookingDialog = null
                activeAddressTextView = null
                dialog.dismiss()
            }
            
            // Debug info
            val tvDebugInfo = findViewById<TextView>(R.id.tvDebugInfo)
            tvDebugInfo.text = "Provider ID: ${service.provider?.providerId ?: "unknown"}"
            
            // Get references to views
            val calendarView = findViewById<CalendarView>(R.id.calendarView)
            val timeSlotContainer = findViewById<LinearLayout>(R.id.timeSlotContainer)
            val tvNoTimeSlots = findViewById<TextView>(R.id.tvNoTimeSlots)
            val btnRetryLoading = findViewById<Button>(R.id.btnRetryLoading)
            val tvAddress = findViewById<TextView>(R.id.tvAddress)
            val tvChangeAddress = findViewById<TextView>(R.id.tvChangeAddress)
            val btnContinue = findViewById<Button>(R.id.btnContinue)
            
            // Store reference to address TextView
            activeAddressTextView = tvAddress
            
            // Set price information
            val servicePrice = parsePrice(service.effectivePrice)
            val tvServicePrice = findViewById<TextView>(R.id.tvServicePrice)
            val tvPaymongoFee = findViewById<TextView>(R.id.tvPaymongoFee)
            val tvAppFee = findViewById<TextView>(R.id.tvAppFee)
            val tvTotalPrice = findViewById<TextView>(R.id.tvTotalPrice)
            
            tvServicePrice.text = getString(R.string.service_price_format, servicePrice.roundToInt())
            
            // Calculate fees (2.5% each)
            val paymongoFee = (servicePrice * 0.025).roundToInt()
            val appFee = (servicePrice * 0.025).roundToInt()
            val totalPrice = servicePrice.roundToInt() + paymongoFee + appFee
            
            tvPaymongoFee.text = getString(R.string.service_price_format, paymongoFee)
            tvAppFee.text = getString(R.string.service_price_format, appFee)
            tvTotalPrice.text = getString(R.string.service_price_format, totalPrice)
            
            // Set minimum date to tomorrow for the calendar
            val tomorrow = Calendar.getInstance()
            tomorrow.add(Calendar.DAY_OF_MONTH, 1)
            calendarView.minDate = tomorrow.timeInMillis
            
            // Load customer's address
            userApiClient.getCustomerProfile(userId, token) { customer, error ->
                runOnUiThread {
                    if (error != null) {
                        Log.e(tag, "Error loading customer profile", error)
                        tvAddress.text = getString(R.string.error_loading_address)
                        return@runOnUiThread
                    }
                    
                    if (customer != null) {
                        customerAddress = customer.address
                        
                        // Log customer ID for debugging
                        Log.d(tag, "Customer ID: ${customer.customerId}")
                        
                        // First try to use the existing address if available
                        if (customer.address != null) {
                            val formattedAddress = "${customer.address.street}, ${customer.address.barangay ?: ""}, ${customer.address.city}, ${customer.address.province}"
                            tvAddress.text = formattedAddress
                        } else {
                            // If no address directly in customer object, fetch from addresses API like in web app
                            fetchAddressesForCustomer(customer.customerId, tvAddress)
                        }
                    } else {
                        tvAddress.text = getString(R.string.no_address_found)
                    }
                }
            }
            
            // Calendar date selection listener
            calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
                // Update selected date
                val newSelectedDate = Calendar.getInstance()
                newSelectedDate.set(year, month, dayOfMonth)
                selectedDate = newSelectedDate
                
                // Reset time slot selection
                selectedTimeSlot = null
                btnContinue.isEnabled = false
                
                // IMPORTANT: Get day of week as a number from 1-7 (Sunday=1, Monday=2, etc.)
                val dayOfWeekNumber = newSelectedDate.get(Calendar.DAY_OF_WEEK)
                
                // Map day number to API expected format
                val dayNames = arrayOf("", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY")
                val dayOfWeek = dayNames[dayOfWeekNumber]
                
                // Also get the human-readable day name for debug info
                val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                val dayOfWeekDisplay = dateFormat.format(newSelectedDate.time).uppercase()
                
                // Show detailed debug info
                tvDebugInfo.text = "Provider ID: ${service.provider?.providerId}, Selected day: $dayOfWeekDisplay ($dayOfWeek), Date: ${year}-${month+1}-${dayOfMonth}"
                Log.d(tag, "Selected date: ${year}-${month+1}-${dayOfMonth}, Day: $dayOfWeek ($dayOfWeekNumber)")
                
                // Clear previous time slots
                timeSlotContainer.removeAllViews()
                
                // Show loading indicator or message
                tvNoTimeSlots.visibility = View.GONE
                btnRetryLoading.visibility = View.GONE
                
                // Create a temporary TextView for "Loading..."
                val loadingText = TextView(this@BrowseServicesActivity)
                loadingText.text = "Loading available time slots..."
                loadingText.gravity = android.view.Gravity.CENTER
                loadingText.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                timeSlotContainer.addView(loadingText)
                
                // Get provider's available schedules for this day of week
                service.provider?.providerId?.let { providerId ->
                    // Make a direct API call to get schedules for the specific day
                    val url = "${Constants.BASE_URL}schedules/provider/${providerId}/day/${dayOfWeek}"
                    Log.d(tag, "Fetching schedules from URL: $url")
                    
                    // Clear previous time slots and show loading
                    timeSlotContainer.removeAllViews()
                    tvNoTimeSlots.visibility = View.GONE
                    btnRetryLoading.visibility = View.GONE
                    
                    // Create a temporary TextView for "Loading..."
                    val loadingText = TextView(this@BrowseServicesActivity)
                    loadingText.text = "Loading available time slots..."
                    loadingText.gravity = android.view.Gravity.CENTER
                    loadingText.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    timeSlotContainer.addView(loadingText)
                    
                    // Use the scheduleApiClient instead of direct OkHttp call
                    scheduleApiClient.getProviderSchedulesByDay(providerId, dayOfWeek, token) { schedulesList, error ->
                        runOnUiThread {
                            // Remove loading text
                            timeSlotContainer.removeAllViews()
                            
                            if (error != null) {
                                Log.e(tag, "Error loading provider schedules: ${error.message}")
                                tvNoTimeSlots.text = "Error loading schedules: ${error.message}"
                                tvNoTimeSlots.visibility = View.VISIBLE
                                btnRetryLoading.visibility = View.VISIBLE
                                return@runOnUiThread
                            }
                            
                            if (schedulesList.isNullOrEmpty()) {
                                Log.d(tag, "No available schedules found")
                                tvNoTimeSlots.text = "No available time slots for $dayOfWeekDisplay"
                                tvNoTimeSlots.visibility = View.VISIBLE
                            } else {
                                // Sort by start time
                                val sortedSchedules = schedulesList.sortedBy { it.startTime }
                                Log.d(tag, "Creating time slots for ${sortedSchedules.size} schedules")
                                
                                // Create time slot buttons
                                createTimeSlots(timeSlotContainer, sortedSchedules) { timeSlot ->
                                    selectedTimeSlot = timeSlot
                                    btnContinue.isEnabled = true
                                    Log.d(tag, "Selected time slot: $timeSlot")
                                }
                            }
                        }
                    }
                } ?: run {
                    timeSlotContainer.removeAllViews()
                    tvNoTimeSlots.text = "Could not find provider information"
                    tvNoTimeSlots.visibility = View.VISIBLE
                }
            }
            
            // Handle retry button
            btnRetryLoading.setOnClickListener {
                // Trigger date selection listener again
                selectedDate?.let { date ->
                    calendarView.setDate(date.timeInMillis, true, true)
                }
            }
            
            // Change address click
            tvChangeAddress.setOnClickListener {
                // Navigate directly to the Profile Management Activity's Address tab
                val intent = Intent(this@BrowseServicesActivity, ProfileManagementActivity::class.java)
                // Pass the tab index for Address (which is 1)
                intent.putExtra("tab_index", 1)
                // Use startActivityForResult to get notified when returning
                startActivityForResult(intent, ADDRESS_UPDATE_REQUEST_CODE)
            }
            
            // Continue button click
            btnContinue.setOnClickListener {
                // Format selected date and time for booking
                val selectedDateObj = selectedDate?.time
                if (selectedDateObj != null && selectedTimeSlot != null) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val formattedDate = dateFormat.format(selectedDateObj)
                    
                    // Show booking review dialog
                    showBookingReviewDialog(service, formattedDate, selectedTimeSlot!!, customerAddress)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this@BrowseServicesActivity, "Please select a date and time", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        dialog.show()
    }
    
    /**
     * Function to fetch addresses for customer from API
     */
    private fun fetchAddressesForCustomer(customerId: Long, tvAddress: TextView) {
        // Set loading text while fetching
        tvAddress.text = getString(R.string.loading_address)
        
        // Use the address API client instead of direct OkHttp call
        val addressApiClient = AddressApiClient(this)
        addressApiClient.getAddressesByUserId(customerId, token) { addresses, error ->
            runOnUiThread {
                if (error != null) {
                    Log.e(tag, "Error fetching addresses", error)
                    tvAddress.text = getString(R.string.error_loading_address)
                    return@runOnUiThread
                }
                
                if (addresses.isNullOrEmpty()) {
                    Log.d(tag, "No addresses found for customer $customerId")
                    tvAddress.text = getString(R.string.no_address_found)
                    return@runOnUiThread
                }
                
                Log.d(tag, "Found ${addresses.size} addresses for customer $customerId")
                
                // First try to find the main address
                var selectedAddress = addresses.find { it.main }
                Log.d(tag, "Main address found: ${selectedAddress != null}")
                
                // If no main address, use the first one
                if (selectedAddress == null && addresses.isNotEmpty()) {
                    selectedAddress = addresses[0]
                    Log.d(tag, "No main address found, using first address")
                }
                
                if (selectedAddress != null) {
                    // Use streetName if available, otherwise use street
                    val streetDisplay = when {
                        !selectedAddress.streetName.isNullOrBlank() -> selectedAddress.streetName
                        !selectedAddress.street.isNullOrBlank() -> selectedAddress.street
                        else -> ""
                    }
                        
                    Log.d(tag, "Building address display, street: $streetDisplay, barangay: ${selectedAddress.barangay}, city: ${selectedAddress.city}")
                    
                    val parts = listOfNotNull(
                        streetDisplay,
                        selectedAddress.barangay?.takeIf { it.isNotEmpty() },
                        selectedAddress.city?.takeIf { it.isNotEmpty() },
                        selectedAddress.province?.takeIf { it.isNotEmpty() }
                    )
                    
                    val formattedAddress = parts.joinToString(", ")
                    Log.d(tag, "Final address display: $formattedAddress")
                    tvAddress.text = formattedAddress
                } else {
                    Log.d(tag, "No address found for customer")
                    tvAddress.text = getString(R.string.no_address_found)
                }
            }
        }
    }
    
    /**
     * Creates time slot buttons based on provider's schedule
     */
    private fun createTimeSlots(container: LinearLayout, schedules: List<Schedule>, onTimeSlotSelected: (String) -> Unit) {
        container.removeAllViews()
        
        if (schedules.isEmpty()) {
            val noTimesText = TextView(this)
            noTimesText.text = "No available time slots on this date"
            noTimesText.textSize = 16f
            noTimesText.gravity = android.view.Gravity.CENTER
            noTimesText.setPadding(16, 16, 16, 16)
            container.addView(noTimesText)
            return
        }
        
        // Create a linear layout for holding time slots
        val timeSlotContainer = LinearLayout(this)
        timeSlotContainer.orientation = LinearLayout.HORIZONTAL
        timeSlotContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        container.addView(timeSlotContainer)
        
        // Log for debugging
        Log.d(tag, "Creating time slots for ${schedules.size} schedules")
        
        // Add time slots for each schedule
        schedules.forEach { schedule ->
            // Create time slot button
            val timeSlotButton = Button(this)
            val params = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )
            params.setMargins(8, 8, 8, 8)
            timeSlotButton.layoutParams = params
            
            // Format times for display
            val startTime = formatTimeDisplay(schedule.startTime)
            val endTime = formatTimeDisplay(schedule.endTime)
            
            // Log for debugging
            Log.d(tag, "Creating time slot: $startTime - $endTime (${schedule.startTime} - ${schedule.endTime})")
            
            timeSlotButton.text = "$startTime -\n$endTime"
            timeSlotButton.setBackgroundResource(R.drawable.selector_time_slot)
            timeSlotButton.setTextColor(getColorStateList(R.color.selector_text_color))
            
            // Set click listener
            timeSlotButton.setOnClickListener {
                // Deselect all buttons in the container
                for (i in 0 until container.childCount) {
                    val childLayout = container.getChildAt(i) as? LinearLayout
                    childLayout?.let { layout ->
                        for (j in 0 until layout.childCount) {
                            val button = layout.getChildAt(j) as? Button
                            button?.isSelected = false
                        }
                    }
                }
                
                // Select this button
                timeSlotButton.isSelected = true
                
                // Log selected time slot for debugging
                Log.d(tag, "Selected time slot: ${schedule.startTime} - ${schedule.endTime}")
                
                // Notify with time slot data
                onTimeSlotSelected("${schedule.startTime}-${schedule.endTime}")
            }
            
            // Add to the container
            timeSlotContainer.addView(timeSlotButton)
        }
    }
    
    /**
     * Format time string for display (handles both HH:mm:ss and HH:mm formats)
     */
    private fun formatTimeDisplay(timeString: String): String {
        return try {
            var format = "HH:mm:ss"
            if (!timeString.contains(":")) {
                return timeString // No formatting needed
            } else if (timeString.split(":").size == 2) {
                format = "HH:mm"
            }
            
            val inputFormat = SimpleDateFormat(format, Locale.getDefault())
            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            
            val date = inputFormat.parse(timeString)
            date?.let { outputFormat.format(it) } ?: timeString
        } catch (e: Exception) {
            Log.e(tag, "Error formatting time: $timeString", e)
            timeString // Return original if parsing fails
        }
    }
    
    /**
     * Converts time from 24-hour format to 12-hour format
     */
    private fun convertTo12HourFormat(time: String): String {
        try {
            val inputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            
            val date = inputFormat.parse(time)
            return date?.let { outputFormat.format(it) } ?: time
        } catch (e: Exception) {
            Log.e("BrowseServices", "Error converting time format: ${e.message}")
            return time
        }
    }
    
    /**
     * Shows a dialog for reviewing booking details before confirming
     */
    private fun showBookingReviewDialog(
        service: Service,
        bookingDate: String,
        timeSlot: String,
        customerAddress: Address?
    ) {
        val dialog = Dialog(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_booking_review)
        
        // Make dialog full width with rounded corners
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        
        // Get user data
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = try {
            sharedPrefs.getLong("userId", 0)
        } catch (e: ClassCastException) {
            val userIdStr = sharedPrefs.getString("userId", "0")
            userIdStr?.toLongOrNull() ?: 0
        }
        
        // Format date for display
        val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(bookingDate)
        val displayDateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val formattedDisplayDate = parsedDate?.let { displayDateFormat.format(it) } ?: bookingDate
        
        // Format time for display
        val timeSlotParts = timeSlot.split("-")
        var formattedDisplayTime = timeSlot
        if (timeSlotParts.size == 2) {
            val startTime = formatTimeDisplay(timeSlotParts[0])
            val endTime = formatTimeDisplay(timeSlotParts[1])
            formattedDisplayTime = "$startTime - $endTime"
        }
        
        // Calculate price and fees
        val servicePrice = parsePrice(service.effectivePrice)
        val paymongoFee = (servicePrice * 0.025).roundToInt()
        val appFee = (servicePrice * 0.025).roundToInt()
        val totalPrice = servicePrice.roundToInt() + paymongoFee + appFee
        
        with(dialog) {
            // Service details
            findViewById<TextView>(R.id.tvServiceName).text = service.serviceName
            findViewById<TextView>(R.id.tvServiceDescription).text = service.serviceDescription
            findViewById<TextView>(R.id.tvServiceCategory).text = "Category: ${service.category?.categoryName ?: "General"}"
            findViewById<TextView>(R.id.tvProviderName).text = "Provider: ${service.provider?.firstName} ${service.provider?.lastName}"
            
            // Service image
            val serviceImageView = findViewById<ImageView>(R.id.ivServiceImage)
            if (!service.imageUrl.isNullOrEmpty()) {
                ImageUtils.loadImageAsync(
                    ImageUtils.getFullImageUrl(service.imageUrl, this@BrowseServicesActivity),
                    serviceImageView
                )
            }
            
            // Booking details
            findViewById<TextView>(R.id.tvBookingDate).text = formattedDisplayDate
            findViewById<TextView>(R.id.tvBookingTime).text = formattedDisplayTime
            
            // Service location (customer address)
            val tvServiceLocation = findViewById<TextView>(R.id.tvServiceLocation)
            
            // If we don't have a customer address, show loading and fetch it
            if (customerAddress == null) {
                tvServiceLocation.text = getString(R.string.loading_address)
                
                // Fetch customer profile to get address
                val userApiClient = UserApiClient(this@BrowseServicesActivity)
                userApiClient.getCustomerProfile(userId, token) { customer, error ->
                    runOnUiThread {
                        if (error != null) {
                            Log.e(tag, "Error loading customer profile", error)
                            tvServiceLocation.text = getString(R.string.error_loading_address)
                            return@runOnUiThread
                        }
                        
                        if (customer != null && customer.address != null) {
                            // Use the address from customer profile
                            val address = customer.address
                            val streetDisplay = when {
                                !address.streetName.isNullOrBlank() -> address.streetName
                                !address.street.isNullOrBlank() -> address.street
                                else -> ""
                            }
                            
                            val formattedAddress = listOfNotNull(
                                streetDisplay,
                                address.barangay?.takeIf { it.isNotEmpty() },
                                address.city?.takeIf { it.isNotEmpty() },
                                address.province?.takeIf { it.isNotEmpty() }
                            ).joinToString(", ")
                            
                            tvServiceLocation.text = formattedAddress
                        } else {
                            // Try fetching addresses from the API
                            fetchAddressesForBookingReview(userId, tvServiceLocation)
                        }
                    }
                }
            } else {
                // Use the address that was passed in
                val streetDisplay = when {
                    !customerAddress.streetName.isNullOrBlank() -> customerAddress.streetName
                    !customerAddress.street.isNullOrBlank() -> customerAddress.street
                    else -> ""
                }
                
                val formattedAddress = listOfNotNull(
                    streetDisplay,
                    customerAddress.barangay?.takeIf { it.isNotEmpty() },
                    customerAddress.city?.takeIf { it.isNotEmpty() },
                    customerAddress.province?.takeIf { it.isNotEmpty() }
                ).joinToString(", ")
                
                tvServiceLocation.text = formattedAddress
            }
            
            // Change address link
            findViewById<TextView>(R.id.tvChangeAddress).setOnClickListener {
                // Navigate to address management page
                val intent = Intent(this@BrowseServicesActivity, ProfileManagementActivity::class.java)
                intent.putExtra("tab_index", 1) // Address tab
                startActivityForResult(intent, ADDRESS_UPDATE_REQUEST_CODE)
            }
            
            // Payment details
            val tvServiceNamePrice = findViewById<TextView>(R.id.tvServiceNamePrice)
            tvServiceNamePrice.text = "${service.serviceName} Price:"
            findViewById<TextView>(R.id.tvServicePrice).text = getString(R.string.service_price_format, servicePrice.roundToInt())
            findViewById<TextView>(R.id.tvPayMongoFee).text = getString(R.string.service_price_format, paymongoFee)
            findViewById<TextView>(R.id.tvAppFee).text = getString(R.string.service_price_format, appFee)
            findViewById<TextView>(R.id.tvTotalPrice).text = getString(R.string.service_price_format, totalPrice)
            
            // Payment method radio group
            val rgPaymentMethod = findViewById<RadioGroup>(R.id.rgPaymentMethod)
            
            // Close button
            findViewById<ImageButton>(R.id.btnCloseDialog).setOnClickListener {
                dialog.dismiss()
            }
            
            // Back button
            findViewById<Button>(R.id.btnBack).setOnClickListener {
                dialog.dismiss()
                // Go back to the service booking dialog
                showServiceBookingDialog(service)
            }
            
            // Proceed to payment button
            findViewById<Button>(R.id.btnProceedToPayment).setOnClickListener {
                // Get special instructions
                val specialInstructions = findViewById<EditText>(R.id.etSpecialInstructions).text.toString()
                
                // Get selected payment method
                val paymentMethod = if (rgPaymentMethod.checkedRadioButtonId == R.id.rbGCash) "GCash" else "Cash"
                
                // Dismiss this dialog and show payment summary
                dialog.dismiss()
                
                // Show payment summary dialog
                showPaymentSummaryDialog(
                    service = service,
                    bookingDate = bookingDate,
                    timeSlot = timeSlot,
                    totalPrice = totalPrice,
                    customerAddress = customerAddress,
                    specialInstructions = specialInstructions,
                    paymentMethod = paymentMethod
                )
            }
        }
        
        dialog.show()
    }
    
    /**
     * Function to fetch addresses specifically for the booking review dialog
     */
    private fun fetchAddressesForBookingReview(customerId: Long, tvServiceLocation: TextView) {
        // Use the address API client
        val addressApiClient = AddressApiClient(this)
        addressApiClient.getAddressesByUserId(customerId, token) { addresses, error ->
            runOnUiThread {
                if (error != null) {
                    Log.e(tag, "Error fetching addresses for booking review", error)
                    tvServiceLocation.text = getString(R.string.error_loading_address)
                    return@runOnUiThread
                }
                
                if (addresses.isNullOrEmpty()) {
                    Log.d(tag, "No addresses found for customer $customerId in booking review")
                    tvServiceLocation.text = getString(R.string.no_address_found)
                    return@runOnUiThread
                }
                
                // First try to find the main address
                var selectedAddress = addresses.find { it.main }
                
                // If no main address, use the first one
                if (selectedAddress == null && addresses.isNotEmpty()) {
                    selectedAddress = addresses[0]
                }
                
                if (selectedAddress != null) {
                    val streetDisplay = when {
                        !selectedAddress.streetName.isNullOrBlank() -> selectedAddress.streetName
                        !selectedAddress.street.isNullOrBlank() -> selectedAddress.street
                        else -> ""
                    }
                    
                    val formattedAddress = listOfNotNull(
                        streetDisplay,
                        selectedAddress.barangay?.takeIf { it.isNotEmpty() },
                        selectedAddress.city?.takeIf { it.isNotEmpty() },
                        selectedAddress.province?.takeIf { it.isNotEmpty() }
                    ).joinToString(", ")
                    
                    tvServiceLocation.text = formattedAddress
                } else {
                    tvServiceLocation.text = getString(R.string.no_address_found)
                }
            }
        }
    }
    
    /**
     * Shows a dialog confirming successful booking
     */
    private fun showBookingSuccessDialog(booking: Booking) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Booking Successful!")
        builder.setMessage("Your booking has been created successfully. You can view and manage it in your bookings history.")
        builder.setPositiveButton("View Bookings") { _, _ ->
            // Navigate to bookings history
            val intent = Intent(this, ProfileManagementActivity::class.java)
            intent.putExtra("tab_index", 2) // Booking history tab
            startActivity(intent)
            finish()
        }
        builder.setNegativeButton("Continue Browsing") { _, _ ->
            // Do nothing, stay on browse services
        }
        builder.setCancelable(false)
        builder.show()
    }
    
    /**
     * Shows a dialog with payment summary before completing booking
     */
    private fun showPaymentSummaryDialog(
        service: Service,
        bookingDate: String,
        timeSlot: String,
        totalPrice: Int,
        customerAddress: Address?,
        specialInstructions: String,
        paymentMethod: String
    ) {
        val dialog = Dialog(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_payment_summary)
        
        // Make dialog full width with rounded corners
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        
        // Get user data
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = try {
            sharedPrefs.getLong("userId", 0)
        } catch (e: ClassCastException) {
            val userIdStr = sharedPrefs.getString("userId", "0")
            userIdStr?.toLongOrNull() ?: 0
        }
        
        // Calculate prices
        val servicePrice = parsePrice(service.effectivePrice).roundToInt()
        val platformFee = totalPrice - servicePrice // Calculate the combined fees
        
        // Setup views
        dialog.findViewById<TextView>(R.id.tvServicePrice).text = "₱$servicePrice"
        dialog.findViewById<TextView>(R.id.tvPlatformFee).text = "₱$platformFee"
        dialog.findViewById<TextView>(R.id.tvTotalAmount).text = "₱$totalPrice"
        
        // Back button
        dialog.findViewById<Button>(R.id.btnBack).setOnClickListener {
            dialog.dismiss()
            // Go back to the booking review dialog
            showBookingReviewDialog(service, bookingDate, timeSlot, customerAddress)
        }
        
        // Complete booking button
        dialog.findViewById<Button>(R.id.btnCompleteBooking).setOnClickListener {
            dialog.dismiss()
            
            if (paymentMethod.equals("GCash", ignoreCase = true)) {
                // Process payment through PayMongo for GCash
                initiateGCashPayment(service, bookingDate, timeSlot, totalPrice, customerAddress, specialInstructions)
            } else {
                // Process as Cash payment (direct booking)
                processCashBooking(service, bookingDate, timeSlot, totalPrice, customerAddress, specialInstructions, paymentMethod)
            }
        }
        
        dialog.show()
    }
    
    /**
     * Initiates a GCash payment through PayMongo
     */
    private fun initiateGCashPayment(
        service: Service,
        bookingDate: String,
        timeSlot: String,
        totalPrice: Int,
        customerAddress: Address?,
        specialInstructions: String
    ) {
        // Show progress dialog
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Initiating payment process...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        
        // Get user ID from SharedPreferences
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = try {
            sharedPrefs.getLong("userId", 0)
        } catch (e: ClassCastException) {
            val userIdStr = sharedPrefs.getString("userId", "0")
            userIdStr?.toLongOrNull() ?: 0
        }
        
        // Create payment client
        val paymentApiClient = PaymentApiClient(this)
        
        // Create checkout session
        val description = "Payment for ${service.serviceName} on $bookingDate"
        paymentApiClient.createGCashCheckout(
            amount = totalPrice.toDouble(),
            description = description,
            token = token
        ) { checkoutUrl, error ->
            runOnUiThread {
                progressDialog.dismiss()
                
                if (error != null) {
                    Log.e(tag, "Error creating GCash checkout", error)
                    Toast.makeText(
                        this,
                        "Error initiating payment: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@runOnUiThread
                }
                
                if (checkoutUrl != null) {
                    // Store these details for later use when payment completes
                    val preferences = getSharedPreferences("PendingBooking", MODE_PRIVATE)
                    preferences.edit().apply {
                        putLong("serviceId", service.serviceId)
                        putLong("customerId", userId)
                        putString("bookingDate", bookingDate)
                        putString("timeSlot", timeSlot)
                        putString("specialInstructions", specialInstructions)
                        putString("paymentMethod", "GCash")
                        putInt("totalPrice", totalPrice)
                        apply()
                    }
                    
                    // Launch WebView for payment
                    val intent = Intent(this, PaymentWebViewActivity::class.java)
                    intent.putExtra(PaymentWebViewActivity.EXTRA_PAYMENT_URL, checkoutUrl)
                    startActivityForResult(intent, PAYMENT_REQUEST_CODE)
                } else {
                    Toast.makeText(
                        this,
                        "Failed to get payment checkout URL",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    /**
     * Process a booking with cash payment
     */
    private fun processCashBooking(
        service: Service,
        bookingDate: String,
        timeSlot: String,
        totalPrice: Int,
        customerAddress: Address?,
        specialInstructions: String,
        paymentMethod: String
    ) {
        // Show confirmation progress
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Creating your booking...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        
        // Get user ID from SharedPreferences
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userId = try {
            sharedPrefs.getLong("userId", 0)
        } catch (e: ClassCastException) {
            val userIdStr = sharedPrefs.getString("userId", "0")
            userIdStr?.toLongOrNull() ?: 0
        }
        
        // Create booking with API
        val bookingApiClient = BookingApiClient(this)
        
        // Extract only the start time from the time slot (e.g., "01:30:00" from "01:30:00-01:45:00")
        val startTime = timeSlot.split("-").firstOrNull() ?: timeSlot
        
        bookingApiClient.createBooking(
            serviceId = service.serviceId,
            customerId = userId,
            bookingDate = bookingDate,
            token = token,
            note = specialInstructions,
            paymentMethod = paymentMethod,
            bookingTime = startTime,
            totalCost = totalPrice.toDouble()
        ) { booking, error ->
            runOnUiThread {
                progressDialog.dismiss()
                
                if (error != null) {
                    Log.e(tag, "Error creating booking", error)
                    Toast.makeText(
                        this,
                        "Error creating booking: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@runOnUiThread
                }
                
                if (booking != null) {
                    // Show success dialog
                    showPaymentSuccessDialog(booking)
                } else {
                    Toast.makeText(
                        this,
                        "Unknown error occurred while creating booking",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    /**
     * Shows a dialog confirming successful payment and booking
     */
    private fun showPaymentSuccessDialog(booking: Booking) {
        val dialog = Dialog(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar_MinWidth)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_payment_success)
        
        // Make dialog full width with rounded corners
        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        
        // Booking history button
        dialog.findViewById<Button>(R.id.btnBookingHistory).setOnClickListener {
            dialog.dismiss()
            // Navigate to bookings history
            val intent = Intent(this, ProfileManagementActivity::class.java)
            intent.putExtra("tab_index", 2) // Booking history tab
            startActivity(intent)
            finish()
        }
        
        // Browse more services button
        dialog.findViewById<Button>(R.id.btnBrowseServices).setOnClickListener {
            dialog.dismiss()
            // Stay on the current screen
            Toast.makeText(
                this@BrowseServicesActivity,
                "Continue browsing services",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        dialog.show()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Replace the deprecated onBackPressed with the recommended approach
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        super.onBackPressed()
    }

    // Handle result from ProfileManagementActivity
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PAYMENT_REQUEST_CODE) {
            when (resultCode) {
                PaymentWebViewActivity.RESULT_PAYMENT_SUCCESS -> {
                    // Payment was successful, create the booking
                    val preferences = getSharedPreferences("PendingBooking", MODE_PRIVATE)
                    val serviceId = preferences.getLong("serviceId", 0)
                    val customerId = preferences.getLong("customerId", 0)
                    val bookingDate = preferences.getString("bookingDate", "") ?: ""
                    val timeSlot = preferences.getString("timeSlot", "") ?: ""
                    val specialInstructions = preferences.getString("specialInstructions", "") ?: ""
                    val paymentMethod = preferences.getString("paymentMethod", "GCash") ?: "GCash"
                    val totalPrice = preferences.getInt("totalPrice", 0)
                    
                    // Extract only the start time from the time slot
                    val startTime = timeSlot.split("-").firstOrNull() ?: timeSlot
                    
                    // Show progress dialog
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("Creating your booking...")
                    progressDialog.setCancelable(false)
                    progressDialog.show()
                    
                    // Create booking with API
                    val bookingApiClient = BookingApiClient(this)
                    bookingApiClient.createBooking(
                        serviceId = serviceId,
                        customerId = customerId,
                        bookingDate = bookingDate,
                        token = token,
                        note = specialInstructions,
                        paymentMethod = paymentMethod,
                        bookingTime = startTime,
                        totalCost = totalPrice.toDouble()
                    ) { booking, error ->
                        runOnUiThread {
                            progressDialog.dismiss()
                            
                            if (error != null) {
                                Log.e(tag, "Error creating booking after payment", error)
                                Toast.makeText(
                                    this,
                                    "Error creating booking: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@runOnUiThread
                            }
                            
                            if (booking != null) {
                                // Clear pending booking data
                                preferences.edit().clear().apply()
                                
                                // Show success dialog
                                showPaymentSuccessDialog(booking)
                            } else {
                                Toast.makeText(
                                    this,
                                    "Unknown error occurred while creating booking",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
                PaymentWebViewActivity.RESULT_PAYMENT_CANCELLED -> {
                    Toast.makeText(this, "Payment was cancelled", Toast.LENGTH_LONG).show()
                }
                else -> {
                    Toast.makeText(this, "Payment process was interrupted", Toast.LENGTH_LONG).show()
                }
            }
        } else if (requestCode == ADDRESS_UPDATE_REQUEST_CODE) {
            // If there's an active dialog and address TextView, refresh it
            if (activeBookingDialog != null && activeBookingDialog?.isShowing == true 
                && activeAddressTextView != null) {
                
                // Get current user ID
                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val userId = try {
                    sharedPref.getLong("userId", 0)
                } catch (e: ClassCastException) {
                    val userIdStr = sharedPref.getString("userId", "0")
                    userIdStr?.toLongOrNull() ?: 0
                }
                
                // Fetch updated address
                Log.d(tag, "Refreshing address after returning from profile management")
                activeAddressTextView?.let { tvAddress ->
                    fetchAddressesForCustomer(userId, tvAddress)
                }
            }
        }
    }
} 