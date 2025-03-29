/*package com.example.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// Simple data class matching your database
data class CustomerRegistration(
    val name: String,
    val email: String,
    val password: String,
    val phone_number: String,
    val address: String
)

interface ApiService {
    @POST("api/customers/register")
    suspend fun registerCustomer(@Body customer: CustomerRegistration): Response<CustomerRegistration>
}*/