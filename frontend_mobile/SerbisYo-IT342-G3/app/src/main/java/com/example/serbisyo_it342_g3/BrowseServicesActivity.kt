package com.example.serbisyo_it342_g3

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.adapters.BrowseServiceAdapter
import com.example.serbisyo_it342_g3.api.ServiceApiClient
import com.example.serbisyo_it342_g3.data.Service
import com.example.serbisyo_it342_g3.data.ServiceCategory
import com.google.android.material.slider.RangeSlider
import java.text.NumberFormat
import java.util.*

class BrowseServicesActivity : AppCompatActivity() {
    private lateinit var rvServices: RecyclerView
    private lateinit var tvServicesCount: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var serviceAdapter: BrowseServiceAdapter
    private lateinit var serviceApiClient: ServiceApiClient
    private lateinit var filterLayout: LinearLayout
    private lateinit var categoryContainer: LinearLayout
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
    private val selectedCategories = mutableSetOf<String>()
    
    private var minPrice = 10f
    private var maxPrice = 2000f
    private var minRating = 0f
    private var minExperience = 0f
    private var sortOption = "recommended"
    
    private val tag = "BrowseServicesActivity"
    private var token: String = ""

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
        categoryContainer = findViewById(R.id.categoryContainer)
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
        
        // Setup filters
        setupFilters()
        
        // Load data
        loadData()
    }
    
    private fun setupRecyclerView() {
        serviceAdapter = BrowseServiceAdapter(filteredServices) { service ->
            // Handle service click - show details/booking
            Toast.makeText(this, "Selected: ${service.serviceName}", Toast.LENGTH_SHORT).show()
            // Could open ServiceDetailsActivity here in the future
        }
        
        rvServices.layoutManager = GridLayoutManager(this, 2)
        rvServices.adapter = serviceAdapter
    }
    
    private fun setupFilters() {
        // Price Range Slider
        priceRangeSlider.valueFrom = 10f
        priceRangeSlider.valueTo = 2000f
        priceRangeSlider.values = listOf(10f, 2000f)
        priceRangeSlider.stepSize = 10f
        
        val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
        formatter.currency = Currency.getInstance("PHP")
        formatter.maximumFractionDigits = 0
        
        priceRangeSlider.addOnChangeListener { slider, _, _ ->
            minPrice = slider.values[0]
            maxPrice = slider.values[1]
            tvMinPrice.text = formatter.format(minPrice).replace("PHP", "₱")
            tvMaxPrice.text = formatter.format(maxPrice).replace("PHP", "₱")
        }
        
        // Set initial values
        tvMinPrice.text = formatter.format(minPrice).replace("PHP", "₱")
        tvMaxPrice.text = formatter.format(maxPrice).replace("PHP", "₱")
        
        // Configure rating and experience sliders
        ratingSlider.valueFrom = 0f
        ratingSlider.valueTo = 5f
        ratingSlider.stepSize = 0.5f
        ratingSlider.values = listOf(0f)
        
        experienceSlider.valueFrom = 0f
        experienceSlider.valueTo = 20f
        experienceSlider.stepSize = 1f
        experienceSlider.values = listOf(0f)
        
        // Apply filters button
        btnApplyFilters.setOnClickListener {
            applyFilters()
        }
        
        // Reset filters button
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
                    
                    // Populate category checkboxes
                    setupCategoryCheckboxes()
                }
            }
        }
    }
    
    private fun setupCategoryCheckboxes() {
        categoryContainer.removeAllViews()
        
        for (category in categories) {
            val checkBox = CheckBox(this)
            checkBox.text = category.categoryName
            checkBox.id = View.generateViewId()
            
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCategories.add(category.categoryName)
                } else {
                    selectedCategories.remove(category.categoryName)
                }
            }
            
            categoryContainer.addView(checkBox)
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
                    
                    // Display all services initially
                    applyFilters()
                    
                    Toast.makeText(this, "Loaded ${servicesList.size} services", Toast.LENGTH_SHORT).show()
                } else {
                    tvServicesCount.text = "0 services found"
                }
            }
        }
    }
    
    private fun applyFilters() {
        filteredServices.clear()
        
        // Apply category filter
        var tempList = if (selectedCategories.isEmpty()) {
            allServices
        } else {
            allServices.filter { service ->
                selectedCategories.contains(service.category?.categoryName)
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
    
    private fun parsePrice(priceString: String): Float {
        return try {
            // Try to extract numeric value from price string
            priceString.replace("[^0-9.]".toRegex(), "").toFloatOrNull() ?: 0f
        } catch (e: Exception) {
            0f
        }
    }
    
    private fun resetFilters() {
        // Reset category selection
        selectedCategories.clear()
        for (i in 0 until categoryContainer.childCount) {
            val checkbox = categoryContainer.getChildAt(i) as? CheckBox
            checkbox?.isChecked = false
        }
        
        // Reset price range
        priceRangeSlider.values = listOf(10f, 2000f)
        
        // Reset rating and experience
        ratingSlider.values = listOf(0f)
        experienceSlider.values = listOf(0f)
        
        // Reset sort option
        sortGroup.check(R.id.rbRecommended)
        sortOption = "recommended"
        
        // Apply reset filters
        applyFilters()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
} 