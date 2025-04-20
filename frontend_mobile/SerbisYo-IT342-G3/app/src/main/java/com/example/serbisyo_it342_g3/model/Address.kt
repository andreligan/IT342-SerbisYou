package com.example.serbisyo_it342_g3.model

data class Address(
    val id: Int = 0,
    val province: String = "",
    val city: String = "",
    val barangay: String = "",
    val street: String = "",
    val zipCode: String = ""
) 