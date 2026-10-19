package com.example.local_db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_history")
data class OrderHistoryItem(
    @PrimaryKey(autoGenerate = true) val orderId: Int = 0,
    val userEmail: String,
    val date: String,
    val totalAmount: String,
    val itemsSummary: String, // e.g. "2 items" or "Latte, Muffin"
    val status: String,
    val fullItemsJson: String = "" // JSON list of CartItem details
)