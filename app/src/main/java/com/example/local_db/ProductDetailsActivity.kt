package com.example.local_db

import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.local_db.databinding.ActivityProductDetailsBinding
import kotlinx.coroutines.launch

/**
 * ProductDetailsActivity displays detailed information about a specific menu item.
 * It allows users to read the item's description and add it to their cart.
 */
class ProductDetailsActivity : AppCompatActivity() {

    // View binding is used to access layout elements without using findViewById
    private lateinit var binding: ActivityProductDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize view binding and set the layout
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        // Set up the back button in the toolbar to close this activity
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Retrieve product information passed from the previous screen via Intent extras
        val id = intent.getIntExtra("EXTRA_ID", -1)
        val name = intent.getStringExtra("EXTRA_NAME") ?: "Product"
        val price = intent.getStringExtra("EXTRA_PRICE") ?: "R0.00"
        val description = intent.getStringExtra("EXTRA_DESC") ?: ""
        val category = intent.getStringExtra("EXTRA_CATEGORY") ?: ""
        val imageResId = intent.getIntExtra("EXTRA_IMAGE", -1)

        // Populate the UI elements with the retrieved data
        binding.tvProductName.text = name
        binding.tvProductPrice.text = price
        binding.tvProductDescription.text = description

        // Load the image
        if (imageResId != -1) {
            binding.ivProductImage.setImageResource(imageResId)
        } else {
            binding.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Sugar Selection Logic
        var sugarCount = 0
        val sugarPricePerUnit = 5.0
        
        if (category.contains("Drinks", ignoreCase = true)) {
            binding.llSugarSelection.visibility = android.view.View.VISIBLE
        }

        fun updatePriceDisplay() {
            val basePriceValue = try {
                price.replace("R", "").replace(",", "").toDouble()
            } catch (e: Exception) {
                0.0
            }
            val totalPrice = basePriceValue + (sugarCount * sugarPricePerUnit)
            binding.tvProductPrice.text = "R${String.format("%.2f", totalPrice)}"
            
            // Toggle Sugar Type Visibility
            binding.llSugarType.visibility = if (sugarCount > 0) android.view.View.VISIBLE else android.view.View.GONE
        }

        binding.btnAddSugar.setOnClickListener {
            sugarCount++
            binding.tvSugarCount.text = sugarCount.toString()
            updatePriceDisplay()
        }

        binding.btnRemoveSugar.setOnClickListener {
            if (sugarCount > 0) {
                sugarCount--
                binding.tvSugarCount.text = sugarCount.toString()
                updatePriceDisplay()
            }
        }

        // Setup Quantity Spinner
        val quantities = (1..10).toList().map { it.toString() }
        val qAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, quantities)
        qAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerQuantityDetail.adapter = qAdapter
        binding.spinnerQuantityDetail.setSelection(0)

        // Handle 'Add to Cart' button click inside the details screen
        binding.btnAddToCartDetail.setOnClickListener {
            lifecycleScope.launch {
                val quantity = binding.spinnerQuantityDetail.selectedItem.toString().toIntOrNull() ?: 1
                
                // Construct customization string
                val sugarType = if (binding.rbBrownSugar.isChecked) "Brown" else "White"
                val customization = if (sugarCount > 0) "$sugarCount $sugarType Sugar(s)" else ""
                
                // Get the final price (which includes sugar cost)
                val finalPrice = binding.tvProductPrice.text.toString()

                val menuItem = MenuItem(
                    id = id, 
                    name = name, 
                    description = description, 
                    price = finalPrice, 
                    originalPrice = null, 
                    category = category, 
                    imageResId = if (imageResId != -1) imageResId else null, 
                    isFavorite = false
                )
                CartManager.addToCart(this@ProductDetailsActivity, userEmail, menuItem, quantity, customization)
                Toast.makeText(this@ProductDetailsActivity, "$quantity x $name added to cart", Toast.LENGTH_SHORT).show()
                
                // Result OK will tell MainActivity to refresh its badge
                setResult(RESULT_OK)
                // Close the details screen and return to the menu
                finish()
            }
        }
    }
}
