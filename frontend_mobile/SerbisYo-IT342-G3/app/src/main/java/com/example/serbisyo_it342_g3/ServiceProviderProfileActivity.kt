package com.example.serbisyo_it342_g3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ServiceProviderProfileActivity : AppCompatActivity() {
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etStreet: EditText
    private lateinit var etCity: EditText
    private lateinit var etProvince: EditText
    private lateinit var etPostalCode: EditText
    private lateinit var etBusinessName: EditText
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_provider_profile)

        // Initialize views
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etStreet = findViewById(R.id.etStreet)
        etCity = findViewById(R.id.etCity)
        etProvince = findViewById(R.id.etProvince)
        etPostalCode = findViewById(R.id.etPostalCode)
        etBusinessName = findViewById(R.id.etBusinessName)
        btnNext = findViewById(R.id.btnNext)

        // Get role from intent
        val role = intent.getStringExtra("ROLE") ?: "SERVICE_PROVIDER"

        btnNext.setOnClickListener {
            if (validateInputs()) {
                val intent = Intent(this, RegisterActivity::class.java)
                intent.putExtra("ROLE", role)
                intent.putExtra("FIRST_NAME", etFirstName.text.toString())
                intent.putExtra("LAST_NAME", etLastName.text.toString())
                intent.putExtra("PHONE_NUMBER", etPhoneNumber.text.toString())
                intent.putExtra("BUSINESS_NAME", etBusinessName.text.toString())
                
                // Address fields are optional now - only pass them if provided
                val street = etStreet.text.toString()
                val city = etCity.text.toString()
                val province = etProvince.text.toString()
                val postalCode = etPostalCode.text.toString()
                
                if (street.isNotEmpty()) intent.putExtra("STREET", street)
                if (city.isNotEmpty()) intent.putExtra("CITY", city)
                if (province.isNotEmpty()) intent.putExtra("PROVINCE", province)
                if (postalCode.isNotEmpty()) intent.putExtra("POSTAL_CODE", postalCode)
                
                startActivity(intent)
            }
        }
    }

    private fun validateInputs(): Boolean {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val businessName = etBusinessName.text.toString().trim()
        
        if (firstName.isEmpty()) {
            Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show()
            etFirstName.requestFocus()
            return false
        }
        
        if (lastName.isEmpty()) {
            Toast.makeText(this, "Please enter your last name", Toast.LENGTH_SHORT).show()
            etLastName.requestFocus()
            return false
        }
        
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show()
            etPhoneNumber.requestFocus()
            return false
        }
        
        if (businessName.isEmpty()) {
            Toast.makeText(this, "Please enter your business name", Toast.LENGTH_SHORT).show()
            etBusinessName.requestFocus()
            return false
        }
        
        // No longer validating address fields - they are optional
        
        return true
    }
}