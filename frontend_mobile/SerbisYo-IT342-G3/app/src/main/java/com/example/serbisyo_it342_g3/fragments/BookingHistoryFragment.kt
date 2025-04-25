package com.example.serbisyo_it342_g3.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.api.BookingApiClient
import com.example.serbisyo_it342_g3.data.Booking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingHistoryFragment : Fragment() {
    private val TAG = "BookingHistoryFragment"
    
    private lateinit var rvBookings: RecyclerView
    private lateinit var tvNoBookings: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var headerText: TextView
    private lateinit var subHeaderText: TextView
    
    private lateinit var bookingApiClient: BookingApiClient
    private var token: String = ""
    private var userId: Long = 0
    private var bookings = mutableListOf<Booking>()
    private lateinit var bookingAdapter: BookingAdapter
    
    companion object {
        fun newInstance(): BookingHistoryFragment {
            val fragment = BookingHistoryFragment()
            return fragment
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_booking_history, container, false)
        
        // Get shared preferences
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", 
                                                             android.content.Context.MODE_PRIVATE)
        // Fix userId retrieval using try-catch
        userId = try {
            // Try to get as Long first (new format)
            sharedPref.getLong("userId", 0)
        } catch (e: ClassCastException) {
            // If that fails, try the String format (old format) and convert
            val userIdStr = sharedPref.getString("userId", "0")
            userIdStr?.toLongOrNull() ?: 0
        }
        token = sharedPref.getString("token", "") ?: ""
        
        // Initialize the API client
        bookingApiClient = BookingApiClient(requireContext())
        
        // Initialize views
        rvBookings = view.findViewById(R.id.rvBookings)
        tvNoBookings = view.findViewById(R.id.tvNoBookings)
        progressBar = view.findViewById(R.id.progressBar)
        headerText = view.findViewById(R.id.headerText)
        subHeaderText = view.findViewById(R.id.subHeaderText)
        
        // Setup header
        headerText.text = "My Booking History"
        subHeaderText.text = "View your past service bookings"
        
        // Setup recycler view
        rvBookings.layoutManager = LinearLayoutManager(context)
        bookingAdapter = BookingAdapter(bookings)
        rvBookings.adapter = bookingAdapter
        
        // Load bookings
        loadBookingHistory()
        
        return view
    }
    
    private fun loadBookingHistory() {
        progressBar.visibility = View.VISIBLE
        
        bookingApiClient.getBookingsByCustomerId(userId, token) { bookingList, error ->
            requireActivity().runOnUiThread {
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Log.e(TAG, "Error loading booking history", error)
                    Toast.makeText(context, "Error loading booking history: ${error.message}", Toast.LENGTH_SHORT).show()
                    tvNoBookings.text = "Failed to load booking history"
                    tvNoBookings.visibility = View.VISIBLE
                    return@runOnUiThread
                }
                
                bookings.clear()
                if (bookingList != null && bookingList.isNotEmpty()) {
                    bookings.addAll(bookingList)
                    tvNoBookings.visibility = View.GONE
                    rvBookings.visibility = View.VISIBLE
                } else {
                    tvNoBookings.visibility = View.VISIBLE
                    rvBookings.visibility = View.GONE
                }
                
                bookingAdapter.notifyDataSetChanged()
            }
        }
    }
    
    // Adapter for displaying bookings
    inner class BookingAdapter(
        private val bookings: List<Booking>
    ) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {
        
        inner class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
            val tvProviderName: TextView = view.findViewById(R.id.tvProviderName)
            val tvBookingDate: TextView = view.findViewById(R.id.tvBookingDate)
            val tvBookingTime: TextView = view.findViewById(R.id.tvBookingTime)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
            val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_booking, parent, false)
            return BookingViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
            val booking = bookings[position]
            
            holder.tvServiceName.text = booking.service?.serviceName ?: "Unknown Service"
            holder.tvProviderName.text = "Provider: ${booking.service?.provider?.businessName ?: booking.service?.provider?.firstName ?: "Unknown Provider"}"
            
            // Format the date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputDateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            
            try {
                val date = dateFormat.parse(booking.bookingDate ?: "") ?: Date()
                holder.tvBookingDate.text = "Date: ${outputDateFormat.format(date)}"
                holder.tvBookingTime.text = "Time: Not available" // No time in booking model
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing date", e)
                holder.tvBookingDate.text = "Date: Not available"
                holder.tvBookingTime.text = "Time: Not available"
            }
            
            // Set status with color
            val statusText = when (booking.status?.lowercase()) {
                "completed" -> "Completed"
                "cancelled" -> "Cancelled"
                "pending" -> "Pending"
                "confirmed" -> "Confirmed"
                "in_progress" -> "In Progress"
                else -> booking.status ?: "Unknown"
            }
            
            holder.tvStatus.text = "Status: $statusText"
            
            // Set color based on status
            when (booking.status?.lowercase()) {
                "completed" -> holder.tvStatus.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                "cancelled" -> holder.tvStatus.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                "pending" -> holder.tvStatus.setTextColor(resources.getColor(android.R.color.holo_orange_dark))
                "confirmed" -> holder.tvStatus.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
                "in_progress" -> holder.tvStatus.setTextColor(resources.getColor(android.R.color.holo_blue_light))
                else -> holder.tvStatus.setTextColor(resources.getColor(android.R.color.darker_gray))
            }
            
            // Set price
            holder.tvPrice.text = "Price: ${booking.service?.priceRange ?: "Not specified"}"
        }
        
        override fun getItemCount() = bookings.size
    }
} 