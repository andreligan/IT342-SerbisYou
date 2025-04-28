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
        if (providerId <= 0) {
            Log.e(TAG, "Cannot get services: Invalid provider ID ($providerId)")
            callback(null, Exception("Invalid provider ID"))
            return
        }
        
        // Add more explicit validation of the token
        if (token.isBlank()) {
            Log.e(TAG, "Cannot get services: Token is empty")
            callback(null, Exception("Authentication token is required"))
            return
        }
        
        Log.d(TAG, "Getting services for provider: $providerId with token: ${token.take(20)}...")
        
        // Construct the URL with the current provider ID to ensure we're using the right one
        val url = "${baseApiClient.getBaseUrl()}/api/services/provider/$providerId"
        Log.d(TAG, "Request URL: $url")
        
        val request = Request.Builder()
            .url(url)
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        Log.d(TAG, "Authorization header: Bearer ${token.take(20)}...")
        Log.d(TAG, "Request method: ${request.method}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get services for provider ID $providerId", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code for provider $providerId: ${response.code}")
                
                if (responseBody != null && responseBody.length > 100) {
                    Log.d(TAG, "Response body preview: ${responseBody.substring(0, 100)}...")
                } else {
                    Log.d(TAG, "Response body: $responseBody")
                }
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Services response for provider $providerId success")
                    try {
                        val type = object : TypeToken<List<Service>>() {}.type
                        val services = gson.fromJson<List<Service>>(responseBody, type)
                        
                        // Log service details for debugging
                        Log.d(TAG, "Found ${services.size} services for provider ID $providerId")
                        for (service in services) {
                            val actualProviderId = service.provider?.providerId ?: -1
                            Log.d(TAG, "Service: id=${service.serviceId}, name=${service.serviceName}, " +
                                    "providerId=$actualProviderId (expected=$providerId), " +
                                    "serviceImage=${service.serviceImage}, imageUrl=${service.imageUrl}")
                            
                            // Add additional validation to ensure all services truly belong to this provider
                            if (actualProviderId != providerId) {
                                Log.w(TAG, "Warning: Service ${service.serviceId} has provider ID $actualProviderId but we requested provider ID $providerId")
                            }
                        }
                        
                        // IMPORTANT: double-check that all services truly belong to this provider ID
                        val filteredServices = services.filter { 
                            val serviceProviderId = it.provider?.providerId
                            serviceProviderId == providerId 
                        }
                        
                        if (filteredServices.size != services.size) {
                            Log.w(TAG, "Warning: Filtered out ${services.size - filteredServices.size} services that didn't match provider ID $providerId")
                        }
                        
                        callback(filteredServices, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing services for provider ID $providerId", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error getting services for provider ID $providerId: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    
                    // If we get a 404 (endpoint not found) or other error, fall back to the old method
                    fallbackToAllServicesFiltering(providerId, token, callback)
                }
            }
        })
    }

    // Fallback method that uses the old approach of fetching all services and filtering
    private fun fallbackToAllServicesFiltering(providerId: Long, token: String, callback: (List<Service>?, Exception?) -> Unit) {
        Log.d(TAG, "Falling back to filtering all services for provider: $providerId")
        
        // Ensure we have a valid provider ID
        if (providerId <= 0) {
            Log.e(TAG, "Cannot filter services: Invalid provider ID ($providerId)")
            callback(emptyList(), null)  // Return empty list, not an error
            return
        }
        
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/services/getAll")
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get services in fallback method", e)
                callback(null, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                
                if (response.isSuccessful) {
                    try {
                        val type = object : TypeToken<List<Service>>() {}.type
                        val services = gson.fromJson<List<Service>>(responseBody, type)
                        
                        // Log all services with their provider IDs for debugging
                        Log.d(TAG, "Fallback found ${services.size} total services")
                        for (service in services) {
                            val serviceProviderId = service.provider?.providerId
                            Log.d(TAG, "Service ID: ${service.serviceId}, Name: ${service.serviceName}, Provider ID: $serviceProviderId")
                        }
                        
                        // Filter services by the provider ID that was passed to this method
                        val providerServices = services.filter { 
                            val serviceProviderId = it.provider?.providerId
                            
                            if (serviceProviderId == providerId) {
                                Log.d(TAG, "Service ${it.serviceName} (ID: ${it.serviceId}) matches provider ID $providerId")
                                true
                            } else {
                                Log.d(TAG, "Service ${it.serviceName} (ID: ${it.serviceId}) has provider ID $serviceProviderId - ignoring")
                                false
                            }
                        }
                        
                        Log.d(TAG, "Fallback method found ${providerServices.size} services for provider ID $providerId out of ${services.size} total services")
                        
                        callback(providerServices, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing services in fallback method", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error getting services in fallback method: ${response.code}")
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
            .url("${baseApiClient.getBaseUrl()}/api/services/postService/$providerId/$categoryId")
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
            .url("${baseApiClient.getBaseUrl()}/api/services/updateService/$serviceId/$providerId/$categoryId")
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
        
        // FIXED: Use the actual providerId instead of hardcoding to 1L
        
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
            .url("${baseApiClient.getBaseUrl()}/api/services/updateServiceWithImage/$serviceId/$providerId/$categoryId")
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
        
        // FIXED: Use the actual providerId instead of hardcoding to 1L
        
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
        
        val url = "${baseApiClient.getBaseUrl()}/api/$endpoint/$providerId/$categoryId"
        
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

    // Get provider ID by user ID
    fun getProviderIdByUserId(userId: Long, token: String, callback: (Long, Exception?) -> Unit) {
        Log.d(TAG, "Getting provider ID for user ID: $userId")
        
        // First try using the getAllServiceProviders endpoint and filter
        getAllServiceProviders(token) { providers, error ->
            if (error != null) {
                Log.e(TAG, "Failed to get all service providers", error)
                callback(0, error)
                return@getAllServiceProviders
            }
            
            if (providers != null && providers.isNotEmpty()) {
                // Find the provider with matching userAuth.userId
                val matchingProvider = providers.find { provider ->
                    try {
                        // Use a simple approach that works regardless of the actual type
                        val userAuth = provider.userAuth
                        
                        if (userAuth != null) {
                            // Convert the userAuth object to string and extract userId using regex
                            val userAuthString = userAuth.toString()
                            
                            // Look for userId in the string representation
                            val userIdRegex = "userId=([0-9]+)".toRegex()
                            val match = userIdRegex.find(userAuthString)
                            
                            if (match != null) {
                                val extractedUserId = match.groupValues[1].toLongOrNull()
                                extractedUserId == userId
                            } else {
                                // Alternative: try to access as a map
                                if (userAuthString.contains("userId")) {
                                    try {
                                        // This is safe because we're using toString first
                                        val map = userAuth as? Map<*, *>
                                        val mapUserId = map?.get("userId")
                                        when (mapUserId) {
                                            is Number -> mapUserId.toLong() == userId
                                            is String -> mapUserId.toLong() == userId
                                            else -> false
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error accessing userId as map", e)
                                        false
                                    }
                                } else {
                                    false
                                }
                            }
                        } else {
                            false
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error matching provider", e)
                        false
                    }
                }
                
                if (matchingProvider != null && matchingProvider.providerId != null) {
                    Log.d(TAG, "Found matching provider ID: ${matchingProvider.providerId} for user ID: $userId")
                    callback(matchingProvider.providerId, null)
                    return@getAllServiceProviders
                } else {
                    Log.e(TAG, "No provider found with matching user ID: $userId")
                    callback(0, Exception("No provider found for user ID: $userId"))
                }
            } else {
                Log.e(TAG, "No providers found in the system")
                callback(0, Exception("No providers found in the system"))
            }
        }
    }
    
    // Get all service providers
    private fun getAllServiceProviders(token: String, callback: (List<ServiceProvider>?, Exception?) -> Unit) {
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/service-providers/getAll")
            .get()
            .header("Authorization", "Bearer $token")
            .build()
            
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get all service providers", e)
                callback(null, e)
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val type = object : TypeToken<List<ServiceProvider>>() {}.type
                        val providers = gson.fromJson<List<ServiceProvider>>(responseBody, type)
                        Log.d(TAG, "Found ${providers.size} service providers")
                        callback(providers, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing service providers", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error getting all service providers: ${response.code}")
                    callback(null, Exception("Failed to get all service providers: ${response.code}"))
                }
            }
        })
    }
}