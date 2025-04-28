package com.example.serbisyo_it342_g3.api

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.concurrent.TimeUnit

object ApiClientFactory {
    
    fun createHttpClient(token: String): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .method(original.method, original.body)
                chain.proceed(requestBuilder.build())
            }
            .build()
    }
    
    fun createPutRequest(url: String, body: RequestBody): Request {
        return Request.Builder()
            .url(url)
            .put(body)
            .build()
    }
} 