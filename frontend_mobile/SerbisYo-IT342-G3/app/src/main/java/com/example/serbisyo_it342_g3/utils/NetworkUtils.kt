package com.example.serbisyo_it342_g3.utils

import android.util.Log
import com.example.serbisyo_it342_g3.data.Address
import com.example.serbisyo_it342_g3.data.Schedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Utility class for network operations that need to be performed in background threads
 */
object NetworkUtils {
    private val TAG = "NetworkUtils"
    
    // Create an OkHttpClient with longer timeouts
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Fetch schedules for a service provider by day of week
     */
    suspend fun fetchProviderSchedulesByDay(
        providerId: Long,
        dayOfWeek: String,
        token: String
    ): List<Schedule> = withContext(Dispatchers.IO) {
        try {
            val url = "${Constants.BASE_URL}schedules/provider/$providerId/day/$dayOfWeek"
            Log.d(TAG, "Fetching schedules from URL: $url")
            
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                Log.e(TAG, "Error fetching schedules: ${response.code}")
                return@withContext emptyList<Schedule>()
            }
            
            val responseString = response.body?.string() ?: "[]"
            Log.d(TAG, "Schedule response: $responseString")
            
            if (responseString.trim().isEmpty() || responseString.trim() == "[]") {
                Log.d(TAG, "Empty schedule response")
                return@withContext emptyList<Schedule>()
            }
            
            val schedules = mutableListOf<Schedule>()
            val jsonArray = JSONArray(responseString)
            
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                
                // Check for availability
                val isAvailable = jsonObject.optBoolean("available", false) || 
                                 jsonObject.optBoolean("isAvailable", false)
                
                if (isAvailable) {
                    val schedule = Schedule(
                        scheduleId = jsonObject.optLong("scheduleId", 0),
                        providerId = jsonObject.optLong("providerId", 0),
                        dayOfWeek = jsonObject.optString("dayOfWeek", ""),
                        startTime = jsonObject.optString("startTime", ""),
                        endTime = jsonObject.optString("endTime", ""),
                        isAvailable = true
                    )
                    schedules.add(schedule)
                    Log.d(TAG, "Added schedule: ${schedule.startTime} - ${schedule.endTime}")
                }
            }
            
            return@withContext schedules
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching schedules", e)
            return@withContext emptyList<Schedule>()
        }
    }
    
    /**
     * Fetch customer addresses
     */
    suspend fun fetchCustomerAddresses(
        customerId: Long,
        token: String
    ): List<Address> = withContext(Dispatchers.IO) {
        try {
            val url = "${Constants.BASE_URL}addresses/getAll"
            Log.d(TAG, "Fetching addresses from URL: $url")
            
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            
            if (!response.isSuccessful) {
                Log.e(TAG, "Error fetching addresses: ${response.code}")
                return@withContext emptyList<Address>()
            }
            
            val responseString = response.body?.string() ?: "[]"
            Log.d(TAG, "All addresses response: $responseString")
            
            if (responseString.trim().isEmpty() || responseString.trim() == "[]") {
                Log.d(TAG, "Empty address response")
                return@withContext emptyList<Address>()
            }
            
            val addresses = mutableListOf<Address>()
            val jsonArray = JSONArray(responseString)
            
            for (i in 0 until jsonArray.length()) {
                try {
                    val jsonObject = jsonArray.getJSONObject(i)
                    
                    // Check all possible ways to find customer ID in the address
                    var addressCustomerId: Long = 0
                    
                    // Option 1: Direct "customerId" field
                    if (jsonObject.has("customerId")) {
                        addressCustomerId = jsonObject.optLong("customerId", 0)
                    }
                    
                    // Option 2: Customer object
                    val customerObj = jsonObject.optJSONObject("customer")
                    if (customerObj != null) {
                        addressCustomerId = customerObj.optLong("customerId", 0)
                    }
                    
                    // Check if address belongs to this customer
                    if (addressCustomerId == customerId) {
                        val isMain = jsonObject.optBoolean("main", false)
                        
                        val address = Address(
                            addressId = jsonObject.optLong("addressId", 0),
                            street = jsonObject.optString("street", ""),
                            streetName = jsonObject.optString("streetName", ""),
                            city = jsonObject.optString("city", ""),
                            province = jsonObject.optString("province", ""),
                            postalCode = jsonObject.optString("postalCode", ""),
                            zipCode = jsonObject.optString("zipCode", ""),
                            barangay = jsonObject.optString("barangay", ""),
                            main = isMain
                        )
                        addresses.add(address)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing address at index $i", e)
                }
            }
            
            return@withContext addresses
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching addresses", e)
            return@withContext emptyList<Address>()
        }
    }
} 