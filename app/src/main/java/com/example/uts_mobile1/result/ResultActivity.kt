package com.example.uts_mobile1.result

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.uts_mobile1.MainActivity
import com.example.uts_mobile1.R
import com.example.uts_mobile1.databinding.ActivityResultBinding
import com.example.uts_mobile1.ui.Constants

/**
 * Result Activity - Registration success summary
 *
 * Displays:
 * - Success message
 * - Registration summary (Seminar, Tanggal, Waktu, Lokasi, ID)
 * - Buttons: Kembali ke Home, Download E-Tiket
 */
class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityResultBinding.inflate(layoutInflater)
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
        // Get data from intent
        val nama = intent.getStringExtra(Constants.IntentKeys.NAMA) ?: ""
        val email = intent.getStringExtra(Constants.IntentKeys.EMAIL) ?: ""
        val hp = intent.getStringExtra(Constants.IntentKeys.HP) ?: ""
        val gender = intent.getStringExtra(Constants.IntentKeys.GENDER) ?: ""
        val seminar = intent.getStringExtra(Constants.IntentKeys.SEMINAR) ?: ""

        // Display in card - we show seminar info (simplified for now)
        binding.tvSeminar.text = seminar.ifBlank { "-" }

        // Generate registration ID
        val regId = "OC-${System.currentTimeMillis().toString().takeLast(8)}"
        binding.tvId.text = regId

        // For now, use placeholder data
        binding.tvTanggal.text = "24 Oktober 2024"
        binding.tvWaktu.text = "14:00 - 16:30 WIB"
        binding.tvLokasi.text = "Virtual via Curator Live Stream"
    }

    private fun setupListeners() {

        // Primary button: Kembali ke Home
        binding.btnPrimary.setOnClickListener {
            navigateToHome()
        }
    }

    /**
     * Navigate to MainActivity (Home)
     */
    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}