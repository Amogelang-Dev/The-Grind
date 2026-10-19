package com.example.local_db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val cartId: Int = 0,
    val userEmail: String,
    val id: Int, // MenuItem ID
    val name: String,
    val price: String,
    var quantity: Int,
    val customization: String,
    val category: String = "",
    val imageResId: Int? = null
)
