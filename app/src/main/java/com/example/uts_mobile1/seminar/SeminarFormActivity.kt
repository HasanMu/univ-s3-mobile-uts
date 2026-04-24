package com.example.uts_mobile1.seminar

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.uts_mobile1.R
import com.example.uts_mobile1.databinding.ActivitySeminarFormBinding
import com.example.uts_mobile1.result.ResultActivity
import com.example.uts_mobile1.ui.Constants
import com.example.uts_mobile1.ui.findById
import com.example.uts_mobile1.util.ValidationUtils
import com.example.uts_mobile1.util.ValidationResult

/**
 * Seminar Form Activity - Registration form with validation
 *
 * Features:
 * - All fields validated (not empty, email format, HP format)
 * - Gender selection (RadioGroup)
 * - Seminar selection (Dropdown)
 * - Agreement checkbox
 * - Real-time validation via TextWatcher
 * - Confirmation dialog before submit
 */
class SeminarFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeminarFormBinding

    // Seminar list for dropdown
    private val seminarTitles: List<String> = Constants.SeminarList.seminars.map { it.title }

    // Seminar ID passed from list (if any)
    private var selectedSeminarId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySeminarFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get seminar ID from intent (if navigated from list)
        selectedSeminarId = intent.getIntExtra("seminar_id", -1)

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
        // Add specific text watchers for real-time validation
        binding.etNama.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val result = ValidationUtils.validateNama(s?.toString() ?: "")
                binding.tilNama.error = if (result is ValidationResult.Error) result.message else null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val result = ValidationUtils.validateEmail(s?.toString() ?: "")
                binding.tilEmail.error = if (result is ValidationResult.Error) result.message else null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etHp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val result = ValidationUtils.validateHp(s?.toString() ?: "")
                binding.tilHp.error = if (result is ValidationResult.Error) result.message else null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Setup seminar dropdown
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, seminarTitles)
        binding.actvSeminar.setAdapter(adapter)

        // Pre-select seminar if passed
        if (selectedSeminarId > 0) {
            val seminar = Constants.SeminarList.seminars.findById(selectedSeminarId)
            seminar?.let {
                binding.actvSeminar.setText(it.title, false)
            }
        }
        
        binding.rgGender.setOnCheckedChangeListener { _, _ ->
            binding.tvGenderLabel.setTextColor(getColor(R.color.on_surface_variant))
        }

        binding.actvSeminar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.tilSeminar.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.cbAgreement.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.cbAgreement.error = null
            }
        }
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Submit button
        binding.btnSubmit.setOnClickListener {
            if (validateFields()) {
                showConfirmationDialog()
            }
        }
    }

    /**
     * Validate all fields
     */
    private fun validateFields(): Boolean {
        var isValid = true

        val nama = binding.etNama.text?.toString() ?: ""
        val email = binding.etEmail.text?.toString() ?: ""
        val hp = binding.etHp.text?.toString() ?: ""
        val gender = binding.rgGender.checkedRadioButtonId
        val seminar = binding.actvSeminar.text?.toString()
        val agreement = binding.cbAgreement.isChecked

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

        // Validate HP
        val hpResult = ValidationUtils.validateHp(hp)
        when (hpResult) {
            is ValidationResult.Error -> {
                binding.tilHp.error = hpResult.message
                isValid = false
            }
            else -> binding.tilHp.error = null
        }

        // Validate gender
        if (gender == -1) {
            binding.tvGenderLabel.setTextColor(getColor(R.color.error))
            isValid = false
        } else {
            binding.tvGenderLabel.setTextColor(getColor(R.color.on_surface_variant))
        }

        // Validate seminar
        val seminarResult = ValidationUtils.validateSeminar(seminar)
        when (seminarResult) {
            is ValidationResult.Error -> {
                binding.tilSeminar.error = seminarResult.message
                isValid = false
            }
            else -> binding.tilSeminar.error = null
        }

        // Validate agreement
        val agreementResult = ValidationUtils.validateAgreement(agreement)
        when (agreementResult) {
            is ValidationResult.Error -> {
                binding.cbAgreement.error = agreementResult.message
                isValid = false
            }
            else -> binding.cbAgreement.error = null
        }

        return isValid
    }

    /**
     * Clear all errors
     */
    private fun clearError() {
        binding.tilNama.error = null
        binding.tilEmail.error = null
        binding.tilHp.error = null
        binding.tvGenderLabel.setTextColor(getColor(R.color.on_surface_variant))
        binding.tilSeminar.error = null
        binding.cbAgreement.error = null
    }

    /**
     * Show confirmation dialog
     */
    private fun showConfirmationDialog() {
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle(R.string.konfirmasi_title)
            .setMessage(R.string.konfirmasi_message)
            .setPositiveButton(R.string.ya) { _, _ ->
                navigateToResult()
            }
            .setNegativeButton(R.string.tidak, null)
            .create()
        dialog.show()
    }

    /**
     * Navigate to Result Activity
     */
    private fun navigateToResult() {
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra(Constants.IntentKeys.NAMA, binding.etNama.text?.toString())
            putExtra(Constants.IntentKeys.EMAIL, binding.etEmail.text?.toString())
            putExtra(Constants.IntentKeys.HP, binding.etHp.text?.toString())
            
            // Gender
            val genderId = binding.rgGender.checkedRadioButtonId
            val gender = when (genderId) {
                R.id.rbLaki -> getString(R.string.laki_laki)
                R.id.rbPerempuan -> getString(R.string.perempuan)
                else -> ""
            }
            putExtra(Constants.IntentKeys.GENDER, gender)
            
            // Seminar
            putExtra(Constants.IntentKeys.SEMINAR, binding.actvSeminar.text?.toString())
        }
        startActivity(intent)
        finish()
    }
}