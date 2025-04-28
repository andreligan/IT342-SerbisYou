package com.example.serbisyo_it342_g3.data

data class Service(
    val serviceId: Long = 0,
    val serviceName: String = "",
    val serviceDescription: String = "",
    val priceRange: String = "",
    val price: String = "",
    val durationEstimate: String = "",
    val serviceImage: String? = null,
    val provider: ServiceProvider? = null,
    val category: ServiceCategory? = null
) {
    // Convenience property to check if image exists
    val imageUrl: String
        get() = serviceImage ?: ""
        
    // Convenience property to get price data regardless of which field is populated
    val effectivePrice: String
        get() = if (price.isNotEmpty()) price else priceRange
}