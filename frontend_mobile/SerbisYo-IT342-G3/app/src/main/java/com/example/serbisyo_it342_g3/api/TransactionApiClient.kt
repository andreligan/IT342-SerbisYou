package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import com.example.serbisyo_it342_g3.data.Transaction
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TransactionApiClient(private val context: Context) {
    private val baseApiClient = BaseApiClient(context)
    private val client = baseApiClient.client
    private val gson = baseApiClient.gson

    private val TAG = "TransactionApiClient"

    /**
     * Creates a transaction record for a successful booking
     */
    fun createTransaction(
        bookingId: Long,
        amount: Double,
        paymentMethod: String,
        token: String,
        callback: (Transaction?, Exception?) -> Unit
    ) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Creating transaction for booking: $bookingId, amount: $amount, payment method: $paymentMethod")

        // Create the current date time in ISO format
        val currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        // Create request JSON
        val jsonObject = JSONObject().apply {
            put("booking", JSONObject().apply {
                put("bookingId", bookingId)
            })
            put("amount", amount)
            put("paymentMethod", paymentMethod)
            put("status", "Completed")
            put("transactionDate", currentDateTime)
        }

        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/transactions/postTransaction")
            .post(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to create transaction", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")

                if (response.isSuccessful && responseBody != null) {
                    try {
                        val transaction = gson.fromJson(responseBody, Transaction::class.java)
                        Log.d(TAG, "Transaction created successfully with ID: ${transaction.transactionId}")
                        callback(transaction, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing transaction response", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error creating transaction: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")

                    val errorMessage = try {
                        val errorJson = JSONObject(responseBody ?: "{}")
                        errorJson.optString("message", "Failed to create transaction: ${response.code}")
                    } catch (e: Exception) {
                        "Failed to create transaction: ${response.code}"
                    }

                    callback(null, Exception(errorMessage))
                }
            }
        })
    }
} 