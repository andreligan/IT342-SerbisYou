package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import com.example.serbisyo_it342_g3.data.Booking
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class BookingApiClient(private val context: Context) {
    private val client = OkHttpClient()
    private val gson = Gson()
    
    // CONFIGURATION FOR BACKEND CONNECTION
    // For Android Emulator - Virtual Device (default)
    private val EMULATOR_URL = "http://10.0.2.2:8080" 
    
    // For Physical Device - Use your computer's actual IP address from ipconfig
    private val PHYSICAL_DEVICE_URL = "http://172.20.10.2:8080"
    
    // SWITCH BETWEEN CONNECTION TYPES:
    // Uncomment the one you need and comment out the other
    // private val BASE_URL = EMULATOR_URL     // For Android Emulator
    private val BASE_URL = PHYSICAL_DEVICE_URL // For Physical Device
    
    private val TAG = "BookingApiClient"

    // Get bookings by customer ID
    fun getBookingsByCustomerId(customerId: Long, token: String, callback: (List<Booking>?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Getting bookings for customer: $customerId with token: $token")
        
        val request = Request.Builder()
            .url("$BASE_URL/api/bookings/getAll")
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get bookings", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Bookings response: $responseBody")
                    try {
                        val type = object : TypeToken<List<Booking>>() {}.type
                        val bookings = gson.fromJson<List<Booking>>(responseBody, type)
                        callback(bookings, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing bookings", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error getting bookings: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    
                    // Try to extract error message from response if available
                    val errorMessage = try {
                        val errorJson = JSONObject(responseBody ?: "{}")
                        errorJson.optString("message", "Failed to get bookings: ${response.code}")
                    } catch (e: Exception) {
                        "Failed to get bookings: ${response.code}"
                    }
                    
                    callback(null, Exception(errorMessage))
                }
            }
        })
    }
    
    // Create a new booking
    fun createBooking(serviceId: Long, customerId: Long, bookingDate: String, token: String, callback: (Booking?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Creating booking for service: $serviceId, customer: $customerId, date: $bookingDate")
        
        val jsonObject = JSONObject().apply {
            // Match the expected format from BookingController in the backend
            put("service", JSONObject().apply {
                put("serviceId", serviceId)
            })
            put("customer", JSONObject().apply {
                put("customerId", customerId)
            })
            put("bookingDate", bookingDate) // Already date part only
            put("status", "Pending")
            put("totalCost", 0) // This will be calculated by the backend based on service price
        }
        
        Log.d(TAG, "Booking JSON: ${jsonObject.toString()}")
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        val request = Request.Builder()
            .url("$BASE_URL/api/bookings/postBooking")
            .post(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to create booking", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Create booking response: $responseBody")
                    try {
                        val booking = gson.fromJson(responseBody, Booking::class.java)
                        callback(booking, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing created booking", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error creating booking: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    
                    // Try to extract error message from response if available
                    val errorMessage = try {
                        val errorJson = JSONObject(responseBody ?: "{}")
                        errorJson.optString("message", "Failed to create booking: ${response.code}")
                    } catch (e: Exception) {
                        "Failed to create booking: ${response.code}"
                    }
                    
                    callback(null, Exception(errorMessage))
                }
            }
        })
    }
    
    // Cancel a booking
    fun cancelBooking(bookingId: Long, token: String, callback: (Boolean, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(false, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Cancelling booking: $bookingId")
        
        val request = Request.Builder()
            .url("$BASE_URL/api/bookings/cancel/$bookingId")
            .put("".toRequestBody(null))
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to cancel booking", e)
                callback(false, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Cancel booking response: $responseBody")
                    callback(true, null)
                } else {
                    Log.e(TAG, "Error cancelling booking: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    
                    // Try to extract error message from response if available
                    val errorMessage = try {
                        val errorJson = JSONObject(responseBody ?: "{}")
                        errorJson.optString("message", "Failed to cancel booking: ${response.code}")
                    } catch (e: Exception) {
                        "Failed to cancel booking: ${response.code}"
                    }
                    
                    callback(false, Exception(errorMessage))
                }
            }
        })
    }
} 