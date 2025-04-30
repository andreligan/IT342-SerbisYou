package com.example.serbisyo_it342_g3.api

import android.content.Context
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import java.security.cert.X509Certificate

class PaymentApiClient(private val context: Context) {
    private val baseApiClient = BaseApiClient(context)
    private val client: OkHttpClient
    private val directClient: OkHttpClient
    private val gson = baseApiClient.gson
    private val PAYMONGO_SECRET_KEY = "sk_test_urq2YA4xDb8zU5LbUYmcjrUA" // Replace with your actual test key if different

    private val TAG = "PaymentApiClient"

    // Create a trust manager that does not validate certificate chains for direct API access
    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })
    
    init {
        // Create a standard client
        client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
            
        // Create a trust all client for direct API access
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        
        directClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    /**
     * Creates a GCash checkout session with PayMongo
     */
    fun createGCashCheckout(
        amount: Double,
        description: String,
        token: String,
        callback: (String?, Exception?) -> Unit
    ) {
        if (token.isBlank()) {
            Log.e(TAG, "Token is empty or blank")
            callback(null, Exception("Authentication token is required"))
            return
        }

        // Check for network availability
        if (!baseApiClient.isNetworkAvailable()) {
            Log.e(TAG, "No network connection available")
            callback(null, Exception("No internet connection. Please check your network settings."))
            return
        }

        // Try to connect directly to Paymongo instead of using the server
        Thread {
            try {
                // Get Paymongo's IP address
                val paymongoAddresses = InetAddress.getAllByName("api.paymongo.com")
                Log.d(TAG, "Pre-resolution of api.paymongo.com successful: ${paymongoAddresses.map { it.hostAddress }}")
                
                if (paymongoAddresses.isNotEmpty()) {
                    try {
                        // Direct connection to Paymongo
                        val directResponse = createDirectPaymongoSource(amount, description)
                        callback(directResponse, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Direct Paymongo connection failed, trying server as fallback", e)
                        // Fallback to server
                        continueWithPaymentRequest(amount, description, token, callback)
                    }
                } else {
                    // Fallback to server
                    continueWithPaymentRequest(amount, description, token, callback)
                }
            } catch (e: UnknownHostException) {
                Log.e(TAG, "Unable to resolve api.paymongo.com in pre-check", e)
                // Fallback to server
                continueWithPaymentRequest(amount, description, token, callback)
            }
        }.start()
    }

    private fun createDirectPaymongoSource(amount: Double, description: String): String {
        val amountInCents = (amount * 100).toInt()
        
        // Create the source request for GCash
        val sourceJsonObject = JSONObject().apply {
            put("data", JSONObject().apply {
                put("attributes", JSONObject().apply {
                    put("amount", amountInCents)
                    put("redirect", JSONObject().apply {
                        put("success", "https://serbisyo.vercel.app/payment-success")
                        put("failed", "https://serbisyo.vercel.app/payment-cancel")
                    })
                    put("type", "gcash")
                    put("currency", "PHP")
                })
            })
        }
        
        Log.d(TAG, "Creating direct Paymongo source for amount: $amount ($amountInCents cents)")
        
        val requestBody = sourceJsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        
        // Paymongo uses Basic auth with API keys
        val credentials = okhttp3.Credentials.basic(PAYMONGO_SECRET_KEY, "")
        
        val request = Request.Builder()
            .url("https://api.paymongo.com/v1/sources")
            .post(requestBody)
            .header("Authorization", credentials)
            .header("Content-Type", "application/json")
            .build()
        
        Log.d(TAG, "Making direct request to Paymongo API")
        
        val response = directClient.newCall(request).execute()
        val responseBody = response.body?.string()
        
        Log.d(TAG, "Direct Paymongo Response code: ${response.code}")
        Log.d(TAG, "Direct Paymongo Response body: $responseBody")
        
        if (!response.isSuccessful) {
            throw IOException("Direct Paymongo API call failed with code ${response.code}: $responseBody")
        }
        
        val jsonResponse = JSONObject(responseBody!!)
        val checkoutUrl = jsonResponse.getJSONObject("data").getJSONObject("attributes").getString("redirect").toString()
        
        Log.d(TAG, "Successfully obtained direct checkout URL: $checkoutUrl")
        return checkoutUrl
    }
    
    private fun continueWithPaymentRequest(
        amount: Double,
        description: String,
        token: String,
        callback: (String?, Exception?) -> Unit
    ) {
        Log.d(TAG, "Falling back to server payment request for amount: $amount, description: $description")

        // Create request JSON with URLs that match the backend's expectations
        val jsonObject = JSONObject().apply {
            put("amount", amount)
            put("description", description)
            
            // Use the URLs that are configured in the backend
            put("successUrl", "https://serbisyo.vercel.app/payment-success")
            put("cancelUrl", "https://serbisyo.vercel.app/payment-cancel")
            
            // Log the URLs being used
            Log.d(TAG, "Success URL: https://serbisyo.vercel.app/payment-success")
            Log.d(TAG, "Cancel URL: https://serbisyo.vercel.app/payment-cancel")
        }

        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("${baseApiClient.getBaseUrl()}/api/create-gcash-checkout")
            .post(requestBody)
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()

        // Log the request URL for debugging
        Log.d(TAG, "Making request to: ${request.url}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to create GCash checkout", e)
                
                // Provide a more specific error message based on the exception type
                val errorMessage = when (e) {
                    is SocketTimeoutException -> "Connection timed out. The server might be slow or unavailable. Please try again later."
                    is UnknownHostException -> "Could not resolve host. Please check your internet connection."
                    else -> "Failed to connect to payment server: ${e.message}"
                }
                
                callback(null, Exception(errorMessage))
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Response code: ${response.code}")
                Log.d(TAG, "Response body: $responseBody")

                if (response.isSuccessful) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val checkoutUrl = jsonResponse.getString("checkout_url")
                        Log.d(TAG, "Successfully obtained checkout URL: $checkoutUrl")
                        callback(checkoutUrl, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing checkout URL", e)
                        callback(null, Exception("Error parsing payment response: ${e.message}"))
                    }
                } else {
                    Log.e(TAG, "Error creating GCash checkout: ${response.code}")
                    Log.e(TAG, "Error response body: $responseBody")

                    // Handle server-side DNS resolution failures
                    if (responseBody?.contains("Failed to resolve 'api.paymongo.com'") == true) {
                        callback(null, Exception("Payment service temporarily unavailable. Please try again later."))
                    } else {
                    val errorMessage = try {
                        val errorJson = JSONObject(responseBody ?: "{}")
                        errorJson.optString("error", "Failed to create GCash checkout: ${response.code}")
                    } catch (e: Exception) {
                        "Failed to create GCash checkout: ${response.code}"
                    }

                    callback(null, Exception(errorMessage))
                    }
                }
            }
        })
    }
} 