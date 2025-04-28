package com.example.serbisyo_it342_g3.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.api.BookingApiClient
import com.example.serbisyo_it342_g3.api.ReviewApiClient
import com.example.serbisyo_it342_g3.data.Booking
import java.text.SimpleDateFormat
import java.util.*

class BookingHistoryFragment : Fragment() {
    private val TAG = "BookingHistoryFragment"
    
    private lateinit var rvBookings: RecyclerView
    private lateinit var tvNoBookings: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var headerText: TextView
    private lateinit var subHeaderText: TextView
    
    private lateinit var bookingApiClient: BookingApiClient
    private lateinit var reviewApiClient: ReviewApiClient
    private var token: String = ""
    private var userId: Long = 0
    private var bookings = mutableListOf<Booking>()
    private lateinit var bookingAdapter: BookingAdapter
    
    // Track bookings that have already been reviewed or are in the process of being reviewed
    private val reviewedBookings = mutableSetOf<Long>()
    
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
        val view = inflater.inflate(R.layout.fragment_booking_history, container, false)
        
        // Initialize API clients
        bookingApiClient = BookingApiClient(requireContext())
        reviewApiClient = ReviewApiClient(requireContext())
        
        // Get token and user ID from SharedPreferences
        val preferences = requireContext().getSharedPreferences("UserPrefs", 0)
        token = preferences.getString("token", "") ?: ""
        userId = preferences.getLong("userId", 0)
        
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
        
        // Load booking history
        loadBookingHistory()
        
