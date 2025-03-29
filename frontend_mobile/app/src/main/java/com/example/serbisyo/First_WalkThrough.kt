package com.example.serbisyo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class First_WalkThrough : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_walk_through)

        // Find the Next button
        val nextButton = findViewById<Button>(R.id.nextButton)

        // Set click listener for the button
        nextButton.setOnClickListener {
            // Navigate to Second Walkthrough
            val intent = Intent(this, Second_WalkThrough::class.java)
            startActivity(intent)
            finish() // Optional: closes the first walkthrough so user can't go back
        }
    }
}