package com.example.serbisyo_it342_g3.utils

object Constants {
    // CONFIGURATION FOR BACKEND CONNECTION
    // For Android Emulator - Virtual Device (default)
    private const val EMULATOR_URL = "http://10.0.2.2:8080/api/" 
    
    // For Physical Device - Use your computer's actual IP address from ipconfig
    private const val PHYSICAL_DEVICE_URL = "http://192.168.200.136:8080/api/"
    
    // Deployed public backend URL
    private const val PRODUCTION_URL = "https://serbisyo-backend.onrender.com/api/"
    
    // SWITCH BETWEEN CONNECTION TYPES:
    // Uncomment the one you need and comment out the others
    // const val BASE_URL = EMULATOR_URL     // For Android Emulator
    //================================================================
    //Comment lng nya ni if ma deploy na
    // const val BASE_URL = PHYSICAL_DEVICE_URL // For Physical Device

    //=============================================
    //Uncomment lng nya ni if ma deploy na
    const val BASE_URL = PRODUCTION_URL // For Production Deployment
    
    const val PREF_TOKEN = "user_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_USERNAME = "username"
    const val PREF_ROLE = "user_role"
}