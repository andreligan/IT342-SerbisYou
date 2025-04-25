package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import com.example.serbisyo_it342_g3.SerbisYoApplication
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ImageUploadApiClient(private val context: Context) {
    private val TAG = "ImageUploadApiClient"
    
    /**
     * Upload an image for a service
     */
    fun uploadImage(
        serviceId: Long,
        base64Image: String,
        token: String,
        callback: (Boolean, Exception?) -> Unit
    ) {
        val baseUrl = SerbisYoApplication.getBaseUrl(context)
        val requestUrl = "$baseUrl/api/services/$serviceId/image"
        
        Log.d(TAG, "Uploading image for service ID: $serviceId")
        
        // Create JSON request body
        val jsonObject = JSONObject().apply {
            put("imageData", base64Image)
        }
        
        val requestBody = jsonObject.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())
        
        // Execute the request on a background thread
        Thread {
            try {
                val client = ApiClientFactory.createHttpClient(token)
                
                val request = ApiClientFactory.createPutRequest(requestUrl, requestBody)
                
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d(TAG, "Image uploaded successfully")
                        callback(true, null)
                    } else {
                        val error = Exception("Failed to upload image: ${response.code}")
                        Log.e(TAG, error.message ?: "Unknown error", error)
                        callback(false, error)
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error", e)
                callback(false, e)
            } catch (e: Exception) {
                Log.e(TAG, "Error uploading image", e)
                callback(false, e)
            }
        }.start()
    }
} 