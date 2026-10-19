package com.example.local_db

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.local_db.databinding.ItemSuggestedBinding
import kotlinx.coroutines.launch

class SuggestedAdapter(
    private val items: List<MenuItem>,
    private val onAddToCart: () -> Unit = {}
) : RecyclerView.Adapter<SuggestedAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSuggestedBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSuggestedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvItemName.text = item.name
            tvItemPrice.text = item.price
            
            // Handle image
            item.imageResId?.let {
                ivItemImage.setImageResource(it)
            } ?: run {
                ivItemImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            btnAdd.setOnClickListener {
                if (holder.itemView.context is LifecycleOwner) {
                    val context = holder.itemView.context
                    val lifecycleOwner = context as LifecycleOwner
                    lifecycleOwner.lifecycleScope.launch {
                        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        val userEmail = sharedPref.getString("userEmail", "") ?: ""
                        CartManager.addToCart(context, userEmail, item)
                        Toast.makeText(context, "${item.name} added to cart", Toast.LENGTH_SHORT).show()
                        onAddToCart()
                    }
                }
            }

            root.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, ProductDetailsActivity::class.java).apply {
                    putExtra("EXTRA_ID", item.id)
                    putExtra("EXTRA_NAME", item.name)
                    putExtra("EXTRA_PRICE", item.price)
                    putExtra("EXTRA_DESC", item.description)
                    putExtra("EXTRA_CATEGORY", item.category)
                    item.imageResId?.let { putExtra("EXTRA_IMAGE", it) }
                }
                
                if (context is MainActivity) {
                    context.launchProductDetails(intent)
                } else if (context is BrowseActivity) {
                    context.launchProductDetails(intent)
                } else {
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount() = items.size
}
