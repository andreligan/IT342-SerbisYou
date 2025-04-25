package com.example.serbisyo_it342_g3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.data.Service

class BrowseServiceAdapter(
    private val services: List<Service>,
    private val onServiceClick: (Service) -> Unit
) : RecyclerView.Adapter<BrowseServiceAdapter.ServiceViewHolder>() {
    
    class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.serviceCard)
        val categoryBadge: TextView = view.findViewById(R.id.tvCategoryBadge)
        val serviceName: TextView = view.findViewById(R.id.tvServiceName)
        val serviceDescription: TextView = view.findViewById(R.id.tvServiceDescription)
        val duration: TextView = view.findViewById(R.id.tvDuration)
        val provider: TextView = view.findViewById(R.id.tvProvider)
        val price: TextView = view.findViewById(R.id.tvPrice)
        val serviceImage: ImageView = view.findViewById(R.id.ivServiceImage)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_browse_service, parent, false)
        return ServiceViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        
        // Set category badge
        holder.categoryBadge.text = service.category?.categoryName ?: "General"
        
        // Set service name and description
        holder.serviceName.text = service.serviceName
        holder.serviceDescription.text = service.serviceDescription
        
        // Set duration and provider
        holder.duration.text = service.durationEstimate
        holder.provider.text = "Provider: ${service.provider?.businessName ?: service.provider?.firstName}"
        
        // Set price
        holder.price.text = "₱${service.effectivePrice}"
        
        // Service image will be set later if available
        
        // Set click listener
        holder.cardView.setOnClickListener {
            onServiceClick(service)
        }
    }
    
    override fun getItemCount() = services.size
} 