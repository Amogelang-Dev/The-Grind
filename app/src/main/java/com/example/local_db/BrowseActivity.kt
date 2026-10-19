package com.example.local_db

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.local_db.databinding.ActivityBrowseBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

/**
 * BrowseActivity allows users to view categories or swipe through all items.
 * Starts with a category overview, then switches to a swipeable view on selection.
 */
class BrowseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBrowseBinding
    private var isDetailViewVisible = false

    private lateinit var db: AppDatabase

    private val detailsLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            updateCartBadge()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrowseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        setupViewPager()
        setupBottomNav()
        setupCategoryCards()
        setupSampleSpecials()
        setupSearch()
        updateCartBadge()

        // Handle 'Track Order' floating action button click
        binding.fabTrackOrder.visibility = if (CartManager.isOrderActive()) View.VISIBLE else View.GONE
        binding.fabTrackOrder.setOnClickListener {
            startActivity(Intent(this, OrderStatusActivity::class.java))
        }

        // Handle the back navigation icon in the toolbar
        binding.toolbar.setNavigationOnClickListener {
            if (binding.etSearch.text.isNotEmpty()) {
                binding.etSearch.text.clear()
            } else if (isDetailViewVisible) {
                hideDetailView()
            } else {
                finish()
            }
        }

        // Handle physical back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.etSearch.text.isNotEmpty()) {
                    binding.etSearch.text.clear()
                } else if (isDetailViewVisible) {
                    hideDetailView()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterSearch(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text.toString().trim()
                val match = MenuProvider.allItems.find { it.name.contains(query, ignoreCase = true) }
                match?.let { openProductDetails(it.id, it.name, it.price, it.description, it.category, it.imageResId) }
                true
            } else false
        }
    }

    private fun filterSearch(query: String) {
        if (query.isEmpty()) {
            binding.rvSearchResults.visibility = View.GONE
            binding.tvNoSearchResultsBrowse.visibility = View.GONE
            binding.tvToolbarTitle.text = getString(R.string.nav_browse)
            
            if (isDetailViewVisible) {
                binding.viewPager.visibility = View.VISIBLE
                binding.tabLayout.visibility = View.VISIBLE
            } else {
                binding.nsvCategoryOverview.visibility = View.VISIBLE
            }
            return
        }

        // Hide main content
        binding.nsvCategoryOverview.visibility = View.GONE
        binding.viewPager.visibility = View.GONE
        binding.tabLayout.visibility = View.GONE
        binding.tvToolbarTitle.text = "Search Results"

        val filtered = MenuProvider.allItems.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.description.contains(query, ignoreCase = true) 
        }

        if (filtered.isEmpty()) {
            binding.rvSearchResults.visibility = View.GONE
            binding.tvNoSearchResultsBrowse.visibility = View.VISIBLE
        } else {
            binding.rvSearchResults.visibility = View.VISIBLE
            binding.tvNoSearchResultsBrowse.visibility = View.GONE
            
            binding.rvSearchResults.adapter = MenuAdapter(filtered, { item ->
                toggleFavorite(item, "") // Email handling not needed for search list simple toggle
            }, {
                updateCartBadge()
            })
        }
    }

    override fun onResume() {
        super.onResume()
        updateCartBadge()
        // Ensure track order FAB is only visible if order is active
        binding.fabTrackOrder.visibility = if (CartManager.isOrderActive()) View.VISIBLE else View.GONE
    }

    fun launchProductDetails(intent: Intent) {
        detailsLauncher.launch(intent)
    }

    private fun setupViewPager() {
        val pagerAdapter = CategoryPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = pagerAdapter.getCategoryName(position)
        }.attach()
    }

    private fun setupCategoryCards() {
        binding.cardDrinksMenu.setOnClickListener { 
            animateClick(it) { showDetailView(0) }
        }
        binding.cardFoodMenu.setOnClickListener { 
            animateClick(it) { showDetailView(1) }
        }
        binding.cardSidesMenu.setOnClickListener { 
            animateClick(it) { showDetailView(2) }
        }
    }

    private fun animateClick(view: View, onComplete: () -> Unit) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .withEndAction {
                        onComplete()
                    }
                    .start()
            }
            .start()
    }

    private fun showDetailView(position: Int) {
        isDetailViewVisible = true
        binding.viewPager.setCurrentItem(position, false)
        
        // Animate the transition
        val transition = AutoTransition()
        transition.duration = 300
        TransitionManager.beginDelayedTransition(binding.root, transition)

        binding.nsvCategoryOverview.visibility = View.GONE
        binding.viewPager.visibility = View.VISIBLE
        binding.tabLayout.visibility = View.VISIBLE
    }

    private fun hideDetailView() {
        isDetailViewVisible = false
        
        // Animate the transition
        val transition = AutoTransition()
        transition.duration = 300
        TransitionManager.beginDelayedTransition(binding.root, transition)

        binding.nsvCategoryOverview.visibility = View.VISIBLE
        binding.viewPager.visibility = View.GONE
        binding.tabLayout.visibility = View.GONE
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_browse
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_browse -> true
                R.id.nav_orders -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_account -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> true
            }
        }
    }

    private fun setupSampleSpecials() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        val specials = MenuProvider.allItems.filter { it.originalPrice != null }

        lifecycleScope.launch {
            val favoriteIds = db.favoriteDao().getFavoriteIdsByUser(userEmail)
            specials.forEach { it.isFavorite = favoriteIds.contains(it.id) }

            binding.rvWeeklySpecials.adapter = MenuAdapter(specials, { item ->
                toggleFavorite(item, userEmail)
            }, {
                updateCartBadge()
            })
        }
    }

    private fun toggleFavorite(item: MenuItem, userEmail: String) {
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

    private fun openProductDetails(id: Int, name: String, price: String, description: String, category: String, imageResId: Int?) {
        val intent = Intent(this, ProductDetailsActivity::class.java).apply {
            putExtra("EXTRA_ID", id)
            putExtra("EXTRA_NAME", name)
            putExtra("EXTRA_PRICE", price)
            putExtra("EXTRA_DESC", description)
            putExtra("EXTRA_CATEGORY", category)
            imageResId?.let { putExtra("EXTRA_IMAGE", it) }
        }
        detailsLauncher.launch(intent)
    }

    fun updateCartBadge() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        lifecycleScope.launch {
            val count = CartManager.getCartCount(this@BrowseActivity, userEmail)
            val badge = binding.bottomNav.getOrCreateBadge(R.id.nav_orders)
            if (count > 0) {
                badge.isVisible = true
                badge.number = count
            } else {
                badge.isVisible = false
            }
        }
    }
}
