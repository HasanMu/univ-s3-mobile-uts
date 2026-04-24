package com.example.uts_mobile1.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.uts_mobile1.MainActivity
import com.example.uts_mobile1.R
import com.example.uts_mobile1.databinding.ActivityLoginBinding
import com.example.uts_mobile1.util.ValidationUtils
import com.example.uts_mobile1.util.ValidationResult

/**
 * Login Activity with real-time validation
 *
 * Features:
 * - Email validation (not empty + valid format)
 * - Password validation (not empty)
 * - Real-time error display using TextInputLayout
 * - Navigation to Register and MainActivity
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // TextWatcher for real-time validation
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            // Clear errors on typing
            clearError()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupViews()
        setupValidation()
        setupListeners()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                resources.getDimensionPixelSize(R.dimen.screen_padding_horizontal),
                systemBars.top,
                resources.getDimensionPixelSize(R.dimen.screen_padding_horizontal),
                systemBars.bottom
            )
            insets
        }
    }

    private fun setupViews() {
        // Add text watchers for real-time validation
        binding.etEmail.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)
    }

    private fun setupValidation() {
        // Validation is triggered on button click and real-time
    }

    private fun setupListeners() {
        // Login button
        binding.btnLogin.setOnClickListener {
            if (validateFields()) {
                navigateToMain()
            }
        }

        // Register action (Text link)
        binding.tvRegisterLink.setOnClickListener {
            navigateToRegister()
        }
        
        binding.llRegister.setOnClickListener {
            navigateToRegister()
        }
    }

    /**
     * Validate all fields
     * @return true if all valid, false otherwise
     */
    private fun validateFields(): Boolean {
        var isValid = true
        val email = binding.etEmail.text?.toString() ?: ""
        val password = binding.etPassword.text?.toString() ?: ""

        // Validate email
        val emailResult = ValidationUtils.validateEmail(email)
        when (emailResult) {
            is ValidationResult.Error -> {
                binding.tilEmail.error = emailResult.message
                isValid = false
            }
            else -> {
                binding.tilEmail.error = null
            }
        }

        // Validate password (not empty)
        val passwordResult = ValidationUtils.validatePasswordRequired(password, getString(R.string.error_password_required))
        when (passwordResult) {
            is ValidationResult.Error -> {
                binding.tilPassword.error = passwordResult.message
                isValid = false
            }
            else -> {
                binding.tilPassword.error = null
            }
        }

        return isValid
    }

    /**
     * Clear all errors
     */
    private fun clearError() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
    }

    /**
     * Navigate to Register Activity
     */
    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    /**
     * Navigate to Main Activity
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        private const val TAG = "LoginActivity"
    }
}
