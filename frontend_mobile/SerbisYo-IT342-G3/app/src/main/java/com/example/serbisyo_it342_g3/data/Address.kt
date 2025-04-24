package com.example.serbisyo_it342_g3.data

import com.google.gson.annotations.SerializedName

data class Address(
    val addressId: Long? = null,
    
    @SerializedName("street")
    val street: String = "",
    
    @SerializedName("streetName")
    val streetName: String? = null,
    
    val city: String = "",
    val province: String = "",
    
    @SerializedName("postalCode")
    val postalCode: String = "",
    
    @SerializedName("zip_code")
    val zipCode: String? = null,
    
    val barangay: String? = null,
    val customer: Customer? = null,
    val main: Boolean = false
)