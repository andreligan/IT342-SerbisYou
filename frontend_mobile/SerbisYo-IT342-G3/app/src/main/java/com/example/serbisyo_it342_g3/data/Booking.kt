package com.example.serbisyo_it342_g3.data

import com.google.gson.*
import java.lang.reflect.Type

data class Booking(
    val bookingId: Long = 0,
    val bookingDate: String? = null,
    val bookingTime: String? = null,
    val status: String? = null,
    val totalCost: Double = 0.0,
    val note: String? = null,
    val paymentMethod: String? = "Cash",
    val customer: Customer? = null,
    val service: Service? = null
)

/**
 * Custom deserializer for the Booking class to handle array format for dates and times
 */
class BookingDeserializer : JsonDeserializer<Booking> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Booking {
        val jsonObject = json.asJsonObject
        
        // Get bookingDate (could be array or string)
        val bookingDate = when {
            jsonObject.has("bookingDate") && jsonObject.get("bookingDate").isJsonArray -> {
                val dateArray = jsonObject.getAsJsonArray("bookingDate")
                if (dateArray.size() >= 3) {
                    val year = dateArray.get(0).asInt
                    val month = dateArray.get(1).asInt
                    val day = dateArray.get(2).asInt
                    String.format("%04d-%02d-%02d", year, month, day)
                } else null
            }
            jsonObject.has("bookingDate") && !jsonObject.get("bookingDate").isJsonNull -> {
                try {
                    jsonObject.get("bookingDate").asString
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
        
        // Get bookingTime (could be array or string)
        val bookingTime = when {
            jsonObject.has("bookingTime") && jsonObject.get("bookingTime").isJsonArray -> {
                val timeArray = jsonObject.getAsJsonArray("bookingTime")
                if (timeArray.size() >= 2) {
                    val hours = timeArray.get(0).asInt
                    val minutes = timeArray.get(1).asInt
                    String.format("%02d:%02d:00", hours, minutes)
                } else null
            }
            jsonObject.has("bookingTime") && !jsonObject.get("bookingTime").isJsonNull -> {
                try {
                    jsonObject.get("bookingTime").asString
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
        
        // Get other fields normally
        val bookingId = if (jsonObject.has("bookingId")) jsonObject.get("bookingId").asLong else 0L
        val status = if (jsonObject.has("status") && !jsonObject.get("status").isJsonNull) 
            jsonObject.get("status").asString else null
        val totalCost = if (jsonObject.has("totalCost")) jsonObject.get("totalCost").asDouble else 0.0
        val note = if (jsonObject.has("note") && !jsonObject.get("note").isJsonNull) 
            jsonObject.get("note").asString else null
        val paymentMethod = if (jsonObject.has("paymentMethod") && !jsonObject.get("paymentMethod").isJsonNull) 
            jsonObject.get("paymentMethod").asString else "Cash"
        
        // Deserialize complex objects with default deserializers
        var customer: Customer? = null
        if (jsonObject.has("customer") && !jsonObject.get("customer").isJsonNull) {
            try {
                customer = context.deserialize(jsonObject.get("customer"), Customer::class.java)
            } catch (e: Exception) {
                // Log error but continue
                e.printStackTrace()
            }
        }
        
        var service: Service? = null
        if (jsonObject.has("service") && !jsonObject.get("service").isJsonNull) {
            try {
                service = context.deserialize(jsonObject.get("service"), Service::class.java)
            } catch (e: Exception) {
                // Log error but continue
                e.printStackTrace()
            }
        }
        
        return Booking(
            bookingId = bookingId,
            bookingDate = bookingDate,
            bookingTime = bookingTime,
            status = status,
            totalCost = totalCost,
            note = note,
            paymentMethod = paymentMethod,
            customer = customer,
            service = service
        )
    }
} 