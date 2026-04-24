package com.example.uts_mobile1.util

import android.util.Patterns

/**
 * Validation utilities following PRD requirements
 */
object ValidationUtils {

    // ==================== Validation Rules ====================

    /**
     * Validate nama - tidak boleh kosong
     */
    fun validateNama(nama: String): ValidationResult {
        return when {
            nama.isBlank() -> ValidationResult.Error("Nama tidak boleh kosong")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate email - tidak kosong + contains @ + valid format
     */
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email tidak boleh kosong")
            !email.contains("@") -> ValidationResult.Error("Format email tidak valid")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult.Error("Format email tidak valid")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate nomor HP - tidak kosong + hanya digits + 10-13 digit + dimulai dengan 08
     */
    fun validateHp(hp: String): ValidationResult {
        return when {
            hp.isBlank() -> ValidationResult.Error("Nomor HP tidak boleh kosong")
            !hp.all { it.isDigit() } -> ValidationResult.Error("Nomor HP harus hanya angka")
            hp.length < 10 || hp.length > 13 -> ValidationResult.Error("Nomor HP harus 10-13 digit")
            !hp.startsWith("08") -> ValidationResult.Error("Nomor HP harus dimulai dengan 08")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate gender - harus dipilih
     */
    fun validateGender(selected: Boolean, errorMessage: String = "Pilih jenis kelamin"): ValidationResult {
        return if (selected) ValidationResult.Valid
        else ValidationResult.Error(errorMessage)
    }

    /**
     * Validate seminar - harus dipilih
     */
    fun validateSeminar(seminar: String?, errorMessage: String = "Pilih seminar"): ValidationResult {
        return when {
            seminar.isNullOrBlank() -> ValidationResult.Error(errorMessage)
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate checkbox agreement - harus dicentang
     */
    fun validateAgreement(checked: Boolean, errorMessage: String = "Anda harus menyetujui syarat dan ketentuan"): ValidationResult {
        return if (checked) ValidationResult.Valid
        else ValidationResult.Error(errorMessage)
    }

    // ==================== Password Validation ====================

    /**
     * Validate password - minimal 8 karakter
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Error("Password tidak boleh kosong")
            password.length < 8 -> ValidationResult.Error("Password minimal 8 karakter")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate password required (not empty) - for login
     */
    fun validatePasswordRequired(password: String, errorMessage: String = "Password tidak boleh kosong"): ValidationResult {
        return if (password.isNotBlank()) ValidationResult.Valid
        else ValidationResult.Error(errorMessage)
    }

    /**
     * Validate confirm password - harus sama dengan password
     */
    fun validateConfirmPassword(password: String, confirmPassword: String): ValidationResult {
        return when {
            confirmPassword.isBlank() -> ValidationResult.Error("Konfirmasi password tidak boleh kosong")
            password != confirmPassword -> ValidationResult.Error("Password tidak cocok")
            else -> ValidationResult.Valid
        }
    }
}

/**
 * Sealed class for validation result
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}