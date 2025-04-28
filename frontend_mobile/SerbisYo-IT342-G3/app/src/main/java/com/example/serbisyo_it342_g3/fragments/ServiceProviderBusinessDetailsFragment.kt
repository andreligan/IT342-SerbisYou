package com.example.serbisyo_it342_g3.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.api.UserApiClient
import com.example.serbisyo_it342_g3.data.ServiceProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ServiceProviderBusinessDetailsFragment : Fragment() {
    private val tag = "SPBusinessFragment"
    
    private lateinit var etBusinessName: EditText
    private lateinit var etBusinessDesc: EditText
    private lateinit var etBusinessCategory: EditText
    private lateinit var etYearEstablished: EditText
    private lateinit var etYearsExperience: EditText
    private lateinit var etAvailabilitySchedule: EditText
    private lateinit var etPaymentMethod: EditText
    private lateinit var dropdownStatus: AutoCompleteTextView
    private lateinit var btnUpdateBusinessDetails: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvErrorMessage: TextView
    private lateinit var tilAvailabilitySchedule: TextInputLayout
    
    private lateinit var userApiClient: UserApiClient
    private var token: String = ""
    private var userId: Long = 0
    private var providerId: Long = 0
    
    // Business details additional data
    private var businessDescription: String = ""
    private var businessCategory: String = ""
    private var yearEstablished: String = ""
    private var status: String = "Active"  // Default status
    
    // Calendar instance for date and time pickers
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_provider_business_details, container, false)
        
        // Get arguments
        arguments?.let {
            userId = it.getLong("userId", 0)
            token = it.getString("token", "") ?: ""
            providerId = it.getLong("providerId", 0)
        }

        // Initialize API client
        userApiClient = UserApiClient(requireContext())
        
        // Initialize views
        etBusinessName = view.findViewById(R.id.etBusinessName)
        etBusinessDesc = view.findViewById(R.id.etBusinessDesc)
        etBusinessCategory = view.findViewById(R.id.etBusinessCategory)
        etYearEstablished = view.findViewById(R.id.etYearEstablished)
        etYearsExperience = view.findViewById(R.id.etYearsExperience)
        etAvailabilitySchedule = view.findViewById(R.id.etAvailabilitySchedule)
        etPaymentMethod = view.findViewById(R.id.etPaymentMethod)
        dropdownStatus = view.findViewById(R.id.dropdownStatus)
        btnUpdateBusinessDetails = view.findViewById(R.id.btnUpdateBusinessDetails)
        progressBar = view.findViewById(R.id.progressBar)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)
        tilAvailabilitySchedule = view.findViewById(R.id.tilAvailabilitySchedule)
        
        // Setup status dropdown
        setupStatusDropdown()
        
        // Setup availability schedule picker
        setupAvailabilitySchedulePicker()
        
        // Load business details
        loadBusinessDetails()
        
        // Set button click listener
        btnUpdateBusinessDetails.setOnClickListener {
            if (validateInputs()) {
                updateBusinessDetails()
            }
        }
        
        return view
    }
    
    private fun setupStatusDropdown() {
        val statuses = arrayOf("Active", "Away", "Busy", "On Vacation")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses)
        dropdownStatus.setAdapter(adapter)
        
        // Set default status if empty
        if (dropdownStatus.text.isNullOrEmpty()) {
            dropdownStatus.setText("Active", false)
        }
    }
    
    private fun setupAvailabilitySchedulePicker() {
        // Make the EditText not directly editable
        etAvailabilitySchedule.isFocusable = false
        etAvailabilitySchedule.isClickable = true
        
        // Set click listener on the EditText
        etAvailabilitySchedule.setOnClickListener {
            showWorkDaysSelector()
        }
        
        // Set click listener on the TextInputLayout's end icon
        tilAvailabilitySchedule.setEndIconOnClickListener {
            showWorkDaysSelector()
        }
    }
    
    private fun showWorkDaysSelector() {
        val dayOptions = arrayOf(
            "Monday", "Tuesday", "Wednesday", "Thursday", 
            "Friday", "Saturday", "Sunday"
        )
        
        val selectedDays = BooleanArray(dayOptions.size) { false }
        
        // Pre-select days if schedule already exists
        val currentSchedule = etAvailabilitySchedule.text.toString()
        if (currentSchedule.isNotEmpty()) {
            if (currentSchedule.contains("Monday")) selectedDays[0] = true
            if (currentSchedule.contains("Tuesday")) selectedDays[1] = true
            if (currentSchedule.contains("Wednesday")) selectedDays[2] = true
            if (currentSchedule.contains("Thursday")) selectedDays[3] = true
            if (currentSchedule.contains("Friday")) selectedDays[4] = true
            if (currentSchedule.contains("Saturday")) selectedDays[5] = true
            if (currentSchedule.contains("Sunday")) selectedDays[6] = true
        } else {
            // Default to weekdays
            for (i in 0..4) {
                selectedDays[i] = true
            }
        }
        
        // Show multi-select dialog for days with Material Design
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Work Days")
            .setMultiChoiceItems(dayOptions, selectedDays) { _, which, isChecked ->
                selectedDays[which] = isChecked
            }
            .setPositiveButton("Next") { _, _ ->
                // Get selected days
                val selectedDaysList = mutableListOf<String>()
                for (i in selectedDays.indices) {
                    if (selectedDays[i]) {
                        selectedDaysList.add(dayOptions[i])
                    }
                }
                
                if (selectedDaysList.isEmpty()) {
                    Toast.makeText(context, "Please select at least one day", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                // Format days for display
                val formattedDays = formatDaysSelection(selectedDaysList)
                
                // Then get start and end times
                showStartTimeSelector(formattedDays)
            }
            .setNegativeButton("Cancel", null)
            .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background))
            .show()
    }
    
    private fun formatDaysSelection(days: List<String>): String {
        // If all 7 days are selected, return "All Days"
        if (days.size == 7) return "All Days"
        
        // If Mon-Fri are selected, return "Weekdays"
        val weekdays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        if (days.size == 5 && days.containsAll(weekdays)) return "Weekdays"
        
        // If Sat-Sun are selected, return "Weekends"
        val weekends = listOf("Saturday", "Sunday")
        if (days.size == 2 && days.containsAll(weekends)) return "Weekends"
        
        // Check for continuous sequences
        if (days.containsAll(listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"))) {
            return if (days.contains("Saturday") && !days.contains("Sunday")) {
                "Monday-Saturday"
            } else if (days.contains("Saturday") && days.contains("Sunday")) {
                "All Days"
            } else {
                "Monday-Friday"
            }
        }
        
        // For non-continuous days, return comma-separated list
        return days.joinToString(", ")
    }
    
    private fun showStartTimeSelector(daysText: String) {
        // Extract start time from current value if it exists
        var startHour = 9  // Default 9 AM
        var startMinute = 0
        
        val currentValue = etAvailabilitySchedule.text.toString()
        if (currentValue.contains(",")) {
            try {
                val timePart = currentValue.substringAfter(",").trim()
                val startTimePart = timePart.substringBefore("-").trim()
                
                if (startTimePart.contains("AM") || startTimePart.contains("PM")) {
                    // Parse time like "9:00AM"
                    val timeValue = startTimePart.replace("AM", "").replace("PM", "").trim()
                    val isPM = startTimePart.contains("PM")
                    
                    var parsedHour = timeValue.substringBefore(":").toInt()
                    val parsedMinute = if (timeValue.contains(":")) {
                        timeValue.substringAfter(":").toInt()
                    } else {
                        0
                    }
                    
                    if (isPM && parsedHour < 12) parsedHour += 12
                    if (!isPM && parsedHour == 12) parsedHour = 0
                    
                    startHour = parsedHour
                    startMinute = parsedMinute
                }
            } catch (e: Exception) {
                Log.e(tag, "Error parsing existing start time", e)
            }
        }
        
        // Use Material TimePicker instead of standard TimePickerDialog
        val materialTimePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(startHour)
            .setMinute(startMinute)
            .setTitleText("Select Start Time")
            .build()
            
        materialTimePicker.addOnPositiveButtonClickListener {
            // After selecting start time, show end time selector
            showEndTimeSelector(daysText, materialTimePicker.hour, materialTimePicker.minute)
        }
        
        materialTimePicker.addOnCancelListener {
            // If cancelled, go back to day selection
            showWorkDaysSelector()
        }
        
        materialTimePicker.show(childFragmentManager, "START_TIME_PICKER")
    }
    
    private fun showEndTimeSelector(daysText: String, startHour: Int, startMinute: Int) {
        // Extract end time from current value if it exists
        var endHour = 17  // Default 5 PM
        var endMinute = 0
        
        val currentValue = etAvailabilitySchedule.text.toString()
        if (currentValue.contains("-")) {
            try {
                val timePart = currentValue.substringAfter(",").trim()
                val endTimePart = timePart.substringAfter("-").trim()
                
                if (endTimePart.contains("AM") || endTimePart.contains("PM")) {
                    // Parse time like "5:00PM"
                    val timeValue = endTimePart.replace("AM", "").replace("PM", "").trim()
                    val isPM = endTimePart.contains("PM")
                    
                    var parsedHour = timeValue.substringBefore(":").toInt()
                    val parsedMinute = if (timeValue.contains(":")) {
                        timeValue.substringAfter(":").toInt()
                    } else {
                        0
                    }
                    
                    if (isPM && parsedHour < 12) parsedHour += 12
                    if (!isPM && parsedHour == 12) parsedHour = 0
                    
                    endHour = parsedHour
                    endMinute = parsedMinute
                }
            } catch (e: Exception) {
                Log.e(tag, "Error parsing existing end time", e)
            }
        }
        
        // Use Material TimePicker for end time
        val materialTimePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(endHour)
            .setMinute(endMinute)
            .setTitleText("Select End Time")
            .build()
            
        materialTimePicker.addOnPositiveButtonClickListener {
            // Format the times in 12-hour format
            val startTimeFormatted = formatTime(startHour, startMinute)
            val endTimeFormatted = formatTime(materialTimePicker.hour, materialTimePicker.minute)
            
            // Check if end time is after start time
            val startTimeInMinutes = startHour * 60 + startMinute
            val endTimeInMinutes = materialTimePicker.hour * 60 + materialTimePicker.minute
            
            if (endTimeInMinutes <= startTimeInMinutes) {
                // Show error and ask to select again
                Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
                // Show the end time picker again
                Handler(Looper.getMainLooper()).postDelayed({
                    showEndTimeSelector(daysText, startHour, startMinute)
                }, 500)
                return@addOnPositiveButtonClickListener
            }
            
            // Combine everything and show a summary
            val fullSchedule = "$daysText, $startTimeFormatted-$endTimeFormatted"
            showScheduleSummary(fullSchedule)
        }
        
        materialTimePicker.addOnCancelListener {
            // If cancelled, go back to start time selection
            showStartTimeSelector(daysText)
        }
        
        materialTimePicker.show(childFragmentManager, "END_TIME_PICKER")
    }
    
    private fun showScheduleSummary(schedule: String) {
        // Show a nice summary dialog with the selected schedule
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Availability Schedule")
            .setMessage("Your availability has been set to:\n\n$schedule")
            .setPositiveButton("Confirm") { _, _ ->
                etAvailabilitySchedule.setText(schedule)
            }
            .setNegativeButton("Edit") { _, _ ->
                // Go back to day selection
                showWorkDaysSelector()
            }
            .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_background))
            .show()
    }
    
    private fun formatTime(hour: Int, minute: Int): String {
        val hourIn12Format = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val amPm = if (hour >= 12) "PM" else "AM"
        return if (minute == 0) {
            "${hourIn12Format}${amPm}"
        } else {
            "${hourIn12Format}:${minute.toString().padStart(2, '0')}${amPm}"
        }
    }
    
    private fun loadBusinessDetails() {
        try {
            if (!isAdded) {
                Log.w(tag, "Fragment not attached when trying to load business details")
                return
            }
            
            progressBar.visibility = View.VISIBLE
            tvErrorMessage.visibility = View.GONE
            
            if (providerId <= 0) {
                Log.e(tag, "Cannot load business details: provider ID is invalid ($providerId)")
                
                // For new accounts, don't show error, just show empty form
                checkIfProviderExists()
                return
            }
            
            Log.d(tag, "Loading business details for provider ID: $providerId")
            userApiClient.getServiceProviderProfile(providerId, token) { provider, error ->
                try {
                    if (!isAdded) {
                        Log.w(tag, "Fragment not attached when receiving business details")
                        return@getServiceProviderProfile
                    }
                    
                    val activity = activity ?: run {
                        Log.w(tag, "Activity null when receiving business details")
                        return@getServiceProviderProfile
                    }
                    
                    activity.runOnUiThread {
                        try {
                            if (!isAdded) {
                                Log.w(tag, "Fragment not attached when updating UI with business details")
                                return@runOnUiThread
                            }
                            
                            progressBar.visibility = View.GONE
                            
                            if (error != null) {
                                Log.e(tag, "Error loading business details", error)
                                
                                // Don't display error message for new accounts
                                checkIfProviderExists()
                                return@runOnUiThread
                            }
                            
                            if (provider != null) {
                                // Store the provider data
                                updateBusinessDetailsUI(provider)
                            } else {
                                // Try to check if provider exists with a different ID
                                checkIfProviderExists()
                            }
                        } catch (e: Exception) {
                            Log.e(tag, "Error updating UI with business details", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error in provider profile callback", e)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error in loadBusinessDetails", e)
            try {
                if (isAdded) {
                    progressBar.visibility = View.GONE
                    tvErrorMessage.visibility = View.VISIBLE
                    tvErrorMessage.text = "Error loading business details: ${e.message}"
                }
            } catch (e2: Exception) {
                Log.e(tag, "Error handling exception in loadBusinessDetails", e2)
            }
        }
    }
    
    private fun checkIfProviderExists() {
        try {
            if (!isAdded) {
                Log.w(tag, "Fragment not attached when checking if provider exists")
                return
            }
            
            // Check if the provider account exists by auth ID
            userApiClient.getServiceProviderByAuthId(userId, token) { provider, error ->
                try {
                    if (!isAdded) {
                        Log.w(tag, "Fragment not attached when checking if provider exists")
                        return@getServiceProviderByAuthId
                    }
                    
                    val activity = activity ?: run {
                        Log.w(tag, "Activity null when checking if provider exists")
                        return@getServiceProviderByAuthId
                    }
                    
                    activity.runOnUiThread {
                        try {
                            if (!isAdded) {
                                Log.w(tag, "Fragment not attached when updating UI after provider check")
                                return@runOnUiThread
                            }
                            
                            if (error != null) {
                                // Provider account doesn't exist due to error
                                if (context != null) {
                                    Toast.makeText(
                                        context,
                                        "Error checking provider account: ${error.message}. Please contact support.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                
                                // Show empty form with defaults for new user
                                setupEmptyBusinessDetails()
                            } else if (provider == null) {
                                // Provider account doesn't exist - show empty form with defaults
                                setupEmptyBusinessDetails()
                                
                                if (context != null) {
                                    Toast.makeText(
                                        context,
                                        "No provider account found. You'll need to set up your business details.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } else {
                                // We found a provider but with a different ID - update it
                                providerId = provider.providerId ?: 0L
                                
                                // Save provider ID to SharedPreferences
                                if (isAdded) {
                                    val userPrefs = activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                                    userPrefs.edit().putLong("providerId", providerId).apply()
                                    
                                    Log.d(tag, "Updated provider ID to: $providerId")
                                    
                                    // Try loading again with the correct provider ID
                                    loadBusinessDetails()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(tag, "Error updating UI when checking provider", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error in checkIfProviderExists callback", e)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error in checkIfProviderExists", e)
            try {
                if (isAdded && context != null) {
                    Toast.makeText(
                        context,
                        "Error checking provider: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    setupEmptyBusinessDetails()
                }
            } catch (e2: Exception) {
                Log.e(tag, "Error handling exception in checkIfProviderExists", e2)
            }
        }
    }
    
    private fun setupEmptyBusinessDetails() {
        try {
            if (!isAdded) {
                Log.w(tag, "Fragment not attached when setting up empty business details")
                return
            }
            
            // Set default values for a new business account
            etBusinessName.setText("")
            etBusinessDesc.setText("")
            etBusinessCategory.setText("")
            etYearEstablished.setText("")
            etYearsExperience.setText("0")
            etAvailabilitySchedule.setText("Monday-Friday, 9AM-5PM") // Default schedule
            etPaymentMethod.setText("")
            dropdownStatus.setText("Active", false)
            
            // Hide error message and progress bar
            tvErrorMessage.visibility = View.GONE
            progressBar.visibility = View.GONE
        } catch (e: Exception) {
            Log.e(tag, "Error in setupEmptyBusinessDetails", e)
        }
    }
    
    private fun updateBusinessDetailsUI(provider: ServiceProvider) {
        try {
            if (!isAdded) {
                Log.w(tag, "Fragment not attached when updating business UI")
                return
            }
            
            val activity = activity ?: run {
                Log.w(tag, "Activity null when updating business UI")
                return
            }
            
            // Fill form with business details - Safely handle nullable values
            etBusinessName.setText(provider.businessName ?: "")
            
            // We're storing these fields in SharedPreferences temporarily until backend supports them
            val prefs = activity.getSharedPreferences("ProviderBusinessDetails", Context.MODE_PRIVATE)
            
            businessDescription = prefs.getString("businessDescription_$providerId", "") ?: ""
            businessCategory = prefs.getString("businessCategory_$providerId", "") ?: ""
            yearEstablished = prefs.getString("yearEstablished_$providerId", "") ?: ""
            status = provider.status ?: prefs.getString("status_$providerId", "Active") ?: "Active"
            
            etBusinessDesc.setText(businessDescription)
            etBusinessCategory.setText(businessCategory)
            etYearEstablished.setText(yearEstablished)
            
            etYearsExperience.setText(provider.yearsOfExperience?.toString() ?: "0")
            etAvailabilitySchedule.setText(provider.availabilitySchedule ?: "Monday-Friday, 9AM-5PM") // Default schedule
            etPaymentMethod.setText(provider.paymentMethod ?: "")
            
            // Set status (use from API if available, otherwise from SharedPreferences)
            dropdownStatus.setText(status.ifEmpty { "Active" }, false)
        } catch (e: Exception) {
            Log.e(tag, "Error updating business details UI", e)
        }
    }
    
    private fun validateInputs(): Boolean {
        try {
            if (!isAdded) {
                Log.w(tag, "Fragment not attached when validating inputs")
                return false
            }
            
            var isValid = true
            
            if (etBusinessName.text.toString().trim().isEmpty()) {
                (etBusinessName.parent.parent as? TextInputLayout)?.error = getString(R.string.error_business_name_empty)
                isValid = false
            } else {
                (etBusinessName.parent.parent as? TextInputLayout)?.error = null
            }
            
            val yearsExperience = etYearsExperience.text.toString().trim()
            if (yearsExperience.isEmpty()) {
                (etYearsExperience.parent.parent as? TextInputLayout)?.error = getString(R.string.error_years_experience_empty)
                isValid = false
            } else {
                try {
                    val years = yearsExperience.toInt()
                    if (years < 0) {
                        (etYearsExperience.parent.parent as? TextInputLayout)?.error = getString(R.string.error_years_experience_negative)
                        isValid = false
                    } else {
                        (etYearsExperience.parent.parent as? TextInputLayout)?.error = null
                    }
                } catch (e: NumberFormatException) {
                    (etYearsExperience.parent.parent as? TextInputLayout)?.error = getString(R.string.error_years_experience_invalid)
                    isValid = false
                }
            }
            
            if (etAvailabilitySchedule.text.toString().trim().isEmpty()) {
                (etAvailabilitySchedule.parent.parent as? TextInputLayout)?.error = getString(R.string.error_availability_empty)
                isValid = false
            } else {
                (etAvailabilitySchedule.parent.parent as? TextInputLayout)?.error = null
            }
            
            // Validate year established if it's not empty
            val yearEstablished = etYearEstablished.text.toString().trim()
            if (yearEstablished.isNotEmpty()) {
                try {
                    val year = yearEstablished.toInt()
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    if (year < 1900 || year > currentYear) {
                        (etYearEstablished.parent.parent as? TextInputLayout)?.error = getString(R.string.error_year_established_invalid)
                        isValid = false
                    } else {
                        (etYearEstablished.parent.parent as? TextInputLayout)?.error = null
                    }
                } catch (e: NumberFormatException) {
                    (etYearEstablished.parent.parent as? TextInputLayout)?.error = getString(R.string.error_year_established_invalid)
                    isValid = false
                }
            }
            
            return isValid
        } catch (e: Exception) {
            Log.e(tag, "Error validating inputs", e)
            return false
        }
    }
    
    private fun updateBusinessDetails() {
        try {
            if (!isAdded) {
                Log.w(tag, "Fragment not attached when updating business details")
                return
            }
            
            val activity = activity ?: run {
                Log.w(tag, "Activity null when updating business details")
                return
            }
            
            progressBar.visibility = View.VISIBLE
            
            val businessName = etBusinessName.text.toString().trim()
            val businessDesc = etBusinessDesc.text.toString().trim()
            val businessCategory = etBusinessCategory.text.toString().trim()
            val yearEstablished = etYearEstablished.text.toString().trim()
            val yearsExperience = etYearsExperience.text.toString().trim().toIntOrNull() ?: 0
            val availabilitySchedule = etAvailabilitySchedule.text.toString().trim()
            val paymentMethod = etPaymentMethod.text.toString().trim().takeIf { it.isNotEmpty() } ?: ""
            val status = dropdownStatus.text.toString()
            
            // Save extended fields in SharedPreferences until backend supports them
            val prefs = activity.getSharedPreferences("ProviderBusinessDetails", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString("businessDescription_$providerId", businessDesc)
                putString("businessCategory_$providerId", businessCategory)
                putString("yearEstablished_$providerId", yearEstablished)
                putString("status_$providerId", status)
                apply()
            }
            
            // If providerId is 0, try to get it from SharedPreferences
            updateProvider()
        } catch (e: Exception) {
            Log.e(tag, "Error in updateBusinessDetails", e)
        }
    }
    
    private fun updateProvider() {
        progressBar.visibility = View.VISIBLE
        
        if (providerId == 0L) {
            val prefs = try {
                if (!isAdded) {
                    Log.w(tag, "Fragment not attached when trying to get prefs")
                    progressBar.visibility = View.GONE
                    return
                }
                requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            } catch (e: Exception) {
                Log.e(tag, "Error accessing SharedPreferences", e)
                progressBar.visibility = View.GONE
                return
            }
            
            providerId = prefs.getLong("providerId", 0)
            
            if (providerId == 0L) {
                // If still 0, try to find it by loading provider profile first
                userApiClient.getServiceProviderProfile(userId, token) { provider, error ->
                    try {
                        if (!isAdded) {
                            Log.w(tag, "Fragment not attached in provider callback")
                            return@getServiceProviderProfile
                        }
                        
                        val activity = activity ?: run {
                            Log.w(tag, "Activity null in provider callback")
                            return@getServiceProviderProfile
                        }
                        
                        if (provider != null && provider.providerId != null && provider.providerId > 0) {
                            providerId = provider.providerId
                            try {
                                val userPrefs = activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                                userPrefs.edit().putLong("providerId", providerId).apply()
                                
                                // Now proceed with the update using the found providerId
                                directUpdateProvider(provider)
                            } catch (e: Exception) {
                                Log.e(tag, "Error saving providerId", e)
                                activity.runOnUiThread {
                                    try {
                                        progressBar.visibility = View.GONE
                                        Toast.makeText(context, "Error saving provider ID: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } catch (e2: Exception) {
                                        Log.e(tag, "Error updating UI", e2)
                                    }
                                }
                            }
                        } else {
                            activity.runOnUiThread {
                                try {
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(context, "Failed to find provider ID. Please refresh and try again.", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e(tag, "Error showing toast", e)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Error in getServiceProviderProfile callback", e)
                    }
                }
                return
            }
        }
        
        // Try to direct update if we have a providerId
        userApiClient.getServiceProviderByAuthId(userId, token) { provider, error ->
            try {
                if (!isAdded) {
                    Log.w(tag, "Fragment not attached in getServiceProviderByAuthId callback")
                    return@getServiceProviderByAuthId
                }
                
                val activity = activity ?: run {
                    Log.w(tag, "Activity null in getServiceProviderByAuthId callback")
                    return@getServiceProviderByAuthId
                }
                
                if (error != null || provider == null) {
                    activity.runOnUiThread {
                        try {
                            progressBar.visibility = View.GONE
                            Toast.makeText(context, "Failed to get provider details", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e(tag, "Error showing toast", e)
                        }
                    }
                    return@getServiceProviderByAuthId
                }
                
                directUpdateProvider(provider)
            } catch (e: Exception) {
                Log.e(tag, "Error in getServiceProviderByAuthId callback", e)
            }
        }
    }
    
    private fun directUpdateProvider(provider: ServiceProvider) {
        try {
            if (!isAdded) {
                Log.w(tag, "Fragment not attached in directUpdateProvider")
                return
            }
            
            val activity = activity ?: run {
                Log.w(tag, "Activity null in directUpdateProvider")
                return
            }
            
            // Get values from form
            val businessName = etBusinessName.text.toString().trim()
            val yearsExperience = etYearsExperience.text.toString().trim().toIntOrNull() ?: 0
            val availabilitySchedule = etAvailabilitySchedule.text.toString().trim()
            val paymentMethod = etPaymentMethod.text.toString().trim().takeIf { it.isNotEmpty() } ?: ""
            val status = dropdownStatus.text.toString()
            
            // Update provider object
            val updatedProvider = ServiceProvider(
                providerId = provider.providerId,
                firstName = provider.firstName,
                lastName = provider.lastName,
                phoneNumber = provider.phoneNumber,
                businessName = businessName,
                yearsOfExperience = yearsExperience,
                availabilitySchedule = availabilitySchedule,
                paymentMethod = paymentMethod,
                status = status,
                address = provider.address,
                userAuth = provider.userAuth,
                profileImage = provider.profileImage
            )
            
            // Make the API call
            userApiClient.updateServiceProviderProfile(updatedProvider, token) { success, error ->
                try {
                    if (!isAdded) {
                        Log.w(tag, "Fragment not attached in updateServiceProvider callback")
                        return@updateServiceProviderProfile
                    }
                    
                    activity.runOnUiThread {
                        try {
                            progressBar.visibility = View.GONE
                            
                            if (error != null) {
                                Toast.makeText(context, getString(R.string.error_business_update, error.message), Toast.LENGTH_SHORT).show()
                            } else if (success) {
                                Toast.makeText(context, R.string.business_update_success, Toast.LENGTH_SHORT).show()
                                
                                // Refresh the business details to show the updated data from the server
                                Handler(Looper.getMainLooper()).postDelayed({
                                    if (isAdded) {
                                        loadBusinessDetails()
                                    }
                                }, 500) // 500ms delay before refreshing
                            } else {
                                Toast.makeText(context, R.string.business_update_failed, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e(tag, "Error updating UI after business details update", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error in updateServiceProvider callback", e)
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error in directUpdateProvider", e)
            try {
                if (isAdded) {
                    requireActivity().runOnUiThread {
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e2: Exception) {
                Log.e(tag, "Error updating UI after exception", e2)
            }
        }
    }
    
    companion object {
        @JvmStatic
        fun newInstance(userId: Long, token: String, providerId: Long) =
            ServiceProviderBusinessDetailsFragment().apply {
                arguments = Bundle().apply {
                    putLong("userId", userId)
                    putString("token", token)
                    putLong("providerId", providerId)
                }
            }
    }
}