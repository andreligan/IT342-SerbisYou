package com.example.serbisyo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Welcome_Page : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_page)

        // Find the Next button
        val nextButton = findViewById<Button>(R.id.nextButton)

        // Set click listener for the button
        nextButton.setOnClickListener {
            // Navigate to First Walkthrough page
            val intent = Intent(this, First_WalkThrough::class.java)  // Changed to First_WalkThrough
            startActivity(intent)
            finish()
        }
    }
}