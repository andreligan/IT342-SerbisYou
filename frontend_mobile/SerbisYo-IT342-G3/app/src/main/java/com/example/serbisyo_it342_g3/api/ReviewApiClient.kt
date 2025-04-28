package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import com.example.serbisyo_it342_g3.data.Review
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URLEncoder

/**
 * API Client for handling review-related operations
 */
class ReviewApiClient(context: Context) : BaseApiClient(context) {
    private val TAG = "ReviewApiClient"
    
    /**
     * Check if a customer can review a specific booking
     * Returns true if the customer hasn't reviewed the booking yet
     */
    fun canCustomerReviewBooking(token: String, customerId: Long, bookingId: Long, callback: (Boolean) -> Unit) {
        if (!isNetworkAvailable()) {
            Log.e(TAG, "Network not available")
            callback(false)
            return
        }
        
        val url = "${getBaseUrl()}/api/reviews/can-review?customerId=$customerId&bookingId=$bookingId"
        val request = Request.Builder()
            .url(url)
            .get()
            .header("Authorization", "Bearer $token")
            .build()
        
        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    
                    if (response.isSuccessful && responseBody != null) {
                        val canReview = responseBody.toBoolean()
                        callback(canReview)
                    } else {
                        Log.e(TAG, "Error checking review status: ${response.code}")
                        callback(true) // Default to true if error occurs to allow review attempt
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error checking review status", e)
                callback(true) // Default to true if error occurs to allow review attempt
            }
        }.start()
    }
    
    /**
     * Submit a review for a booking
     */
    fun submitReview(
        token: String, 
        customerId: Long,
        providerId: Long,
        bookingId: Long,
        rating: Int,
        comment: String,
        callback: (Boolean, String?) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            Log.e(TAG, "Network not available")
            callback(false, "No internet connection")
            return
        }
        
        val reviewDate = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            .format(java.util.Date())
        
        // URL with query parameters for the review submission
        val url = "${getBaseUrl()}/api/reviews/createWithIDs" +
                "?customerId=$customerId" +
                "&providerId=$providerId" +
                "&bookingId=$bookingId" +
                "&rating=$rating" +
                "&comment=${URLEncoder.encode(comment, "UTF-8")}" +
                "&reviewDate=$reviewDate"
        
        val emptyBody = "".toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .post(emptyBody)
            .header("Authorization", "Bearer $token")
            .build()
        
        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    
                    if (response.isSuccessful) {
                        Log.d(TAG, "Review submitted successfully: $responseBody")
                        callback(true, null)
                    } else {
                        Log.e(TAG, "Error submitting review: ${response.code} - $responseBody")
                        callback(false, "Error submitting review: ${response.message}")
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error submitting review", e)
                callback(false, "Network error: ${e.message}")
            }
        }.start()
    }
    
    /**
     * Get all reviews for a specific provider
     */
    fun getProviderReviews(token: String, providerId: Long, callback: (List<Review>?, String?) -> Unit) {
        if (!isNetworkAvailable()) {
            Log.e(TAG, "Network not available")
            callback(null, "No internet connection")
            return
        }
        
        val request = Request.Builder()
            .url("${getBaseUrl()}/api/reviews/provider/$providerId")
            .get()
            .header("Authorization", "Bearer $token")
            .build()
        
        Thread {
            try {
                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string()
                    
                    if (response.isSuccessful && responseBody != null) {
                        val type = TypeToken.getParameterized(List::class.java, Review::class.java).type
                        val reviews = gson.fromJson<List<Review>>(responseBody, type)
                        callback(reviews, null)
                    } else {
                        Log.e(TAG, "Error getting provider reviews: ${response.code}")
                        callback(null, "Error fetching reviews: ${response.message}")
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error getting provider reviews", e)
                callback(null, "Network error: ${e.message}")
            }
        }.start()
    }
}