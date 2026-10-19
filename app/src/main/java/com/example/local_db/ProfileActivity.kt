package com.example.local_db

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.local_db.databinding.ActivityProfileBinding
import kotlinx.coroutines.launch

/**
 * ProfileActivity allows users to manage their account settings and 
 * access features like the Rewards program and Order History.
 */
class ProfileActivity : AppCompatActivity() {
    // View binding for accessing the profile layout
    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up the view binding and content view
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        // Set up profile options with correct names and icons
        setupProfileOptions()

        // Set the 'Account' item as selected in the bottom navigation
        binding.bottomNav.selectedItemId = R.id.nav_account

        // Navigate to Edit Profile
        binding.tvViewProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Navigate to Favorites
        binding.btnFavorites.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        // Navigate to Order History
        binding.btnOrderHistory.setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }

        // Set up click listener for the Rewards option
        binding.optionRewards.root.setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
        }

        // Set up click listener for Help
        binding.optionHelp.root.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        // Set up click listener for Settings
        binding.optionSettings.root.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Navigate to Promotions
        binding.optionPromotions.root.setOnClickListener {
            startActivity(Intent(this, PromotionsActivity::class.java))
        }

        // Handle the logout click
        binding.btnLogoutAction.setOnClickListener {
            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout") { _, _ ->
                    val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putBoolean("isLoggedIn", false)
                    editor.remove("userEmail")
                    editor.apply()

                    Toast.makeText(this, "You have logged out", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()
            // Set the color of the Logout button to red
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                ?.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.error_red))
        }

        // Set up the bottom navigation listener
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_browse -> {
                    startActivity(Intent(this, BrowseActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_orders -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_account -> true
                else -> true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh user info when returning
        loadUserInfo()
    }

    /**
     * Loads current user information from the database.
     */
    private fun loadUserInfo() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        lifecycleScope.launch {
            val user = db.userDao().getUserByEmail(userEmail)
            user?.let {
                binding.tvProfileName.text = it.username
                it.profileImageUri?.let { uriString ->
                    binding.ivProfileImage.setImageURI(Uri.parse(uriString))
                }
            }
        }
    }

    /**
     * Configures the titles and icons for the reusable profile option items.
     */
    private fun setupProfileOptions() {
        // Rewards Option
        binding.optionRewards.tvOptionTitle.text = getString(R.string.rewards)
        binding.optionRewards.ivOptionIcon.setImageResource(R.drawable.ic_star)

        // Settings Option
        binding.optionSettings.tvOptionTitle.text = getString(R.string.settings)
        binding.optionSettings.ivOptionIcon.setImageResource(R.drawable.ic_settings)

        // Promotions Option
        binding.optionPromotions.tvOptionTitle.text = getString(R.string.promotions)
        binding.optionPromotions.ivOptionIcon.setImageResource(R.drawable.ic_discount)

        // Help Option
        binding.optionHelp.tvOptionTitle.text = getString(R.string.help)
        binding.optionHelp.ivOptionIcon.setImageResource(R.drawable.ic_help)
    }
}