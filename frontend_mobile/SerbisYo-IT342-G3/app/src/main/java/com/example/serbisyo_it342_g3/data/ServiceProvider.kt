package com.example.serbisyo_it342_g3.data

data class ServiceProvider(
    val providerId: Long? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val businessName: String? = null,
    val yearsOfExperience: Int? = 0,
    val availabilitySchedule: String? = null,
    val paymentMethod: String? = null,
    val status: String? = null,
    val preferredWorkingHours: String? = null,
    val profileImage: String? = null,
    val address: Address? = null,
    val userAuth: User? = null
)