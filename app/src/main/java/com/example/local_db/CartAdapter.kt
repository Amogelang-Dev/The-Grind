package com.example.local_db

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.local_db.databinding.ItemCartProductBinding

class CartAdapter(
    private var items: MutableList<CartItem>,
    private val onDeleteClick: (CartItem) -> Unit,
    private val onQuantityChange: (CartItem, Int) -> Unit,
    private val onItemClick: (CartItem) -> Unit = {},
    private val onEditSugarClick: (CartItem) -> Unit = {}
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCartProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            // Setup Quantity Spinner
            val quantities = (1..10).toList().map { it.toString() }
            val adapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, quantities)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerQuantity.adapter = adapter
            
            // Set current quantity
            spinnerQuantity.setSelection(item.quantity - 1)

            spinnerQuantity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    val newQuantity = quantities[pos].toInt()
                    if (newQuantity != item.quantity) {
                        onQuantityChange(item, newQuantity)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            tvProductName.text = item.name
            tvProductPrice.text = item.price
            tvProductCustomization.text = item.customization

            // Bind Image
            item.imageResId?.let {
                ivCartProduct.setImageResource(it)
            } ?: run {
                ivCartProduct.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            btnDelete.setOnClickListener {
                onDeleteClick(item)
            }

            // --- ITEM CLICK LOGIC (View Details) ---
            root.setOnClickListener {
                onItemClick(item)
            }

            // --- SUGAR EDIT LOGIC ---
            tvProductCustomization.setOnClickListener {
                onEditSugarClick(item)
            }
            btnEditCustomization.setOnClickListener {
                onEditSugarClick(item)
            }
        }
    }

    fun updateItems(newItems: List<CartItem>) {
        this.items = newItems.toMutableList()
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
}
