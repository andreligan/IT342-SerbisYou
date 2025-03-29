package com.example.serbisyo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Delay for 3 seconds and then start Welcome_Page activity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Welcome_Page::class.java) // Changed to Welcome_Page
            startActivity(intent)
            finish()
        }, 3000) // 3000 milliseconds = 3 seconds
    }
}