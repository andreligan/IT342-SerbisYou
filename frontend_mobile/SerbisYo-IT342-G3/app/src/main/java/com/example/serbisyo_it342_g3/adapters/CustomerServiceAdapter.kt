package com.example.serbisyo_it342_g3.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.data.Service

class CustomerServiceAdapter(
    private val services: List<Service>,
    private val onServiceClick: (Service) -> Unit
) : RecyclerView.Adapter<CustomerServiceAdapter.ServiceViewHolder>() {

    // Category colors for the indicator bar
    private val categoryColors = mapOf(
        "Cleaning" to "#4CAF50",
        "Plumbing" to "#2196F3",
        "Electrical" to "#FFC107",
        "Carpentry" to "#795548",
        "Gardening" to "#8BC34A",
        "Home Repair" to "#FF5722",
        "Painting" to "#9C27B0",
        "Appliance Repair" to "#F44336",
        "Computer Services" to "#3F51B5",
        "Moving Services" to "#009688",
        "Beauty & Wellness" to "#E91E63",
        "Tutoring" to "#673AB7",
        "Pet Care" to "#FF9800",
        "Event Planning" to "#607D8B",
        "Automotive" to "#00BCD4"
    )

    class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
        val tvServiceDescription: TextView = view.findViewById(R.id.tvServiceDescription)
        val tvPriceRange: TextView = view.findViewById(R.id.tvPriceRange)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvProviderName: TextView = view.findViewById(R.id.tvProviderName)
        val categoryIndicator: View = view.findViewById(R.id.categoryIndicator)
        val btnBookService: Button = view.findViewById(R.id.btnBookService)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_customer_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]

        // Set basic service information
        holder.tvServiceName.text = service.serviceName
        holder.tvServiceDescription.text = service.serviceDescription
        holder.tvPriceRange.text = "₱${service.effectivePrice}"
        holder.tvDuration.text = "${service.durationEstimate}"
        
        // Set category and provider information
        val categoryName = service.category?.categoryName ?: "General"
        holder.tvCategory.text = categoryName
        holder.tvProviderName.text = "Provider: ${service.provider?.businessName ?: service.provider?.firstName}"
        
        // Set category indicator color
        val color = categoryColors[categoryName] ?: "#4CAF50"  // Default to green if category not found
        holder.categoryIndicator.setBackgroundColor(Color.parseColor(color))
        
        // Set click listeners
        holder.btnBookService.setOnClickListener {
            onServiceClick(service)
        }
        
        holder.itemView.setOnClickListener {
            onServiceClick(service)
        }
    }

    override fun getItemCount() = services.size
} 