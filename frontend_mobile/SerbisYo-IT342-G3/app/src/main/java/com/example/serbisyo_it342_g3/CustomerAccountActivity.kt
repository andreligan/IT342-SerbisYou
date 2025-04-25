package com.example.serbisyo_it342_g3

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.serbisyo_it342_g3.fragments.AddressFragment
import com.example.serbisyo_it342_g3.fragments.BookingHistoryFragment
import com.example.serbisyo_it342_g3.fragments.ChangePasswordFragment
import com.example.serbisyo_it342_g3.fragments.ProfileFragment

class CustomerAccountActivity : AppCompatActivity() {
    private lateinit var menuProfile: CardView
    private lateinit var menuAddress: CardView
    private lateinit var menuBookingHistory: CardView
    private lateinit var menuChangePassword: CardView
    
    private lateinit var txtProfile: TextView
    private lateinit var txtAddress: TextView
    private lateinit var txtBookingHistory: TextView
    private lateinit var txtChangePassword: TextView
    
    private lateinit var sharedPreferences: SharedPreferences
    private var userId: Long = 0
    private var token: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_account)
        
        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "My Account"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Get user data from SharedPreferences
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        token = sharedPreferences.getString("token", "") ?: ""
        
        // Fix userId retrieval using try-catch
        userId = try {
            // Try to get as Long first (new format)
            sharedPreferences.getLong("userId", 0)
        } catch (e: ClassCastException) {
            // If that fails, try the String format (old format) and convert
            val userIdStr = sharedPreferences.getString("userId", "0")
            userIdStr?.toLongOrNull() ?: 0
        }
        
        // Initialize views
        menuProfile = findViewById(R.id.menuProfile)
        menuAddress = findViewById(R.id.menuAddress)
        menuBookingHistory = findViewById(R.id.menuBookingHistory)
        menuChangePassword = findViewById(R.id.menuChangePassword)
        
        txtProfile = findViewById(R.id.txtProfile)
        txtAddress = findViewById(R.id.txtAddress)
        txtBookingHistory = findViewById(R.id.txtBookingHistory)
        txtChangePassword = findViewById(R.id.txtChangePassword)
        
        // Set click listeners for menu items
        menuProfile.setOnClickListener { loadFragment(ProfileFragment(), "profile") }
        menuAddress.setOnClickListener { loadFragment(AddressFragment(), "address") }
        menuBookingHistory.setOnClickListener { loadFragment(BookingHistoryFragment(), "booking") }
        menuChangePassword.setOnClickListener { loadFragment(ChangePasswordFragment(), "password") }
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(ProfileFragment(), "profile")
        }
    }
    
    private fun loadFragment(fragment: Fragment, tag: String) {
        // Reset all menu item styles
        resetMenuStyles()
        
        // Highlight selected menu item
        when (tag) {
            "profile" -> highlightMenuItem(txtProfile, menuProfile)
            "address" -> highlightMenuItem(txtAddress, menuAddress)
            "booking" -> highlightMenuItem(txtBookingHistory, menuBookingHistory)
            "password" -> highlightMenuItem(txtChangePassword, menuChangePassword)
        }
        
        // Load fragment
        val bundle = Bundle()
        bundle.putLong("userId", userId)
        bundle.putString("token", token)
        fragment.arguments = bundle
        
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
    
    private fun resetMenuStyles() {
        // Reset text colors
        txtProfile.setTextColor(ContextCompat.getColor(this, R.color.black))
        txtAddress.setTextColor(ContextCompat.getColor(this, R.color.black))
        txtBookingHistory.setTextColor(ContextCompat.getColor(this, R.color.black))
        txtChangePassword.setTextColor(ContextCompat.getColor(this, R.color.black))
        
        // Reset background colors
        menuProfile.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        menuAddress.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        menuBookingHistory.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
        menuChangePassword.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
    }
    
    private fun highlightMenuItem(textView: TextView, cardView: CardView) {
        textView.setTextColor(ContextCompat.getColor(this, R.color.primary_green))
        cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.light_green))
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 