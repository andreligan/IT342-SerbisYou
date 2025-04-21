package com.example.serbisyo_it342_g3.data

data class Booking(
    val bookingId: Long = 0,
    val bookingDate: String? = null,
    val status: String? = null,
    val totalCost: Double = 0.0,
    val customer: Customer? = null,
    val service: Service? = null
) 