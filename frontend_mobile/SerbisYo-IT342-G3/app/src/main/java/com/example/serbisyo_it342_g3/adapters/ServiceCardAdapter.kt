package com.example.serbisyo_it342_g3.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.api.BaseApiClient
import com.example.serbisyo_it342_g3.data.Service
import com.example.serbisyo_it342_g3.utils.ImageUtils

class ServiceCardAdapter(
    private val services: List<Service>,
    private val onEditClick: (Service) -> Unit,
    private val onDeleteClick: (Service) -> Unit,
    private val onAddImageClick: (Service) -> Unit
) : RecyclerView.Adapter<ServiceCardAdapter.ServiceCardViewHolder>() {
    
    private val TAG = "ServiceCardAdapter"

    class ServiceCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardService: CardView = itemView.findViewById(R.id.cardService)
        val ivServiceImage: ImageView = itemView.findViewById(R.id.ivServiceImage)
        val tvNoImage: TextView = itemView.findViewById(R.id.tvNoImage)
        val btnAddImage: Button = itemView.findViewById(R.id.btnAddImage)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvServiceName: TextView = itemView.findViewById(R.id.tvServiceName)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceCardViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_card, parent, false)
        return ServiceCardViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ServiceCardViewHolder, position: Int) {
        val service = services[position]
        val context = holder.itemView.context
        
        // Debug logging for image properties
        Log.d(TAG, "Service ${service.serviceId} (${service.serviceName}) - serviceImage: ${service.serviceImage}, imageUrl property: ${service.imageUrl}")
        
        // Set service name
        holder.tvServiceName.text = service.serviceName
        
        // Set category
        holder.tvCategory.text = service.category?.categoryName ?: "Uncategorized"
        
        // Format duration with proper text
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
        holder.tvDuration.text = formattedDuration
        
        // Format price with currency symbol
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
        holder.tvPrice.text = formattedPrice
        
        // Set price text color to green
        holder.tvPrice.setTextColor(holder.itemView.context.getColor(R.color.primary_green))
        
        // Set description
        holder.tvDescription.text = service.serviceDescription
        
        // Handle image loading - check if service has an image URL
        // Check both serviceImage and imageUrl to handle both properties
        val serviceImagePath = service.serviceImage ?: service.imageUrl
        
        if (!serviceImagePath.isNullOrBlank()) {
            // Hide No Image text and update Add Image button text to "Change Image"
            holder.tvNoImage.visibility = View.GONE
            holder.btnAddImage.text = "Change Image"
            holder.btnAddImage.visibility = View.VISIBLE
            holder.ivServiceImage.visibility = View.VISIBLE
            
            // Use our utility class to get the properly formatted image URL
            var imageUrl = ImageUtils.getFullImageUrl(serviceImagePath, context)
            
            // Add timestamp to prevent caching issues
            if (imageUrl != null) {
                imageUrl += if (imageUrl.contains("?")) "&" else "?"
                imageUrl += "t=${System.currentTimeMillis()}"
            }
            
            Log.d(TAG, "Loading image from: $imageUrl")
            
            // Use our custom image loader to load the image asynchronously
            ImageUtils.loadImageAsync(imageUrl, holder.ivServiceImage)
                
        } else {
            // No image - show placeholder and Add Image button
            holder.ivServiceImage.setImageResource(R.drawable.ic_image_placeholder)
            holder.tvNoImage.visibility = View.VISIBLE
            holder.btnAddImage.text = "Add Image"
            holder.btnAddImage.visibility = View.VISIBLE
        }
        
        // Set click listener for Add/Change Image button
        holder.btnAddImage.setOnClickListener {
            onAddImageClick(service)
        }
        
        // Set click listeners for edit and delete buttons
        holder.btnEdit.setOnClickListener {
            onEditClick(service)
        }
        
        holder.btnDelete.setOnClickListener {
            onDeleteClick(service)
        }
    }

    override fun getItemCount(): Int = services.size
}