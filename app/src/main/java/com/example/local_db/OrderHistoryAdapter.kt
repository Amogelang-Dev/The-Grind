package com.example.local_db

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.local_db.databinding.ItemOrderHistoryBinding

class OrderHistoryAdapter(private val orders: List<OrderHistoryItem>) :
    RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemOrderHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        holder.binding.apply {
            tvOrderDate.text = order.date
            tvOrderTotal.text = order.totalAmount
            tvOrderSummary.text = order.itemsSummary
            tvOrderStatus.text = order.status
            
            root.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, OrderDetailActivity::class.java).apply {
                    putExtra("EXTRA_ORDER_ID", order.orderId)
                    putExtra("EXTRA_DATE", order.date)
                    putExtra("EXTRA_TOTAL", order.totalAmount)
                    putExtra("EXTRA_ITEMS_JSON", order.fullItemsJson)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = orders.size
}