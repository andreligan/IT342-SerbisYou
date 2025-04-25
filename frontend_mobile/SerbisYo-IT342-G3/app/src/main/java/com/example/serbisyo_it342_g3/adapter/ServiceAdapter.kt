package com.example.serbisyo_it342_g3.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.data.Service


class ServiceAdapter(
    private val services: List<Service>,
    private val onEditClick: (Service) -> Unit,
    private val onDeleteClick: (Service) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
        val tvServiceDescription: TextView = view.findViewById(R.id.tvServiceDescription)
        val tvPriceRange: TextView = view.findViewById(R.id.tvPriceRange)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
        val btnDelete: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]

        holder.tvServiceName.text = service.serviceName
        holder.tvServiceDescription.text = service.serviceDescription
        holder.tvPriceRange.text = "Price: ${service.effectivePrice}"
        holder.tvDuration.text = "Duration: ${service.durationEstimate}"
        holder.tvCategory.text = "Category: ${service.category?.categoryName}"

        holder.btnEdit.setOnClickListener { onEditClick(service) }
        holder.btnDelete.setOnClickListener { onDeleteClick(service) }
    }

    override fun getItemCount() = services.size
}