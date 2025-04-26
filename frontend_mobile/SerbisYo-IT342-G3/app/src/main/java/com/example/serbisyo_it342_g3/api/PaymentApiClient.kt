package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException

class PaymentApiClient(private val context: Context) {
    private val baseApiClient = BaseApiClient(context)
    private val client = baseApiClient.client
    private val gson = baseApiClient.gson

    private val TAG = "PaymentApiClient"

    /**
     * Creates a GCash checkout session with PayMongo
     */
    fun createGCashCheckout(
        amount: Double,
        description: String,
        token: String,
        callback: (String?, Exception?) -> Unit
    ) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        // Check for network availability
        if (!baseApiClient.isNetworkAvailable()) {
            Log.e(TAG, "No network connection available")
            callback(null, Exception("No internet connection. Please check your network settings."))
            return
        }

        Log.d(TAG, "Creating GCash checkout for amount: $amount, description: $description")

        // Create request JSON with URLs that match the backend's expectations
        val jsonObject = JSONObject().apply {
            put("amount", amount)
            put("description", description)
            
            // Use the URLs that are configured in the backend
            put("successUrl", "http://localhost:5173/payment-success")
            put("cancelUrl", "http://localhost:5173/payment-cancel")
            
            // Log the URLs being used
            Log.d(TAG, "Success URL: http://localhost:5173/payment-success")
            Log.d(TAG, "Cancel URL: http://localhost:5173/payment-cancel")
        }

        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/create-gcash-checkout")
            .post(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        // Log the request URL for debugging
        Log.d(TAG, "Making request to: ${request.url}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to create GCash checkout", e)
                
                // Provide a more specific error message based on the exception type
                val errorMessage = when (e) {
                    is SocketTimeoutException -> "Connection timed out. The server might be slow or unavailable. Please try again later."
                    else -> "Failed to connect to payment server: ${e.message}"
                }
                
                callback(null, Exception(errorMessage))
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")

                if (response.isSuccessful) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val checkoutUrl = jsonResponse.getString("checkout_url")
                        Log.d(TAG, "Successfully obtained checkout URL: $checkoutUrl")
                        callback(checkoutUrl, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing checkout URL", e)
                        callback(null, Exception("Error parsing payment response: ${e.message}"))
                    }
                } else {
                    Log.e(TAG, "Error creating GCash checkout: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")

                    val errorMessage = try {
                        val errorJson = JSONObject(responseBody ?: "{}")
                        errorJson.optString("error", "Failed to create GCash checkout: ${response.code}")
                    } catch (e: Exception) {
                        "Failed to create GCash checkout: ${response.code}"
                    }

                    callback(null, Exception(errorMessage))
                }
            }
        })
    }
} 