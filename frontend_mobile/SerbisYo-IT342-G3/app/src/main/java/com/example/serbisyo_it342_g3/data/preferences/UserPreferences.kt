package com.example.serbisyo_it342_g3.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.serbisyo_it342_g3.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences private constructor(private val context: Context) {

    private val tokenKey = stringPreferencesKey(Constants.PREF_TOKEN)
    private val userIdKey = longPreferencesKey(Constants.PREF_USER_ID)
    private val usernameKey = stringPreferencesKey(Constants.PREF_USERNAME)
    private val roleKey = stringPreferencesKey(Constants.PREF_ROLE)

    fun getToken(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[tokenKey] ?: ""
    }

    fun getUserId(): Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[userIdKey] ?: -1L
    }

    fun getUsername(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[usernameKey] ?: ""
    }

    fun getRole(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[roleKey] ?: ""
    }

    suspend fun saveUserSession(token: String, userId: Long, username: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[tokenKey] = token
            preferences[userIdKey] = userId
            preferences[usernameKey] = username
            preferences[roleKey] = role
        }
    }

    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreferences? = null

        fun getInstance(context: Context = applicationContext): UserPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferences(context).also { INSTANCE = it }
            }
        }

        private lateinit var applicationContext: Context

        fun init(context: Context) {
            applicationContext = context.applicationContext
        }
    }
}