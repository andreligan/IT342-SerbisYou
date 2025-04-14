package com.example.serbisyo_it342_g3.data

data class LoginResponse(
    val token: String,
    val userId: Long,
    val userName: String,
    val role: String
)