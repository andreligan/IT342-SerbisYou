package com.example.serbisyo_it342_g3.data

data class Address(
    val addressId: Long? = null,
    val street: String,
    val city: String,
    val province: String,
    val postalCode: String,
    val barangay: String? = null
)