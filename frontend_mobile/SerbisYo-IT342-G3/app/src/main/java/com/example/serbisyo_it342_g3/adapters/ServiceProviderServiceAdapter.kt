package com.example.serbisyo_it342_g3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.data.Service

class ServiceProviderServiceAdapter(
    private val services: List<Service>,
    private val onServiceClick: (Service) -> Unit
) : RecyclerView.Adapter<ServiceProviderServiceAdapter.ServiceViewHolder>() {

    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvServiceName: TextView = itemView.findViewById(R.id.tvServiceName)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val cardService: CardView = itemView.findViewById(R.id.cardService)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_provider_service, parent, false)
        return ServiceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val currentService = services[position]
        
        holder.tvServiceName.text = currentService.serviceName
        holder.tvCategory.text = currentService.category?.categoryName ?: "Uncategorized"
        holder.tvPrice.text = currentService.effectivePrice
        holder.tvDescription.text = currentService.serviceDescription
        
        // Set click listener for the entire card
        holder.cardService.setOnClickListener {
            onServiceClick(currentService)
        }
    }

    override fun getItemCount(): Int {
        return services.size
    }
}