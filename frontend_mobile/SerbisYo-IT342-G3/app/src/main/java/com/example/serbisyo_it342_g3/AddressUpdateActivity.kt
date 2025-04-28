package com.example.serbisyo_it342_g3

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * This activity is deprecated. Address updates are now handled directly in the 
 * AddressFragment and ServiceProviderAddressFragment using in-place editing.
 */
class AddressUpdateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inform the user that this activity is no longer used
        Toast.makeText(this, 
            "Address updates are now handled directly in the address list. This page is no longer used.", 
            Toast.LENGTH_LONG).show()
        
        // Close this activity and return to previous screen
        finish()
    }
}