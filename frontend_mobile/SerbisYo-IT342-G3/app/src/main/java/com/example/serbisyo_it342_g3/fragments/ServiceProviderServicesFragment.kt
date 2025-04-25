package com.example.serbisyo_it342_g3.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.AddServiceActivity
import com.example.serbisyo_it342_g3.ManageServicesActivity
import com.example.serbisyo_it342_g3.adapters.ServiceProviderServiceAdapter
import com.example.serbisyo_it342_g3.api.ServiceApiClient
import com.example.serbisyo_it342_g3.data.Service
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ServiceProviderServicesFragment : Fragment() {
    private val tag = "SPServicesFragment"
    
    private lateinit var rvServices: RecyclerView
    private lateinit var btnAddService: Button
    private lateinit var btnManageServices: Button
    private lateinit var fabAddService: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoServices: TextView
    private lateinit var tvErrorMessage: TextView
    
    private lateinit var serviceAdapter: ServiceProviderServiceAdapter
    private lateinit var serviceApiClient: ServiceApiClient
    private var servicesList: MutableList<Service> = mutableListOf()
    
    private var token: String = ""
    private var userId: Long = 0
    private var providerId: Long = 0

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

        // Initialize API client
        serviceApiClient = ServiceApiClient(requireContext())
        
        // Initialize views
        rvServices = view.findViewById(R.id.rvServices)
        btnAddService = view.findViewById(R.id.btnAddService)
        btnManageServices = view.findViewById(R.id.btnManageServices)
        fabAddService = view.findViewById(R.id.fabAddService)
        progressBar = view.findViewById(R.id.progressBar)
        tvNoServices = view.findViewById(R.id.tvNoServices)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup buttons
        setupButtons()
        
        // Load services
        loadServices()
        
        return view
    }

    private fun setupRecyclerView() {
        serviceAdapter = ServiceProviderServiceAdapter(
            servicesList,
            onServiceClick = { service ->
                // Handle service click - could show details or edit
                showServiceDetails(service)
            }
        )
        
        rvServices.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = serviceAdapter
        }
    }
    
    private fun setupButtons() {
        btnAddService.setOnClickListener {
            navigateToAddService()
        }
        
        btnManageServices.setOnClickListener {
            navigateToManageServices()
        }
        
        fabAddService.setOnClickListener {
            navigateToAddService()
        }
    }
    
    private fun loadServices() {
        progressBar.visibility = View.VISIBLE
        tvNoServices.visibility = View.GONE
        tvErrorMessage.visibility = View.GONE
        
        if (providerId <= 0) {
            tvErrorMessage.text = getString(R.string.provider_id_missing)
            tvErrorMessage.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            return
        }
        
        serviceApiClient.getServicesByProviderId(providerId, token) { services, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(tag, "Error loading services", error)
                    tvErrorMessage.text = getString(R.string.error_loading_services)
                    tvErrorMessage.visibility = View.VISIBLE
                    return@runOnUiThread
                }
                
                // Update the services list
                servicesList.clear()
                if (services != null && services.isNotEmpty()) {
                    servicesList.addAll(services)
                    serviceAdapter.notifyDataSetChanged()
                } else {
                    tvNoServices.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private fun showServiceDetails(service: Service) {
        // This would navigate to a service detail/edit screen
        val intent = Intent(context, AddServiceActivity::class.java).apply {
            putExtra("SERVICE_ID", service.serviceId)
            putExtra("IS_EDIT_MODE", true)
        }
        startActivity(intent)
    }
    
    private fun navigateToAddService() {
        val intent = Intent(context, AddServiceActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToManageServices() {
        val intent = Intent(context, ManageServicesActivity::class.java)
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