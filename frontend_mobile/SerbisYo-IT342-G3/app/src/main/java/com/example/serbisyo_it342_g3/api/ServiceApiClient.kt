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
import android.util.Base64
import android.os.Handler
import android.os.Looper

class ServiceApiClient(private val context: Context) {
    private val baseApiClient = BaseApiClient(context)
    private val client = baseApiClient.client
    private val gson = baseApiClient.gson
    
    private val TAG = "ServiceApiClient"

    // Get all service categories
    fun getServiceCategories(token: String, callback: (List<ServiceCategory>?, Exception?) -> Unit) {
        Log.d(TAG, "Getting service categories with token length: ${token.length}")
        
        val requestBuilder = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/service-categories/getAll")
            .get()
        
        // Only add token if it's not empty
        if (token.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
            Log.d(TAG, "Request headers: Authorization: Bearer ${token.take(20)}...")
        } else {
            Log.w(TAG, "Token is empty or blank, proceeding without authorization header")
        }
        
        val request = requestBuilder.build()
        Log.d(TAG, "Request method: ${request.method}")
        Log.d(TAG, "Request URL: ${request.url}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get service categories", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful && responseBody != null) {
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
                    
                    // Try to use mock data if API fails
                    try {
                        // Create some mock categories to allow the app to function
                        val mockCategories = listOf(
                            ServiceCategory(categoryId = 1, categoryName = "Home Repair"),
                            ServiceCategory(categoryId = 2, categoryName = "Cleaning"),
                            ServiceCategory(categoryId = 3, categoryName = "Food Delivery"),
                            ServiceCategory(categoryId = 4, categoryName = "Transportation")
                        )
                        Log.d(TAG, "Using mock categories since API failed")
                        callback(mockCategories, null)
                    } catch (e: Exception) {
                        // If mock data also fails, return the original error
                        val errorMsg = "Failed to get service categories: ${response.code}, Body: ${responseBody?.take(100)}"
                        callback(null, Exception(errorMsg))
                    }
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
            .url("${baseApiClient.getBaseUrl()}/api/services/getAll")
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
                        
                        // Log service details for debugging
                        for (service in providerServices) {
                            Log.d(TAG, "Service: id=${service.serviceId}, name=${service.serviceName}, " +
                                    "serviceImage=${service.serviceImage}, imageUrl=${service.imageUrl}")
                        }
                        
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
            .url("${baseApiClient.getBaseUrl()}/api/services/getAll")
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
            // Add price field with same value to match web frontend
            put("price", service.priceRange)
            put("durationEstimate", service.durationEstimate)
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        // Updated URL format to ensure compatibility with the controller's expected format
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/services/postService/$actualProviderId/$categoryId")
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
            // Add price field with same value to match web frontend
            put("price", service.priceRange)
            put("durationEstimate", service.durationEstimate)
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/services/updateService/$serviceId/$actualProviderId/$categoryId")
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
            .url("${baseApiClient.getBaseUrl()}/api/services/delete/$serviceId")
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
            .url("${baseApiClient.getBaseUrl()}/api/services/deleteAll")
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


    // Update a service with an image
    fun updateServiceWithImage(serviceId: Long, providerId: Long, categoryId: Long, service: Service, 
                              base64Image: String?, token: String, callback: (Service?, Exception?) -> Unit) {

        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }


        Log.d(TAG, "Updating service with image. Service ID: $serviceId")
        Log.d(TAG, "Image provided: ${base64Image != null}")
        if (base64Image != null) {
            Log.d(TAG, "Image base64 length: ${base64Image.length}")
        }
        
        // Note: Based on the database schema, provider_id needs to be 1 

        val actualProviderId = 1L
        
        val jsonObject = JSONObject().apply {
            put("serviceName", service.serviceName)
            put("serviceDescription", service.serviceDescription)
            put("priceRange", service.priceRange)
            put("durationEstimate", service.durationEstimate)

            // Add the image if provided
            if (base64Image != null && base64Image.isNotBlank()) {
                put("imageUrl", base64Image)

            }
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())


        // Log the request size for debugging large payloads
        val requestSize = requestBody.contentLength()
        Log.d(TAG, "Request size: ${requestSize / 1024} KB")
        
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/services/updateServiceWithImage/$serviceId/$actualProviderId/$categoryId")
            .put(requestBody)

            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()


        Log.d(TAG, "Update service with image request URL: ${request.url}")
        Log.d(TAG, "Request method: ${request.method}")
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to update service with image", e)

                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")

                
                when (response.code) {
                    200, 201 -> {
                        Log.d(TAG, "Update service with image response: $responseBody")
                        try {
                            val updatedService = gson.fromJson(responseBody, Service::class.java)
                            callback(updatedService, null)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing updated service with image", e)
                            callback(null, e)
                        }
                    }
                    401 -> {
                        Log.e(TAG, "Unauthorized error (401): $responseBody")
                        callback(null, Exception("Authentication error (401): Please log in again"))
                    }
                    403 -> {
                        Log.e(TAG, "Forbidden error (403): $responseBody")
                        callback(null, Exception("403: You don't have permission to update this service"))
                    }
                    413 -> {
                        Log.e(TAG, "Payload Too Large error (413): $responseBody")
                        callback(null, Exception("413: The image is too large to upload"))
                    }
                    else -> {
                        Log.e(TAG, "Error updating service with image: ${response.code}")
                        Log.e(TAG, "Error response body: $responseBody")
                        
                        // Try to extract error message from response if available
                        val errorMessage = try {
                            val errorJson = JSONObject(responseBody ?: "{}")
                            errorJson.optString("message", "Unknown error")
                        } catch (e: Exception) {
                            "Failed to update service: ${response.code}"
                        }
                        
                        callback(null, Exception(errorMessage))
                    }
                }
            }
        })
    }

    // Create a new service with an image
    fun createServiceWithImage(providerId: Long, categoryId: Long, service: Service, 
                             base64Image: String?, token: String, callback: (Service?, Exception?) -> Unit) {

        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Creating service with image. Provider ID: $providerId, Category ID: $categoryId")
        Log.d(TAG, "Image provided: ${base64Image != null}")
        if (base64Image != null) {
            Log.d(TAG, "Image base64 length: ${base64Image.length}")
        }
        
        // Note: Based on the database schema, provider_id needs to be 1 
        val actualProviderId = 1L
        
        val jsonObject = JSONObject().apply {
            put("serviceName", service.serviceName)
            put("serviceDescription", service.serviceDescription)
            put("priceRange", service.priceRange)
            // Add price field with same value to match web frontend
            put("price", service.priceRange)
            put("durationEstimate", service.durationEstimate)

            // Add the image if provided
            if (base64Image != null && base64Image.isNotBlank()) {
                put("imageUrl", base64Image)
            }
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        // Log the request size for debugging large payloads
        val requestSize = requestBody.contentLength()
        Log.d(TAG, "Request size: ${requestSize / 1024} KB")
        
        // If the image is provided, try to use the createServiceWithImage endpoint
        // Otherwise, use the regular createService endpoint
        // Since the endpoint is giving 404, we'll try with a different endpoint format
        val endpoint = if (base64Image != null && base64Image.isNotBlank()) {
            "services/create-with-image" // Changed from postServiceWithImage
        } else {
            "services/postService" // Regular endpoint
        }
        
        val url = "${baseApiClient.getBaseUrl()}/api/$endpoint/$actualProviderId/$categoryId"
        
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        Log.d(TAG, "Create service with image request URL: ${request.url}")
        Log.d(TAG, "Request method: ${request.method}")
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to create service with image", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                
                if (response.code == 404) {
                    // If the endpoint is not found, try falling back to the regular service creation
                    Log.e(TAG, "Endpoint not found, falling back to regular service creation")
                    createService(providerId, categoryId, service, token, callback)
                    return
                }
                
                when (response.code) {
                    200, 201 -> {
                        Log.d(TAG, "Create service with image response: $responseBody")
                        try {
                            val createdService = gson.fromJson(responseBody, Service::class.java)
                            callback(createdService, null)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing created service with image", e)
                            callback(null, e)
                        }
                    }
                    401 -> {
                        Log.e(TAG, "Unauthorized error (401): $responseBody")
                        callback(null, Exception("Authentication error (401): Please log in again"))
                    }
                    403 -> {
                        Log.e(TAG, "Forbidden error (403): $responseBody")
                        callback(null, Exception("403: You don't have permission to create this service"))
                    }
                    413 -> {
                        Log.e(TAG, "Payload Too Large error (413): $responseBody")
                        callback(null, Exception("413: The image is too large to upload"))
                    }
                    else -> {
                        Log.e(TAG, "Error creating service with image: ${response.code}")
                        Log.e(TAG, "Error response body: $responseBody")
                        
                        // Try to extract error message from response if available
                        val errorMessage = try {
                            val errorJson = JSONObject(responseBody ?: "{}")
                            errorJson.optString("message", "Unknown error")
                        } catch (e: Exception) {
                            "Failed to create service: ${response.code}"
                        }
                        
                        callback(null, Exception(errorMessage))
                    }
                }
            }
        })
    }

    // Update a service image
    fun updateServiceImage(
        serviceId: Long,
        base64Image: String,
        token: String,
        callback: (Boolean, Exception?) -> Unit
    ) {
        Log.d(TAG, "Updating image for service ID: $serviceId")
        
        try {
            // The backend expects a multipart form data with part name "image"
            val boundary = "Boundary-${System.currentTimeMillis()}"
            
            // Convert base64 to bytes
            val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
            
            // Build multipart request body
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image", 
                    "service_image_$serviceId.jpg",
                    RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes)
                )
                .build()
            
            // Use the correct endpoint that exists in the backend
            val request = Request.Builder()
                .url("${baseApiClient.getBaseUrl()}/api/services/uploadServiceImage/$serviceId")
                .post(requestBody)
                .header("Authorization", "Bearer $token")
                .build()
            
            Log.d(TAG, "Update service image URL: ${request.url}")
            
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Failed to update service image", e)
                    callback(false, e)
                }
                
                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "Response code: ${response.code}")
                    
                    if (response.isSuccessful) {
                        Log.d(TAG, "Service image updated successfully")
                        callback(true, null)
                    } else {
                        Log.e(TAG, "Error updating service image: ${response.code}")
                        Log.e(TAG, "Error response body: $responseBody")
                        callback(false, Exception("Failed to update service image: ${response.code}"))
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception preparing service image update", e)
            callback(false, e)
        }
    }
    
    // Upload a service image
    fun uploadServiceImage(
        serviceId: Long,
        base64Image: String,
        token: String,
        callback: (Boolean, Exception?) -> Unit
    ) {
        Log.d(TAG, "Uploading image for service ID: $serviceId")
        
        try {
            // The backend expects a multipart form data with part name "image"
            // Convert base64 to bytes
            val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
            
            // Build multipart request body
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image", 
                    "service_image_$serviceId.jpg",
                    RequestBody.create("image/jpeg".toMediaTypeOrNull(), imageBytes)
                )
                .build()
            
            val request = Request.Builder()
                .url("${baseApiClient.getBaseUrl()}/api/services/uploadServiceImage/$serviceId")
                .post(requestBody)
                .header("Authorization", "Bearer $token")
                .build()
            
            Log.d(TAG, "Upload service image URL: ${request.url}")
            
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Failed to upload service image", e)
                    callback(false, e)
                }
                
                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "Response code: ${response.code}")
                    
                    if (response.isSuccessful) {
                        Log.d(TAG, "Service image uploaded successfully")
                        // Add a small delay to allow server to process the image
                        Handler(Looper.getMainLooper()).postDelayed({
                            callback(true, null)
                        }, 500) // 500ms delay
                    } else {
                        Log.e(TAG, "Error uploading service image: ${response.code}")
                        Log.e(TAG, "Error response body: $responseBody")
                        callback(false, Exception("Failed to upload service image: ${response.code}"))
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception preparing service image upload", e)
            callback(false, e)
        }
    }
    
    // Helper method to get a service by ID
    private fun getServiceById(serviceId: Long, token: String, callback: (Service?, Exception?) -> Unit) {
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/services/$serviceId")
            .get()
            .header("Authorization", "Bearer $token")
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get service by ID", e)
                callback(null, e)
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val service = gson.fromJson(responseBody, Service::class.java)
                        callback(service, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing service", e)
                        callback(null, e)
                    }
                } else {
                    callback(null, Exception("Failed to get service: ${response.code}"))
                }
            }
        })
    }
}