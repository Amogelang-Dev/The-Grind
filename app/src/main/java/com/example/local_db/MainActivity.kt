package com.example.local_db

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.local_db.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Collections

/**
 * MainActivity serves as the main dashboard of the application.
 * It displays categories and a list of available Grind Coffee Company locations.
 */
class MainActivity : AppCompatActivity() {
    // View binding to access layout elements efficiently
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private var allMenuItems = mutableListOf<MenuItem>()

    private val detailsLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            updateCartBadge()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        // Set the 'Home' item as selected in the bottom navigation
        binding.bottomNav.selectedItemId = R.id.nav_home

        setupDashboard()
        setupSuggestedItems()
        setupCoffeeAndTeas()
        setupSearch()
        updateCartBadge()
        
        // Trigger Promotion Notification (Mocked for once per day)
        checkAndSendPromotions()

        // Hide track order FAB by default, only show if order is active
        binding.fabTrackOrder.visibility = if (CartManager.isOrderActive()) View.VISIBLE else View.GONE

        // Handle the 'Track Order' floating action button click
        binding.fabTrackOrder.setOnClickListener {
            // Navigate to the order status tracking page
            val intent = Intent(this, OrderStatusActivity::class.java)
            startActivity(intent)
        }

        // Set up the bottom navigation listener to handle menu clicks
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home, no action needed
                    true
                }
                R.id.nav_browse -> {
                    // Navigate to the Merged Browse page
                    startActivity(Intent(this, BrowseActivity::class.java))
                    true
                }
                R.id.nav_orders -> {
                    // Navigate to the Cart/Orders page
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                R.id.nav_account -> {
                    // Navigate to the Profile/Account page
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> {
                    true
                }
            }
        }
    }

    fun launchProductDetails(intent: Intent) {
        detailsLauncher.launch(intent)
    }

    override fun onResume() {
        super.onResume()
        refreshUserData()
        // Ensure track order FAB is only visible if order is active
        binding.fabTrackOrder.visibility = if (CartManager.isOrderActive()) View.VISIBLE else View.GONE
    }

    private fun refreshUserData() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        lifecycleScope.launch {
            val user = db.userDao().getUserByEmail(userEmail)
            user?.let {
                val displayName = it.username
                binding.tvUserName.text = displayName
                
                // Personalize message
                binding.tvPersonalizedMessage.text = if (it.rewardsPoints > 5) {
                    "Your next coffee is almost here, $displayName!"
                } else {
                    "Ready for your daily grind, $displayName?"
                }

                // Load profile image
                it.profileImageUri?.let { uriString ->
                    binding.ivHomeProfile.setImageURI(Uri.parse(uriString))
                }
                
                // Update dynamic rewards on homepage
                val points = it.rewardsPoints
                val maxPoints = 10
                binding.tvHomeRewardsPoints.text = "$points / $maxPoints points to your next free coffee"
                binding.cpHomeRewards.progress = (points.toFloat() / maxPoints * 100).toInt()
            }
        }
    }

    private fun setupDashboard() {
        // Dynamic Greeting
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when (hour) {
            in 0..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            else -> "Good Evening,"
        }

        refreshUserData()

        // Navigate to Rewards
        binding.cardHomeRewards.setOnClickListener {
            startActivity(Intent(this, RewardsActivity::class.java))
        }
        
        // Quick access to profile from dashboard
        binding.ivHomeProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
    }

    private fun setupSuggestedItems() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        lifecycleScope.launch {
            val user = db.userDao().getUserByEmail(userEmail)
            val name = user?.username ?: "You"
            binding.tvSuggestedTitle.text = "Suggested for $name"
            
            val suggestions = mutableListOf(
                MenuItem(101, "Cappuccino", "Esspresso with steamed milk and foam.", "R38.99", null, "Drinks", R.drawable.drink_cuppacino),
                MenuItem(102, "Egg Benedict", "Poached eggs and hollandaise over muffins.", "R130.99", null, "Food", R.drawable.food_egg_benedict),
                MenuItem(103, "Iced Latte", "Chilled espresso mixed with cold milk.", "R41.99", null, "Drinks", R.drawable.drink_iced_latte),
                MenuItem(104, "Choc Peanut Bowl (Vegan)", "Healthy morning protein boost.", "R94.99", null, "Food", R.drawable.food_choc_pranut_bowl_vegan),
                MenuItem(105, "Cold Brew Coffee", "Ground cofee steeped in cold water.", "R44.99", null, "Drinks", R.drawable.drink_cold_brew_coffee),
                MenuItem(106, "Pain Au Chocolate", "Flaky puff pastry wrapping a chocolate bar inside.", "R29.99", null, "Food", R.drawable.food_pain_au_chocolate)
            )
            suggestions.shuffle()
            
            binding.rvSuggested.adapter = SuggestedAdapter(suggestions) {
                updateCartBadge()
            }
        }
    }

    private fun setupCoffeeAndTeas() {
        allMenuItems = MenuProvider.allItems.toMutableList()
        val displayItems = allMenuItems.shuffled().take(8)

        // Set to a vertical list for a better look with horizontal menu cards
        binding.rvCoffeeShops.layoutManager = LinearLayoutManager(this)
        binding.rvCoffeeShops.adapter = MenuAdapter(displayItems, onFavoriteClick = { item ->
            toggleFavorite(item)
        }, onAddToCart = {
            updateCartBadge()
        })
    }

    private fun setupSearch() {
        binding.etHomeSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMenu(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        // Handle keyboard "Search" button
        binding.etHomeSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etHomeSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    val match = MenuProvider.allItems.find { 
                        it.name.contains(query, ignoreCase = true) 
                    }
                    match?.let {
                        val intent = Intent(this, ProductDetailsActivity::class.java).apply {
                            putExtra("EXTRA_ID", it.id)
                            putExtra("EXTRA_NAME", it.name)
                            putExtra("EXTRA_PRICE", it.price)
                            putExtra("EXTRA_DESC", it.description)
                            putExtra("EXTRA_CATEGORY", it.category)
                            it.imageResId?.let { resId -> putExtra("EXTRA_IMAGE", resId) }
                        }
                        detailsLauncher.launch(intent)
                    }
                }
                true
            } else {
                false
            }
        }
    }

    private fun filterMenu(query: String) {
        val filteredList = if (query.isEmpty()) {
            allMenuItems.shuffled().take(8)
        } else {
            allMenuItems.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true) 
            }
        }

        val adapter = binding.rvCoffeeShops.adapter as? MenuAdapter
        adapter?.updateItems(filteredList)

        if (filteredList.isEmpty() && query.isNotEmpty()) {
            binding.rvCoffeeShops.visibility = View.GONE
            binding.tvNoSearchResults.visibility = View.VISIBLE
        } else {
            binding.rvCoffeeShops.visibility = View.VISIBLE
            binding.tvNoSearchResults.visibility = View.GONE
        }
    }

    private fun toggleFavorite(item: MenuItem) {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""
        
        lifecycleScope.launch {
            if (item.isFavorite) {
                val favoriteItem = FavoriteItem(
                    userEmail = userEmail,
                    id = item.id,
                    name = item.name,
                    description = item.description,
                    price = item.price,
                    category = item.category,
                    imageResId = item.imageResId
                )
                db.favoriteDao().addFavorite(favoriteItem)
            } else {
                db.favoriteDao().removeFavoriteById(item.id, userEmail)
            }
        }
    }

    private fun updateCartBadge() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        lifecycleScope.launch {
            val count = CartManager.getCartCount(this@MainActivity, userEmail)
            val badge = binding.bottomNav.getOrCreateBadge(R.id.nav_orders)
            if (count > 0) {
                badge.isVisible = true
                badge.number = count
            } else {
                badge.isVisible = false
            }
        }
    }

    private fun checkAndSendPromotions() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val lastPromoDate = sharedPref.getString("last_promo_date", "")
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

        if (lastPromoDate != today) {
            // Send a random promotion or special
            val promos = listOf(
                "Exclusive Deal" to "Buy one coffee, get a muffin 50% off today!",
                "New Special" to "Try our seasonal Honey Lavender Latte, now available!",
                "Reward Alert" to "You're close to a free coffee! Place an order today to earn more points."
            )
            val randomPromo = promos.random()
            NotificationHelper.sendNotification(this, randomPromo.first, randomPromo.second)

            // Save today's date so we don't send another one until tomorrow
            sharedPref.edit().putString("last_promo_date", today).apply()
        }
    }
}