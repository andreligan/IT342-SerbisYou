package com.example.serbisyo_it342_g3.data

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