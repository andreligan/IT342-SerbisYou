package com.example.serbisyo_it342_g3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CustomerProfileActivity : AppCompatActivity() {
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etStreet: EditText
    private lateinit var etCity: EditText
    private lateinit var etProvince: EditText
    private lateinit var etPostalCode: EditText
    private lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_profile)

        // Initialize views
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etStreet = findViewById(R.id.etStreet)
        etCity = findViewById(R.id.etCity)
        etProvince = findViewById(R.id.etProvince)
        etPostalCode = findViewById(R.id.etPostalCode)
        btnNext = findViewById(R.id.btnNext)

        // Get role from intent
        val role = intent.getStringExtra("ROLE") ?: "CUSTOMER"

        btnNext.setOnClickListener {
            if (validateInputs()) {
                // Pass data to registration screen
                val intent = Intent(this, RegisterActivity::class.java)
                intent.putExtra("ROLE", role)
                intent.putExtra("FIRST_NAME", etFirstName.text.toString())
                intent.putExtra("LAST_NAME", etLastName.text.toString())
                intent.putExtra("PHONE_NUMBER", etPhoneNumber.text.toString())
                intent.putExtra("STREET", etStreet.text.toString())
                intent.putExtra("CITY", etCity.text.toString())
                intent.putExtra("PROVINCE", etProvince.text.toString())
                intent.putExtra("POSTAL_CODE", etPostalCode.text.toString())
                startActivity(intent)
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (etFirstName.text.toString().isEmpty() ||
            etLastName.text.toString().isEmpty() ||
            etPhoneNumber.text.toString().isEmpty() ||
            etStreet.text.toString().isEmpty() ||
            etCity.text.toString().isEmpty() ||
            etProvince.text.toString().isEmpty() ||
            etPostalCode.text.toString().isEmpty()) {

            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}