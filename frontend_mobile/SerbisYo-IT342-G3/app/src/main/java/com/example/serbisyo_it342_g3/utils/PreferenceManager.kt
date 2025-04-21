package com.example.serbisyo_it342_g3.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class to manage SharedPreferences operations
 */
class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "serbisyo_prefs", Context.MODE_PRIVATE
    )
    
    /**
     * Store a string value in SharedPreferences
     */
    fun setString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    
    /**
     * Get a string value from SharedPreferences
     */
    fun getString(key: String, defaultValue: String): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
    
    /**
     * Store an integer value in SharedPreferences
     */
    fun setInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    
    /**
     * Get an integer value from SharedPreferences
     */
    fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
    
    /**
     * Store a boolean value in SharedPreferences
     */
    fun setBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    /**
     * Get a boolean value from SharedPreferences
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    
    /**
     * Clear all SharedPreferences data
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
    
    /**
     * Remove a specific key from SharedPreferences
     */
    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
} 