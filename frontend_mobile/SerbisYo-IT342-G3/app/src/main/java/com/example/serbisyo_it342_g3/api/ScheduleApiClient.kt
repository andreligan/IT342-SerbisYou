package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import com.example.serbisyo_it342_g3.data.Schedule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

class ScheduleApiClient(context: Context) {
    // Reference to the API service
    private val apiService = ApiClient.apiService
    private val appContext = context
    private val tag = "ScheduleApiClient"
    private val gson = Gson()

    // Network check function
    private fun isNetworkAvailable(): Boolean {
        // Simply return true for now to avoid errors
        // In a real app, this would check network connectivity
        return true
    }

    private suspend fun <T> executeWithToken(token: String, apiCall: suspend () -> Response<T>): Response<T> {
        // We would normally set the Authorization header here,
        // but the ApiClient already handles this through interceptors
        return apiCall()
    }

    // Get all schedules for a provider
    fun getProviderSchedules(
        providerId: Long,
        token: String,
        callback: (List<Schedule>?, Throwable?) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            callback(null, Exception("No internet connection"))
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(tag, "Fetching schedules for provider: $providerId")
                val response = executeWithToken(token) {
                    apiService.getProviderSchedules(providerId)
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        try {
                            val responseBody = response.body()
                            if (responseBody != null) {
                                val responseString = responseBody.string()
                                Log.d(tag, "Raw response: $responseString")
                                
                                if (responseString.isEmpty()) {
                                    Log.d(tag, "Empty response, returning empty list")
                                    callback(emptyList(), null)
                                    return@withContext
                                }
                                
                                try {
                                    // First try to parse as JSONArray
                                    if (responseString.trim().startsWith("[")) {
                                        val listType = object : TypeToken<List<Schedule>>() {}.type
                                        val schedules = gson.fromJson<List<Schedule>>(responseString, listType)
                                        Log.d(tag, "Parsed ${schedules.size} schedules as JSONArray")
                                        callback(schedules, null)
                                    } 
                                    // If it's not an array, try to handle it as an error message
                                    else {
                                        Log.d(tag, "Response is not a JSONArray, treating as empty list")
                                        callback(emptyList(), null)
                                    }
                                } catch (e: Exception) {
                                    Log.e(tag, "Error parsing JSON: ${e.message}", e)
                                    callback(null, Exception("Failed to parse response: ${e.message}"))
                                }
                            } else {
                                Log.d(tag, "Null response body, treating as empty list")
                                callback(emptyList(), null)
                            }
                        } catch (e: IOException) {
                            Log.e(tag, "IO Exception reading response: ${e.message}", e)
                            callback(null, e)
                        }
                    } else {
                        val errorMsg = "Failed to fetch schedules: HTTP ${response.code()}"
                        Log.e(tag, errorMsg)
                        callback(null, Exception(errorMsg))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception when fetching schedules", e)
                withContext(Dispatchers.Main) {
                    callback(null, e)
                }
            }
        }
    }

    // Create a new schedule
    fun createSchedule(
        providerId: Long,
        schedule: Schedule,
        token: String,
        callback: (Schedule?, Throwable?) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            callback(null, Exception("No internet connection"))
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(tag, "Creating schedule for provider: $providerId")
                Log.d(tag, "Schedule data: Day=${schedule.dayOfWeek}, Start=${schedule.startTime}, End=${schedule.endTime}, Available=${schedule.isAvailable}")
                
                val response = executeWithToken(token) {
                    apiService.createSchedule(providerId, schedule)
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        try {
                            val responseBody = response.body()
                            if (responseBody != null) {
                                val responseString = responseBody.string()
                                Log.d(tag, "Create schedule raw response: $responseString")
                                
                                if (responseString.isEmpty()) {
                                    Log.d(tag, "Empty response, but request was successful")
                                    // Even without a body, we consider it a success if the HTTP status is 200-299
                                    callback(schedule, null)
                                    return@withContext
                                }
                                
                                try {
                                    // Try to parse as Schedule object
                                    val createdSchedule = gson.fromJson(responseString, Schedule::class.java)
                                    Log.d(tag, "Successfully parsed created schedule")
                                    callback(createdSchedule, null)
                                } catch (e: Exception) {
                                    Log.e(tag, "Error parsing schedule JSON: ${e.message}", e)
                                    // Even if parsing fails, if the response was successful, return the original schedule
                                    callback(schedule, null)
                                }
                            } else {
                                Log.d(tag, "Null response body, but request was successful")
                                callback(schedule, null)
                            }
                        } catch (e: IOException) {
                            Log.e(tag, "IO Exception reading response: ${e.message}", e)
                            callback(null, e)
                        }
                    } else {
                        val errorMsg = "Failed to create schedule: HTTP ${response.code()}"
                        Log.e(tag, errorMsg)
                        callback(null, Exception(errorMsg))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception when creating schedule", e)
                withContext(Dispatchers.Main) {
                    callback(null, e)
                }
            }
        }
    }

    // Delete a schedule
    fun deleteSchedule(
        scheduleId: Long,
        token: String,
        callback: (Boolean, Throwable?) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            callback(false, Exception("No internet connection"))
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = executeWithToken(token) {
                    apiService.deleteSchedule(scheduleId)
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        callback(true, null)
                    } else {
                        callback(false, Exception(response.message() ?: "Failed to delete schedule"))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(false, e)
                }
            }
        }
    }
} 