        return view
    }
    
    private fun loadBookingHistory() {
        progressBar.visibility = View.VISIBLE
        tvNoBookings.visibility = View.GONE
        
        // Load bookings for the logged-in user
        bookingApiClient.getUserBookings(
            token = token,
            userId = userId,
            onSuccess = { loadedBookings ->
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    
                    // Update the bookings list
                    bookings.clear()
                    if (loadedBookings != null) {
                        bookings.addAll(loadedBookings)
                    }
                    
                    // Show no bookings message if list is empty
                    if (bookings.isEmpty()) {
                        tvNoBookings.visibility = View.VISIBLE
                    } else {
                        tvNoBookings.visibility = View.GONE
                    }
                    
                    // Notify adapter of data change
                    bookingAdapter.notifyDataSetChanged()
                }
            },
            onError = { error ->
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    tvNoBookings.visibility = View.VISIBLE
                    tvNoBookings.text = "Error loading bookings: $error"
                    Toast.makeText(requireContext(), "Failed to load bookings: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    
    // Adapter for displaying bookings
    inner class BookingAdapter(
        private val bookings: List<Booking>
    ) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {
        
        inner class BookingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
            val tvCustomerName: TextView = view.findViewById(R.id.tvCustomerName)
            val tvProviderName: TextView = view.findViewById(R.id.tvProviderName)
            val tvBookingDate: TextView = view.findViewById(R.id.tvBookingDate)
            val tvBookingTime: TextView = view.findViewById(R.id.tvBookingTime)
            val tvStatus: TextView = view.findViewById(R.id.tvStatus)
            val tvPrice: TextView = view.findViewById(R.id.tvPrice)
            val tvPaymentMethod: TextView = view.findViewById(R.id.tvPaymentMethod)
            val tvNotes: TextView = view.findViewById(R.id.tvNotes)
            val notesContainer: LinearLayout = view.findViewById(R.id.notesContainer)
            
            // Add references for the review button layout
            val writeReviewLayout: LinearLayout = view.findViewById(R.id.writeReviewLayout)
            val btnWriteReview: Button = view.findViewById(R.id.btnWriteReview)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_booking, parent, false)
            return BookingViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
            val booking = bookings[position]
            
            // Service name
            holder.tvServiceName.text = booking.service?.serviceName ?: "Unknown Service"
            
            // Customer name - get the actual customer name from the booking
            val firstName = booking.customer?.firstName ?: ""
            val lastName = booking.customer?.lastName ?: ""
            holder.tvCustomerName.text = if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                "$firstName $lastName"
            } else {
                "Unknown Customer"
            }
            
            // Provider name
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
            
            // Display payment method
            holder.tvPaymentMethod.text = booking.paymentMethod ?: "Cash"
            
            // Display total cost if available
            if (booking.totalCost > 0) {
                holder.tvPrice.text = "Price: ₱${booking.totalCost.toInt()}"
            } else if (booking.service?.effectivePrice != null) {
                holder.tvPrice.text = "Price: ${booking.service.effectivePrice}"
            } else {
                holder.tvPrice.text = "Price: Not specified"
            }
            
            // Display special instructions (notes)
            if (!booking.note.isNullOrEmpty()) {
                holder.notesContainer.visibility = View.VISIBLE
                holder.tvNotes.text = booking.note
            } else {
                holder.notesContainer.visibility = View.VISIBLE // Still keep visible but show default message
                holder.tvNotes.text = "No special instructions provided."
            }
            
            // Display Write Review button for completed bookings
            val bookingId = booking.bookingId
            if (booking.status?.lowercase() == "completed" && bookingId != null && !reviewedBookings.contains(bookingId)) {
                holder.writeReviewLayout.visibility = View.VISIBLE
                
                // Set click listener for the review button
                holder.btnWriteReview.setOnClickListener {
                    checkIfCanReview(booking)
                }
            } else {
                holder.writeReviewLayout.visibility = View.GONE
            }
        }
        
        override fun getItemCount() = bookings.size
    }
    
    // Check if user can review this booking (hasn't reviewed it already)
    private fun checkIfCanReview(booking: Booking) {
        val bookingId = booking.bookingId ?: return
        val customerId = booking.customer?.customerId ?: userId
        
        // Show loading toast
        val loadingToast = Toast.makeText(requireContext(), "Checking review status...", Toast.LENGTH_SHORT)
        loadingToast.show()
        
        // Use the ReviewApiClient to check if the user can review this booking
        reviewApiClient.canCustomerReviewBooking(token, customerId, bookingId) { canReview: Boolean ->
            requireActivity().runOnUiThread {
                loadingToast.cancel()
                
                if (canReview) {
                    showReviewDialog(booking)
                } else {
                    Toast.makeText(requireContext(), "You have already reviewed this booking", Toast.LENGTH_SHORT).show()
                    // Add to reviewed bookings set to hide the button
                    reviewedBookings.add(bookingId)
                    bookingAdapter.notifyDataSetChanged()
                }
            }
        }
    }
    
    // Show the review dialog
    private fun showReviewDialog(booking: Booking) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_review)
        
        // Get references to dialog views
        val tvServiceName = dialog.findViewById<TextView>(R.id.tvReviewServiceName)
        val tvProviderName = dialog.findViewById<TextView>(R.id.tvReviewProviderName)
        val etComment = dialog.findViewById<EditText>(R.id.etReviewComment)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancelReview)
        val btnSubmit = dialog.findViewById<Button>(R.id.btnSubmitReview)
        val tvRatingValue = dialog.findViewById<TextView>(R.id.tvRatingValue)
        
        // Get star ImageViews
        val star1 = dialog.findViewById<ImageView>(R.id.star1)
        val star2 = dialog.findViewById<ImageView>(R.id.star2)
        val star3 = dialog.findViewById<ImageView>(R.id.star3)
        val star4 = dialog.findViewById<ImageView>(R.id.star4)
        val star5 = dialog.findViewById<ImageView>(R.id.star5)
        
        // Set initial values
        tvServiceName.text = booking.service?.serviceName ?: "Unknown Service"
        tvProviderName.text = booking.service?.provider?.businessName ?: 
                             "${booking.service?.provider?.firstName ?: ""} ${booking.service?.provider?.lastName ?: ""}"
        
        // Variable to track the selected rating
        var selectedRating = 5
        
        // Initialize with 5 stars
        setStarRating(dialog, 5)
        
        // Set click listeners for stars
        star1.setOnClickListener { setStarRating(dialog, 1); selectedRating = 1 }
        star2.setOnClickListener { setStarRating(dialog, 2); selectedRating = 2 }
        star3.setOnClickListener { setStarRating(dialog, 3); selectedRating = 3 }
        star4.setOnClickListener { setStarRating(dialog, 4); selectedRating = 4 }
        star5.setOnClickListener { setStarRating(dialog, 5); selectedRating = 5 }
        
        // Set cancel button click listener
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        // Set submit button click listener
        btnSubmit.setOnClickListener {
            val comment = etComment.text.toString()
            if (comment.isBlank()) {
                Toast.makeText(requireContext(), "Please enter your comments", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            submitReview(booking, selectedRating, comment, dialog)
        }
        
        dialog.show()
    }
    
    // Helper function to set star rating in the dialog
    private fun setStarRating(dialog: Dialog, rating: Int) {
        val star1 = dialog.findViewById<ImageView>(R.id.star1)
        val star2 = dialog.findViewById<ImageView>(R.id.star2)
        val star3 = dialog.findViewById<ImageView>(R.id.star3)
        val star4 = dialog.findViewById<ImageView>(R.id.star4)
        val star5 = dialog.findViewById<ImageView>(R.id.star5)
        val tvRatingValue = dialog.findViewById<TextView>(R.id.tvRatingValue)
        
        // Update star images
        star1.setImageResource(if (rating >= 1) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
        star2.setImageResource(if (rating >= 2) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
        star3.setImageResource(if (rating >= 3) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
        star4.setImageResource(if (rating >= 4) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
        star5.setImageResource(if (rating >= 5) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
        
        // Update rating text
        tvRatingValue.text = "$rating out of 5"
        tvRatingValue.visibility = View.VISIBLE
    }
    
    // Submit the review to the server
    private fun submitReview(booking: Booking, rating: Int, comment: String, dialog: Dialog) {
        val customerId = booking.customer?.customerId ?: userId
        val providerId = booking.service?.provider?.providerId
        val bookingId = booking.bookingId
        
        if (providerId == null || bookingId == null) {
            Toast.makeText(requireContext(), "Missing provider or booking information", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show loading indicator
        val loadingToast = Toast.makeText(requireContext(), "Submitting your review...", Toast.LENGTH_LONG)
        loadingToast.show()
        
        // Use the ReviewApiClient to submit the review
        reviewApiClient.submitReview(
            token = token,
            customerId = customerId,
            providerId = providerId,
            bookingId = bookingId,
            rating = rating,
            comment = comment,
            callback = { success: Boolean, errorMessage: String? ->
                requireActivity().runOnUiThread {
                    loadingToast.cancel()
                    
                    if (success) {
                        Toast.makeText(requireContext(), "Review submitted successfully!", Toast.LENGTH_SHORT).show()
                        
                        // Add to reviewed bookings set
                        reviewedBookings.add(bookingId)
                        
                        // Refresh adapter to hide review button
                        bookingAdapter.notifyDataSetChanged()
                        
                        dialog.dismiss()
                    } else {
                        Toast.makeText(requireContext(), errorMessage ?: "Failed to submit review. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}