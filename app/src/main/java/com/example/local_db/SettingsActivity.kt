package com.example.local_db

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.local_db.databinding.ActivitySettingsBinding

/**
 * SettingsActivity handles app configurations like Dark Mode and Notifications.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            saveNotificationPreference(true)
        } else {
            binding.switchNotifications.isChecked = false
            Toast.makeText(this, "Permission denied. Notifications will not be sent.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar back navigation
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Dark Mode Toggle Logic
        setupDarkModeToggle()

        // Push Notifications Toggle
        setupNotificationToggle()
    }

    private fun setupNotificationToggle() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val notificationsEnabled = sharedPref.getBoolean("notificationsEnabled", true)
        
        binding.switchNotifications.isChecked = notificationsEnabled

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    saveNotificationPreference(true)
                }
            } else {
                saveNotificationPreference(false)
            }
        }
    }

    private fun saveNotificationPreference(enabled: Boolean) {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("notificationsEnabled", enabled)
        editor.apply()

        val message = if (enabled) "Push notifications turned ON" else "Push notifications turned OFF"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Configures the Dark Mode switch and saves the preference.
     */
    private fun setupDarkModeToggle() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        
        // Set initial state based on saved preference
        val isDarkMode = sharedPref.getBoolean("isDarkMode", false)
        binding.switchDarkMode.isChecked = isDarkMode

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            // Save preference in SharedPreferences
            val editor = sharedPref.edit()
            editor.putBoolean("isDarkMode", isChecked)
            editor.apply()

            // Apply theme immediately
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}