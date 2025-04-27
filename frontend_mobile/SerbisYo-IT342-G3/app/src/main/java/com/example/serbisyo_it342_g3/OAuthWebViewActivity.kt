package com.example.serbisyo_it342_g3

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.serbisyo_it342_g3.api.BaseApiClient
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class OAuthWebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var baseApiClient: BaseApiClient
    private val TAG = "OAuthWebView"
    private var isRegistration = false
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oauth_web_view)
        
        // Initialize API client
        baseApiClient = BaseApiClient(this)
        
        // Initialize views
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        
        // Get URL and parameters from intent
        val authUrl = intent.getStringExtra("auth_url") ?: ""
        isRegistration = intent.getBooleanExtra("is_registration", false)
        
        if (authUrl.isEmpty()) {
            Toast.makeText(this, "Error: Missing authentication URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Set up WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        
        // Set WebViewClient to handle redirects and loading
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                Log.d(TAG, "URL loading: $url")
                
                // Skip initial authorization URLs - only process actual redirects
                // Don't intercept the initial Google auth URL
                if (url.contains("/oauth2/authorization/google") || 
                    url.startsWith("https://accounts.google.com/o/oauth2/auth")) {
                    return false // Let WebView handle this URL normally
                }
                
                // Handle OAuth redirect URLs
                if (url.contains("/oauth2/redirect") || url.contains("/oauth-role-selection") || 
                    url.contains("/login/oauth2/code/google") || url.contains("oauth2callback")) {
                    Log.d(TAG, "OAuth redirect detected: $url")
                    // Parse the URL parameters
                    extractAndHandleParameters(url)
                    return true
                }
                
                // Let the WebView handle other URLs
                return false
            }
            
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }
        }
        
        // Load the authentication URL
        Log.d(TAG, "Loading auth URL: $authUrl")
        webView.loadUrl(authUrl)
    }
    
    private fun extractAndHandleParameters(url: String) {
        try {
            // Extract parameters from URL
            val uri = android.net.Uri.parse(url)
            
            // Check if this is a redirect with authorization code
            if (url.contains("/login/oauth2/code/google")) {
                val code = uri.getQueryParameter("code")
                if (code != null) {
                    // We received an authorization code, now need to exchange it for a token
                    // We'll do this by completing the flow in the WebView and letting the backend handle it
                    Log.d(TAG, "Received authorization code, continuing OAuth flow in WebView")
                    return  // Let the WebView continue loading to complete the OAuth flow
                }
            }
            
            // For existing users with token
            val token = uri.getQueryParameter("token")
            val userId = uri.getQueryParameter("userId")
            val role = uri.getQueryParameter("role")
            
            // For new users who need to select role
            val email = uri.getQueryParameter("email")
            val name = uri.getQueryParameter("name")
            val picture = uri.getQueryParameter("picture")
            
            Log.d(TAG, "Extracted params - token: $token, role: $role, email: $email, name: $name")
            
            if (token != null && role != null) {
                // Existing user flow
                handleExistingUser(token, userId, role)
            } else if (email != null && name != null) {
                // New user flow
                handleNewUser(email, name, picture ?: "")
            } else {
                // Error case
                Log.e(TAG, "Invalid or missing parameters in redirect URL: $url")
                Toast.makeText(this, "Authentication error: Invalid response", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing URL parameters: ${e.message}", e)
            Toast.makeText(this, "Authentication error: ${e.message}", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }
    
    private fun handleExistingUser(token: String, userId: String?, role: String) {
        Log.d(TAG, "Handling existing user with role: $role")
        // Save authentication data
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("token", token)
            // Convert userId if present
            val userIdLong = userId?.toLongOrNull() ?: 0L
            putLong("userId", userIdLong)
            putString("role", role)
            apply()
        }
        
        // Navigate based on role
        if (role.lowercase().contains("customer")) {
            startActivity(Intent(this, CustomerDashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        } else {
            startActivity(Intent(this, ServiceProviderDashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
        
        finish()
    }
    
    private fun handleNewUser(email: String, name: String, picture: String) {
        Log.d(TAG, "Handling new user with email: $email, name: $name")
        // Redirect to role selection
        val intent = Intent(this, OAuth2RoleSelectionActivity::class.java).apply {
            putExtra("email", email) 
            putExtra("name", name)
            putExtra("picture", picture)
            putExtra("isManualEntry", false) // This came from real Google OAuth
        }
        startActivity(intent)
        finish()
    }
    
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
} 