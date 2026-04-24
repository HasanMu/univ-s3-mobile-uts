package com.example.uts_mobile1.seminar

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uts_mobile1.databinding.FragmentSeminarBinding
import com.example.uts_mobile1.ui.Constants

/**
 * Seminar Fragment - List of available seminars
 *
 * Features:
 * - Featured seminar card
 * - Category chips filter
 * - RecyclerView list of seminars
 * - Navigate to form on item click
 */
class SeminarFragment : Fragment() {

    private var _binding: FragmentSeminarBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SeminarAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeminarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupChips()
        setupFeaturedCard()
    }

    private fun setupRecyclerView() {
        adapter = SeminarAdapter(Constants.SeminarList.seminars) { seminar ->
            // Navigate to form with seminar
            val intent = Intent(requireContext(), SeminarFormActivity::class.java)
            intent.putExtra("seminar_id", seminar.id)
            intent.putExtra("seminar_title", seminar.title)
            startActivity(intent)
        }

        binding.rvSeminar.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SeminarFragment.adapter
        }
    }

    private fun setupChips() {
        // Chip filter listeners (placeholder for filtering)
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            // Filter logic would go here
        }
    }

    private fun setupFeaturedCard() {
        // Featured card click - navigate to form for featured seminar
        binding.btnDaftarSekarang.setOnClickListener {
            val intent = Intent(requireContext(), SeminarFormActivity::class.java)
            intent.putExtra("seminar_id", 3) // Featured seminar (AI)
            intent.putExtra("seminar_title", "Seminar Nasional: Masa Depan AI")
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}