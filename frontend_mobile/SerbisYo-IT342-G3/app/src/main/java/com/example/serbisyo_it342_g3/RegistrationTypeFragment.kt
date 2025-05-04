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
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
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
                
                // Get the OAuth URL with mobile parameters
                val finalAuthUrl = baseApiClient.getGoogleOAuthUrlForMobile()
                
                Log.d(TAG, "Starting Google Sign-In with URL: $finalAuthUrl")
                
                // Try to use Chrome Custom Tabs for a better user experience
                try {
                    // Create and customize the Chrome Custom Tabs intent
                    val customTabsIntent = CustomTabsIntent.Builder()
                        .setColorSchemeParams(
                            CustomTabsIntent.COLOR_SCHEME_LIGHT,
                            CustomTabColorSchemeParams.Builder()
                                .setToolbarColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                                .build()
                        )
                        .setShowTitle(true)
                        .build()
                    
                    // Use Chrome browser if available
                    val packageName = "com.android.chrome"
                    customTabsIntent.intent.setPackage(packageName)
                    
                    Log.d(TAG, "Launching Chrome Custom Tabs for Google Auth")
                    customTabsIntent.launchUrl(requireContext(), Uri.parse(finalAuthUrl))
                } catch (e: Exception) {
                    // Fallback to regular browser if Chrome Custom Tabs fails
                    Log.d(TAG, "Falling back to regular browser with URL: $finalAuthUrl")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalAuthUrl))
                    startActivity(intent)
                    Log.e(TAG, "Error using CustomTabs: ${e.message}", e)
                }
                
                // Show more detailed instructions to the user
                Toast.makeText(
                    requireContext(), 
                    "Please sign in with Google. After signing in, you'll be redirected back to the app to complete registration.",
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