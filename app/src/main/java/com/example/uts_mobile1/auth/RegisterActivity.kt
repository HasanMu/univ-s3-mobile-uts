package com.example.uts_mobile1.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.uts_mobile1.MainActivity
import com.example.uts_mobile1.R
import com.example.uts_mobile1.databinding.ActivityRegisterBinding
import com.example.uts_mobile1.util.ValidationUtils
import com.example.uts_mobile1.util.ValidationResult

/**
 * Register Activity with validation
 *
 * Features:
 * - Nama lengkap validation (not empty)
 * - Email validation (not empty + valid format)
 * - Password validation (minimal 8 karakter)
 * - Confirm password validation (match with password)
 * - Real-time error display
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    // TextWatcher for real-time validation
    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            clearError()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupViews()
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
        binding.etNama.addTextChangedListener(textWatcher)
        binding.etEmail.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)
        binding.etConfirmPassword.addTextChangedListener(textWatcher)

        // Per-field real-time validation (gives immediate feedback while typing)
        binding.etNama.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val nama = s?.toString() ?: ""
                val result = ValidationUtils.validateNama(nama)
                when (result) {
                    is ValidationResult.Error -> binding.tilNama.error = result.message
                    else -> binding.tilNama.error = null
                }
            }
        })

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val email = s?.toString() ?: ""
                val result = ValidationUtils.validateEmail(email)
                when (result) {
                    is ValidationResult.Error -> binding.tilEmail.error = result.message
                    else -> binding.tilEmail.error = null
                }
            }
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val password = s?.toString() ?: ""
                val result = ValidationUtils.validatePassword(password)
                when (result) {
                    is ValidationResult.Error -> binding.tilPassword.error = result.message
                    else -> binding.tilPassword.error = null
                }
            }
        })

        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val confirm = s?.toString() ?: ""
                val password = binding.etPassword.text?.toString() ?: ""
                val result = ValidationUtils.validateConfirmPassword(password, confirm)
                when (result) {
                    is ValidationResult.Error -> binding.tilConfirmPassword.error = result.message
                    ValidationResult.Valid -> binding.tilConfirmPassword.error = null
                }
            }
        })
    }

    private fun setupListeners() {
        // Register button
        binding.btnRegister.setOnClickListener {
            if (validateFields()) {
                navigateToMain()
            }
        }

        // Login link
        binding.tvLoginLink.setOnClickListener {
            finish() // Go back to login
        }
    }

    /**
     * Validate all fields
     */
    private fun validateFields(): Boolean {
        var isValid = true

        val nama = binding.etNama.text?.toString() ?: ""
        val email = binding.etEmail.text?.toString() ?: ""
        val password = binding.etPassword.text?.toString() ?: ""
        val confirmPassword = binding.etConfirmPassword.text?.toString() ?: ""

        // Validate nama
        val namaResult = ValidationUtils.validateNama(nama)
        when (namaResult) {
            is ValidationResult.Error -> {
                binding.tilNama.error = namaResult.message
                isValid = false
            }
            else -> binding.tilNama.error = null
        }

        // Validate email
        val emailResult = ValidationUtils.validateEmail(email)
        when (emailResult) {
            is ValidationResult.Error -> {
                binding.tilEmail.error = emailResult.message
                isValid = false
            }
            else -> binding.tilEmail.error = null
        }

        // Validate password
        val passwordResult = ValidationUtils.validatePassword(password)
        when (passwordResult) {
            is ValidationResult.Error -> {
                binding.tilPassword.error = passwordResult.message
                isValid = false
            }
            else -> binding.tilPassword.error = null
        }

        // Validate confirm password
        val confirmResult = ValidationUtils.validateConfirmPassword(password, confirmPassword)
        when (confirmResult) {
            is ValidationResult.Error -> {
                binding.tilConfirmPassword.error = confirmResult.message
                isValid = false
            }
            ValidationResult.Valid -> binding.tilConfirmPassword.error = null
        }

        return isValid
    }

    /**
     * Clear all errors
     */
    private fun clearError() {
        binding.tilNama.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
    }

    /**
     * Navigate to Main Activity
     */
    private fun navigateToMain() {
        Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
