package com.example.serbisyo_it342_g3.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data class representing a review submitted by a customer for a service booking
 */
data class Review(
    @SerializedName("reviewId")
    val reviewId: Long? = null,
    
    @SerializedName("rating")
    val rating: Int = 0,
    
    @SerializedName("comment")
    val comment: String? = null,
    
    @SerializedName("reviewDate")
    val reviewDate: String? = null,
    
    @SerializedName("customer")
    val customer: Customer? = null,
    
    @SerializedName("provider")
    val provider: ServiceProvider? = null,
    
    @SerializedName("booking")
    val booking: Booking? = null
) : Serializable