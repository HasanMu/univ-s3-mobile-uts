package com.example.uts_mobile1

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.uts_mobile1.databinding.ActivityMainBinding
import com.example.uts_mobile1.home.HomeFragment
import com.example.uts_mobile1.seminar.SeminarFragment

/**
 * Main Activity with Bottom Navigation
 *
 * Hosts HomeFragment and SeminarFragment via BottomNavigationView
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupBottomNavigation()

        // Show default fragment
        if (savedInstanceState == null) {
            showFragment(HomeFragment())
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply padding to fragment container, not bottom nav (it handles itself)
            binding.fragmentContainer.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    showFragment(HomeFragment())
                    true
                }
                R.id.nav_seminar -> {
                    showFragment(SeminarFragment())
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Show fragment in container
     */
    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /**
     * Navigate to Seminar tab programmatically
     */
    fun navigateToSeminar() {
        binding.bottomNavigation.selectedItemId = R.id.nav_seminar
    }
}