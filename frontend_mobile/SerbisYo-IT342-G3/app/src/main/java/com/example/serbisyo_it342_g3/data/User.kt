package com.example.serbisyo_it342_g3.data

data class User(
    val userId: Long? = null,
    val userName: String,
    val password: String,
    val email: String? = null,
    val role: String? = null
)