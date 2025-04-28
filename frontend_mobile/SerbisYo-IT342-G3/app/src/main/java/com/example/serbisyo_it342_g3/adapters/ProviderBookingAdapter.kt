package com.example.serbisyo_it342_g3.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.data.Booking
import java.text.SimpleDateFormat
import java.util.Locale

class ProviderBookingAdapter(
    private var bookings: List<Booking>,
    private val onConfirmClick: (Booking) -> Unit,
    private val onCancelClick: (Booking) -> Unit,
    private val onStartServiceClick: (Booking) -> Unit,
    private val onCompleteServiceClick: (Booking) -> Unit,
    private val onConfirmPaymentClick: (Booking) -> Unit,
    private val onViewDetailsClick: (Booking) -> Unit
) : RecyclerView.Adapter<ProviderBookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.bind(booking)
    }

    override fun getItemCount() = bookings.size

    fun updateBookings(newBookings: List<Booking>) {
        bookings = newBookings
        notifyDataSetChanged()
    }

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvServiceName: TextView = itemView.findViewById(R.id.tvServiceName)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvCustomerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        private val tvBookingDate: TextView = itemView.findViewById(R.id.tvBookingDate)
        private val tvBookingTime: TextView = itemView.findViewById(R.id.tvBookingTime)
        private val tvPaymentMethod: TextView = itemView.findViewById(R.id.tvPaymentMethod)
        private val tvTotalCost: TextView = itemView.findViewById(R.id.tvPrice) // Updated to use tvPrice instead of tvTotalCost
        private val tvNotes: TextView = itemView.findViewById(R.id.tvNotes) // Added missing tvNotes declaration
        
        private val notesContainer: LinearLayout = itemView.findViewById(R.id.notesContainer)
        private val pendingActionsLayout: LinearLayout = itemView.findViewById(R.id.pendingActionsLayout)
        private val confirmedActionsLayout: LinearLayout = itemView.findViewById(R.id.confirmedActionsLayout)
        private val inProgressActionsLayout: LinearLayout = itemView.findViewById(R.id.inProgressActionsLayout)
        private val cashPaymentLayout: LinearLayout = itemView.findViewById(R.id.cashPaymentLayout)
        
        private val btnConfirm: Button = itemView.findViewById(R.id.btnConfirm)
        private val btnCancel: Button = itemView.findViewById(R.id.btnCancel)
        private val btnStartService: Button = itemView.findViewById(R.id.btnStartService)
        private val btnCompleteService: Button = itemView.findViewById(R.id.btnCompleteService)
        private val btnViewDetails: Button = itemView.findViewById(R.id.btnViewDetails)
        private val btnConfirmPayment: Button = itemView.findViewById(R.id.btnConfirmPayment)

        fun bind(booking: Booking) {
            // Set basic booking information
            tvServiceName.text = booking.service?.serviceName ?: "Unknown Service"
            
            // Format status text and set appropriate background color
            tvStatus.text = booking.status
            setStatusAppearance(booking.status)
            
            // Set customer details
            val customerName = booking.customer?.let {
                "${it.firstName ?: ""} ${it.lastName ?: ""}"
            } ?: "Unknown Customer"
            tvCustomerName.text = customerName
            
            // Format and set booking date
            val formattedDate = try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val parsedDate = dateFormat.parse(booking.bookingDate)
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                outputFormat.format(parsedDate!!)
            } catch (e: Exception) {
                booking.bookingDate
            }
            tvBookingDate.text = formattedDate
            
            // Format and set booking time
            val formattedTime = try {
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val parsedTime = timeFormat.parse(booking.bookingTime ?: "00:00:00")
                val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                outputFormat.format(parsedTime!!)
            } catch (e: Exception) {
                booking.bookingTime ?: "Not specified"
            }
            tvBookingTime.text = formattedTime
            
            // Set payment information
            tvPaymentMethod.text = booking.paymentMethod ?: "Cash"
            tvTotalCost.text = "₱${String.format("%.2f", booking.totalCost)}"
            
            // Show or hide notes based on availability
            if (!booking.note.isNullOrEmpty()) {
                notesContainer.visibility = View.VISIBLE
                tvNotes.text = booking.note
            } else {
                notesContainer.visibility = View.GONE
            }
            
            // Set up action buttons based on booking status
            setupActionButtons(booking)
        }

        private fun setStatusAppearance(status: String?) {
            val context = itemView.context
            when (status) {
                "Pending" -> {
                    tvStatus.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_light))
                    tvStatus.setTextColor(Color.WHITE)
                }
                "Confirmed" -> {
                    tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.primary_green))
                    tvStatus.setTextColor(Color.WHITE)
                }
                "In Progress" -> {
                    tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.primary_yellow))
                    tvStatus.setTextColor(Color.WHITE)
                }
                "Completed" -> {
                    tvStatus.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
                    tvStatus.setTextColor(Color.WHITE)
                }
                "Cancelled" -> {
                    tvStatus.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_light))
                    tvStatus.setTextColor(Color.WHITE)
                }
                else -> {
                    tvStatus.setBackgroundColor(Color.GRAY)
                    tvStatus.setTextColor(Color.WHITE)
                }
            }
        }

        private fun setupActionButtons(booking: Booking) {
            // Hide all action layouts by default
            pendingActionsLayout.visibility = View.GONE
            confirmedActionsLayout.visibility = View.GONE
            inProgressActionsLayout.visibility = View.GONE
            cashPaymentLayout.visibility = View.GONE

            // Show appropriate action buttons based on status
            when (booking.status) {
                "Pending" -> {
                    pendingActionsLayout.visibility = View.VISIBLE
                    
                    btnConfirm.setOnClickListener { onConfirmClick(booking) }
                    btnCancel.setOnClickListener { onCancelClick(booking) }
                }
                "Confirmed" -> {
                    confirmedActionsLayout.visibility = View.VISIBLE
                    
                    btnStartService.setOnClickListener { onStartServiceClick(booking) }
                }
                "In Progress" -> {
                    inProgressActionsLayout.visibility = View.VISIBLE
                    
                    btnCompleteService.setOnClickListener { onCompleteServiceClick(booking) }
                    btnViewDetails.setOnClickListener { onViewDetailsClick(booking) }
                    
                    // If payment method is cash, also show payment confirmation button
                    if (booking.paymentMethod?.contains("Cash", ignoreCase = true) == true) {
                        cashPaymentLayout.visibility = View.VISIBLE
                        btnConfirmPayment.setOnClickListener { onConfirmPaymentClick(booking) }
                    }
                }
                "Completed" -> {
                    // No action buttons for completed bookings
                    // Allow viewing details in case this is needed
                    inProgressActionsLayout.visibility = View.VISIBLE
                    btnCompleteService.visibility = View.GONE
                    btnViewDetails.setOnClickListener { onViewDetailsClick(booking) }
                    
                    // Layout parameters to make the view details button take full width
                    val params = btnViewDetails.layoutParams as LinearLayout.LayoutParams
                    params.width = LinearLayout.LayoutParams.MATCH_PARENT
                    params.weight = 1f
                    params.marginStart = 0
                    btnViewDetails.layoutParams = params
                }
                "Cancelled" -> {
                    // No action buttons for cancelled bookings
                    // Allow viewing details in case this is needed
                    inProgressActionsLayout.visibility = View.VISIBLE
                    btnCompleteService.visibility = View.GONE
                    btnViewDetails.setOnClickListener { onViewDetailsClick(booking) }
                    
                    // Layout parameters to make the view details button take full width
                    val params = btnViewDetails.layoutParams as LinearLayout.LayoutParams
                    params.width = LinearLayout.LayoutParams.MATCH_PARENT
                    params.weight = 1f
                    params.marginStart = 0
                    btnViewDetails.layoutParams = params
                }
            }
        }
    }
}