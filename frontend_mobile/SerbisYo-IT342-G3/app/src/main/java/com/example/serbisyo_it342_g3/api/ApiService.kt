package com.example.serbisyo_it342_g3.api

import com.example.serbisyo_it342_g3.data.Customer
import com.example.serbisyo_it342_g3.data.LoginResponse
import com.example.serbisyo_it342_g3.data.ServiceProvider
import com.example.serbisyo_it342_g3.data.User
import com.example.serbisyo_it342_g3.data.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Authentication
    @POST("user-auth/login")
    suspend fun login(@Body user: User): Response<LoginResponse>

    @POST("user-auth/register")
    suspend fun register(@Body registrationData: Map<String, Any>): Response<String>

    // Customer
    @GET("customer/{id}")
    suspend fun getCustomerProfile(@Path("id") id: Long): Response<Customer>

    // Service Provider
    @GET("service-provider/{id}")
    suspend fun getServiceProviderProfile(@Path("id") id: Long): Response<ServiceProvider>
}