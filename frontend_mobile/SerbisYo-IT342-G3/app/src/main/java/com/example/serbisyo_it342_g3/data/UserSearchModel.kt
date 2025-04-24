package com.example.serbisyo_it342_g3.data

data class UserSearchModel(
    val userId: String,
    val userName: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val businessName: String? = null,
    val email: String? = null,
    val role: String,
    val profileImage: String? = null
)
