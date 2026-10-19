package com.example.local_db

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import com.example.local_db.databinding.ActivityRewardsBinding
import kotlinx.coroutines.launch

/**
 * RewardsActivity tracks the user's loyalty progress.
 * Users earn 1 point per order towards a free coffee.
 */
class RewardsActivity : AppCompatActivity() {

    // View binding to interact with UI components
    private lateinit var binding: ActivityRewardsBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate layout and initialize binding
        binding = ActivityRewardsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        // Handle the toolbar navigation click to go back to the profile
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnRedeem.setOnClickListener {
            redeemReward()
        }

        loadRewards()
    }

    private fun redeemReward() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        lifecycleScope.launch {
            val user = db.userDao().getUserByEmail(userEmail)
            user?.let {
                if (it.rewardsPoints >= 10) {
                    val updatedUser = it.copy(rewardsPoints = it.rewardsPoints - 10)
                    db.userDao().updateUser(updatedUser)
                    android.widget.Toast.makeText(this@RewardsActivity, "Reward Redeemed! Enjoy your free coffee.", android.widget.Toast.LENGTH_LONG).show()
                    loadRewards() // Refresh UI
                }
            }
        }
    }

    private fun loadRewards() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        lifecycleScope.launch {
            val user = db.userDao().getUserByEmail(userEmail)
            val points = user?.rewardsPoints ?: 0
            val maxPoints = 10
            
            binding.tvCurrentPoints.text = "$points / $maxPoints Points"
            binding.pbRewards.progress = (points.toFloat() / maxPoints * 100).toInt()
            
            val remaining = maxPoints - points
            if (remaining > 0) {
                binding.tvPointsRemaining.text = "$remaining more orders to reach your reward"
                binding.btnRedeem.isEnabled = false
                binding.ivCelebrate.visibility = View.GONE
            } else {
                binding.tvPointsRemaining.text = "You've reached your reward! Enjoy a free coffee."
                binding.btnRedeem.isEnabled = true
                showCelebration()
            }
        }
    }

    private fun showCelebration() {
        binding.ivCelebrate.visibility = View.VISIBLE
        
        val imageLoader = ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()

        val celebrateGif = "https://cdn.pixabay.com/animation/2023/04/10/22/02/22-02-14-118_512.gif" // Colorful confetti
        binding.ivCelebrate.load(celebrateGif, imageLoader) {
            crossfade(true)
        }
    }
}