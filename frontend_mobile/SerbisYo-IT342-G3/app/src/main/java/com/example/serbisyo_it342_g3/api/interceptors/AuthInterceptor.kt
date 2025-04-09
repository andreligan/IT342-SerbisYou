package com.example.serbisyo_it342_g3.api.interceptors

import com.example.serbisyo_it342_g3.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth for login and register
        if (originalRequest.url.encodedPath.contains("login") ||
            originalRequest.url.encodedPath.contains("register")) {
            return chain.proceed(originalRequest)
        }

        val token = runBlocking {
            UserPreferences.getInstance().getToken().first()
        }

        val newRequest = if (token.isNotEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}