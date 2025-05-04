package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import com.example.serbisyo_it342_g3.data.Transaction
import com.example.serbisyo_it342_g3.data.TransactionDeserializer
import com.google.gson.GsonBuilder
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class TransactionApiClient(private val context: Context) {
    private val baseApiClient = BaseApiClient(context)
    private val client = baseApiClient.client
    // Create a custom Gson instance with our TransactionDeserializer
    private val gson = GsonBuilder()
        .registerTypeAdapter(Transaction::class.java, TransactionDeserializer())
        .create()

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

        // Create the current date time in ISO format (without requiring API 26)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val currentDateTime = dateFormat.format(Calendar.getInstance().time)

        // Create request JSON
        val jsonObject = JSONObject()
        val bookingObject = JSONObject()
        bookingObject.put("bookingId", bookingId)
        jsonObject.put("booking", bookingObject)
        jsonObject.put("amount", amount)
        jsonObject.put("paymentMethod", paymentMethod)
        jsonObject.put("status", "Completed")
        jsonObject.put("transactionDate", currentDateTime)

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
                        // Use our custom Gson with deserializer
                        val transaction = gson.fromJson(responseBody, Transaction::class.java)
                        Log.d(TAG, "Transaction created successfully with ID: ${transaction.transactionId}")
                        callback(transaction, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing transaction response", e)
                        // Manual parsing fallback
                        try {
                            val jsonObj = JSONObject(responseBody)
                            val transactionId = jsonObj.optLong("transactionId", 0)
                            // Create a minimal transaction object with just the essential info
                            val simpleTransaction = Transaction(
                                transactionId = transactionId,
                                amount = amount,
                                paymentMethod = paymentMethod,
                                status = "Completed"
                            )
                            Log.d(TAG, "Transaction created (manual parsing) with ID: $transactionId")
                            callback(simpleTransaction, null)
                        } catch (jsonException: Exception) {
                            Log.e(TAG, "Error with manual JSON parsing", jsonException)
                            callback(null, e)
                        }
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