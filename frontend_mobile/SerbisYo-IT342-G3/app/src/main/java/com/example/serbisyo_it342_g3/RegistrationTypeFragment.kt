package com.example.serbisyo_it342_g3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.serbisyo_it342_g3.api.BaseApiClient

class RegistrationTypeFragment : Fragment() {

    private lateinit var cardCustomer: CardView
    private lateinit var cardServiceProvider: CardView
    private lateinit var cardGoogleSignIn: CardView
    private lateinit var btnNext: Button
    private var selectedType: String = ""
    
    private val TAG = "RegistrationTypeFragment"
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set up ActivityResultLauncher for Google Sign-In
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                // Pass the result directly to LoginActivity to handle
                val intent = Intent(requireActivity(), LoginActivity::class.java).apply {
                    putExtra("googleSignInData", result.data)
                    putExtra("isFromRegistration", true)
                }
                startActivity(intent)
                requireActivity().finish()
            } else {
                Log.d(TAG, "Google Sign-In failed or was cancelled")
                Toast.makeText(requireContext(), "Google Sign-In failed or was cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registration_type, container, false)

        // Initialize UI components
        cardCustomer = view.findViewById(R.id.cardCustomer)
        cardServiceProvider = view.findViewById(R.id.cardServiceProvider)
        cardGoogleSignIn = view.findViewById(R.id.cardGoogleSignIn)
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
        
        // Google Sign-In using direct browser access
        cardGoogleSignIn.setOnClickListener {
            try {
                // Get the base URL
                val baseApiClient = BaseApiClient(requireContext())
                val baseUrl = baseApiClient.getBaseUrl()
                
                // Create direct Google OAuth URL
                val googleAuthUrl = "$baseUrl/oauth2/authorization/google"
                
                // Open using Chrome Custom Tabs or external browser
                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(googleAuthUrl))
                startActivity(intent)
                
                // Show more detailed instructions to the user
                Toast.makeText(
                    requireContext(), 
                    "Please sign in with Google in the browser. After signing in, if you're not redirected to the app, manually return here.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Log.e(TAG, "Error starting Google Sign-In: ${e.message}", e)
                Toast.makeText(requireContext(), "Error starting Google Sign-In: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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