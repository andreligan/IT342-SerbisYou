package com.example.serbisyo_it342_g3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.data.Service

data class CategorySection(
    val categoryName: String,
    val services: List<Service>
)

class CategorySectionAdapter(
    private val categories: List<CategorySection>,
    private val onEditClick: (Service) -> Unit,
    private val onDeleteClick: (Service) -> Unit,
    private val onAddImageClick: (Service) -> Unit
) : RecyclerView.Adapter<CategorySectionAdapter.CategorySectionViewHolder>() {

    class CategorySectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvItemCount: TextView = itemView.findViewById(R.id.tvItemCount)
        val rvCategoryServices: RecyclerView = itemView.findViewById(R.id.rvCategoryServices)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategorySectionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_category_section, parent, false)
        return CategorySectionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CategorySectionViewHolder, position: Int) {
        val categorySection = categories[position]
        
        // Set category name
        holder.tvCategoryName.text = categorySection.categoryName
        
        // Set item count
        holder.tvItemCount.text = categorySection.services.size.toString()
        
        // Set up RecyclerView for services
        val serviceAdapter = ServiceCardAdapter(
            categorySection.services,
            onEditClick,
            onDeleteClick,
            onAddImageClick
        )
        
        holder.rvCategoryServices.adapter = serviceAdapter
    }

    override fun getItemCount(): Int = categories.size
} 