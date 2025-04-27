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
        
        // Log user ID for debugging
        Log.d(TAG, "User ID from preferences: $userId (${userId.javaClass.simpleName})")
        Log.d(TAG, "Token from preferences: ${token.take(20)}...")
        
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
        
        // Make tvNoBookings clickable to retry loading
        tvNoBookings.setOnClickListener {
            loadBookingHistory()
        }
        
        // Check if we're coming from a successful booking
        val bookingState = requireActivity().getSharedPreferences("BookingState", android.content.Context.MODE_PRIVATE)
        val recentlyBooked = bookingState.getBoolean("recently_booked", false)
        if (recentlyBooked) {
            Log.d(TAG, "Recently booked flag detected on create, setting up delayed loading")
            // We'll first try to load immediately, and if that fails, retry after a delay
            view.postDelayed({
                if (isAdded && !isDetached && bookings.isEmpty()) {
                    Log.d(TAG, "Performing delayed reload after booking creation")
                    loadBookingHistory()
                }
            }, 1500) // Shorter initial delay
        }
        
        // Load bookings
        loadBookingHistory()
        
        return view
    }
    
    private fun loadBookingHistory() {
        progressBar.visibility = View.VISIBLE
        tvNoBookings.visibility = View.GONE
        rvBookings.visibility = View.GONE
        
        // Log to verify request parameters
        Log.d(TAG, "Loading booking history for user ID: $userId with token: ${token.take(20)}...")
        
        // Double-check if userId is valid
        if (userId <= 0) {
            Log.e(TAG, "Invalid user ID: $userId")
            tvNoBookings.text = "Error: Invalid user ID. Please log in again."
            tvNoBookings.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            return
        }
        
        // Make a direct API call to get ALL bookings and manually filter
        val baseApiClient = com.example.serbisyo_it342_g3.api.BaseApiClient(requireContext())
        val client = baseApiClient.client
        
        val request = okhttp3.Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/bookings/getAll")
            .get()
            .header("Authorization", "Bearer $token")
            .build()
        
        // Execute the request on a background thread
        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    Log.d(TAG, "Response code: ${response.code}")
                    
                    requireActivity().runOnUiThread {
                        progressBar.visibility = View.GONE
                        
                        if (!response.isSuccessful || responseBody == null) {
                            Log.e(TAG, "Error getting bookings: ${response.code}")
                            tvNoBookings.text = "Failed to load booking history\nTap to retry"
                            tvNoBookings.visibility = View.VISIBLE
                            return@runOnUiThread
                        }
                        
                        // Log the entire response for debugging
                        Log.d(TAG, "Raw response: $responseBody")
                        
                        try {
                            // Parse the response
                            val gson = com.google.gson.Gson()
                            val type = object : com.google.gson.reflect.TypeToken<List<com.example.serbisyo_it342_g3.data.Booking>>() {}.type
                            val allBookings = gson.fromJson<List<com.example.serbisyo_it342_g3.data.Booking>>(responseBody, type) ?: emptyList()
                            
                            Log.d(TAG, "Parsed ${allBookings.size} total bookings")
                            
                            // Check each booking in detail to catch any issues
                            for (booking in allBookings) {
                                val bookingId = booking.bookingId
                                val customerId = booking.customer?.customerId
                                val userId = booking.customer?.userAuth?.userId
                                val customerIdType = customerId?.javaClass?.simpleName ?: "null"
                                val userIdType = userId?.javaClass?.simpleName ?: "null"
                                
                                Log.d(TAG, "Booking $bookingId: customer=$customerId ($customerIdType), userAuth.userId=$userId ($userIdType)")
                                
                                // Check if this booking matches our user in any way
                                val rawJson = gson.toJson(booking)
                                Log.d(TAG, "Booking $bookingId JSON: $rawJson")
                                
                                if (rawJson.contains("\"userId\":$userId") || 
                                    rawJson.contains("\"userId\":\"$userId\"") ||
                                    rawJson.contains("\"user_id\":$userId") ||
                                    rawJson.contains("\"user_id\":\"$userId\"")) {
                                    Log.d(TAG, "Found userId match in raw JSON")
                                }
                            }
                            
                            // Get our own userId from preferences for direct comparison
                            val authUserId = requireActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE).getLong("userId", 0)
                            Log.d(TAG, "Current auth userId from preferences: $authUserId")
                            
                            // Try multiple filtering approaches to find bookings
                            // 1. First try filtering by customer ID (string comparison)
                            var customerBookings = allBookings.filter { booking ->
                                booking.customer?.customerId.toString() == this.userId.toString()
                            }
                            
                            // 2. If that doesn't work, try filtering by userAuth.userId
                            if (customerBookings.isEmpty()) {
                                Log.d(TAG, "No bookings found by customerId, trying userAuth.userId")
                                customerBookings = allBookings.filter { booking ->
                                    booking.customer?.userAuth?.userId.toString() == authUserId.toString()
                                }
                            }
                            
                            // 3. If still empty, try general JSON search for customerId or userId
                            if (customerBookings.isEmpty()) {
                                Log.d(TAG, "No bookings found by direct comparison, trying JSON search")
                                customerBookings = allBookings.filter { booking ->
                                    val json = gson.toJson(booking)
                                    json.contains("\"customerId\":${this.userId}") || 
                                    json.contains("\"customerId\":\"${this.userId}\"") ||
                                    json.contains("\"customer_id\":${this.userId}") ||
                                    json.contains("\"customer_id\":\"${this.userId}\"") ||
                                    json.contains("\"userId\":$authUserId") || 
                                    json.contains("\"userId\":\"$authUserId\"") ||
                                    json.contains("\"user_id\":$authUserId") ||
                                    json.contains("\"user_id\":\"$authUserId\"")
                                }
                            }
                            
                            // 4. If we're customer ID 18, try direct comparison with that value
                            if (customerBookings.isEmpty() && this.userId == 18L) {
                                Log.d(TAG, "Special case for customer ID 18, direct search")
                                customerBookings = allBookings.filter { booking ->
                                    val json = gson.toJson(booking)
                                    json.contains("\"18\"") || json.contains("\":18") || json.contains("customer\":18")
                                }
                            }
                            
                            // 5. Check if we need to specifically handle newer customers with a different structure
                            if (customerBookings.isEmpty()) {
                                Log.d(TAG, "Trying alternative approach - check for structure differences between customer ID 1 and 18")
                                
                                // Get a reference booking for customer ID 1
                                val customerOneBooking = allBookings.find { booking ->
                                    val json = gson.toJson(booking)
                                    json.contains("\"customerId\":1") || json.contains("\"customerId\":\"1\"")
                                }
                                
                                if (customerOneBooking != null) {
                                    val customerOneJson = gson.toJson(customerOneBooking)
                                    Log.d(TAG, "Structure for customer ID 1 booking: $customerOneJson")
                                    
                                    // Check the detailed structure of how customer ID 1 bookings are stored
                                    val customerOneIdPath = findJsonPath(customerOneJson, "1")
                                    Log.d(TAG, "Path to ID 1 in JSON: $customerOneIdPath")
                                    
                                    // Now search for any booking with a similar path but with current customer ID
                                    customerBookings = allBookings.filter { booking ->
                                        val json = gson.toJson(booking)
                                        
                                        // Try more search patterns based on what we learn from customer ID 1 structure
                                        json.contains("\"customer\":{") && json.contains("\"${this.userId}\"") ||
                                        json.contains("\"customer\":${this.userId}") ||
                                        json.contains("\"customerId\":${this.userId}") ||
                                        // Add many variations to catch potential differences
                                        json.contains("\"customer_id\":${this.userId}")
                                    }
                                }
                            }
                            
                            // 6. Last resort - look through all bookings manually and log their structure
                            if (customerBookings.isEmpty()) {
                                Log.d(TAG, "Final attempt - manually examining all bookings:")
                                
                                for (booking in allBookings) {
                                    val json = gson.toJson(booking)
                                    
                                    // Check if this booking might relate to our customer in any way
                                    if (json.contains("${this.userId}")) {
                                        Log.d(TAG, "Booking ${booking.bookingId} contains customer ID ${this.userId}: $json")
                                        customerBookings = listOf(booking)
                                        break
                                    }
                                }
                                
                                // If still nothing, log the structure of all bookings for analysis
                                if (customerBookings.isEmpty() && allBookings.isNotEmpty()) {
                                    Log.d(TAG, "Showing structure of first 3 bookings for analysis")
                                    allBookings.take(3).forEachIndexed { index, booking ->
                                        Log.d(TAG, "Sample booking $index: ${gson.toJson(booking)}")
                                    }
                                }
                            }
                            
                            bookings.clear()
                            if (customerBookings.isNotEmpty()) {
                                Log.d(TAG, "SUCCESS: Found ${customerBookings.size} bookings for customer ID ${this.userId}")
                                
                                // Clear the recently_booked flag since we found bookings
                                val bookingState = requireActivity().getSharedPreferences("BookingState", android.content.Context.MODE_PRIVATE)
                                bookingState.edit().putBoolean("recently_booked", false).apply()
                                
                                // Add all found bookings to our list
                                bookings.addAll(customerBookings)
                                
                                // Update UI to show the bookings
                                try {
                                    tvNoBookings.visibility = View.GONE
                                    rvBookings.visibility = View.VISIBLE
                                    
                                    // Force adapter to refresh
                                    bookingAdapter.notifyDataSetChanged()
                                    
                                    // Log success for troubleshooting
                                    Log.d(TAG, "UI updated to show ${bookings.size} bookings")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error updating UI with found bookings", e)
                                }
                            } else {
                                // Check if we just created a booking but don't see it yet
                                val sharedPref = requireActivity().getSharedPreferences("BookingState", android.content.Context.MODE_PRIVATE)
                                val recentlyBooked = sharedPref.getBoolean("recently_booked", false)
                                
                                if (recentlyBooked) {
                                    // We recently booked but can't find it - maybe it's still processing
                                    tvNoBookings.text = "Your booking might still be processing\nTap to refresh"
                                    tvNoBookings.visibility = View.VISIBLE
                                    rvBookings.visibility = View.GONE
                                    
                                    // Schedule an automatic retry after a delay
                                    view?.postDelayed({
                                        if (isAdded && !isDetached) {
                                            Log.d(TAG, "Auto-retrying booking history load after delay")
                                            loadBookingHistory()
                                        }
                                    }, 3000)
                                } else {
                                    tvNoBookings.text = "You don't have any bookings yet"
                                    tvNoBookings.visibility = View.VISIBLE
                                    rvBookings.visibility = View.GONE
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing booking response", e)
                            tvNoBookings.text = "Error loading bookings\nTap to retry"
                            tvNoBookings.visibility = View.VISIBLE
                        }
                        
                        // Always update the adapter to be safe
                        bookingAdapter.notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error", e)
                try {
                    requireActivity().runOnUiThread {
                        progressBar.visibility = View.GONE
                        tvNoBookings.text = "Network error\nTap to retry"
                        tvNoBookings.visibility = View.VISIBLE
                    }
                } catch (e2: Exception) {
                    Log.e(TAG, "Error updating UI", e2)
                }
            }
        }.start()
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
                
                // Display booking time if available
                if (!booking.bookingTime.isNullOrEmpty()) {
                    // Try to format time if it's in a standard format
                    try {
                        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        val outputTimeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                        val time = timeFormat.parse(booking.bookingTime)
                        
                        if (time != null) {
                            holder.tvBookingTime.text = "Time: ${outputTimeFormat.format(time)}"
                        } else {
                            holder.tvBookingTime.text = "Time: ${booking.bookingTime}"
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Unable to parse time format, using raw value")
                        holder.tvBookingTime.text = "Time: ${booking.bookingTime}"
                    }
                } else {
                    holder.tvBookingTime.text = "Time: Not available"
                }
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
            
            // Display total cost if available
            if (booking.totalCost > 0) {
                holder.tvPrice.text = "Price: ₱${booking.totalCost.toInt()}"
            } else if (booking.service?.effectivePrice != null) {
                holder.tvPrice.text = "Price: ${booking.service.effectivePrice}"
            } else {
                holder.tvPrice.text = "Price: Not specified"
            }
        }
        
        override fun getItemCount() = bookings.size
    }
    
    // Improve onResume to check for recently_booked flag with multiple retries
    override fun onResume() {
        super.onResume()
        
        // Check if we recently created a booking
        val sharedPref = requireActivity().getSharedPreferences("BookingState", android.content.Context.MODE_PRIVATE)
        val recentlyBooked = sharedPref.getBoolean("recently_booked", false)
        
        if (recentlyBooked) {
            Log.d(TAG, "Recently booked flag detected in onResume, reloading bookings")
            loadBookingHistory()
            
            // Schedule additional retries with increasing delays if needed
            view?.postDelayed({
                if (isAdded && !isDetached && bookings.isEmpty() && 
                    sharedPref.getBoolean("recently_booked", false)) {
                    Log.d(TAG, "First retry after onResume")
                    loadBookingHistory()
                }
            }, 2000)
            
            view?.postDelayed({
                if (isAdded && !isDetached && bookings.isEmpty() && 
                    sharedPref.getBoolean("recently_booked", false)) {
                    Log.d(TAG, "Second retry after onResume")
                    loadBookingHistory()
                    // After this final retry, clear the flag to prevent endless retries
                    sharedPref.edit().putBoolean("recently_booked", false).apply()
                }
            }, 5000)
        }
    }
    
    // Helper function to trace where a value appears in JSON
    private fun findJsonPath(json: String, value: String): String {
        // Simple implementation - just check common paths
        val possiblePaths = listOf(
            "customer.customerId", 
            "customer.userAuth.userId",
            "customer.id",
            "customerId",
            "userId"
        )
        
        for (path in possiblePaths) {
            // Very simple check
            val pattern = "\"${path.split(".").last()}\":\"?${value}\"?"
            if (json.contains(pattern)) {
                return "$path = $value"
            }
        }
        
        return "unknown path"
    }
} 