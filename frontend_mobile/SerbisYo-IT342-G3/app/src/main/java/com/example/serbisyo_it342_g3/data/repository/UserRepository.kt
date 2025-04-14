package com.example.serbisyo_it342_g3.data.repository

import com.example.serbisyo_it342_g3.api.ApiClient
import com.example.serbisyo_it342_g3.data.LoginResponse
import com.example.serbisyo_it342_g3.data.User
import com.example.serbisyo_it342_g3.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {
    private val apiService = ApiClient.apiService
    private val userPreferences = UserPreferences.getInstance()

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(User(userName = username, password = password))
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    userPreferences.saveUserSession(
                        loginResponse.token,
                        loginResponse.userId,
                        loginResponse.userName,
                        loginResponse.role
                    )
                    Result.success(loginResponse)
                } else {
                    Result.failure(Exception("Login failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun register(
        username: String,
        password: String,
        email: String,
        role: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        street: String,
        city: String,
        province: String,
        postalCode: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val userAuth = mapOf(
                    "userName" to username,
                    "password" to password,
                    "email" to email,
                    "role" to role
                )

                val address = mapOf(
                    "street" to street,
                    "city" to city,
                    "province" to province,
                    "postalCode" to postalCode
                )

                val profileData = mapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "phoneNumber" to phoneNumber
                )

                val requestBody = if (role == "CUSTOMER") {
                    mapOf(
                        "userAuth" to userAuth,
                        "customer" to profileData + mapOf("address" to address)
                    )
                } else {
                    mapOf(
                        "userAuth" to userAuth,
                        "serviceProvider" to profileData + mapOf(
                            "address" to address,
                            "businessName" to "$firstName's Services",
                            "yearsOfExperience" to 0,
                            "availabilitySchedule" to "Monday-Friday, 9AM-5PM"
                        )
                    )
                }

                val response = apiService.register(requestBody)
                if (response.isSuccessful) {
                    Result.success(response.body() ?: "Registration successful")
                } else {
                    Result.failure(Exception("Registration failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun logout() {
        userPreferences.clearUserSession()
    }
}