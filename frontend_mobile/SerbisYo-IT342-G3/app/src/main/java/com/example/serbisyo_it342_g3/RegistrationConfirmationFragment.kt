package com.example.serbisyo_it342_g3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class RegistrationConfirmationFragment : Fragment() {

    // UI Components
    private lateinit var tvAccountType: TextView
    private lateinit var tvName: TextView
    private lateinit var tvPhoneNumber: TextView
    private lateinit var tvBusinessInfo: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnBack: Button
    private lateinit var btnRegister: Button

    // Activity reference
    private lateinit var registrationActivity: MultiStepRegistrationActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registration_confirmation, container, false)

        // Get reference to the activity
        registrationActivity = activity as MultiStepRegistrationActivity

        // Initialize UI components
        initializeViews(view)
        populateData()
        setupListeners()

        return view
    }

    private fun initializeViews(view: View) {
        // TextViews
        tvAccountType = view.findViewById(R.id.tvAccountType)
        tvName = view.findViewById(R.id.tvName)
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber)
        tvBusinessInfo = view.findViewById(R.id.tvBusinessInfo)
        tvUsername = view.findViewById(R.id.tvUsername)
        tvEmail = view.findViewById(R.id.tvEmail)

        // Buttons
        btnBack = view.findViewById(R.id.btnBack)
        btnRegister = view.findViewById(R.id.btnRegister)
    }

    private fun populateData() {
        // Get user type
        val userType = registrationActivity.getUserType()
        
        // Set account type
        val accountTypeText = if (userType == "customer") "Customer" else "Service Provider"
        tvAccountType.text = accountTypeText

        // Set name
        val fullName = "${registrationActivity.getFirstName()} ${registrationActivity.getLastName()}"
        tvName.text = fullName

        // Set phone number
        tvPhoneNumber.text = registrationActivity.getPhoneNumber()
        
        // Set business info for service providers
        if (userType == "serviceProvider") {
            val businessName = registrationActivity.getBusinessName()
            val yearsExperience = registrationActivity.getYearsExperience()
            tvBusinessInfo.visibility = View.VISIBLE
            tvBusinessInfo.text = "Business: $businessName, Experience: $yearsExperience years"
        } else {
            tvBusinessInfo.visibility = View.GONE
        }

        // Set username and email
        tvUsername.text = registrationActivity.getUsername()
        tvEmail.text = registrationActivity.getEmail()
    }

    private fun setupListeners() {
        // Back button
        btnBack.setOnClickListener {
            registrationActivity.goToPreviousStep()
        }

        // Register button
        btnRegister.setOnClickListener {
            registrationActivity.completeRegistration()
        }
    }
} 