package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import com.example.serbisyo_it342_g3.data.Address
import com.example.serbisyo_it342_g3.data.Service
import com.example.serbisyo_it342_g3.data.ServiceCategory
import com.example.serbisyo_it342_g3.data.ServiceProvider
import com.example.serbisyo_it342_g3.data.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ServiceApiClient(private val context: Context) {
    private val client = OkHttpClient()
    private val gson = Gson()
    //if using the android emulator
    //private val BASE_URL = "http://10.0.2.2:8080" // Use your actual backend URL

    //if using the physical device
    private val BASE_URL = "http://192.168.254.103:8080" // sa dalaguete
    //private val BASE_URL = "http://192.168.17.136:8080" // Your computer's IP address sa guada?
    private val TAG = "ServiceApiClient"

    // Get all service categories
    fun getServiceCategories(token: String, callback: (List<ServiceCategory>?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Getting service categories with token length: ${token.length}")
        Log.d(TAG, "Request URL: $BASE_URL/api/service-categories/getAll")
        
        val request = Request.Builder()
            .url("$BASE_URL/api/service-categories/getAll")
            .get()
            .header("Authorization", "Bearer $token")
            .build()
        
        Log.d(TAG, "Request headers: Authorization: Bearer ${token.take(20)}...")
        Log.d(TAG, "Request method: ${request.method}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get service categories", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Service Categories response: $responseBody")
                    try {
                        val type = object : TypeToken<List<ServiceCategory>>() {}.type
                        val categories = gson.fromJson<List<ServiceCategory>>(responseBody, type)
                        callback(categories, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing service categories", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error getting service categories: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    callback(null, Exception("Failed to get service categories: ${response.code}"))
                }
            }
        })
    }

    // Get all services by provider ID
    fun getServicesByProviderId(providerId: Long, token: String, callback: (List<Service>?, Exception?) -> Unit) {
        Log.d(TAG, "Getting services for provider: $providerId with token: $token")
        
        // Note: Based on the database schema, provider_id needs to be 1 
        // even though the user ID is 5 (there seems to be a mismatch in the database)
        val actualProviderId = 1L
        
        val request = Request.Builder()
            .url("$BASE_URL/api/services/getAll")
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        Log.d(TAG, "Request URL: ${request.url}")
        Log.d(TAG, "Authorization header: Bearer ${token.take(20)}...")
        Log.d(TAG, "Request method: ${request.method}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get services", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Services response: $responseBody")
                    try {
                        val type = object : TypeToken<List<Service>>() {}.type
                        val services = gson.fromJson<List<Service>>(responseBody, type)
                        // Filter services by provider ID (using the actual provider ID from database)
                        val providerServices = services.filter { it.provider?.providerId == actualProviderId }
                        callback(providerServices, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing services", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error getting services: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    callback(null, Exception("Failed to get services: ${response.code}"))
                }
            }
        })
    }

    // Get all services for customers
    fun getAllServices(token: String, callback: (List<Service>?, Exception?) -> Unit) {
        Log.d(TAG, "Getting all services with token: $token")
        
        val request = Request.Builder()
            .url("$BASE_URL/api/services/getAll")
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get all services", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    Log.d(TAG, "All services response: $responseBody")
                    try {
                        val type = object : TypeToken<List<Service>>() {}.type
                        val services = gson.fromJson<List<Service>>(responseBody, type)
                        callback(services, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing all services", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error getting all services: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    callback(null, Exception("Failed to get all services: ${response.code}"))
                }
            }
        })
    }

    // Create a new service
    fun createService(providerId: Long, categoryId: Long, service: Service, token: String, callback: (Service?, Exception?) -> Unit) {
        Log.d(TAG, "Creating service for provider: $providerId, category: $categoryId")
        
        // Note: Based on the database schema, provider_id needs to be 1 
        // even though the user ID is 5 (there seems to be a mismatch in the database)
        val actualProviderId = 1L
        
        val jsonObject = JSONObject().apply {
            put("serviceName", service.serviceName)
            put("serviceDescription", service.serviceDescription)
            put("priceRange", service.priceRange)
            put("durationEstimate", service.durationEstimate)
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        // Updated URL format to ensure compatibility with the controller's expected format
        val request = Request.Builder()
            .url("$BASE_URL/api/services/postService/$actualProviderId/$categoryId")
            .post(requestBody) // Ensure we're using POST method
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json") // Add explicit content type
            .build()

        Log.d(TAG, "Request URL: ${request.url}")
        Log.d(TAG, "Request body: $jsonObject")
        Log.d(TAG, "Authorization header: Bearer $token")
        Log.d(TAG, "Request method: ${request.method}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to create service", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Create service response: $responseBody")
                    try {
                        val createdService = gson.fromJson(responseBody, Service::class.java)
                        callback(createdService, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing created service", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error creating service: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    callback(null, Exception("Failed to create service: ${response.code}"))
                }
            }
        })
    }

    // Update an existing service
    fun updateService(serviceId: Long, providerId: Long, categoryId: Long, service: Service, token: String, callback: (Service?, Exception?) -> Unit) {
        Log.d(TAG, "Updating service with ID: $serviceId")
        
        // Note: Based on the database schema, provider_id needs to be 1 
        val actualProviderId = 1L
        
        val jsonObject = JSONObject().apply {
            put("serviceName", service.serviceName)
            put("serviceDescription", service.serviceDescription)
            put("priceRange", service.priceRange)
            put("durationEstimate", service.durationEstimate)
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("$BASE_URL/api/services/updateService/$serviceId/$actualProviderId/$categoryId")
            .put(requestBody) // Ensure we're using PUT method
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json") // Add explicit content type
            .build()

        Log.d(TAG, "Update service request URL: ${request.url}")
        Log.d(TAG, "Update service request body: $jsonObject")
        Log.d(TAG, "Authorization header: Bearer $token")
        Log.d(TAG, "Request method: ${request.method}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to update service", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Update service response: $responseBody")
                    try {
                        val updatedService = gson.fromJson(responseBody, Service::class.java)
                        callback(updatedService, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing updated service", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error updating service: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    callback(null, Exception("Failed to update service: ${response.code}"))
                }
            }
        })
    }

    // Delete a service
    fun deleteService(serviceId: Long, token: String, callback: (Boolean, Exception?) -> Unit) {
        Log.d(TAG, "Deleting service with ID: $serviceId")
        val request = Request.Builder()
            .url("$BASE_URL/api/services/delete/$serviceId")
            .delete() // Ensure we're using DELETE method
            .header("Authorization", "Bearer $token")
            .build()

        Log.d(TAG, "Delete service request URL: ${request.url}")
        Log.d(TAG, "Authorization header: Bearer $token")
        Log.d(TAG, "Request method: ${request.method}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to delete service", e)
                callback(false, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Service deleted successfully")
                    callback(true, null)
                } else {
                    Log.e(TAG, "Error deleting service: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    callback(false, Exception("Failed to delete service: ${response.code}"))
                }
            }
        })
    }

    // Delete all services (cleanup function)
    fun deleteAllServices(callback: (Boolean, Exception?) -> Unit) {
        val request = Request.Builder()
            .url("$BASE_URL/api/services/deleteAll")
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to delete all services", e)
                callback(false, e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d(TAG, "All services deleted successfully")
                    callback(true, null)
                } else {
                    Log.e(TAG, "Error deleting all services: ${response.code}")
                    callback(false, Exception("Failed to delete all services: ${response.code}"))
                }
            }
        })
    }

    // Create a new service with image
    fun createServiceWithImage(providerId: Long, categoryId: Long, service: Service, token: String, callback: (Service?, Exception?) -> Unit) {
        Log.d(TAG, "Creating service with image for provider: $providerId, category: $categoryId")
        
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }
        
        // Note: Based on the database schema, provider_id needs to be 1 
        // even though the user ID is 5 (there seems to be a mismatch in the database)
        val actualProviderId = 1L
        
        val jsonObject = JSONObject().apply {
            put("serviceName", service.serviceName)
            put("serviceDescription", service.serviceDescription)
            put("priceRange", service.priceRange)
            put("durationEstimate", service.durationEstimate)
            // Add the image as a base64 string
            if (service.imageUrl.isNotEmpty()) {
                put("imageUrl", service.imageUrl)
                Log.d(TAG, "Image URL length: ${service.imageUrl.length}")
            } else {
                Log.d(TAG, "No image URL provided")
            }
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        // Call the updated backend endpoint that accepts images
        val request = Request.Builder()
            .url("$BASE_URL/api/services/postServiceWithImage/$actualProviderId/$categoryId")
            .post(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        Log.d(TAG, "Request URL: ${request.url}")
        Log.d(TAG, "Request body contains image: ${service.imageUrl.isNotEmpty()}")
        Log.d(TAG, "Authorization header: Bearer ${token.take(20)}...")
        Log.d(TAG, "Request method: ${request.method}")
        Log.d(TAG, "Request body size: ${requestBody.contentLength()} bytes")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to create service with image", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                when (response.code) {
                    200, 201 -> {
                        Log.d(TAG, "Create service with image response: $responseBody")
                        try {
                            val createdService = gson.fromJson(responseBody, Service::class.java)
                            callback(createdService, null)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing created service", e)
                            callback(null, Exception("Error parsing server response: ${e.message}"))
                        }
                    }
                    401, 403 -> {
                        Log.e(TAG, "Authentication error: ${response.code}")
                        Log.e(TAG, "Error response body: $responseBody")
                        callback(null, Exception("Authentication failed. Please log in again."))
                    }
                    413 -> {
                        Log.e(TAG, "Payload too large: ${response.code}")
                        Log.e(TAG, "Error response body: $responseBody")
                        callback(null, Exception("Image is too large. Please use a smaller image."))
                    }
                    else -> {
                        Log.e(TAG, "Error creating service with image: ${response.code}")
                        Log.e(TAG, "Error response body: $responseBody")
                        var errorMessage = "Failed to create service"
                        try {
                            // Try to extract error message from response body if it's in JSON format
                            val errorJson = JSONObject(responseBody ?: "{}")
                            if (errorJson.has("message")) {
                                errorMessage = errorJson.getString("message")
                            } else if (errorJson.has("error")) {
                                errorMessage = errorJson.getString("error")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing error message", e)
                        }
                        callback(null, Exception("$errorMessage (Status: ${response.code})"))
                    }
                }
            }
        })
    }

    // Update an existing service with image
    fun updateServiceWithImage(serviceId: Long, providerId: Long, categoryId: Long, service: Service, token: String, callback: (Service?, Exception?) -> Unit) {
        Log.d(TAG, "Updating service with image, ID: $serviceId")
        
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }
        
        // Note: Based on the database schema, provider_id needs to be 1 
        val actualProviderId = 1L
        
        val jsonObject = JSONObject().apply {
            put("serviceName", service.serviceName)
            put("serviceDescription", service.serviceDescription)
            put("priceRange", service.priceRange)
            put("durationEstimate", service.durationEstimate)
            // Add the image as a base64 string
            if (service.imageUrl.isNotEmpty()) {
                put("imageUrl", service.imageUrl)
                Log.d(TAG, "Image URL length: ${service.imageUrl.length}")
            } else {
                Log.d(TAG, "No image URL provided")
            }
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        // Call the updated backend endpoint that accepts images
        val request = Request.Builder()
            .url("$BASE_URL/api/services/updateServiceWithImage/$serviceId/$actualProviderId/$categoryId")
            .put(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        Log.d(TAG, "Request URL: ${request.url}")
        Log.d(TAG, "Request body contains image: ${service.imageUrl.isNotEmpty()}")
        Log.d(TAG, "Authorization header: Bearer ${token.take(20)}...")
        Log.d(TAG, "Request method: ${request.method}")
        Log.d(TAG, "Request body size: ${requestBody.contentLength()} bytes")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to update service with image", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                when (response.code) {
                    200, 201 -> {
                        Log.d(TAG, "Update service with image response: $responseBody")
                        try {
                            val updatedService = gson.fromJson(responseBody, Service::class.java)
                            callback(updatedService, null)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing updated service", e)
                            callback(null, Exception("Error parsing server response: ${e.message}"))
                        }
                    }
                    401, 403 -> {
                        Log.e(TAG, "Authentication error: ${response.code}")
                        Log.e(TAG, "Error response body: $responseBody")
                        callback(null, Exception("Authentication failed. Please log in again."))
                    }
                    413 -> {
                        Log.e(TAG, "Payload too large: ${response.code}")
                        Log.e(TAG, "Error response body: $responseBody")
                        callback(null, Exception("Image is too large. Please use a smaller image."))
                    }
                    else -> {
                        Log.e(TAG, "Error updating service with image: ${response.code}")
                        Log.e(TAG, "Error response body: $responseBody")
                        var errorMessage = "Failed to update service"
                        try {
                            // Try to extract error message from response body if it's in JSON format
                            val errorJson = JSONObject(responseBody ?: "{}")
                            if (errorJson.has("message")) {
                                errorMessage = errorJson.getString("message")
                            } else if (errorJson.has("error")) {
                                errorMessage = errorJson.getString("error")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing error message", e)
                        }
                        callback(null, Exception("$errorMessage (Status: ${response.code})"))
                    }
                }
            }
        })
    }
}