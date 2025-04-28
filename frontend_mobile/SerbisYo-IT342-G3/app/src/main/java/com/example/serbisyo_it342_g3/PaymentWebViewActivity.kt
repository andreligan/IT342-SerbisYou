package com.example.serbisyo_it342_g3

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class PaymentWebViewActivity : AppCompatActivity() {
    
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var paymentUrl: String
    private val tag = "PaymentWebViewActivity"
    
    companion object {
        const val EXTRA_PAYMENT_URL = "extra_payment_url"
        const val EXTRA_BOOKING_ID = "extra_booking_id"
        const val RESULT_PAYMENT_SUCCESS = Activity.RESULT_FIRST_USER + 1
        const val RESULT_PAYMENT_CANCELLED = Activity.RESULT_FIRST_USER + 2
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_webview)
        
        // Initialize toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Payment"
        
        // Get the payment URL from the intent
        paymentUrl = intent.getStringExtra(EXTRA_PAYMENT_URL) ?: ""
        if (paymentUrl.isEmpty()) {
            Log.e(tag, "No payment URL provided")
            finish()
            return
        }
        
        // Set up WebView and ProgressBar
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        
        // Configure WebView settings
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            loadsImagesAutomatically = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        
        // Set up WebViewClient to handle page loading and URL redirects
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
                
                Log.d(tag, "Loading URL: $url")
                
                // Check if we've been redirected to the success or cancel URL
                url?.let { currentUrl ->
                    when {
                        currentUrl.contains("localhost") && currentUrl.contains("payment-success") -> {
                            Log.d(tag, "Payment success detected via localhost URL: $currentUrl")
                            handlePaymentSuccess()
                        }
                        currentUrl.contains("localhost") && currentUrl.contains("payment-cancel") -> {
                            Log.d(tag, "Payment cancellation detected via localhost URL: $currentUrl")
                            handlePaymentCancelled()
                        }
                        currentUrl.contains("payment-success") -> {
                            Log.d(tag, "Payment success detected: $currentUrl")
                            handlePaymentSuccess()
                        }
                        currentUrl.contains("payment-cancel") -> {
                            Log.d(tag, "Payment cancellation detected: $currentUrl")
                            handlePaymentCancelled()
                        }
                    }
                }
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                Log.d(tag, "Page finished loading: $url")
            }
            
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                Log.d(tag, "Checking if should override URL: $url")
                
                // If the URL is a success or cancel URL, handle it
                when {
                    url.contains("localhost") && url.contains("payment-success") -> {
                        Log.d(tag, "Payment success detected during override: $url")
                        handlePaymentSuccess()
                        return true // Don't load this URL in WebView
                    }
                    url.contains("localhost") && url.contains("payment-cancel") -> {
                        Log.d(tag, "Payment cancellation detected during override: $url")
                        handlePaymentCancelled()
                        return true // Don't load this URL in WebView
                    }
                    url.contains("payment-success") -> {
                        Log.d(tag, "Payment success detected during override: $url")
                        handlePaymentSuccess()
                        return true
                    }
                    url.contains("payment-cancel") -> {
                        Log.d(tag, "Payment cancellation detected during override: $url")
                        handlePaymentCancelled()
                        return true
                    }
                }
                
                // Let the WebView handle all other URLs
                return false
            }
            
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Log.e(tag, "WebView error: ${error?.description}, URL: ${request?.url}")
                
                // Show error message
                runOnUiThread {
                    Toast.makeText(
                        this@PaymentWebViewActivity,
                        "Payment error: ${error?.description}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        
        // Load the payment URL
        Log.d(tag, "Loading payment URL: $paymentUrl")
        try {
            webView.loadUrl(paymentUrl)
        } catch (e: Exception) {
            Log.e(tag, "Error loading payment URL", e)
            Toast.makeText(this, "Error loading payment page: ${e.message}", Toast.LENGTH_LONG).show()
            setResult(RESULT_PAYMENT_CANCELLED)
            finish()
        }
    }
    
    private fun handlePaymentSuccess() {
        Log.d(tag, "Processing payment success")
        // Set result and finish the activity
        val intent = Intent()
        intent.putExtra(EXTRA_BOOKING_ID, getIntent().getLongExtra(EXTRA_BOOKING_ID, 0))
        setResult(RESULT_PAYMENT_SUCCESS, intent)
        Toast.makeText(this, "Payment successful!", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun handlePaymentCancelled() {
        Log.d(tag, "Processing payment cancellation")
        // Set result and finish the activity
        setResult(RESULT_PAYMENT_CANCELLED)
        Toast.makeText(this, "Payment was cancelled", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            setResult(RESULT_PAYMENT_CANCELLED)
            super.onBackPressed()
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(tag, "New intent received: ${intent?.data}")
        
        // Handle deep link
        intent?.data?.let { uri ->
            when {
                uri.path?.contains("payment-success") == true -> {
                    Log.d(tag, "Payment success from deep link")
                    handlePaymentSuccess()
                }
                uri.path?.contains("payment-cancel") == true -> {
                    Log.d(tag, "Payment cancellation from deep link")
                    handlePaymentCancelled()
                }
            }
        }
    }
} 