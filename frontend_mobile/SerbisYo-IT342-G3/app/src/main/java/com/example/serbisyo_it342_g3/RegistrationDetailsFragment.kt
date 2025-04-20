package com.example.serbisyo_it342_g3

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegistrationDetailsFragment : Fragment() {

    // UI Components
    private lateinit var tilFirstName: TextInputLayout
    private lateinit var tilLastName: TextInputLayout
    private lateinit var tilPhoneNumber: TextInputLayout
    private lateinit var tilBusinessName: TextInputLayout
    private lateinit var tilYearsExperience: TextInputLayout
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etPhoneNumber: TextInputEditText
    private lateinit var etBusinessName: TextInputEditText
    private lateinit var etYearsExperience: TextInputEditText
    private lateinit var btnBack: Button
    private lateinit var btnNext: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registration_details, container, false)

        // Initialize UI components
        initializeViews(view)
        setupServiceProviderFields()
        setupListeners()

        return view
    }

    private fun initializeViews(view: View) {
        // TextInputLayouts
        tilFirstName = view.findViewById(R.id.tilFirstName)
        tilLastName = view.findViewById(R.id.tilLastName)
        tilPhoneNumber = view.findViewById(R.id.tilPhoneNumber)
        tilBusinessName = view.findViewById(R.id.tilBusinessName)
        tilYearsExperience = view.findViewById(R.id.tilYearsExperience)

        // EditTexts
        etFirstName = view.findViewById(R.id.etFirstName)
        etLastName = view.findViewById(R.id.etLastName)
        etPhoneNumber = view.findViewById(R.id.etPhoneNumber)
        etBusinessName = view.findViewById(R.id.etBusinessName)
        etYearsExperience = view.findViewById(R.id.etYearsExperience)

        // Buttons
        btnBack = view.findViewById(R.id.btnBack)
        btnNext = view.findViewById(R.id.btnNext)
    }
    
    private fun setupServiceProviderFields() {
        // Show or hide service provider fields based on selected user type
        val userType = (activity as MultiStepRegistrationActivity).getUserType()
        
        if (userType == "serviceProvider") {
            tilBusinessName.visibility = View.VISIBLE
            tilYearsExperience.visibility = View.VISIBLE
        } else {
            tilBusinessName.visibility = View.GONE
            tilYearsExperience.visibility = View.GONE
        }
    }

    private fun setupListeners() {
        // Text change listeners for validation
        etFirstName.addTextChangedListener(createTextWatcher(tilFirstName))
        etLastName.addTextChangedListener(createTextWatcher(tilLastName))
        etPhoneNumber.addTextChangedListener(createTextWatcher(tilPhoneNumber))
        
        // Service provider fields
        if ((activity as MultiStepRegistrationActivity).getUserType() == "serviceProvider") {
            etBusinessName.addTextChangedListener(createTextWatcher(tilBusinessName))
            etYearsExperience.addTextChangedListener(createTextWatcher(tilYearsExperience))
        }

        // Button click listeners
        btnBack.setOnClickListener {
            (activity as MultiStepRegistrationActivity).goToPreviousStep()
        }

        btnNext.setOnClickListener {
            if (validateInputs()) {
                saveDataAndContinue()
            }
        }
    }

    private fun createTextWatcher(textInputLayout: TextInputLayout): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textInputLayout.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate first name
        if (etFirstName.text.toString().trim().isEmpty()) {
            tilFirstName.error = "First name is required"
            isValid = false
        }

        // Validate last name
        if (etLastName.text.toString().trim().isEmpty()) {
            tilLastName.error = "Last name is required"
            isValid = false
        }

        // Validate phone number
        val phoneNumber = etPhoneNumber.text.toString().trim()
        if (phoneNumber.isEmpty()) {
            tilPhoneNumber.error = "Phone number is required"
            isValid = false
        } else if (phoneNumber.length < 10) {
            tilPhoneNumber.error = "Please enter a valid phone number"
            isValid = false
        }
        
        // Validate service provider fields if applicable
        if ((activity as MultiStepRegistrationActivity).getUserType() == "serviceProvider") {
            // Validate business name
            if (etBusinessName.text.toString().trim().isEmpty()) {
                tilBusinessName.error = "Business name is required"
                isValid = false
            }
            
            // Validate years of experience (optional validation)
            val yearsStr = etYearsExperience.text.toString().trim()
            if (yearsStr.isNotEmpty()) {
                try {
                    val years = yearsStr.toInt()
                    if (years < 0) {
                        tilYearsExperience.error = "Years of experience cannot be negative"
                        isValid = false
                    }
                } catch (e: NumberFormatException) {
                    tilYearsExperience.error = "Please enter a valid number"
                    isValid = false
                }
            }
        }

        return isValid
    }

    private fun saveDataAndContinue() {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        
        // Get business info if service provider
        val businessName = if (tilBusinessName.visibility == View.VISIBLE) 
            etBusinessName.text.toString().trim() else ""
        val yearsExperience = if (tilYearsExperience.visibility == View.VISIBLE) 
            etYearsExperience.text.toString().trim() else "0"

        // Save to the activity
        (activity as MultiStepRegistrationActivity).setPersonalDetails(
            firstName, lastName, phoneNumber, businessName, yearsExperience
        )

        // Go to the next step
        (activity as MultiStepRegistrationActivity).goToNextStep()
    }
} 