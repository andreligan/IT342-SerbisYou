package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import com.example.serbisyo_it342_g3.data.Booking
import com.example.serbisyo_it342_g3.data.BookingDeserializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray
import java.io.IOException

class BookingApiClient(private val context: Context) {
    private val baseApiClient = BaseApiClient(context)
    private val client = baseApiClient.client
    private val gson = GsonBuilder()
        .registerTypeAdapter(Booking::class.java, BookingDeserializer())
        .create()
    private val serviceApiClient = ServiceApiClient(context)

    private val TAG = "BookingApiClient"

    // Get bookings by customer ID
    fun getBookingsByCustomerId(customerId: Long, token: String, callback: (List<Booking>?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Getting bookings for customer: $customerId with token: ${token.take(20)}...")

        // Try to fetch bookings directly from the customer endpoint first
        val directUrl = "${baseApiClient.getBaseUrl()}/api/bookings/customer/$customerId"
        
        val request = Request.Builder()
            .url(directUrl)
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get bookings from direct endpoint, falling back to filtering", e)
                // On failure, fallback to the old method of getting all bookings and filtering
                getAllBookingsAndFilter(customerId, token, callback)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code from direct endpoint: ${response.code}")
                
                if (response.isSuccessful) {
                    try {
                        val bookingListType = object : TypeToken<List<Booking>>() {}.type
                        val customerBookings = gson.fromJson<List<Booking>>(responseBody, bookingListType) ?: emptyList()
                        
                        Log.d(TAG, "Directly received ${customerBookings.size} bookings for customer $customerId")
                        callback(customerBookings, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing bookings from direct endpoint, falling back to filtering", e)
                        // On parsing error, fallback to the old method
                        getAllBookingsAndFilter(customerId, token, callback)
                    }
                } else {
                    // If the endpoint doesn't exist (404) or returns an error, fall back to old method
                    Log.d(TAG, "Direct endpoint returned ${response.code}, falling back to filtering")
                    getAllBookingsAndFilter(customerId, token, callback)
                }
            }
        })
    }

    // Fallback method to get all bookings and filter them client-side
    private fun getAllBookingsAndFilter(customerId: Long, token: String, callback: (List<Booking>?, Exception?) -> Unit) {
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/bookings/getAll")
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
                Log.d(TAG, "Response code from getAll: ${response.code}")
                Log.d(TAG, "Raw response body: ${responseBody?.take(200)}...")
                
                if (response.isSuccessful) {
                    try {
                        // Try with automatic deserialization first
                        try {
                            val bookingListType = object : TypeToken<List<Booking>>() {}.type
                            val allBookings = gson.fromJson<List<Booking>>(responseBody, bookingListType) ?: emptyList()
                        
                        Log.d(TAG, "Received ${allBookings.size} total bookings")
                        
                        // Filter bookings by customer ID with improved handling
                        val customerBookings = allBookings.filter { booking ->
                            val bookingCustomer = booking.customer
                            if (bookingCustomer == null) {
                                Log.d(TAG, "Booking ${booking.bookingId} has null customer")
                                false
                            } else {
                                val bookingCustomerId = bookingCustomer.customerId
                                val customerIdMatches = bookingCustomerId == customerId
                                
                                // Additional debugging to diagnose the issue
                                Log.d(TAG, "Booking ${booking.bookingId}: customerID=${bookingCustomerId} (${bookingCustomerId.javaClass.simpleName}), " +
                                        "looking for customerId=$customerId (${customerId.javaClass.simpleName}), match=$customerIdMatches")
                                
                                // Try string comparison as fallback if types don't match
                                customerIdMatches || bookingCustomerId.toString() == customerId.toString()
                            }
                        }
                        
                        Log.d(TAG, "Found ${customerBookings.size} bookings for customer $customerId after filtering")
                        
                        if (customerBookings.isEmpty()) {
                            // Try looking for customerId in other places in case the backend structure is inconsistent
                            val potentialBookings = allBookings.filter { booking ->
                                val jsonString = gson.toJson(booking)
                                jsonString.contains("\"customerId\":$customerId") || 
                                jsonString.contains("\"customerId\":\"$customerId\"") ||
                                jsonString.contains("\"customer_id\":$customerId") ||
                                jsonString.contains("\"customer_id\":\"$customerId\"")
                            }
                            
                            if (potentialBookings.isNotEmpty()) {
                                Log.d(TAG, "Found ${potentialBookings.size} potential bookings by searching JSON")
                                callback(potentialBookings, null)
                                return
                            }
                            
                            Log.d(TAG, "No bookings found for customer $customerId")
                        }
                        
                        callback(customerBookings, null)
                        } catch (e: Exception) {
                            // If automatic deserialization fails, try manual parsing
                            Log.e(TAG, "Error with automatic deserialization, trying manual approach", e)
                            
                            try {
                                // Parse the JSON array manually
                                val jsonArray = JSONArray(responseBody)
                                val manualBookings = ArrayList<Booking>()
                                
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject = jsonArray.getJSONObject(i)
                                    val bookingId = jsonObject.optLong("bookingId", 0)
                                    
                                    // Try to extract customer ID to filter
                                    var bookingCustomerId: Long = -1L
                                    if (jsonObject.has("customer") && !jsonObject.isNull("customer")) {
                                        val customerObj = jsonObject.getJSONObject("customer")
                                        bookingCustomerId = customerObj.optLong("customerId", -1L)
                                    }
                                    
                                    // Only include if it matches our customer ID
                                    if (bookingCustomerId == customerId || bookingCustomerId == -1L) {
                                        val booking = Booking(
                                            bookingId = bookingId,
                                            status = jsonObject.optString("status", ""),
                                            totalCost = jsonObject.optDouble("totalCost", 0.0),
                                            bookingDate = extractSimpleDateString(jsonObject, "bookingDate"),
                                            bookingTime = extractSimpleTimeString(jsonObject, "bookingTime"),
                                            paymentMethod = jsonObject.optString("paymentMethod", "Cash"),
                                            note = jsonObject.optString("note", "")
                                        )
                                        manualBookings.add(booking)
                                    }
                                }
                                
                                Log.d(TAG, "Manually parsed ${manualBookings.size} bookings for customer $customerId")
                                callback(manualBookings, null)
                            } catch (jsonException: Exception) {
                                Log.e(TAG, "Error with manual JSON parsing", jsonException)
                                callback(null, jsonException)
                            }
                        }
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

    // Helper method to extract date string from a JSON object that might contain an array
    private fun extractSimpleDateString(jsonObject: JSONObject, fieldName: String): String? {
        return try {
            if (jsonObject.has(fieldName)) {
                if (jsonObject.get(fieldName) is JSONArray) {
                    val dateArray = jsonObject.getJSONArray(fieldName)
                    if (dateArray.length() >= 3) {
                        val year = dateArray.getInt(0)
                        val month = dateArray.getInt(1)
                        val day = dateArray.getInt(2)
                        String.format("%04d-%02d-%02d", year, month, day)
                    } else null
                } else {
                    jsonObject.optString(fieldName, null)
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    // Helper method to extract time string from a JSON object that might contain an array
    private fun extractSimpleTimeString(jsonObject: JSONObject, fieldName: String): String? {
        return try {
            if (jsonObject.has(fieldName)) {
                if (jsonObject.get(fieldName) is JSONArray) {
                    val timeArray = jsonObject.getJSONArray(fieldName)
                    if (timeArray.length() >= 2) {
                        val hours = timeArray.getInt(0)
                        val minutes = timeArray.getInt(1)
                        String.format("%02d:%02d:00", hours, minutes)
                    } else null
                } else {
                    jsonObject.optString(fieldName, null)
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Create a new booking
    fun createBooking(
        serviceId: Long, 
        customerId: Long, 
        bookingDate: String, 
        token: String, 
        note: String? = null,
        paymentMethod: String? = "Cash",
        bookingTime: String? = null,
        totalCost: Double? = null,
        callback: (Booking?, Exception?) -> Unit
    ) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Creating booking for service: $serviceId, customer: $customerId, date: $bookingDate, time: $bookingTime")

        // FIRST: Get complete service details to ensure we have provider information
        // This step fixes the issue where the backend expects the provider in the service
        serviceApiClient.getServiceById(serviceId, token) { service, serviceError ->
            if (serviceError != null) {
                Log.e(TAG, "Failed to get service details", serviceError)
                callback(null, Exception("Failed to fetch service details: ${serviceError.message}"))
                return@getServiceById
            }

            if (service == null) {
                Log.e(TAG, "Service not found")
                callback(null, Exception("Service not found"))
                return@getServiceById
            }

            // Now create booking with complete service details
        val jsonObject = JSONObject().apply {
                // Make sure we include the full service object with provider details
            put("service", JSONObject().apply {
                put("serviceId", serviceId)
                    // The backend will load the full service from this ID
            })
                
            put("customer", JSONObject().apply {
                put("customerId", customerId)
            })
                
                // Format the date properly
                put("bookingDate", bookingDate)
            put("status", "Pending")
            
                // Add time in the proper format
            if (!bookingTime.isNullOrBlank()) {
                    // Check if it's an array format like [6,0]
                    if (bookingTime.contains("[") && bookingTime.contains("]")) {
                        try {
                            // Parse the array format
                            val timeStr = bookingTime.replace("[", "").replace("]", "")
                            val timeParts = timeStr.split(",")
                            if (timeParts.size >= 2) {
                                val hours = timeParts[0].trim().toInt()
                                val minutes = timeParts[1].trim().toInt()
                                val formattedTime = String.format("%02d:%02d:00", hours, minutes)
                                put("bookingTime", formattedTime)
                                Log.d(TAG, "Formatted array time [$bookingTime] to: $formattedTime")
            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing time array: $bookingTime", e)
                            // Fall back to a default time
                            put("bookingTime", "08:00:00")
                        }
                    } else if (bookingTime.contains(":")) {
                        // It's already a time string with colon
                        val formattedTime = if (bookingTime.split(":").size == 2) {
                            "$bookingTime:00"  // Add seconds if missing
                        } else {
                            bookingTime
                        }
                        put("bookingTime", formattedTime)
                    } else {
                        // Try to parse as a number of hours
                        try {
                            val hours = bookingTime.toInt()
                            put("bookingTime", String.format("%02d:00:00", hours))
                        } catch (e: Exception) {
                            // Default time if parsing fails
                            put("bookingTime", "08:00:00")
                        }
                    }
                }
                
                // Ensure totalCost is included and is a valid number
            put("totalCost", totalCost ?: 0.0)
            
            // Add note if not null or empty
            if (!note.isNullOrBlank()) {
                put("note", note)
            }
            
                // Add payment method
            put("paymentMethod", paymentMethod ?: "Cash")
                
                // Add fullPayment flag (defaults to true for cash payments)
                put("fullPayment", true)
        }

        Log.d(TAG, "Booking JSON: ${jsonObject.toString()}")

        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/bookings/postBooking")
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
                            // Manual parsing as a fallback when the main deserializer fails
                    try {
                        val booking = gson.fromJson(responseBody, Booking::class.java)
                        callback(booking, null)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error with deserializer, falling back to manual parsing", e)
                                // Parse manually to extract just the booking ID
                                val jsonObject = JSONObject(responseBody!!)
                                val bookingId = jsonObject.getLong("bookingId")
                                // Create a simple booking object with just the essentials
                                val simpleBooking = Booking(
                                    bookingId = bookingId,
                                    status = "Pending", // Assume status is Pending for a new booking
                                    totalCost = jsonObject.optDouble("totalCost", 0.0),
                                    bookingDate = jsonObject.optString("bookingDate", ""),
                                    paymentMethod = jsonObject.optString("paymentMethod", "Cash")
                                )
                                Log.d(TAG, "Successfully created booking with ID: $bookingId using manual parsing")
                                callback(simpleBooking, null)
                            }
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
            .url("${baseApiClient.getBaseUrl()}/api/bookings/cancel/$bookingId")
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

    // Get bookings by provider ID (for service providers)
    fun getBookingsByProviderId(providerId: Long, token: String, callback: (List<Booking>?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Getting bookings for provider: $providerId")
        
        // First try the direct endpoint for provider bookings
        val providerEndpoint = "${baseApiClient.getBaseUrl()}/api/bookings/getProviderBookings"
        
        val request = Request.Builder()
            .url(providerEndpoint)
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get provider bookings from direct endpoint", e)
                // Fallback to getting all bookings and filtering
                getAllBookingsAndFilterByProvider(providerId, token, callback)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code from provider endpoint: ${response.code}")
                
                if (response.isSuccessful) {
                    try {
                        // Try with automatic deserialization first
                        try {
                            val bookingListType = object : TypeToken<List<Booking>>() {}.type
                            val allBookings = gson.fromJson<List<Booking>>(responseBody, bookingListType) ?: emptyList()
                        
                            Log.d(TAG, "Received ${allBookings.size} total bookings")
                            
                            // Filter bookings by service provider ID
                            val providerBookings = allBookings.filter { booking ->
                                val service = booking.service ?: return@filter false
                                val provider = service.provider ?: return@filter false
                                provider.providerId == providerId
                            }
                            
                            Log.d(TAG, "Found ${providerBookings.size} bookings for provider $providerId after filtering")
                        callback(providerBookings, null)
                        } catch (e: Exception) {
                            // If automatic deserialization fails, try manual parsing
                            Log.e(TAG, "Error with automatic deserialization, trying manual approach", e)
                            
                            try {
                                // Parse the JSON array manually
                                val jsonArray = JSONArray(responseBody)
                                val manualBookings = ArrayList<Booking>()
                                
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject = jsonArray.getJSONObject(i)
                                    val bookingId = jsonObject.optLong("bookingId", 0)
                                    
                                    // Try to extract provider ID to filter
                                    var serviceProviderId: Long = -1L
                                    if (jsonObject.has("service") && !jsonObject.isNull("service")) {
                                        val serviceObj = jsonObject.getJSONObject("service")
                                        if (serviceObj.has("provider") && !serviceObj.isNull("provider")) {
                                            val providerObj = serviceObj.getJSONObject("provider")
                                            serviceProviderId = providerObj.optLong("providerId", -1L)
                                        }
                                    }
                                    
                                    // Only include if it matches our provider ID
                                    if (serviceProviderId == providerId) {
                                        val booking = Booking(
                                            bookingId = bookingId,
                                            status = jsonObject.optString("status", ""),
                                            totalCost = jsonObject.optDouble("totalCost", 0.0),
                                            bookingDate = extractSimpleDateString(jsonObject, "bookingDate"),
                                            bookingTime = extractSimpleTimeString(jsonObject, "bookingTime"),
                                            paymentMethod = jsonObject.optString("paymentMethod", "Cash"),
                                            note = jsonObject.optString("note", "")
                                        )
                                        manualBookings.add(booking)
                                    }
                                }
                                
                                Log.d(TAG, "Manually parsed ${manualBookings.size} bookings for provider $providerId")
                                callback(manualBookings, null)
                            } catch (jsonException: Exception) {
                                Log.e(TAG, "Error with manual JSON parsing", jsonException)
                                callback(null, jsonException)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing bookings", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error getting all bookings: ${response.code}")
                    callback(null, Exception("Failed to get bookings: ${response.code}"))
                }
            }
        })
    }

    // Fallback method to get all bookings and filter for a provider
    private fun getAllBookingsAndFilterByProvider(providerId: Long, token: String, callback: (List<Booking>?, Exception?) -> Unit) {
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/bookings/getAll")
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get all bookings", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code from getAll: ${response.code}")
                
                if (response.isSuccessful) {
                    try {
                        // Try with automatic deserialization first
                        try {
                            val bookingListType = object : TypeToken<List<Booking>>() {}.type
                            val allBookings = gson.fromJson<List<Booking>>(responseBody, bookingListType) ?: emptyList()
                        
                        Log.d(TAG, "Received ${allBookings.size} total bookings")
                        
                        // Filter bookings by service provider ID
                        val providerBookings = allBookings.filter { booking ->
                            val service = booking.service ?: return@filter false
                            val provider = service.provider ?: return@filter false
                            provider.providerId == providerId
                        }
                        
                        Log.d(TAG, "Found ${providerBookings.size} bookings for provider $providerId after filtering")
                        callback(providerBookings, null)
                        } catch (e: Exception) {
                            // If automatic deserialization fails, try manual parsing
                            Log.e(TAG, "Error with automatic deserialization, trying manual approach", e)
                            
                            try {
                                // Parse the JSON array manually
                                val jsonArray = JSONArray(responseBody)
                                val manualBookings = ArrayList<Booking>()
                                
                                for (i in 0 until jsonArray.length()) {
                                    val jsonObject = jsonArray.getJSONObject(i)
                                    val bookingId = jsonObject.optLong("bookingId", 0)
                                    
                                    // Try to extract provider ID to filter
                                    var serviceProviderId: Long = -1L
                                    if (jsonObject.has("service") && !jsonObject.isNull("service")) {
                                        val serviceObj = jsonObject.getJSONObject("service")
                                        if (serviceObj.has("provider") && !serviceObj.isNull("provider")) {
                                            val providerObj = serviceObj.getJSONObject("provider")
                                            serviceProviderId = providerObj.optLong("providerId", -1L)
                                        }
                                    }
                                    
                                    // Only include if it matches our provider ID
                                    if (serviceProviderId == providerId) {
                                        val booking = Booking(
                                            bookingId = bookingId,
                                            status = jsonObject.optString("status", ""),
                                            totalCost = jsonObject.optDouble("totalCost", 0.0),
                                            bookingDate = extractSimpleDateString(jsonObject, "bookingDate"),
                                            bookingTime = extractSimpleTimeString(jsonObject, "bookingTime"),
                                            paymentMethod = jsonObject.optString("paymentMethod", "Cash"),
                                            note = jsonObject.optString("note", "")
                                        )
                                        manualBookings.add(booking)
                                    }
                                }
                                
                                Log.d(TAG, "Manually parsed ${manualBookings.size} bookings for provider $providerId")
                                callback(manualBookings, null)
                            } catch (jsonException: Exception) {
                                Log.e(TAG, "Error with manual JSON parsing", jsonException)
                                callback(null, jsonException)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing bookings", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error getting all bookings: ${response.code}")
                    callback(null, Exception("Failed to get bookings: ${response.code}"))
                }
            }
        })
    }

    // Update booking status (for confirming, rejecting, completing bookings)
    fun updateBookingStatus(bookingId: Long, newStatus: String, token: String, callback: (Booking?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Updating booking $bookingId status to $newStatus")

        val jsonObject = JSONObject().apply {
            put("status", newStatus)
        }

        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/bookings/updateStatus/$bookingId")
            .put(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to update booking status", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")

                if (response.isSuccessful) {
                    try {
                        // Try with deserializer first
                        try {
                            // Use our custom deserializer which handles array format dates and times
                        val updatedBooking = gson.fromJson(responseBody, Booking::class.java)
                        callback(updatedBooking, null)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error with deserializer, falling back to manual parsing", e)
                            // Parse manually to extract just the booking ID
                            val jsonObject = JSONObject(responseBody!!)
                            val bookingId = jsonObject.getLong("bookingId")
                            // Create a simple booking object with just the essentials
                            val simpleBooking = Booking(
                                bookingId = bookingId,
                                status = newStatus,
                                totalCost = jsonObject.optDouble("totalCost", 0.0),
                                bookingDate = jsonObject.optString("bookingDate", ""),
                                paymentMethod = jsonObject.optString("paymentMethod", "Cash")
                            )
                            Log.d(TAG, "Successfully updated booking status to $newStatus using manual parsing")
                            callback(simpleBooking, null)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing updated booking", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error updating booking status: ${response.code}")
                    callback(null, Exception("Failed to update booking status: ${response.code}"))
                }
            }
        })
    }
    
    // Mark booking as completed (automatically releases schedule for provider)
    fun completeBooking(bookingId: Long, token: String, callback: (Booking?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Marking booking $bookingId as completed")

        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/bookings/complete/$bookingId")
            .put("".toRequestBody(null))
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to complete booking", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")

                if (response.isSuccessful) {
                    try {
                        // Try with deserializer first
                        try {
                            // Use our custom deserializer which handles array format dates and times
                        val completedBooking = gson.fromJson(responseBody, Booking::class.java)
                        callback(completedBooking, null)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error with deserializer, falling back to manual parsing", e)
                            // Parse manually to extract just the booking ID
                            val jsonObject = JSONObject(responseBody!!)
                            val bookingId = jsonObject.getLong("bookingId")
                            // Create a simple booking object with just the essentials
                            val simpleBooking = Booking(
                                bookingId = bookingId,
                                status = "Completed", // Completed status
                                totalCost = jsonObject.optDouble("totalCost", 0.0),
                                bookingDate = jsonObject.optString("bookingDate", ""),
                                paymentMethod = jsonObject.optString("paymentMethod", "Cash")
                            )
                            Log.d(TAG, "Successfully completed booking using manual parsing")
                            callback(simpleBooking, null)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing completed booking", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error completing booking: ${response.code}")
                    callback(null, Exception("Failed to complete booking: ${response.code}"))
                }
            }
        })
    }

    // Get bookings for a user (handles both customer and provider roles)
    fun getUserBookings(
        token: String,
        userId: Long,
        onSuccess: (List<Booking>?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            onError("Authentication token is required")
            return
        }

        Log.d(TAG, "Getting bookings for user: $userId")
        
        // Try to get bookings as a customer first
        getBookingsByCustomerId(userId, token) { customerBookings, customerError ->
            if (customerError == null && customerBookings != null) {
                Log.d(TAG, "Retrieved ${customerBookings.size} bookings for customer $userId")
                onSuccess(customerBookings)
            } else {
                // If that fails or returns no results, try as a provider
                getBookingsByProviderId(userId, token) { providerBookings, providerError ->
                    if (providerError == null && providerBookings != null) {
                        Log.d(TAG, "Retrieved ${providerBookings.size} bookings for provider $userId")
                        onSuccess(providerBookings)
                    } else {
                        // Both attempts failed
                        Log.e(TAG, "Failed to get bookings for user $userId")
                        onError(customerError?.message ?: providerError?.message ?: "Unknown error")
                    }
                }
            }
        }
    }
}