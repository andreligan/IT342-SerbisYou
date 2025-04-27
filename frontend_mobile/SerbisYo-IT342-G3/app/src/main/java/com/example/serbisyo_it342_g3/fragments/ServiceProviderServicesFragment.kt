package com.example.serbisyo_it342_g3.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.AddServiceActivity
import com.example.serbisyo_it342_g3.EditServiceActivity
import com.example.serbisyo_it342_g3.adapters.CategorySection
import com.example.serbisyo_it342_g3.adapters.CategorySectionAdapter
import com.example.serbisyo_it342_g3.api.ServiceApiClient
import com.example.serbisyo_it342_g3.data.Service
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.util.Base64
import com.example.serbisyo_it342_g3.api.ImageUploadApiClient
import com.example.serbisyo_it342_g3.utils.ImageUtils

class ServiceProviderServicesFragment : Fragment() {
    private val tag = "SPServicesFragment"
    
    private lateinit var btnAddService: Button
    private lateinit var fabAddService: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoServices: TextView
    private lateinit var tvErrorMessage: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var servicesContainer: LinearLayout
    
    private lateinit var serviceApiClient: ServiceApiClient
    private lateinit var imageUploadApiClient: ImageUploadApiClient
    private var servicesList: MutableList<Service> = mutableListOf()
    private var categorizedServices: MutableMap<String, MutableList<Service>> = mutableMapOf()
    
    private var token: String = ""
    private var userId: Long = 0
    private var providerId: Long = 0
    private var currentSelectedServiceForImage: Service? = null
    
