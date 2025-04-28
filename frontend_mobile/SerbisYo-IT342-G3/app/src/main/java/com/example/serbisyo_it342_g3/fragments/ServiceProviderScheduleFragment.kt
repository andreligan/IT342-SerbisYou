package com.example.serbisyo_it342_g3.fragments

import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.adapter.ScheduleAdapter
import com.example.serbisyo_it342_g3.api.ApiClient
import com.example.serbisyo_it342_g3.api.ScheduleApiClient
import com.example.serbisyo_it342_g3.data.Schedule
import com.example.serbisyo_it342_g3.utils.Constants
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ServiceProviderScheduleFragment : Fragment() {
    private val tag = "SPScheduleFragment"
    
    // UI Components
    private lateinit var spinnerDayOfWeek: Spinner
    private lateinit var etStartTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var btnSelectStartTime: ImageButton
    private lateinit var btnSelectEndTime: ImageButton
    private lateinit var checkboxAvailable: CheckBox
    private lateinit var btnAddSchedule: Button
    private lateinit var recyclerViewSchedules: RecyclerView
    private lateinit var tvNoSchedules: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvErrorMessage: TextView
    private lateinit var tvSuccessMessage: TextView
    
    // Data
    private lateinit var scheduleApiClient: ScheduleApiClient
    private var token: String = ""
    private var userId: Long = 0
    private var providerId: Long = 0
    private val schedules = mutableListOf<Schedule>()
    private lateinit var scheduleAdapter: ScheduleAdapter
    
    // Calendar for time pickers
    private val calendar = Calendar.getInstance()
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_provider_schedule, container, false)
        
        // Get arguments
        arguments?.let {
            userId = it.getLong("userId", 0)
            token = it.getString("token", "") ?: ""
            providerId = it.getLong("providerId", 0)
        }

        // If provider ID is not passed, try to get from SharedPreferences
        if (providerId <= 0) {
            val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            providerId = prefs.getLong("provider_id", 0)
            
            // Also try to get the token if it's not passed
            if (token.isEmpty()) {
                token = prefs.getString("auth_token", "") ?: ""
            }
        }

        Log.d(tag, "Using provider ID: $providerId, with token length: ${token.length}")

        // Initialize API client
        scheduleApiClient = ScheduleApiClient(requireContext())
        
        // Initialize views
        spinnerDayOfWeek = view.findViewById(R.id.spinnerDayOfWeek)
        etStartTime = view.findViewById(R.id.etStartTime)
        etEndTime = view.findViewById(R.id.etEndTime)
        btnSelectStartTime = view.findViewById(R.id.btnSelectStartTime)
        btnSelectEndTime = view.findViewById(R.id.btnSelectEndTime)
        checkboxAvailable = view.findViewById(R.id.checkboxAvailable)
        btnAddSchedule = view.findViewById(R.id.btnAddSchedule)
        recyclerViewSchedules = view.findViewById(R.id.recyclerViewSchedules)
        tvNoSchedules = view.findViewById(R.id.tvNoSchedules)
        progressBar = view.findViewById(R.id.progressBar)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)
        tvSuccessMessage = view.findViewById(R.id.tvSuccessMessage)
        
        // Setup day of week spinner
        setupDayOfWeekSpinner()
        
        // Setup time pickers
        setupTimePickers()
        
        // Setup recycler view
        setupRecyclerView()
        
        // Set button click listener
        btnAddSchedule.setOnClickListener {
            if (validateInputs()) {
                addSchedule()
            }
        }
        
        // Load schedules
        loadSchedules()
        
        return view
    }
    
    private fun setupDayOfWeekSpinner() {
        val days = arrayOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
        val daysDisplay = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, daysDisplay)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDayOfWeek.adapter = adapter
        
        spinnerDayOfWeek.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Day selected - do nothing special here
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    private fun setupTimePickers() {
        // Set default times
        etStartTime.setText("08:00")
        etEndTime.setText("17:00")
        
        // Setup time picker dialogs
        btnSelectStartTime.setOnClickListener {
            showTimePickerDialog(true)
        }
        
        btnSelectEndTime.setOnClickListener {
            showTimePickerDialog(false)
        }
        
        // Make edit texts not directly editable
        etStartTime.setOnClickListener {
            showTimePickerDialog(true)
        }
        
        etEndTime.setOnClickListener {
            showTimePickerDialog(false)
        }
    }
    
    private fun showTimePickerDialog(isStartTime: Boolean) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minuteOfHour ->
                val formattedTime = String.format("%02d:%02d", hourOfDay, minuteOfHour)
                if (isStartTime) {
                    etStartTime.setText(formattedTime)
                } else {
                    etEndTime.setText(formattedTime)
                }
            },
            hour,
            minute,
            true // 24-hour format
        )
        
        timePickerDialog.show()
    }
    
    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter(schedules) { schedule ->
            deleteSchedule(schedule)
        }
        
        recyclerViewSchedules.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = scheduleAdapter
        }
    }
    
    private fun loadSchedules() {
        if (providerId <= 0) {
            checkProviderId()
            return
        }
        
        progressBar.visibility = View.VISIBLE
        tvErrorMessage.visibility = View.GONE
        
        // Alternative approach: Use a direct OkHttp client call to match the web implementation
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(tag, "Directly fetching schedules for provider: $providerId")
                val client = OkHttpClient()
                val url = "${Constants.BASE_URL}schedules/provider/$providerId"
                
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: "[]"
                        Log.d(tag, "Raw schedule response: $responseBody")
                        
                        val fetchedSchedules = mutableListOf<Schedule>()
                        
                        if (responseBody.trim().startsWith("[")) {
                            val jsonArray = JSONArray(responseBody)
                            for (i in 0 until jsonArray.length()) {
                                val jsonObject = jsonArray.getJSONObject(i)
                                
                                try {
                                    val schedule = Schedule(
                                        scheduleId = jsonObject.optLong("scheduleId"),
                                        providerId = jsonObject.optLong("providerId"),
                                        dayOfWeek = jsonObject.optString("dayOfWeek", ""),
                                        startTime = jsonObject.optString("startTime", ""),
                                        endTime = jsonObject.optString("endTime", ""),
                                        isAvailable = jsonObject.optBoolean("isAvailable", true)
                                    )
                                    fetchedSchedules.add(schedule)
                                    Log.d(tag, "Parsed schedule: ${schedule.dayOfWeek} ${schedule.startTime}-${schedule.endTime}")
                                } catch (e: Exception) {
                                    Log.e(tag, "Error parsing schedule at position $i", e)
                                }
                            }
                        }
                        
                        withContext(Dispatchers.Main) {
                            if (!isAdded) return@withContext
                            
                            progressBar.visibility = View.GONE
                            
                            if (fetchedSchedules.isEmpty()) {
                                tvNoSchedules.visibility = View.VISIBLE
                                tvNoSchedules.text = "No schedules found. Add a schedule to get started."
                            } else {
                                tvNoSchedules.visibility = View.GONE
                                schedules.clear()
                                schedules.addAll(fetchedSchedules)
                                scheduleAdapter.updateSchedules(fetchedSchedules)
                                Log.d(tag, "Updated adapter with ${fetchedSchedules.size} schedules")
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            if (!isAdded) return@withContext
                            
                            progressBar.visibility = View.GONE
                            
                            // For 404 errors (no schedules found), show empty state
                            if (response.code == 404) {
                                tvNoSchedules.visibility = View.VISIBLE
                                tvNoSchedules.text = "No schedules found. Add a schedule to get started."
                            } else {
                                val errorMsg = "Failed to fetch schedules: HTTP ${response.code}"
                                Log.e(tag, errorMsg)
                                tvErrorMessage.text = errorMsg
                                tvErrorMessage.visibility = View.VISIBLE
                                
                                // Could be a problem with provider ID - check it
                                if (response.code == 400 || response.code == 401) {
                                    checkProviderId()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception when fetching schedules", e)
                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext
                    
                    progressBar.visibility = View.GONE
                    
                    // Show empty state instead of error for new providers
                    tvNoSchedules.visibility = View.VISIBLE
                    tvNoSchedules.text = "No schedules found. Add a schedule to get started."
                }
            }
        }
    }
    
    private fun checkProviderId() {
        progressBar.visibility = View.VISIBLE
        
        // Use Coroutines for background processing
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Make API call to get provider ID by user ID
                val client = OkHttpClient()
                val url = "${Constants.BASE_URL}user-auth/getUserByAuthId/$userId"
                
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: "{}"
                        val jsonObject = JSONObject(responseBody)
                        val serviceProvider = jsonObject.optJSONObject("serviceProvider")
                        
                        if (serviceProvider != null) {
                            val providerId = serviceProvider.optLong("providerId", 0)
                            
                            withContext(Dispatchers.Main) {
                                if (!isAdded) return@withContext
                                
                                if (providerId > 0) {
                                    this@ServiceProviderScheduleFragment.providerId = providerId
                                    
                                    // Save to SharedPreferences
                                    saveProviderId(providerId)
                                    
                                    // Now load schedules with this provider ID
                                    loadSchedules()
                                } else {
                                    // New provider account with no schedules yet
                                    progressBar.visibility = View.GONE
                                    tvNoSchedules.visibility = View.VISIBLE
                                    tvNoSchedules.text = "No schedules found. Add a schedule to get started."
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                if (!isAdded) return@withContext
                                
                                // New provider account with no schedules yet
                                progressBar.visibility = View.GONE
                                tvNoSchedules.visibility = View.VISIBLE
                                tvNoSchedules.text = "No schedules found. Add a schedule to get started."
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            if (!isAdded) return@withContext
                            
                            progressBar.visibility = View.GONE
                            
                            // For new providers, show empty state instead of error
                            tvNoSchedules.visibility = View.VISIBLE
                            tvNoSchedules.text = "No schedules found. Add a schedule to get started."
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error checking provider ID", e)
                
                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext
                    
                    progressBar.visibility = View.GONE
                    tvNoSchedules.visibility = View.VISIBLE
                    tvNoSchedules.text = "No schedules found. Add a schedule to get started."
                }
            }
        }
    }
    
    private fun saveProviderId(providerId: Long) {
        // Save provider ID to both SharedPreferences locations for consistency
        val userPrefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userPrefs.edit().putLong("providerId", providerId).apply()
        
        val userPrefs2 = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userPrefs2.edit().putLong("providerId", providerId).apply()
        
        Log.d(tag, "Saved provider ID: $providerId to SharedPreferences")
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        val startTime = etStartTime.text.toString()
        val endTime = etEndTime.text.toString()
        
        if (startTime.isEmpty()) {
            Toast.makeText(context, "Please select a start time", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        if (endTime.isEmpty()) {
            Toast.makeText(context, "Please select an end time", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        // Compare times as strings since they're in HH:MM format
        if (startTime.isNotEmpty() && endTime.isNotEmpty() && startTime >= endTime) {
            Toast.makeText(context, "End time must be after start time", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        return isValid
    }
    
    private fun addSchedule() {
        progressBar.visibility = View.VISIBLE
        
        val selectedDayPosition = spinnerDayOfWeek.selectedItemPosition
        val days = arrayOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
        val dayOfWeek = days[selectedDayPosition]
        
        val startTime = etStartTime.text.toString()
        val endTime = etEndTime.text.toString()
        val isAvailable = checkboxAvailable.isChecked
        
        Log.d(tag, "Adding schedule: day=$dayOfWeek, start=$startTime, end=$endTime, available=$isAvailable")
        
        // Create a JSON object directly to match the web application's approach
        val scheduleData = JSONObject().apply {
            put("dayOfWeek", dayOfWeek)
            put("startTime", startTime)
            put("endTime", endTime)
            put("isAvailable", isAvailable)
            put("available", isAvailable)  // Include both formats for compatibility
        }
        
        // Use Coroutines and OkHttp for a more direct approach
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create the schedule object for local use
                val newSchedule = Schedule(
                    providerId = providerId,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    isAvailable = isAvailable
                )
                
                // Use OkHttp to match web implementation
                val client = OkHttpClient()
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val requestBody = scheduleData.toString().toRequestBody(mediaType)
                
                val request = Request.Builder()
                    .url("${Constants.BASE_URL}schedules/provider/$providerId")
                    .addHeader("Authorization", "Bearer $token")
                    .post(requestBody)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    withContext(Dispatchers.Main) {
                        if (!isAdded) return@withContext
                        
                        progressBar.visibility = View.GONE
                        
                        if (response.isSuccessful) {
                            // Add to local list and update UI
                            schedules.add(newSchedule)
                            scheduleAdapter.updateSchedules(schedules)
                            
                            // Show success message
                            tvSuccessMessage.text = "Schedule added successfully!"
                            tvSuccessMessage.visibility = View.VISIBLE
                            tvNoSchedules.visibility = View.GONE
                            tvSuccessMessage.postDelayed({ 
                                if (isAdded) tvSuccessMessage.visibility = View.GONE 
                            }, 3000)
                            
                            // Reset form
                            etStartTime.setText("08:00")
                            etEndTime.setText("17:00")
                            checkboxAvailable.isChecked = true
                            
                            // Reload schedules after a short delay to allow server to process
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (isAdded) loadSchedules()
                            }, 1000)
                        } else {
                            val errorBody = response.body?.string() ?: "Unknown error"
                            Log.e(tag, "Failed to add schedule: HTTP ${response.code}, $errorBody")
                            Toast.makeText(context, "Failed to add schedule: ${response.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception adding schedule", e)
                withContext(Dispatchers.Main) {
                    if (!isAdded) return@withContext
                    
                    progressBar.visibility = View.GONE
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun deleteSchedule(schedule: Schedule) {
        if (schedule.scheduleId == null) {
            Toast.makeText(context, "Invalid schedule ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        val scheduleId = schedule.scheduleId
        
        progressBar.visibility = View.VISIBLE
        
        scheduleApiClient.deleteSchedule(scheduleId, token) { success, error ->
            if (!isAdded) return@deleteSchedule
            
            try {
                val activity = requireActivity()
                activity.runOnUiThread {
                    progressBar.visibility = View.GONE
                    
                    if (error != null) {
                        Log.e(tag, "Error deleting schedule", error)
                        Toast.makeText(context, "Failed to delete schedule: ${error.message}", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }
                    
                    if (success) {
                        // Show success message
                        Toast.makeText(context, "Schedule deleted successfully", Toast.LENGTH_SHORT).show()
                        
                        // Reload schedules
                        loadSchedules()
                    } else {
                        Toast.makeText(context, "Failed to delete schedule", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IllegalStateException) {
                Log.e(tag, "Fragment not attached to activity when deleting schedule", e)
                // Cannot update UI if fragment is detached
            }
        }
    }
    
    companion object {
        @JvmStatic
        fun newInstance(userId: Long, token: String, providerId: Long) =
            ServiceProviderScheduleFragment().apply {
                arguments = Bundle().apply {
                    putLong("userId", userId)
                    putString("token", token)
                    putLong("providerId", providerId)
                }
            }
    }
}