package com.example.serbisyo_it342_g3.data

data class ServiceProvider(
    val providerId: Long? = null,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val businessName: String,
    val yearsOfExperience: Int,
    val availabilitySchedule: String,
    val address: Address,
    val userAuth: User? = null
)