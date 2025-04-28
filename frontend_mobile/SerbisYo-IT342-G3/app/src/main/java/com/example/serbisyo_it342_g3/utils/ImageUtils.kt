package com.example.serbisyo_it342_g3.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import com.example.serbisyo_it342_g3.R
import com.example.serbisyo_it342_g3.api.BaseApiClient
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

/**
 * Utility class for handling images and image URLs in the application
 */
object ImageUtils {
    private const val TAG = "ImageUtils"
    
    // Executor service for background tasks
    private val executor = Executors.newCachedThreadPool()
    
    // Handler for posting results back to the main thread
    private val handler = Handler(Looper.getMainLooper())
    
    /**
     * Normalizes an image path returned from the server to ensure it works correctly
     * with the app's base URL configuration.
     * 
     * @param imagePath The raw image path from the server
     * @param context Context for getting base URL
     * @return The full, normalized image URL
     */
    fun getFullImageUrl(imagePath: String?, context: Context): String? {
        if (imagePath.isNullOrBlank()) {
            Log.d(TAG, "Image path is null or blank, returning null")
            return null
        }
        
        Log.d(TAG, "Raw image path: $imagePath")
        
        // If it's already a full URL, return it as is
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            Log.d(TAG, "Image path is already a full URL, returning as is")
            return imagePath
        }
        
        val baseUrl = BaseApiClient(context).getBaseUrl()
        
        // Clean up the image path - handle different formats returned from server
        val cleanPath = imagePath.trim()
            .replace("\\", "/") // Replace backslashes with forward slashes
        
        // Ensure path starts with / for correct URL construction
        val normalizedPath = if (!cleanPath.startsWith("/")) "/$cleanPath" else cleanPath
        
        // Construct full image URL
        val imageUrl = "$baseUrl$normalizedPath"
        Log.d(TAG, "Normalized image URL: $imageUrl")
        
        return imageUrl
    }
    
    /**
     * Load an image from a URL into an ImageView asynchronously
     * 
     * @param url URL of the image to load
     * @param imageView ImageView to load the image into
     */
    fun loadImageAsync(url: String?, imageView: ImageView) {
        if (url.isNullOrBlank()) {
            imageView.setImageResource(R.drawable.ic_image_placeholder)
            return
        }
        
        // Set placeholder while loading
        imageView.setImageResource(R.drawable.ic_image_placeholder)
        
        // Load image in background
        executor.execute {
            try {
                val bitmap = loadBitmapFromUrl(url)
                
                // Post result back to the main thread
                handler.post {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                    } else {
                        imageView.setImageResource(R.drawable.ic_image_placeholder)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image from URL: $url", e)
                
                handler.post {
                    imageView.setImageResource(R.drawable.ic_image_placeholder)
                }
            }
        }
    }
    
    /**
     * Load a bitmap from a URL
     * 
     * @param urlString URL of the image to load
     * @return Bitmap or null if loading failed
     */
    private fun loadBitmapFromUrl(urlString: String): Bitmap? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(input)
            input.close()
            connection.disconnect()
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bitmap from URL: $urlString", e)
            null
        }
    }
    
    /**
     * Resize a bitmap to fit within the specified dimensions while maintaining aspect ratio
     */
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap // No need to resize
        }
        
        // Calculate target dimensions, keeping aspect ratio
        val widthRatio = maxWidth.toFloat() / width.toFloat()
        val heightRatio = maxHeight.toFloat() / height.toFloat()
        
        // Use the smaller ratio to ensure both width and height are within limits
        val ratio = minOf(widthRatio, heightRatio)
        
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Convert a bitmap to Base64 string for API uploads
     */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 85): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    
    /**
     * Convert Base64 string to bitmap
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting base64 to bitmap", e)
            null
        }
    }
} 