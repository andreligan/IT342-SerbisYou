package com.example.serbisyo_it342_g3.data

import android.util.Log
import com.example.serbisyo_it342_g3.data.Booking
import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class TransactionDeserializer : JsonDeserializer<Transaction> {
    private val TAG = "TransactionDeserializer"

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Transaction {
        try {
            val jsonObject = json.asJsonObject
            
            // Parse transaction basics
            val transactionId = jsonObject.get("transactionId")?.asLong ?: 0L
            val amount = jsonObject.get("amount")?.asDouble ?: 0.0
            val paymentMethod = jsonObject.get("paymentMethod")?.asString ?: ""
            val status = jsonObject.get("status")?.asString ?: ""
            
            // Parse booking
            var booking: Booking? = null
            if (jsonObject.has("booking") && !jsonObject.get("booking").isJsonNull) {
                val bookingElement = jsonObject.get("booking")
                try {
                    booking = context.deserialize(bookingElement, Booking::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing booking in transaction", e)
                }
            }
            
            // Parse transaction date (handle both string and array formats)
            var transactionDate = ""
            if (jsonObject.has("transactionDate")) {
                val dateElement = jsonObject.get("transactionDate")
                
                if (dateElement.isJsonArray) {
                    // Handle array format like [2025,5,1,20,35,6,100087000]
                    try {
                        val dateArray = dateElement.asJsonArray
                        if (dateArray.size() >= 3) {
                            val year = dateArray.get(0).asInt
                            val month = dateArray.get(1).asInt
                            val day = dateArray.get(2).asInt
                            
                            // Include time if available in the array
                            if (dateArray.size() >= 6) {
                                val hour = dateArray.get(3).asInt
                                val minute = dateArray.get(4).asInt
                                val second = dateArray.get(5).asInt
                                transactionDate = String.format("%04d-%02d-%02d %02d:%02d:%02d", 
                                    year, month, day, hour, minute, second)
                            } else {
                                transactionDate = String.format("%04d-%02d-%02d", year, month, day)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing transaction date array", e)
                    }
                } else if (dateElement.isJsonPrimitive) {
                    // It's already a string
                    transactionDate = dateElement.asString
                }
            }
            
            return Transaction(
                transactionId = transactionId,
                booking = booking,
                amount = amount,
                paymentMethod = paymentMethod,
                status = status,
                transactionDate = transactionDate
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in TransactionDeserializer", e)
            // Return a minimal transaction to avoid null
            return Transaction()
        }
    }
} 