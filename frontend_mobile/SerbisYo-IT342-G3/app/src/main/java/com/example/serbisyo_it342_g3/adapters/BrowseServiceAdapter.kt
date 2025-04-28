package com.example.serbisyo_it342_g3.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.data.Service
import com.example.serbisyo_it342_g3.utils.ImageUtils

class BrowseServiceAdapter(
    private val services: List<Service>,
    private val onServiceClick: (Service) -> Unit
) : RecyclerView.Adapter<BrowseServiceAdapter.ServiceViewHolder>() {
    
    private val TAG = "BrowseServiceAdapter"
    
    class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.serviceCard)
        val categoryBadge: TextView = view.findViewById(R.id.tvCategoryBadge)
        val serviceName: TextView = view.findViewById(R.id.tvServiceName)
        val serviceDescription: TextView = view.findViewById(R.id.tvServiceDescription)
        val duration: TextView = view.findViewById(R.id.tvDuration)
        val provider: TextView = view.findViewById(R.id.tvProvider)
        val price: TextView = view.findViewById(R.id.tvPrice)
        val serviceImage: ImageView = view.findViewById(R.id.ivServiceImage)
        val noImageText: TextView = view.findViewById(R.id.tvNoImage)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val reviewsText: TextView = view.findViewById(R.id.tvReviews)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_browse_service, parent, false)
        return ServiceViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        val context = holder.itemView.context
        
        // Set category badge - using the standard primary_green color from the XML
        val categoryName = service.category?.categoryName ?: "General"
        holder.categoryBadge.text = categoryName
        
        // Set service name and description
        holder.serviceName.text = service.serviceName
        holder.serviceDescription.text = service.serviceDescription
        
        // Set duration and provider
        holder.duration.text = service.durationEstimate
        holder.provider.text = "Provider: ${service.provider?.businessName ?: service.provider?.firstName}"
        
        // Set price (styled with yellow background and white text in XML)
        holder.price.text = "₱${service.effectivePrice}"
        
        // Set reviews placeholder (for future implementation)
        holder.ratingBar.rating = 0f
        holder.reviewsText.text = "No reviews yet"
        
        // Load service image if available
        if (!service.imageUrl.isNullOrEmpty()) {
            Log.d(TAG, "Loading service image for ${service.serviceName}: ${service.imageUrl}")
            val imageUrl = ImageUtils.getFullImageUrl(service.imageUrl, context)
            
            // Add timestamp to prevent caching issues
            val finalUrl = if (imageUrl != null) {
                imageUrl + (if (imageUrl.contains("?")) "&" else "?") + "t=${System.currentTimeMillis()}"
            } else {
                null
            }
            
            if (finalUrl != null) {
                ImageUtils.loadImageAsync(finalUrl, holder.serviceImage)
                holder.noImageText.visibility = View.GONE
                Log.d(TAG, "Image URL for ${service.serviceName}: $finalUrl")
            } else {
                holder.serviceImage.setImageResource(R.drawable.ic_image_placeholder)
                holder.noImageText.visibility = View.VISIBLE
                Log.d(TAG, "Could not get full image URL for ${service.serviceName}")
            }
        } else {
            holder.serviceImage.setImageResource(R.drawable.ic_image_placeholder)
            holder.noImageText.visibility = View.VISIBLE
            Log.d(TAG, "No image for service: ${service.serviceName}")
        }
        
        // Set click listener
        holder.cardView.setOnClickListener {
            onServiceClick(service)
        }
    }
    
    override fun getItemCount() = services.size
} 