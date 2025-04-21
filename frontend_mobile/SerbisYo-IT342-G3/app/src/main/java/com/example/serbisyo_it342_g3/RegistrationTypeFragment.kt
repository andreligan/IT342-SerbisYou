package com.example.serbisyo_it342_g3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class RegistrationTypeFragment : Fragment() {

    private lateinit var cardCustomer: CardView
    private lateinit var cardServiceProvider: CardView
    private lateinit var btnNext: Button
    private var selectedType: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registration_type, container, false)

        // Initialize UI components
        cardCustomer = view.findViewById(R.id.cardCustomer)
        cardServiceProvider = view.findViewById(R.id.cardServiceProvider)
        btnNext = view.findViewById(R.id.btnNext)

        setupClickListeners()

        return view
    }

    private fun setupClickListeners() {
        // Customer card selection
        cardCustomer.setOnClickListener {
            selectedType = "customer"
            updateCardSelection()
        }

        // Service Provider card selection
        cardServiceProvider.setOnClickListener {
            selectedType = "serviceProvider"
            updateCardSelection()
        }

        // Next button
        btnNext.setOnClickListener {
            val activity = activity as MultiStepRegistrationActivity
            activity.setUserType(selectedType)
            activity.goToNextStep()
        }
    }

    private fun updateCardSelection() {
        // Reset card backgrounds
        cardCustomer.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        cardServiceProvider.setCardBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))

        // Highlight selected card
        when (selectedType) {
            "customer" -> cardCustomer.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_gray))
            "serviceProvider" -> cardServiceProvider.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_gray))
        }

        // Enable/disable next button
        btnNext.isEnabled = selectedType.isNotEmpty()
    }
} 