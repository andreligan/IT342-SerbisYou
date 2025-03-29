package com.example.serbisyo

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.DriverManager
import java.sql.SQLException

class RegisterPage : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var addressInput: EditText
    private lateinit var registerButton: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        try {
            nameInput = findViewById(R.id.name_input)
            emailInput = findViewById(R.id.email_input)
            passwordInput = findViewById(R.id.password_input)
            phoneInput = findViewById(R.id.phone_input)
            addressInput = findViewById(R.id.address_input)
            registerButton = findViewById(R.id.register_button)
        } catch (e: Exception) {
            Log.e("RegisterPage", "Error initializing views: ${e.message}", e)
            Toast.makeText(this, "Error initializing app", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            if (validateInputs()) {
                registerCustomer()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val phone = phoneInput.text.toString().trim()
        val address = addressInput.text.toString().trim()

        when {
            name.isEmpty() -> {
                nameInput.error = "Please enter your name"
                return false
            }
            email.isEmpty() -> {
                emailInput.error = "Please enter your email"
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailInput.error = "Please enter a valid email"
                return false
            }
            password.isEmpty() -> {
                passwordInput.error = "Please enter your password"
                return false
            }
            password.length < 6 -> {
                passwordInput.error = "Password must be at least 6 characters"
                return false
            }
            phone.isEmpty() -> {
                phoneInput.error = "Please enter your phone number"
                return false
            }
            address.isEmpty() -> {
                addressInput.error = "Please enter your address"
                return false
            }
        }
        return true
    }

    private fun registerCustomer() {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val phone = phoneInput.text.toString().trim()
        val address = addressInput.text.toString().trim()

        Log.d("RegisterPage", "Starting registration process for: $name")

        lifecycleScope.launch(Dispatchers.IO) {
            var connection: java.sql.Connection? = null
            try {
                // Updated database configuration
                val url = "jdbc:mysql://10.0.2.2:3306/serbisyo_db?" +
                        "useSSL=false&" +
                        "allowPublicKeyRetrieval=true&" +
                        "serverTimezone=UTC&" +
                        "connectTimeout=5000&" +
                        "autoReconnect=true"
                val dbUser = "root"
                val dbPassword = "123456" // Your MySQL root password

                Log.d("RegisterPage", "Attempting database connection with URL: $url")

                // Set connection properties
                val props = java.util.Properties()
                props.setProperty("user", dbUser)
                props.setProperty("password", dbPassword)
                props.setProperty("connectTimeout", "5000")
                props.setProperty("socketTimeout", "30000")

                Class.forName("com.mysql.cj.jdbc.Driver")
                connection = DriverManager.getConnection(url, props)
                Log.d("RegisterPage", "Database connection successful")

                val checkEmailSql = "SELECT COUNT(*) FROM customers WHERE email = ?"
                connection.prepareStatement(checkEmailSql).use { checkStatement ->
                    checkStatement.setString(1, email)
                    val resultSet = checkStatement.executeQuery()
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        throw SQLException("Email already exists")
                    }
                }

                val sql = """
                INSERT INTO customers (name, email, password, phone_number, address) 
                VALUES (?, ?, ?, ?, ?)
            """.trimIndent()

                connection.prepareStatement(sql).use { statement ->
                    statement.setString(1, name)
                    statement.setString(2, email)
                    statement.setString(3, password)
                    statement.setString(4, phone)
                    statement.setString(5, address)

                    val result = statement.executeUpdate()
                    Log.d("RegisterPage", "Registration successful, rows affected: $result")

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterPage, "Registration successful!", Toast.LENGTH_LONG).show()
                        clearInputs()
                    }
                }

            } catch (e: SQLException) {
                Log.e("RegisterPage", "Database error: ${e.message}", e)
                val errorMessage = when {
                    e.message?.contains("Communications link failure") == true ->
                        "Cannot connect to database. Please check your internet connection."
                    e.message?.contains("Email already exists") == true ->
                        "This email is already registered."
                    e.message?.contains("Access denied") == true ->
                        "Database access denied. Please check credentials."
                    else -> "Registration failed: ${e.message}"
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterPage, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("RegisterPage", "Unexpected error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RegisterPage,
                        "An unexpected error occurred: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } finally {
                try {
                    connection?.close()
                    Log.d("RegisterPage", "Database connection closed")
                } catch (e: Exception) {
                    Log.e("RegisterPage", "Error closing connection", e)
                }
            }
        }
    }

    private fun clearInputs() {
        nameInput.text.clear()
        emailInput.text.clear()
        passwordInput.text.clear()
        phoneInput.text.clear()
        addressInput.text.clear()
        nameInput.requestFocus()
    }
}