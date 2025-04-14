package com.example.serbisyo_it342_g3.data

data class Service(
    val serviceId: Long = 0,
    val serviceName: String = "",
    val serviceDescription: String = "",
    val priceRange: String = "",
    val durationEstimate: String = "",
    val provider: ServiceProvider? = null,
    val category: ServiceCategory? = null
)