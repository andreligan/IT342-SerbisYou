package com.example.serbisyo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Second_WalkThrough : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second_walk_through)

        // Find both buttons
        val getStartedButton = findViewById<Button>(R.id.skipButton)

        // Set click listener for Get Started button
        getStartedButton.setOnClickListener {
            val intent = Intent(this, RegisterPage::class.java)
            startActivity(intent)
            finish()
        }
    }
}