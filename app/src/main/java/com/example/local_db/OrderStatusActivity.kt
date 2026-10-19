package com.example.local_db

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import com.example.local_db.databinding.ActivityOrderStatusBinding

/**
 * OrderStatusActivity shows the customer the progress of their pickup order.
 * It provides the estimated time and store location for pickup.
 */
class OrderStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderStatusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- GIF LOADING LOGIC ---
        val imageLoader = ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()

        val gifUrl = "https://cdn.dribbble.com/userupload/27960729/file/original-25f837cc7f41130f9571a60230667f9d.gif"
        binding.ivCoffeeStatus.load(gifUrl, imageLoader) {
            crossfade(true)
            placeholder(R.drawable.loading_preparing_order) // Show static image while loading
            error(R.drawable.loading_preparing_order) // Fallback if link fails
        }

        // Mocking order details
        binding.tvOrderNumber.text = getString(R.string.order_number, "77321")
        
        // Get pickup time from Intent or use default
        val pickupTime = intent.getStringExtra("EXTRA_PICKUP_TIME") ?: "10:45 AM (In 15 mins)"
        binding.tvPickupTime.text = pickupTime

        binding.btnBackHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}