package com.example.uts_mobile1.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.uts_mobile1.MainActivity
import com.example.uts_mobile1.R
import com.example.uts_mobile1.databinding.FragmentHomeBinding
import com.example.uts_mobile1.seminar.SeminarFormActivity
import com.example.uts_mobile1.util.ValidationUtils
import com.example.uts_mobile1.util.ValidationResult

/**
 * Home Fragment - Landing page after login
 *
 * Features:
 * - Welcome message
 * - Daftar Seminar button
 * - List of nearby seminars
 * - Newsletter subscription
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        // Daftar Seminar button - navigate to Seminar tab
        binding.btnDaftarSeminar.setOnClickListener {
            (activity as? MainActivity)?.navigateToSeminar()
        }

        // Lihat Semua - navigate to Seminar tab
        binding.tvLihatSemua.setOnClickListener {
            (activity as? MainActivity)?.navigateToSeminar()
        }

        // Card click listeners (placeholder - could navigate to form with pre-filled seminar)
        binding.cardSeminar1.setOnClickListener {
            // Navigate to form with seminar 1
        }

        binding.cardSeminar2.setOnClickListener {
            // Navigate to form with seminar 2
        }

        // Newsletter Gabung button
        binding.btnGabung.setOnClickListener {
            val email = binding.etNewsletter.text?.toString()
            val result = ValidationUtils.validateEmail(email ?: "")
            when (result) {
                is ValidationResult.Error -> {
                    binding.tilNewsletter.error = result.message
                }
                else -> {
                    binding.tilNewsletter.error = null
                    Toast.makeText(context, "Berhasil berlangganan!", Toast.LENGTH_SHORT).show()
                    binding.etNewsletter.setText("")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}