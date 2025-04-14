package com.example.serbisyo_it342_g3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.serbisyo_it342_g3.data.preferences.UserPreferences
import com.example.serbisyo_it342_g3.LoginActivity
import com.example.serbisyo_it342_g3.CustomerDashboardActivity
import com.example.serbisyo_it342_g3.ServiceProviderDashboardActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UserPreferences
        UserPreferences.init(applicationContext)

        // Splash screen delay
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, 2000)
    }

    private fun checkLoginStatus() {
        CoroutineScope(Dispatchers.Main).launch {
            val token = UserPreferences.getInstance().getToken().first()
            val role = UserPreferences.getInstance().getRole().first()

            if (token.isNotEmpty()) {
                // User is logged in, navigate to appropriate dashboard
                when (role) {
                    "CUSTOMER" -> startActivity(Intent(this@MainActivity, CustomerDashboardActivity::class.java))
                    "SERVICE_PROVIDER" -> startActivity(Intent(this@MainActivity, ServiceProviderDashboardActivity::class.java))
                    else -> startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                }
            } else {
                // User is not logged in, navigate to login screen
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            }
            finish()
        }
    }
}