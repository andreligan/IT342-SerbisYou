package com.example.serbisyo_it342_g3.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class to provide static access to shared preferences
 */
object SharedPreferencesUtil {
    private const val PREFS_NAME = "serbisyo_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_EMAIL = "email"
    private const val KEY_PROFILE_IMAGE = "profile_image"

    /**
     * Get SharedPreferences instance
     */
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Save user details after login
     */
    fun saveUserDetails(
        context: Context, 
        userId: String, 
        token: String,
        userName: String,
        fullName: String = "",
        email: String = "",
        role: String = "",
        profileImage: String = ""
    ) {
        getPrefs(context).edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_TOKEN, token)
            putString(KEY_USER_NAME, userName)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_EMAIL, email)
            putString(KEY_USER_ROLE, role)
            putString(KEY_PROFILE_IMAGE, profileImage)
            apply()
        }
    }

    /**
     * Get user ID
     */
    fun getUserId(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_ID, null)
    }

    /**
     * Get authentication token
     */
    fun getToken(context: Context): String? {
        return getPrefs(context).getString(KEY_TOKEN, null)
    }

    /**
     * Get user role
     */
    fun getUserRole(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_ROLE, null)
    }

    /**
     * Get user name
     */
    fun getUserName(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_NAME, null)
    }

    /**
     * Get full name
     */
    fun getFullName(context: Context): String? {
        return getPrefs(context).getString(KEY_FULL_NAME, null)
    }

    /**
     * Get email
     */
    fun getEmail(context: Context): String? {
        return getPrefs(context).getString(KEY_EMAIL, null)
    }

    /**
     * Get profile image URL
     */
    fun getProfileImage(context: Context): String? {
        return getPrefs(context).getString(KEY_PROFILE_IMAGE, null)
    }

    /**
     * Clear all user data (logout)
     */
    fun clearUserData(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
} 