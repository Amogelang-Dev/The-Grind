package com.example.local_db

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.local_db.databinding.ActivityPromotionsBinding

class PromotionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPromotionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPromotionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}