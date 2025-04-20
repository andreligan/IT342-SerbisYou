package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.Serializable
import okhttp3.*
import java.util.concurrent.TimeUnit

/**
 * API Client for the Philippine Standard Geographic Code (PSGC) API
 * Provides methods to fetch provinces, cities/municipalities, and barangays
 */
class PSGCApiClient(private val context: Context) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val TAG = "PSGCApiClient"
    
    // Base URL for the PSGC API
    private val baseUrl = "https://psgc.gitlab.io/api"
    
    /**
     * Fetch all provinces
     */
    fun getProvinces(
        onSuccess: (List<Province>) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = Request.Builder()
            .url("$baseUrl/provinces")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Network error while fetching provinces: ${e.message}")
                onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onError("Error ${response.code}: ${response.message}")
                    return
                }

                try {
                    val responseBody = response.body?.string()
                    if (responseBody == null) {
                        onError("Empty response")
                        return
                    }

                    val jsonArray = JSONArray(responseBody)
                    val provinces = mutableListOf<Province>()

                    for (i in 0 until jsonArray.length()) {
                        val provinceObj = jsonArray.getJSONObject(i)
                        provinces.add(
                            Province(
                                code = provinceObj.getString("code"),
                                name = provinceObj.getString("name"),
                                regionCode = provinceObj.getString("regionCode")
                            )
                        )
                    }

                    // Sort provinces alphabetically by name
                    provinces.sortBy { it.name }
                    
                    onSuccess(provinces)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing province data: ${e.message}")
                    onError("Error parsing data: ${e.message}")
                }
            }
        })
    }
    
    /**
     * Fetch cities/municipalities for a specific province
     */
    fun getCitiesByProvince(
        provinceCode: String,
        onSuccess: (List<City>) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = Request.Builder()
            .url("$baseUrl/provinces/$provinceCode/cities-municipalities")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Network error while fetching cities: ${e.message}")
                onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onError("Error ${response.code}: ${response.message}")
                    return
                }

                try {
                    val responseBody = response.body?.string()
                    if (responseBody == null) {
                        onError("Empty response")
                        return
                    }

                    val jsonArray = JSONArray(responseBody)
                    val cities = mutableListOf<City>()

                    for (i in 0 until jsonArray.length()) {
                        val cityObj = jsonArray.getJSONObject(i)
                        cities.add(
                            City(
                                code = cityObj.getString("code"),
                                name = cityObj.getString("name"),
                                provinceCode = provinceCode,
                                cityClass = cityObj.optString("cityClass", "")
                            )
                        )
                    }

                    // Sort cities alphabetically by name
                    cities.sortBy { it.name }
                    
                    onSuccess(cities)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing city data: ${e.message}")
                    onError("Error parsing data: ${e.message}")
                }
            }
        })
    }
    
    /**
     * Fetch barangays for a specific city/municipality
     */
    fun getBarangaysByCity(
        cityCode: String,
        onSuccess: (List<Barangay>) -> Unit,
        onError: (String) -> Unit
    ) {
        val request = Request.Builder()
            .url("$baseUrl/cities-municipalities/$cityCode/barangays")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Network error while fetching barangays: ${e.message}")
                onError("Network error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onError("Error ${response.code}: ${response.message}")
                    return
                }

                try {
                    val responseBody = response.body?.string()
                    if (responseBody == null) {
                        onError("Empty response")
                        return
                    }

                    val jsonArray = JSONArray(responseBody)
                    val barangays = mutableListOf<Barangay>()

                    for (i in 0 until jsonArray.length()) {
                        val barangayObj = jsonArray.getJSONObject(i)
                        barangays.add(
                            Barangay(
                                code = barangayObj.getString("code"),
                                name = barangayObj.getString("name"),
                                cityCode = cityCode
                            )
                        )
                    }

                    // Sort barangays alphabetically by name
                    barangays.sortBy { it.name }
                    
                    onSuccess(barangays)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing barangay data: ${e.message}")
                    onError("Error parsing data: ${e.message}")
                }
            }
        })
    }
}

/**
 * Data class representing a Province
 */
data class Province(
    val code: String,
    val name: String,
    val regionCode: String
) : Serializable

/**
 * Data class representing a City or Municipality
 */
data class City(
    val code: String,
    val name: String,
    val provinceCode: String,
    val cityClass: String = ""
) : Serializable

/**
 * Data class representing a Barangay
 */
data class Barangay(
    val code: String,
    val name: String,
    val cityCode: String
) : Serializable 