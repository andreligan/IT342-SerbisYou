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
    private val baseApiClient = BaseApiClient(context)
    private val client = baseApiClient.client
    private val gson = baseApiClient.gson
    
    private val TAG = "AddressApiClient"

    // Get addresses by user ID
    fun getAddressesByUserId(userId: Long, token: String, callback: (List<Address>?, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Getting addresses for user: $userId")
        
        // Changed to use the getAll endpoint which exists in the backend
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/addresses/getAll")
            .get()
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
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
                        val allAddresses = gson.fromJson<List<Address>>(responseBody, type)
                        
                        if (allAddresses.isEmpty()) {
                            callback(emptyList(), null)
                            return
                        }
                        
                        // Get all addresses that match the user ID
                        // 1. Filter addresses that have customer field populated with our user ID
                        // 2. If none found, this user might not have addresses yet, so return empty list
                        val customerAddresses = allAddresses.filter { address -> 
                            // Consider an address to be for this customer if:
                            // 1. It has a customer object with matching customerId
                            val matchingCustomer = address.customer?.customerId == userId
                            
                            // Log address details for debugging
                            Log.d(TAG, "Checking address ${address.addressId}: customerId=${address.customer?.customerId}, province=${address.province}, city=${address.city}")
                            
                            // Return true if any condition matches
                            matchingCustomer
                        }
                        
                        // Log found addresses
                        Log.d(TAG, "Found ${customerAddresses.size} addresses for user $userId")
                        customerAddresses.forEach { address ->
                            Log.d(TAG, "Matched address: ${address.addressId}, Province: ${address.province}, City: ${address.city}")
                        }
                        
                        callback(customerAddresses, null)
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
        
        // Get ZIP code value, ensuring it's not null
        val zipCodeValue = when {
            !address.postalCode.isNullOrBlank() -> address.postalCode
            !address.zipCode.isNullOrBlank() -> address.zipCode
            else -> ""
        }
        
        // Log detailed information about the address being added
        Log.d(TAG, "Address details: street=${address.street}, barangay=${address.barangay}, " +
                "city=${address.city}, province=${address.province}, " +
                "postalCode=${address.postalCode}, zipCode=${address.zipCode}, " +
                "using zipCodeValue=$zipCodeValue")
        
        // Modified to match the structure used in the web version
        val jsonObject = JSONObject().apply {
            put("streetName", address.street) // Using the field name expected by the entity
            put("barangay", address.barangay)
            put("city", address.city)
            put("province", address.province)
            put("zipCode", zipCodeValue) // Using the field name expected by the entity with determined value
            put("main", false) // Set as non-main address by default
            
            // Create a customer object to properly associate with the address
            put("customer", JSONObject().apply {
                put("customerId", userId)
            })
            
            put("serviceProvider", JSONObject.NULL) // Set service provider to null for customer addresses
        }
        
        // Log request for debugging
        Log.d(TAG, "Request body: ${jsonObject.toString()}")
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        // Use the correct endpoint path that exists in the backend
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/addresses/postAddress")
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
        
        // Fixed URL to match the backend endpoint pattern
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/addresses/delete/$addressId")
            .delete()
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
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
        
        // Fixed URL to match the backend endpoint pattern
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/addresses/$addressId")
            .get()
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
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
    fun createAddress(address: com.example.serbisyo_it342_g3.model.Address, customerId: Long, token: String, callback: (Int?, String?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, "Authentication token is required")
            return
        }

        Log.d(TAG, "Creating new address for customer: $customerId")
        
        val jsonObject = JSONObject().apply {
            put("streetName", address.street)  // Fixed: Using camelCase instead of snake_case
            put("barangay", address.barangay)
            put("city", address.city)
            put("province", address.province)
            put("zipCode", address.zipCode)    // Fixed: Using camelCase instead of snake_case
            put("main", true)                  // Changed field name to match entity
            put("customer", JSONObject().apply {
                put("customerId", customerId)  // Properly structure the customer relationship
            })
            put("serviceProvider", JSONObject.NULL) // Set serviceProvider to null for customer addresses
        }
        
        // Log request body to debug
        Log.d(TAG, "Request body: ${jsonObject.toString()}")
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        // Fixing URL to match the backend endpoint pattern
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/addresses/postAddress")
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
                        // Parse the response to get the new address ID
                        val dataAddress = gson.fromJson(responseBody, com.example.serbisyo_it342_g3.data.Address::class.java)
                        val newAddressId = dataAddress.addressId?.toInt() ?: 0
                        
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
    
    // Update address (for data.Address compatibility with AddressFragment)
    fun updateAddress(address: Address, token: String, callback: (Boolean, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(false, Exception("Authentication token is required"))
            return
        }

        val addressId = address.addressId?.toInt() ?: 0
        if (addressId <= 0) {
            Log.e(TAG, "Invalid address ID: $addressId")
            callback(false, Exception("Invalid address ID"))
            return
        }

        Log.d(TAG, "Updating address: $addressId")
        
        // Get ZIP code value, ensuring it's not null
        val zipCodeValue = when {
            !address.postalCode.isNullOrBlank() -> address.postalCode
            !address.zipCode.isNullOrBlank() -> address.zipCode
            else -> ""
        }
        
        // Log detailed information about the address being updated
        Log.d(TAG, "Address details: addressId=$addressId, street=${address.street}, barangay=${address.barangay}, " +
                "city=${address.city}, province=${address.province}, " +
                "postalCode=${address.postalCode}, zipCode=${address.zipCode}, " +
                "using zipCodeValue=$zipCodeValue")
        
        val jsonObject = JSONObject().apply {
            put("addressId", addressId)
            put("streetName", address.street)
            put("barangay", address.barangay)
            put("city", address.city)
            put("province", address.province)
            put("zipCode", zipCodeValue)
            put("main", address.main) // Preserve main status
            
            // Preserve customer association
            if (address.customer != null) {
                put("customer", JSONObject().apply {
                    put("customerId", address.customer.customerId)
                })
            }
        }
        
        // Log request for debugging
        Log.d(TAG, "Request body: ${jsonObject.toString()}")
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/addresses/updateAddress/$addressId")
            .put(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to update address", e)
                callback(false, e)
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
                    val errorMessage = when (response.code) {
                        403 -> "Permission denied: You don't have access to update this address"
                        401 -> "Authentication failed: Please log in again"
                        404 -> "Address not found"
                        400 -> try {
                            val errorJson = JSONObject(responseBody ?: "{}")
                            errorJson.optString("message", "Invalid address data")
                        } catch (e: Exception) {
                            "Invalid address data"
                        }
                        else -> try {
                            val errorJson = JSONObject(responseBody ?: "{}")
                            errorJson.optString("message", "Failed to update address: ${response.code}")
                        } catch (e: Exception) {
                            "Failed to update address: ${response.code}"
                        }
                    }
                    
                    callback(false, Exception(errorMessage))
                }
            }
        })
    }
    
    // Update an address main status
    fun updateAddressMainStatus(addressId: Int, isMain: Boolean, token: String, callback: (Boolean, String?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(false, "Authentication token is required")
            return
        }

        Log.d(TAG, "Updating address $addressId main status to $isMain")
        
        // First get the address to update
        val getRequest = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/addresses/getAll")
            .get()
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()
            
        client.newCall(getRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to get address for main update", e)
                callback(false, e.message)
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                
                if (!response.isSuccessful || responseBody == null) {
                    Log.e(TAG, "Error getting addresses: ${response.code}")
                    callback(false, "Failed to get address data: ${response.code}")
                    return
                }
                
                try {
                    val type = object : TypeToken<List<Address>>() {}.type
                    val addresses = gson.fromJson<List<Address>>(responseBody, type)
                    
                    // Find the address to update
                    val addressToUpdate = addresses.find { it.addressId?.toInt() == addressId }
                    
                    if (addressToUpdate == null) {
                        callback(false, "Address not found")
                        return
                    }
                    
                    Log.d(TAG, "Found address to update: ${addressToUpdate.addressId}, zipCode: ${addressToUpdate.zipCode}, postalCode: ${addressToUpdate.postalCode}")
                    
                    // Create an updated address with main=true
                    val jsonObject = JSONObject().apply {
                        // Preserve all existing fields
                        put("streetName", when {
                            !addressToUpdate.streetName.isNullOrEmpty() -> addressToUpdate.streetName
                            else -> addressToUpdate.street
                        })
                        put("barangay", addressToUpdate.barangay ?: "")
                        put("city", addressToUpdate.city)
                        put("province", addressToUpdate.province)
                        put("zipCode", when {
                            !addressToUpdate.zipCode.isNullOrEmpty() -> addressToUpdate.zipCode
                            !addressToUpdate.postalCode.isNullOrEmpty() -> addressToUpdate.postalCode
                            else -> ""
                        })
                        put("main", isMain)
                        
                        // Preserve the relationship with customer
                        addressToUpdate.customer?.let { customer ->
                            put("customer", JSONObject().apply {
                                put("customerId", customer.customerId)
                            })
                        }
                        
                        // Set serviceProvider to null for customer addresses
                        put("serviceProvider", JSONObject.NULL)
                    }
                    
                    // Log for debugging
                    Log.d(TAG, "Updating address main status with payload: $jsonObject")
                    
                    // First, set all other addresses to non-main if setting this as main
                    if (isMain) {
                        updateAllOtherAddressesToNonMain(addresses, addressId, token) { success, error ->
                            if (!success) {
                                Log.e(TAG, "Failed to update other addresses to non-main: $error")
                                // Continue anyway to update this address
                            }
                            
                            // Now update this address
                            updateThisAddressMainStatus(addressId, jsonObject, token, callback)
                        }
                    } else {
                        // Just update this address
                        updateThisAddressMainStatus(addressId, jsonObject, token, callback)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing addresses for main update", e)
                    callback(false, "Error processing addresses: ${e.message}")
                }
            }
        })
    }
    
    private fun updateThisAddressMainStatus(addressId: Int, jsonObject: JSONObject, token: String, callback: (Boolean, String?) -> Unit) {
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        val updateRequest = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/addresses/updateAddress/$addressId")
            .put(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()
            
        client.newCall(updateRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to update address main status", e)
                callback(false, e.message)
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")
                
                if (response.isSuccessful) {
                    Log.d(TAG, "Updated address main status successfully")
                    callback(true, null)
                } else {
                    Log.e(TAG, "Error updating address main status: ${response.code}")
                    callback(false, "Failed to update address: ${response.code}")
                }
            }
        })
    }
    
    private fun updateAllOtherAddressesToNonMain(addresses: List<Address>, currentAddressId: Int, token: String, callback: (Boolean, String?) -> Unit) {
        // Get all other addresses that are currently marked as main
        val otherMainAddresses = addresses.filter { 
            it.main && it.addressId?.toInt() != currentAddressId 
        }
        
        if (otherMainAddresses.isEmpty()) {
            // No other main addresses to update
            callback(true, null)
            return
        }
        
        var successCount = 0
        var failCount = 0
        val totalToUpdate = otherMainAddresses.size
        
        for (address in otherMainAddresses) {
            val addressId = address.addressId?.toInt() ?: continue
            
            val jsonObject = JSONObject().apply {
                // Preserve all existing fields
                put("streetName", when {
                    !address.streetName.isNullOrEmpty() -> address.streetName
                    else -> address.street
                })
                put("barangay", address.barangay ?: "")
                put("city", address.city)
                put("province", address.province)
                put("zipCode", when {
                    !address.zipCode.isNullOrEmpty() -> address.zipCode
                    !address.postalCode.isNullOrEmpty() -> address.postalCode
                    else -> ""
                })
                put("main", false) // Set to not main
                
                // Preserve the relationship with customer
                address.customer?.let { customer ->
                    put("customer", JSONObject().apply {
                        put("customerId", customer.customerId)
                    })
                }
                
                // Set serviceProvider to null for customer addresses
                put("serviceProvider", JSONObject.NULL)
            }
            
            val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
            
            val updateRequest = Request.Builder()
                .url("${baseApiClient.getBaseUrl()}/api/addresses/updateAddress/$addressId")
                .put(requestBody)
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()
                
            client.newCall(updateRequest).execute().use { response ->
                if (response.isSuccessful) {
                    successCount++
                } else {
                    failCount++
                    Log.e(TAG, "Failed to update address ${addressId} to non-main: ${response.code}")
                }
                
                // Check if we've processed all addresses
                if (successCount + failCount == totalToUpdate) {
                    if (failCount > 0) {
                        Log.w(TAG, "Some addresses could not be updated: $failCount failures out of $totalToUpdate")
                    }
                    callback(true, null) // Consider the operation successful as long as the main address was updated
                }
            }
        }
    }
    
    // Add a new address for service provider
    fun addServiceProviderAddress(providerId: Long, address: Address, token: String, callback: (Boolean, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(false, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Adding address for service provider: $providerId")
        
        // Get ZIP code value, ensuring it's not null
        val zipCodeValue = when {
            !address.postalCode.isNullOrBlank() -> address.postalCode
            !address.zipCode.isNullOrBlank() -> address.zipCode
            else -> ""
        }
        
        // Log detailed information about the address being added
        Log.d(TAG, "Address details: street=${address.street}, barangay=${address.barangay}, " +
                "city=${address.city}, province=${address.province}, " +
                "postalCode=${address.postalCode}, zipCode=${address.zipCode}, " +
                "using zipCodeValue=$zipCodeValue")
        
        // Create payload matching backend expectations
        val jsonObject = JSONObject().apply {
            put("streetName", address.street) // Using the field name expected by the entity
            put("barangay", address.barangay)
            put("city", address.city)
            put("province", address.province)
            put("zipCode", zipCodeValue) // Using the field name expected by the entity with the determined value
            put("main", false) // Set as non-main address by default
            
            // Create a service provider object to properly associate with the address
            put("serviceProvider", JSONObject().apply {
                put("providerId", providerId)
            })
            
            put("customer", JSONObject.NULL) // Set customer to null for service provider addresses
        }
        
        // Log request for debugging
        Log.d(TAG, "Request body: ${jsonObject.toString()}")
        
        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        // Use the correct endpoint path that exists in the backend
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/addresses/postAddress")
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
                    callback(true, null)
                } else {
                    callback(false, Exception("Failed to add address: ${response.code}, ${responseBody ?: "No response body"}"))
                }
            }
        })
    }
    
    // Delete service provider address
    fun deleteServiceProviderAddress(addressId: Long, token: String, callback: (Boolean, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(false, Exception("Authentication token is required"))
            return
        }

        Log.d(TAG, "Deleting address: $addressId")
        
        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/addresses/delete/$addressId")
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
                    callback(true, null)
                } else {
                    Log.e(TAG, "Error deleting address: ${response.code}, ${responseBody ?: "No response body"}")
                    
                    // If we get a 404, try the alternate endpoint format
                    if (response.code == 404) {
                        Log.d(TAG, "Trying alternate endpoint format for delete")
                        tryAlternateDeleteEndpoint(addressId, token, callback)
                    } else {
                        callback(false, Exception("Failed to delete address: ${response.code}, ${responseBody ?: "No response body"}"))
                    }
                }
            }
        })
    }
    
    // Try alternate endpoint format for deleting an address
    private fun tryAlternateDeleteEndpoint(addressId: Long, token: String, callback: (Boolean, Exception?) -> Unit) {
        val alternateRequest = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/addresses/deleteAddress/$addressId")
            .delete()
            .header("Authorization", "Bearer $token")
            .build()
            
        client.newCall(alternateRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to delete address with alternate endpoint", e)
                callback(false, e)
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Alternate delete response code: ${response.code}")
                Log.d(TAG, "Alternate delete response body: $responseBody")
                
                if (response.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, Exception("Failed to delete address with alternate endpoint: ${response.code}, ${responseBody ?: "No response body"}"))
                }
            }
        })
    }
    
    // Set address as service provider's main address
    fun setServiceProviderMainAddress(providerId: Long, addressId: Long, token: String, callback: (Boolean, Exception?) -> Unit) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(false, Exception("Authentication token is required"))
            return
        }
        
        Log.d(TAG, "Setting address $addressId as main for provider $providerId")
        
        // First get the specific address to update
        val url = "${baseApiClient.getBaseUrl()}/api/addresses/getAll"
        
        val request = Request.Builder()
            .url(url)
            .get()
            .header("Authorization", "Bearer $token")
            .build()
            
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to fetch addresses", e)
                callback(false, e)
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                
                if (!response.isSuccessful || responseBody == null) {
                    Log.e(TAG, "Failed to fetch addresses: ${response.code}")
                    callback(false, Exception("Failed to fetch addresses: ${response.code}"))
                    return
                }
                
                try {
                    val type = object : TypeToken<List<Address>>() {}.type
                    val addresses = gson.fromJson<List<Address>>(responseBody, type)
                    
                    // Find the address to update
                    val addressToUpdate = addresses.find { it.addressId == addressId }
                    
                    if (addressToUpdate == null) {
                        Log.e(TAG, "Address not found with ID: $addressId")
                        callback(false, Exception("Address not found"))
                        return
                    }
                    
                    // Log the full address details for debugging
                    Log.d(TAG, "Found address to update: ID=${addressToUpdate.addressId}, streetName=${addressToUpdate.streetName}, street=${addressToUpdate.street}")
                    Log.d(TAG, "Address data: city=${addressToUpdate.city}, province=${addressToUpdate.province}")
                    Log.d(TAG, "ZIP code data: postalCode='${addressToUpdate.postalCode}', zipCode='${addressToUpdate.zipCode}'")
                    
                    // Get ZIP code, ensuring it's not null
                    val zipCodeValue = when {
                        !addressToUpdate.postalCode.isNullOrBlank() -> addressToUpdate.postalCode
                        !addressToUpdate.zipCode.isNullOrBlank() -> addressToUpdate.zipCode
                        else -> ""
                    }
                    
                    // Create updated address JSON with main=true and correct provider reference
                    val jsonObject = JSONObject().apply {
                        // Preserve all existing fields
                        put("streetName", when {
                            !addressToUpdate.streetName.isNullOrEmpty() -> addressToUpdate.streetName
                            else -> addressToUpdate.street
                        })
                        put("barangay", addressToUpdate.barangay ?: "")
                        put("city", addressToUpdate.city)
                        put("province", addressToUpdate.province)
                        put("zipCode", zipCodeValue)  // Use the determined ZIP code value
                        put("main", true) // Set as main address
                        
                        // Important: Include service provider relationship
                        put("serviceProvider", JSONObject().apply {
                            put("providerId", providerId)
                        })
                        
                        // Set customer to null
                        put("customer", JSONObject.NULL)
                    }
                    
                    // Log the request for debugging
                    Log.d(TAG, "Updating address with payload: $jsonObject")
                    
                    val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
                    
                    // Use the correct endpoint for updating addresses
                    val updateRequest = Request.Builder()
                        .url("${baseApiClient.getBaseUrl()}/api/addresses/updateAddress/${addressId}")
                        .put(requestBody)
                        .header("Authorization", "Bearer $token")
                        .header("Content-Type", "application/json")
                        .build()
                    
                    // Execute the update request
                    client.newCall(updateRequest).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.e(TAG, "Failed to update address as main", e)
                            callback(false, e)
                        }
                        
                        override fun onResponse(call: Call, response: Response) {
                            val updateResponseBody = response.body?.string()
                            Log.d(TAG, "Update response code: ${response.code}")
                            Log.d(TAG, "Update response body: $updateResponseBody")
                            
                            if (response.isSuccessful) {
                                // Now update all other addresses to non-main
                                updateOtherAddressesToNonMain(addresses, providerId, addressId, token, callback)
                            } else {
                                Log.e(TAG, "Failed to update address as main: ${response.code}")
                                callback(false, Exception("Failed to update address as main: ${response.code}"))
                            }
                        }
                    })
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing address data", e)
                    callback(false, e)
                }
            }
        })
    }
    
    // Update all other addresses to non-main
    private fun updateOtherAddressesToNonMain(addresses: List<Address>, providerId: Long, currentAddressId: Long, token: String, callback: (Boolean, Exception?) -> Unit) {
        // Get all other addresses that are currently marked as main
        val otherMainAddresses = addresses.filter { 
            it.main && it.addressId != currentAddressId 
        }
        
        if (otherMainAddresses.isEmpty()) {
            // No other main addresses to update
            Log.d(TAG, "No other main addresses to update")
            callback(true, null)
            return
        }
        
        Log.d(TAG, "Found ${otherMainAddresses.size} other main addresses to update")
        
        var successCount = 0
        var failCount = 0
        val totalToUpdate = otherMainAddresses.size
        
        for (address in otherMainAddresses) {
            val addressId = address.addressId ?: continue
            
            // Get ZIP code, ensuring it's not null
            val zipCodeValue = when {
                !address.postalCode.isNullOrBlank() -> address.postalCode
                !address.zipCode.isNullOrBlank() -> address.zipCode
                else -> ""
            }
            
            val jsonObject = JSONObject().apply {
                // Preserve all existing fields
                put("streetName", when {
                    !address.streetName.isNullOrEmpty() -> address.streetName
                    else -> address.street
                })
                put("barangay", address.barangay ?: "")
                put("city", address.city)
                put("province", address.province)
                put("zipCode", zipCodeValue)  // Use the determined ZIP code value
                put("main", false) // Set to not main
                
                // Include service provider relationship
                put("serviceProvider", JSONObject().apply {
                    put("providerId", providerId)
                })
                
                // Set customer to null
                put("customer", JSONObject.NULL)
            }
            
            val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
            
            // Use the correct endpoint for updating addresses
            val updateRequest = Request.Builder()
                .url("${baseApiClient.getBaseUrl()}/api/addresses/updateAddress/${addressId}")
                .put(requestBody)
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .build()
            
            client.newCall(updateRequest).execute().use { response ->
                if (response.isSuccessful) {
                    successCount++
                } else {
                    failCount++
                    Log.e(TAG, "Failed to update address ${addressId} to non-main: ${response.code}")
                }
                
                // Check if we've processed all addresses
                if (successCount + failCount == totalToUpdate) {
                    if (failCount > 0) {
                        Log.w(TAG, "Some addresses could not be updated: $failCount failures out of $totalToUpdate")
                    }
                    callback(true, null) // Consider the operation successful as long as the main address was updated
                }
            }
        }
    }
}