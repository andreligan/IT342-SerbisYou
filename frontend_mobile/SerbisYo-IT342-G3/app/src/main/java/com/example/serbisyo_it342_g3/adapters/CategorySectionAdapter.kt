package com.example.serbisyo_it342_g3.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.data.Service
import com.example.serbisyo_it342_g3.utils.ImageUtils

// Data class to hold category information and services
data class CategorySection(val categoryName: String, val services: List<Service>)

class CategorySectionAdapter(
    private val categorySections: List<CategorySection>,
    private val onEditClick: (Service) -> Unit,
    private val onDeleteClick: (Service) -> Unit,
    private val onAddImageClick: (Service) -> Unit
) : RecyclerView.Adapter<CategorySectionAdapter.CategorySectionViewHolder>() {

    private val tag = "CategorySectionAdapter"

    inner class CategorySectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvItemCount: TextView = itemView.findViewById(R.id.tvItemCount)
        val rvCategoryServices: RecyclerView = itemView.findViewById(R.id.rvCategoryServices)
        val singleServiceContainer: FrameLayout = itemView.findViewById(R.id.singleServiceContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategorySectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_category_section, parent, false)
        return CategorySectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategorySectionViewHolder, position: Int) {
        val categorySection = categorySections[position]
        
        // Set category name
        holder.tvCategoryName.text = categorySection.categoryName
        
        // Set item count
        val itemCount = categorySection.services.size
        holder.tvItemCount.text = itemCount.toString()
        
        // Check if category has only one service or multiple services
        if (itemCount == 1) {
            Log.d(tag, "Category ${categorySection.categoryName} has only one service. Using full-width layout.")
            // Hide RecyclerView and show single service container
            holder.rvCategoryServices.visibility = View.GONE
            holder.singleServiceContainer.visibility = View.VISIBLE
            
            // Setup the single service view
            setupSingleServiceView(holder.singleServiceContainer, categorySection.services[0])
        } else {
            Log.d(tag, "Category ${categorySection.categoryName} has $itemCount services. Using horizontal RecyclerView.")
            // Show RecyclerView and hide single service container
            holder.rvCategoryServices.visibility = View.VISIBLE
            holder.singleServiceContainer.visibility = View.GONE
            
            // Setup the RecyclerView for multiple services
            holder.rvCategoryServices.layoutManager = LinearLayoutManager(
                holder.itemView.context,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            
            // Create and set adapter for category services
            val adapter = ServiceCardAdapter(
                categorySection.services,
                onEditClick,
                onDeleteClick,
                onAddImageClick
            )
            holder.rvCategoryServices.adapter = adapter
        }
    }
    
    private fun setupSingleServiceView(container: FrameLayout, service: Service) {
        // Inflate the full-width service card layout
        container.removeAllViews()
        val view = LayoutInflater.from(container.context)
            .inflate(R.layout.item_service_card_fullwidth, container, true)
        
        // Find views
        val cardView = view.findViewById<CardView>(R.id.cardView)
        val ivServiceImage = view.findViewById<ImageView>(R.id.ivServiceImage)
        val tvNoImage = view.findViewById<TextView>(R.id.tvNoImage)
        val btnAddImage = view.findViewById<Button>(R.id.btnAddImage)
        val tvServiceName = view.findViewById<TextView>(R.id.tvServiceName)
        val tvCategory = view.findViewById<TextView>(R.id.tvCategory)
        val tvDuration = view.findViewById<TextView>(R.id.tvDuration)
        val tvPrice = view.findViewById<TextView>(R.id.tvPrice)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val btnEdit = view.findViewById<ImageButton>(R.id.btnEdit)
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDelete)
        
        // Set data
        tvServiceName.text = service.serviceName
        tvCategory.text = service.category?.categoryName ?: "Uncategorized"
        tvDuration.text = "${service.durationEstimate ?: "N/A"}"
        tvPrice.text = "₱${service.effectivePrice ?: "N/A"}"
        tvDescription.text = service.serviceDescription
        
        // Handle image
        if (!service.imageUrl.isNullOrEmpty()) {
            Log.d(tag, "Service has image URL: ${service.imageUrl}")
            tvNoImage.visibility = View.GONE
            btnAddImage.visibility = View.GONE
            ivServiceImage.visibility = View.VISIBLE
            
            // Load image using ImageUtils with context
            val imageUrl = ImageUtils.getFullImageUrl(service.imageUrl, container.context)
            ImageUtils.loadImageAsync(imageUrl, ivServiceImage)
        } else {
            Log.d(tag, "Service has no image")
            tvNoImage.visibility = View.VISIBLE
            btnAddImage.visibility = View.VISIBLE
            ivServiceImage.visibility = View.GONE
        }
        
        // Set click listeners
        btnEdit.setOnClickListener { onEditClick(service) }
        btnDelete.setOnClickListener { onDeleteClick(service) }
        btnAddImage.setOnClickListener { onAddImageClick(service) }
    }

    override fun getItemCount(): Int {
        return categorySections.size
    }
} 