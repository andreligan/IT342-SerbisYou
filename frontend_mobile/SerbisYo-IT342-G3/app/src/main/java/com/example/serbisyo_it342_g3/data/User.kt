package com.example.serbisyo_it342_g3.data

data class User(
    val userId: Long? = null,
    val userName: String,
    val password: String? = null,
    val email: String? = null,
    val role: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val profileImage: String? = null
)