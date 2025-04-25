package com.example.serbisyo_it342_g3.api

import com.example.serbisyo_it342_g3.api.interceptors.AuthInterceptor
import com.example.serbisyo_it342_g3.utils.Constants
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(AuthInterceptor())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
        
    // Create a lenient Gson instance to handle malformed JSON
    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(List::class.java, SafeListDeserializer<Any>())
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
    
    // Helper class to handle errors when JSON isn't a list but needs to be
    class SafeListDeserializer<T> : com.google.gson.JsonDeserializer<List<T>> {
        override fun deserialize(
            json: com.google.gson.JsonElement,
            typeOfT: java.lang.reflect.Type,
            context: com.google.gson.JsonDeserializationContext
        ): List<T> {
            try {
                if (json.isJsonArray) {
                    return context.deserialize(json, typeOfT)
                }
            } catch (e: JsonSyntaxException) {
                // Return empty list on error
            }
            return emptyList()
        }
    }
}