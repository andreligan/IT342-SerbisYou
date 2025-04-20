package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import com.example.serbisyo_it342_g3.data.Address
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException

class AddressApiClient(private val context: Context) {
    private val client = OkHttpClient()
    private val gson = Gson()
    
    // CONFIGURATION FOR BACKEND CONNECTION
    // For Android Emulator - Virtual Device (default)
    private val EMULATOR_URL = "http://10.0.2.2:8080" 
    
    // For Physical Device - Use your computer's actual IP address from ipconfig
    private val PHYSICAL_DEVICE_URL = "http://192.168.254.103:8080"
    
    // SWITCH BETWEEN CONNECTION TYPES:
    // Uncomment the one you need and comment out the other
    // private val BASE_URL = EMULATOR_URL     // For Android Emulator
    private val BASE_URL = PHYSICAL_DEVICE_URL // For Physical Device
    
    private val TAG = "AddressApiClient"

    // Get addresses by user ID
    fun getAddressesByUserId(userId: Long, token: String, callback: (List<Address>?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Getting addresses for user: $userId")
        
        val request = Request.Builder()
            .url("$BASE_URL/api/addresses/user/$userId")
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get addresses", e)
                
                // Check if it's a connection timeout
                val errorMessage = if (e is SocketTimeoutException) {
                    "Connection timeout: Please check your internet connection and server availability"
                } else {
                    "Failed to load addresses: ${e.message}"
                }
                
                callback(null, Exception(errorMessage))
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Addresses response: $responseBody")
                    try {
                        val type = object : TypeToken<List<Address>>() {}.type
                        val addresses = gson.fromJson<List<Address>>(responseBody, type)
                        callback(addresses, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing addresses", e)
                        callback(null, e)
                    }
                } else {
                    Log.e(TAG, "Error getting addresses: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    
                    // Try to extract error message from response if available
                    val errorMessage = when (response.code) {
                        403 -> {
                            val detailedMessage = try {
                                val errorJson = JSONObject(responseBody ?: "{}")
                                errorJson.optString("message", "")
                            } catch (e: Exception) {
                                ""
                            }
                            
                            if (detailedMessage.isNotEmpty()) {
                                "Permission denied: $detailedMessage (403 Forbidden)"
                            } else {
                                "Permission denied: You don't have access to view addresses (403 Forbidden). Please try logging out and logging back in, or contact support."
                            }
                        }
                        401 -> "Authentication failed: Please log in again (401 Unauthorized)"
                        404 -> "No addresses found for this user"
                        else -> try {
                            val errorJson = JSONObject(responseBody ?: "{}")
                            errorJson.optString("message", "Failed to get addresses: ${response.code}")
                        } catch (e: Exception) {
                            "Failed to get addresses: ${response.code}"
                        }
                    }
                    
                    callback(null, Exception(errorMessage))
                }
            }
        })
    }
    
    // Add a new address
    fun addAddress(userId: Long, address: Address, token: String, callback: (Boolean, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(false, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Adding address for user: $userId")
        
        val jsonObject = JSONObject().apply {
            put("userId", userId)
            put("street", address.street)
            put("barangay", address.barangay)
            put("city", address.city)
            put("province", address.province)
            put("postalCode", address.postalCode)
            put("isMainAddress", true) // Set as main address by default
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        val request = Request.Builder()
            .url("$BASE_URL/api/addresses/add")
            .post(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to add address", e)
                callback(false, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Add address response: $responseBody")
                    callback(true, null)
                } else {
                    Log.e(TAG, "Error adding address: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    
                    // Try to extract error message from response if available
                    val errorMessage = when (response.code) {
                        403 -> {
                            val detailedMessage = try {
                                val errorJson = JSONObject(responseBody ?: "{}")
                                errorJson.optString("message", "")
                            } catch (e: Exception) {
                                ""
                            }
                            
                            if (detailedMessage.isNotEmpty()) {
                                "Permission denied: $detailedMessage (403 Forbidden)"
                            } else {
                                "Permission denied: You don't have access to add addresses (403 Forbidden). Please try logging out and logging back in, or contact support."
                            }
                        }
                        401 -> "Authentication failed: Please log in again (401 Unauthorized)"
                        400 -> try {
                            val errorJson = JSONObject(responseBody ?: "{}")
                            errorJson.optString("message", "Invalid address data (400 Bad Request)")
                        } catch (e: Exception) {
                            "Invalid address data (400 Bad Request)"
                        }
                        else -> try {
                            val errorJson = JSONObject(responseBody ?: "{}")
                            errorJson.optString("message", "Failed to add address: ${response.code}")
                        } catch (e: Exception) {
                            "Failed to add address: ${response.code}"
                        }
                    }
                    
                    callback(false, Exception(errorMessage))
                }
            }
        })
    }
    
    // Delete an address
    fun deleteAddress(addressId: Int, token: String, callback: (Boolean, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(false, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Deleting address: $addressId")
        
        val request = Request.Builder()
            .url("$BASE_URL/api/addresses/delete/$addressId")
            .delete()
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to delete address", e)
                callback(false, e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Delete address response: $responseBody")
                    callback(true, null)
                } else {
                    Log.e(TAG, "Error deleting address: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    
                    // Try to extract error message from response if available
                    val errorMessage = try {
                        val errorJson = JSONObject(responseBody ?: "{}")
                        errorJson.optString("message", "Failed to delete address: ${response.code}")
                    } catch (e: Exception) {
                        "Failed to delete address: ${response.code}"
                    }
                    
                    callback(false, Exception(errorMessage))
                }
            }
        })
    }
    
    // Get an address by ID
    fun getAddressById(addressId: Int, token: String, callback: (com.example.serbisyo_it342_g3.model.Address?, String?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, "Authentication token is required")
            return
        }

        Log.d(TAG, "Getting address with ID: $addressId")
        
        val request = Request.Builder()
            .url("$BASE_URL/api/addresses/$addressId")
            .get()
            .header("Authorization", "Bearer $token")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get address", e)
                callback(null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful && responseBody != null) {
                    try {
                        // Convert the data.Address to model.Address
                        val dataAddress = gson.fromJson(responseBody, com.example.serbisyo_it342_g3.data.Address::class.java)
                        
                        // Map the fields to create a model.Address
                        val modelAddress = com.example.serbisyo_it342_g3.model.Address(
                            id = dataAddress.addressId?.toInt() ?: 0,
                            street = dataAddress.street,
                            city = dataAddress.city,
                            province = dataAddress.province,
                            barangay = dataAddress.barangay ?: "",
                            zipCode = dataAddress.postalCode
                        )
                        
                        callback(modelAddress, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing address data", e)
                        callback(null, "Error parsing address data: ${e.message}")
                    }
                } else {
                    Log.e(TAG, "Error getting address: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    
                    // Try to extract error message from response if available
                    val errorMessage = try {
                        val errorJson = JSONObject(responseBody ?: "{}")
                        errorJson.optString("message", "Failed to get address: ${response.code}")
                    } catch (e: Exception) {
                        "Failed to get address: ${response.code}"
                    }
                    
                    callback(null, errorMessage)
                }
            }
        })
    }
    
    // Create a new address
    fun createAddress(address: com.example.serbisyo_it342_g3.model.Address, token: String, callback: (Int?, String?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, "Authentication token is required")
            return
        }

        Log.d(TAG, "Creating new address")
        
        val jsonObject = JSONObject().apply {
            put("street", address.street)
            put("barangay", address.barangay)
            put("city", address.city)
            put("province", address.province)
            put("postalCode", address.zipCode)
            put("isMainAddress", true) // Set as main address by default
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        val request = Request.Builder()
            .url("$BASE_URL/api/addresses/create")
            .post(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to create address", e)
                callback(null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val newAddressId = jsonResponse.optInt("id", 0)
                        if (newAddressId > 0) {
                            callback(newAddressId, null)
                        } else {
                            callback(null, "Failed to get new address ID from response")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing create address response", e)
                        callback(null, "Error parsing response: ${e.message}")
                    }
                } else {
                    Log.e(TAG, "Error creating address: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    
                    // Try to extract error message from response if available
                    val errorMessage = try {
                        val errorJson = JSONObject(responseBody ?: "{}")
                        errorJson.optString("message", "Failed to create address: ${response.code}")
                    } catch (e: Exception) {
                        "Failed to create address: ${response.code}"
                    }
                    
                    callback(null, errorMessage)
                }
            }
        })
    }
    
    // Update an address
    fun updateAddress(addressId: Int, address: com.example.serbisyo_it342_g3.model.Address, token: String, callback: (Boolean, String?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(false, "Authentication token is required")
            return
        }

        Log.d(TAG, "Updating address: $addressId")
        
        val jsonObject = JSONObject().apply {
            put("street", address.street)
            put("barangay", address.barangay)
            put("city", address.city)
            put("province", address.province)
            put("postalCode", address.zipCode)
        }
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        val request = Request.Builder()
            .url("$BASE_URL/api/addresses/update/$addressId")
            .put(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to update address", e)
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Update address response: $responseBody")
                    callback(true, null)
                } else {
                    Log.e(TAG, "Error updating address: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")
                    
                    // Try to extract error message from response if available
                    val errorMessage = try {
                        val errorJson = JSONObject(responseBody ?: "{}")
                        errorJson.optString("message", "Failed to update address: ${response.code}")
                    } catch (e: Exception) {
                        "Failed to update address: ${response.code}"
                    }
                    
                    callback(false, errorMessage)
                }
            }
        })
    }
} 