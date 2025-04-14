package com.example.serbisyo_it342_g3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class RoleSelection : AppCompatActivity() {
    private lateinit var btnCustomer: Button
    private lateinit var btnServiceProvider: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        btnCustomer = findViewById(R.id.btnCustomer)
        btnServiceProvider = findViewById(R.id.btnServiceProvider)

        btnCustomer.setOnClickListener {
            val intent = Intent(this, CustomerProfileActivity::class.java)
            intent.putExtra("ROLE", "CUSTOMER")
            startActivity(intent)
        }

        btnServiceProvider.setOnClickListener {
            val intent = Intent(this, ServiceProviderProfileActivity::class.java)
            intent.putExtra("ROLE", "SERVICE_PROVIDER")
            startActivity(intent)
        }
    }
}