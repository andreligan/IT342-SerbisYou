package com.example.serbisyo_it342_g3

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.adapters.ProviderBookingAdapter
import com.example.serbisyo_it342_g3.api.BookingApiClient
import com.example.serbisyo_it342_g3.data.Booking
import com.google.android.material.tabs.TabLayout

class ProviderBookingsActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var rvBookings: RecyclerView
    private lateinit var tvNoBookings: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var bookingAdapter: ProviderBookingAdapter
    private lateinit var bookingApiClient: BookingApiClient

    private var providerId: Long = 0
    private var token: String = ""
    private val TAG = "ProviderBookings"
    private val bookings = mutableListOf<Booking>()

    // Status filter constants
    companion object {
        const val FILTER_ALL = "All"
        const val FILTER_PENDING = "Pending"
        const val FILTER_CONFIRMED = "Confirmed"
        const val FILTER_IN_PROGRESS = "In Progress"
        const val FILTER_COMPLETED = "Completed"
        const val FILTER_CANCELLED = "Cancelled"
    }

    // Current filter for bookings
    private var currentFilter = FILTER_ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_bookings)

        initViews()
        setupToolbar()
        setupTabLayout()

        // Initialize API client
        bookingApiClient = BookingApiClient(this)

        // Get provider ID and token from intent or shared preferences
        providerId = intent.getLongExtra("PROVIDER_ID", 0)
        if (providerId <= 0) {
            providerId = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getLong("providerId", 0)
        }

        token = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .getString("token", "") ?: ""

        // Setup RecyclerView and adapter
        setupRecyclerView()

        // Load bookings
        loadBookings()
    }

    private fun initViews() {
        tabLayout = findViewById(R.id.bookingsTabLayout)
        rvBookings = findViewById(R.id.rvBookings)
        tvNoBookings = findViewById(R.id.tvNoBookings)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Customer Bookings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupTabLayout() {
        with(tabLayout) {
            addTab(newTab().setText(FILTER_ALL))
            addTab(newTab().setText(FILTER_PENDING))
            addTab(newTab().setText(FILTER_CONFIRMED))
            addTab(newTab().setText(FILTER_IN_PROGRESS))
            addTab(newTab().setText(FILTER_COMPLETED))
            addTab(newTab().setText(FILTER_CANCELLED))

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.let {
                        currentFilter = it.text.toString()
                        filterBookings()
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    // Not needed
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    // Not needed
                }
            })
        }
    }

    private fun setupRecyclerView() {
        rvBookings.layoutManager = LinearLayoutManager(this)
        bookingAdapter = ProviderBookingAdapter(
            bookings = bookings,
            onConfirmClick = { booking -> confirmBooking(booking) },
            onCancelClick = { booking -> cancelBooking(booking) },
            onStartServiceClick = { booking -> startService(booking) },
            onCompleteServiceClick = { booking -> completeService(booking) },
            onConfirmPaymentClick = { booking -> confirmCashPayment(booking) },
            onViewDetailsClick = { booking -> viewBookingDetails(booking) }
        )
        rvBookings.adapter = bookingAdapter
    }

    private fun loadBookings() {
        showLoading(true)
        bookingApiClient.getBookingsByProviderId(providerId, token) { loadedBookings, error ->
            runOnUiThread {
                showLoading(false)
                if (error != null) {
                    Log.e(TAG, "Error loading bookings: ${error.message}")
                    showErrorToast("Failed to load bookings: ${error.message}")
                    return@runOnUiThread
                }

                loadedBookings?.let {
                    bookings.clear()
                    bookings.addAll(it)
                    filterBookings()
                } ?: run {
                    showNoBookings("No bookings found")
                }
            }
        }
    }

    private fun filterBookings() {
        val filteredList = if (currentFilter == FILTER_ALL) {
            bookings
        } else {
            bookings.filter { it.status == currentFilter }
        }

        if (filteredList.isEmpty()) {
            showNoBookings("No $currentFilter bookings found")
        } else {
            rvBookings.visibility = View.VISIBLE
            tvNoBookings.visibility = View.GONE
        }

        bookingAdapter.updateBookings(filteredList)
    }

    private fun confirmBooking(booking: Booking) {
        showConfirmationDialog(
            title = "Confirm Booking",
            message = "Are you sure you want to confirm this booking?",
            positiveAction = {
                showLoading(true)
                bookingApiClient.updateBookingStatus(booking.bookingId, "Confirmed", token) { updatedBooking, error ->
                    runOnUiThread {
                        showLoading(false)
                        if (error != null) {
                            showErrorToast("Failed to confirm booking: ${error.message}")
                            return@runOnUiThread
                        }

                        updatedBooking?.let {
                            val index = bookings.indexOfFirst { b -> b.bookingId == it.bookingId }
                            if (index >= 0) {
                                bookings[index] = it
                                filterBookings()
                                showSuccessToast("Booking confirmed successfully")
                            }
                        }
                    }
                }
            }
        )
    }

    private fun cancelBooking(booking: Booking) {
        showConfirmationDialog(
            title = "Cancel Booking",
            message = "Are you sure you want to cancel this booking?",
            positiveAction = {
                showLoading(true)
                bookingApiClient.updateBookingStatus(booking.bookingId, "Cancelled", token) { updatedBooking, error ->
                    runOnUiThread {
                        showLoading(false)
                        if (error != null) {
                            showErrorToast("Failed to cancel booking: ${error.message}")
                            return@runOnUiThread
                        }

                        updatedBooking?.let {
                            val index = bookings.indexOfFirst { b -> b.bookingId == it.bookingId }
                            if (index >= 0) {
                                bookings[index] = it
                                filterBookings()
                                showSuccessToast("Booking cancelled successfully")
                            }
                        }
                    }
                }
            }
        )
    }

    private fun startService(booking: Booking) {
        showConfirmationDialog(
            title = "Start Service",
            message = "Are you ready to start this service now?",
            positiveAction = {
                showLoading(true)
                bookingApiClient.updateBookingStatus(booking.bookingId, "In Progress", token) { updatedBooking, error ->
                    runOnUiThread {
                        showLoading(false)
                        if (error != null) {
                            showErrorToast("Failed to update booking status: ${error.message}")
                            return@runOnUiThread
                        }

                        updatedBooking?.let {
                            val index = bookings.indexOfFirst { b -> b.bookingId == it.bookingId }
                            if (index >= 0) {
                                bookings[index] = it
                                filterBookings()
                                showSuccessToast("Service started successfully")
                            }
                        }
                    }
                }
            }
        )
    }

    private fun completeService(booking: Booking) {
        showConfirmationDialog(
            title = "Complete Service",
            message = "Has this service been completed?",
            positiveAction = {
                showLoading(true)
                bookingApiClient.completeBooking(booking.bookingId, token) { updatedBooking, error ->
                    runOnUiThread {
                        showLoading(false)
                        if (error != null) {
                            showErrorToast("Failed to complete service: ${error.message}")
                            return@runOnUiThread
                        }

                        updatedBooking?.let {
                            val index = bookings.indexOfFirst { b -> b.bookingId == it.bookingId }
                            if (index >= 0) {
                                bookings[index] = it
                                filterBookings()
                                showSuccessToast("Service marked as completed")
                            }
                        } ?: showErrorToast("Error updating booking status")
                    }
                }
            }
        )
    }

    private fun confirmCashPayment(booking: Booking) {
        showConfirmationDialog(
            title = "Confirm Payment",
            message = "Confirm that you have received cash payment for this service?",
            positiveAction = {
                showLoading(true)
                // Mark booking as Completed and handle cash payment
                bookingApiClient.completeBooking(booking.bookingId, token) { updatedBooking, error ->
                    runOnUiThread {
                        showLoading(false)
                        if (error != null) {
                            showErrorToast("Failed to confirm payment: ${error.message}")
                            return@runOnUiThread
                        }

                        updatedBooking?.let {
                            val index = bookings.indexOfFirst { b -> b.bookingId == it.bookingId }
                            if (index >= 0) {
                                bookings[index] = it
                                filterBookings()
                                showSuccessToast("Payment confirmed and service completed")
                            }
                        } ?: showErrorToast("Error updating booking status")
                    }
                }
            }
        )
    }

    private fun viewBookingDetails(booking: Booking) {
        // Show a dialog with full booking details
        val message = buildString {
            append("Service: ${booking.service?.serviceName}\n")
            append("Customer: ${booking.customer?.userAuth?.userName}\n")
            append("Date: ${booking.bookingDate}\n")
            append("Time: ${booking.bookingTime}\n")
            append("Status: ${booking.status}\n")
            append("Payment Method: ${booking.paymentMethod}\n")
            append("Total: ₱${String.format("%.2f", booking.totalCost)}\n")
            booking.note?.let {
                if (it.isNotEmpty()) {
                    append("\nSpecial Instructions: $it\n")
                }
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Booking Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showConfirmationDialog(title: String, message: String, positiveAction: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ -> positiveAction() }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        rvBookings.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showNoBookings(message: String) {
        rvBookings.visibility = View.GONE
        tvNoBookings.visibility = View.VISIBLE
        tvNoBookings.text = message
    }

    private fun showSuccessToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}