    private val getImageContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null && currentSelectedServiceForImage != null) {
                uploadServiceImage(imageUri, currentSelectedServiceForImage!!)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_provider_services, container, false)
        
        // Get arguments
        arguments?.let {
            userId = it.getLong("userId", 0)
            token = it.getString("token", "") ?: ""
            providerId = it.getLong("providerId", 0)
        }

        // Initialize API clients
        serviceApiClient = ServiceApiClient(requireContext())
        imageUploadApiClient = ImageUploadApiClient(requireContext())
        
        // Initialize views
        btnAddService = view.findViewById(R.id.btnAddService)
        fabAddService = view.findViewById(R.id.fabAddService)
        progressBar = view.findViewById(R.id.progressBar)
        tvNoServices = view.findViewById(R.id.tvNoServices)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)
        tabLayout = view.findViewById(R.id.tabLayout)
        servicesContainer = view.findViewById(R.id.servicesContainer)
        
        // Setup buttons
        setupButtons()
        
        // Setup TabLayout
        setupTabLayout()
        
        // Load services
        loadServices()
        
        return view
    }
    
    private fun setupButtons() {
        btnAddService.setOnClickListener {
            navigateToAddService()
        }
        
        fabAddService.setOnClickListener {
            navigateToAddService()
        }
    }
    
    private fun setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText("All"))
        
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                filterServicesByCategory(tab.position)
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Not needed
            }
            
            override fun onTabReselected(tab: TabLayout.Tab) {
                // Not needed
            }
        })
    }
    
    private fun loadServices() {
        progressBar.visibility = View.VISIBLE
        tvNoServices.visibility = View.GONE
        tvErrorMessage.visibility = View.GONE
        servicesContainer.visibility = View.GONE
        
        if (providerId <= 0) {
            // For a new service provider, check if they exist in the system first
            checkProviderExists()
            return
        }
        
        serviceApiClient.getServicesByProviderId(providerId, token) { services, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(tag, "Error loading services", error)
                    
                    // Could be a new account - check if provider exists
                    checkProviderExists()
                    return@runOnUiThread
                }
                
                // Update the services list
                servicesList.clear()
                categorizedServices.clear()
                
                if (services != null && services.isNotEmpty()) {
                    servicesList.addAll(services)
                    
                    // Group services by category
                    categorizeServices(services)
                    
                    // Update TabLayout with categories
                    updateTabsWithCategories()
                    
                    // Display services by category
                    displayServicesByCategory()
                    
                    servicesContainer.visibility = View.VISIBLE
                    tvNoServices.visibility = View.GONE
                } else {
                    // Show "No services" message for this provider
                    tvNoServices.visibility = View.VISIBLE
                    servicesContainer.visibility = View.GONE
                }
            }
        }
    }
    
    private fun checkProviderExists() {
        val serviceApiClient = ServiceApiClient(requireContext())
        
        // Try to find the provider by user ID
        serviceApiClient.getProviderIdByUserId(userId, token) { providerId, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null || providerId == 0L) {
                    Log.e(tag, "Error getting provider ID: ${error?.message ?: "Provider ID is 0"}")
                    
                    // This is likely a new account with no services
                    tvNoServices.text = "You haven't added any services yet. Add your first service to get started!"
                    tvNoServices.visibility = View.VISIBLE
                    servicesContainer.visibility = View.GONE
                } else {
                    // Update the provider ID
                    this.providerId = providerId
                    
                    // Save to SharedPreferences for future use
                    saveProviderId(providerId)
                    
                    // Now try loading services again with the correct ID
                    loadServicesWithId(providerId)
                }
            }
        }
    }
    
    private fun saveProviderId(providerId: Long) {
        // Save in both SharedPreferences for consistency
        val userPrefs = requireActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
        userPrefs.edit().putLong("providerId", providerId).apply()
        
        val userPrefs2 = requireActivity().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        userPrefs2.edit().putLong("providerId", providerId).apply()
        
        Log.d(tag, "Saved provider ID: $providerId to SharedPreferences")
    }
    
    private fun loadServicesWithId(providerId: Long) {
        progressBar.visibility = View.VISIBLE
        
        serviceApiClient.getServicesByProviderId(providerId, token) { services, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(tag, "Error loading services with ID $providerId", error)
                    tvErrorMessage.text = "Could not load services: ${error.message}"
                    tvErrorMessage.visibility = View.VISIBLE
                    return@runOnUiThread
                }
                
                // Update the services list
                servicesList.clear()
                categorizedServices.clear()
                
                if (services != null && services.isNotEmpty()) {
                    servicesList.addAll(services)
                    
                    // Group services by category
                    categorizeServices(services)
                    
                    // Update TabLayout with categories
                    updateTabsWithCategories()
                    
                    // Display services by category
                    displayServicesByCategory()
                    
                    servicesContainer.visibility = View.VISIBLE
                    tvNoServices.visibility = View.GONE
                } else {
                    // Show empty state for new provider
                    tvNoServices.text = "You haven't added any services yet. Add your first service to get started!"
                    tvNoServices.visibility = View.VISIBLE
                    servicesContainer.visibility = View.GONE
                }
            }
        }
    }
    
    private fun categorizeServices(services: List<Service>) {
        // First, group by category
        for (service in services) {
            val categoryName = service.category?.categoryName ?: "Uncategorized"
            if (!categorizedServices.containsKey(categoryName)) {
                categorizedServices[categoryName] = mutableListOf()
            }
            categorizedServices[categoryName]?.add(service)
        }
    }
    
    private fun updateTabsWithCategories() {
        // Clear existing tabs except "All"
        while (tabLayout.tabCount > 1) {
            tabLayout.removeTabAt(1)
        }
        
        // Add a tab for each category
        for (category in categorizedServices.keys) {
            val count = categorizedServices[category]?.size ?: 0
            tabLayout.addTab(tabLayout.newTab().setText("$category ($count)"))
        }
    }
    
    private fun displayServicesByCategory() {
        servicesContainer.removeAllViews()
        
        // Convert categorizedServices map to a list of CategorySection objects
        val categorySections = categorizedServices.map { (categoryName, services) ->
            CategorySection(categoryName, services)
        }
        
        // Create and set the adapter
        val adapter = CategorySectionAdapter(
            categorySections,
            onEditClick = { service -> editService(service) },
            onDeleteClick = { service -> confirmDeleteService(service) },
            onAddImageClick = { service -> selectImageForService(service) }
        )
        
        // Add RecyclerView for each category
        for (i in 0 until adapter.itemCount) {
            val viewHolder = adapter.createViewHolder(
                servicesContainer,
                adapter.getItemViewType(i)
            )
            adapter.bindViewHolder(viewHolder, i)
            servicesContainer.addView(viewHolder.itemView)
        }
    }
    
    private fun filterServicesByCategory(position: Int) {
        if (position == 0) {
            // "All" tab - show all categories
            displayServicesByCategory()
            return
        }
        
        servicesContainer.removeAllViews()
        
        // Get the selected category name (adjust for the "All" tab)
        val categories = categorizedServices.keys.toList()
        if (position - 1 < categories.size) {
            val selectedCategory = categories[position - 1]
            val services = categorizedServices[selectedCategory] ?: listOf()
            
            // Create a single category section
            val categorySections = listOf(CategorySection(selectedCategory, services))
            
            // Create and set the adapter
            val adapter = CategorySectionAdapter(
                categorySections,
                onEditClick = { service -> editService(service) },
                onDeleteClick = { service -> confirmDeleteService(service) },
                onAddImageClick = { service -> selectImageForService(service) }
            )
            
            // Add RecyclerView for the category
            val viewHolder = adapter.createViewHolder(
                servicesContainer,
                adapter.getItemViewType(0)
            )
            adapter.bindViewHolder(viewHolder, 0)
            servicesContainer.addView(viewHolder.itemView)
        }
    }
    
    private fun editService(service: Service) {
        val intent = Intent(requireContext(), EditServiceActivity::class.java)
        intent.putExtra("SERVICE_ID", service.serviceId)
        intent.putExtra("PROVIDER_ID", providerId)
        intent.putExtra("SERVICE_NAME", service.serviceName)
        intent.putExtra("CATEGORY_ID", service.category?.categoryId)
        intent.putExtra("SERVICE_DESCRIPTION", service.serviceDescription)
        intent.putExtra("PRICE_RANGE", service.effectivePrice)
        intent.putExtra("DURATION_ESTIMATE", service.durationEstimate)
        startActivity(intent)
    }
    
    private fun confirmDeleteService(service: Service) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Service")
            .setMessage("Are you sure you want to delete ${service.serviceName}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteService(service)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteService(service: Service) {
        progressBar.visibility = View.VISIBLE
        
        serviceApiClient.deleteService(service.serviceId, token) { success, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Toast.makeText(
                        requireContext(),
                        "Error deleting service: ${error.message ?: "Unknown error"}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@runOnUiThread
                }
                
                if (success) {
                    Toast.makeText(
                        requireContext(),
                        "${service.serviceName} has been deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Reload services
                    loadServices()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to delete service",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    private fun selectImageForService(service: Service) {
        currentSelectedServiceForImage = service
        
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getImageContent.launch(intent)
    }
    
    private fun uploadServiceImage(imageUri: Uri, service: Service) {
        progressBar.visibility = View.VISIBLE
        
        try {
            // Convert image to base64
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
            
            // Resize the image to reduce file size using our utility class
            val resizedBitmap = ImageUtils.resizeBitmap(bitmap, 800, 800)
            val base64Image = ImageUtils.bitmapToBase64(resizedBitmap)
            
            Log.d(tag, "Image size: ${base64Image.length} characters")
            
            // Display a progress dialog with message
            val progressDialog = AlertDialog.Builder(requireContext())
                .setTitle("Uploading Image")
                .setMessage("Please wait, uploading image for ${service.serviceName}...")
                .setCancelable(false)
                .create()
            progressDialog.show()
            
            // Upload image - using multipart form data now
            serviceApiClient.uploadServiceImage(
                service.serviceId,
                base64Image,
                token
            ) { success, error ->
                requireActivity().runOnUiThread {
                    progressDialog.dismiss()
                    progressBar.visibility = View.GONE
                    
                    if (error != null) {
                        // Show more detailed error message
                        val errorMessage = when {
                            error.message?.contains("413") == true -> 
                                "Image is too large. Please choose a smaller image or resize it."
                            error.message?.contains("404") == true -> 
                                "Service not found or endpoint not available. Please try again later."
                            error.message?.contains("401") == true -> 
                                "Authentication error. Please log in again."
                            error.message?.contains("500") == true -> {
                                Log.e(tag, "Server error 500: ${error.message}")
                                "Server error: The image couldn't be processed. Please try a different image."
                            }
                            else -> "Error uploading image: ${error.message ?: "Unknown error"}"
                        }
                        
                        Log.e(tag, "Image upload error: $errorMessage")
                        
                        AlertDialog.Builder(requireContext())
                            .setTitle("Upload Failed")
                            .setMessage(errorMessage)
                            .setPositiveButton("OK", null)
                            .show()
                        
                        return@runOnUiThread
                    }
                    
                    if (success) {
                        Toast.makeText(
                            requireContext(),
                            "Image uploaded successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Instead of trying to update the local list, just reload all services
                        // This ensures we get the fresh data with updated image paths
                        loadServices()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to upload image",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Log an error for troubleshooting
                        Log.e(tag, "Image upload failed but no exception was thrown")
                    }
                }
            }
        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            Log.e(tag, "Error preparing image for upload", e)
            
            Toast.makeText(
                requireContext(),
                "Error preparing image: ${e.message ?: "Unknown error"}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun navigateToAddService() {
        val intent = Intent(requireContext(), AddServiceActivity::class.java)
        intent.putExtra("PROVIDER_ID", providerId)
        startActivity(intent)
    }
    
    override fun onResume() {
        super.onResume()
        // Reload services when returning to this fragment
        loadServices()
    }
    
    companion object {
        @JvmStatic
        fun newInstance(userId: Long, token: String, providerId: Long) =
            ServiceProviderServicesFragment().apply {
                arguments = Bundle().apply {
                    putLong("userId", userId)
                    putString("token", token)
                    putLong("providerId", providerId)
                }
            }
    }
}