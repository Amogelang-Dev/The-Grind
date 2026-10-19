package com.example.local_db

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.local_db.databinding.ItemSpecialBinding
import kotlinx.coroutines.launch

/**
 * MenuAdapter manages the list of menu items (specials, drinks, food, etc.).
 * It handles displaying the item data, click events for details, and toggling favorites.
 */
class MenuAdapter(
    private var items: List<MenuItem>,
    private val onFavoriteClick: (MenuItem) -> Unit = {},
    private val onAddToCart: () -> Unit = {}
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    // ViewHolder holds the binding for an individual item row
    class ViewHolder(val binding: ItemSpecialBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the item layout using view binding
        val binding = ItemSpecialBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            // Set standard text fields
            tvSpecialName.text = item.name
            tvSpecialDesc.text = item.description
            tvSpecialPrice.text = item.price

            // Handle image
            item.imageResId?.let {
                ivSpecialImage.setImageResource(it)
            } ?: run {
                ivSpecialImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
            
            // Handle original price (strike-through) if it exists
            if (item.originalPrice != null) {
                tvOriginalPrice.text = item.originalPrice
                tvOriginalPrice.visibility = View.VISIBLE
            } else {
                tvOriginalPrice.visibility = View.GONE
            }

            // --- FAVORITE BUTTON LOGIC ---
            // Update the star icon based on whether the item is a favorite
            val favoriteIcon = if (item.isFavorite) {
                android.R.drawable.btn_star_big_on // Filled star
            } else {
                android.R.drawable.btn_star_big_off // Outline star
            }
            btnFavorite.setIconResource(favoriteIcon)
            
            // Change star color (gold for favorites, grey for others)
            val iconTint = if (item.isFavorite) R.color.coffee_accent else R.color.grey_dark
            btnFavorite.setIconTintResource(iconTint)

            // Handle clicking the favorite star
            btnFavorite.setOnClickListener {
                item.isFavorite = !item.isFavorite // Toggle the state
                
                // Show a confirmation toast to the user
                val message = if (item.isFavorite) "Added to Favorites" else "Removed from Favorites"
                Toast.makeText(holder.itemView.context, message, Toast.LENGTH_SHORT).show()
                
                // Trigger the callback to save/remove from database
                onFavoriteClick(item)
                
                // Refresh only this item in the list to update the icon color
                notifyItemChanged(position)
            }

            // --- ADD TO CART BUTTON LOGIC ---
            btnAddToCart.setOnClickListener {
                // Use a coroutine since addToCart is now suspend
                val context = holder.itemView.context
                if (context is androidx.lifecycle.LifecycleOwner) {
                    val lifecycleOwner = context as androidx.lifecycle.LifecycleOwner
                    lifecycleOwner.lifecycleScope.launch {
                        val sharedPref = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                        val userEmail = sharedPref.getString("userEmail", "") ?: ""
                        CartManager.addToCart(context, userEmail, item, 1)
                        Toast.makeText(context, "${item.name} added to cart", Toast.LENGTH_SHORT).show()
                        onAddToCart()
                    }
                }
            }

            // When the entire item card is tapped, navigate to the details screen
            root.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, ProductDetailsActivity::class.java).apply {
                    // Pass the item's data to the details activity via Intent extras
                    putExtra("EXTRA_ID", item.id)
                    putExtra("EXTRA_NAME", item.name)
                    putExtra("EXTRA_PRICE", item.price)
                    putExtra("EXTRA_DESC", item.description)
                    putExtra("EXTRA_CATEGORY", item.category)
                    item.imageResId?.let { putExtra("EXTRA_IMAGE", it) }
                }
                
                // If the context is MainActivity, use the launcher to get a result back
                if (context is MainActivity) {
                    // We can't access detailsLauncher directly if it's private, but we can call a method
                    context.launchProductDetails(intent)
                } else if (context is BrowseActivity) {
                    context.launchProductDetails(intent)
                } else {
                    context.startActivity(intent)
                }
            }
        }
    }

    fun updateItems(newItems: List<MenuItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}
