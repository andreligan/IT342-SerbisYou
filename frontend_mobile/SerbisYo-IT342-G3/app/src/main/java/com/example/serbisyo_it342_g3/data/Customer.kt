package com.example.serbisyo_it342_g3.data

data class Customer(
    val customerId: Long = 0,
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val address: Address? = null,
    val userAuth: User? = null
)