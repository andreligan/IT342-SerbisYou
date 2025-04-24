package com.example.serbisyo_it342_g3.model

data class Address(
    val id: Int = 0,
    val street: String = "",
    val city: String = "",
    val province: String = "",
    val barangay: String = "",
    val zipCode: String = ""
)