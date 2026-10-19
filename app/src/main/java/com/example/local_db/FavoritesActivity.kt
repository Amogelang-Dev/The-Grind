package com.example.local_db

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.local_db.databinding.ActivityFavoritesBinding
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        loadFavorites()
    }

    private fun loadFavorites() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        lifecycleScope.launch {
            val favorites = db.favoriteDao().getFavoritesByUser(userEmail)
            
            if (favorites.isEmpty()) {
                binding.tvNoFavorites.visibility = View.VISIBLE
                binding.rvFavorites.visibility = View.GONE
            } else {
                binding.tvNoFavorites.visibility = View.GONE
                binding.rvFavorites.visibility = View.VISIBLE
            }
            
            // Map FavoriteItem to MenuItem for the adapter
            val menuItems = favorites.map {
                MenuItem(it.id, it.name, it.description, it.price, null, it.category, it.imageResId, true)
            }
            
            // Always set a new adapter to force a clean UI state and avoid sync issues
            binding.rvFavorites.adapter = MenuAdapter(menuItems, { menuItem ->
                removeFavorite(menuItem, userEmail)
            })
        }
    }

    private fun removeFavorite(item: MenuItem, userEmail: String) {
        lifecycleScope.launch {
            db.favoriteDao().removeFavoriteById(item.id, userEmail)
            // Explicitly reload from database to ensure UI matches state
            loadFavorites()
        }
    }
}
