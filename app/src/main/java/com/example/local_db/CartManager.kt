package com.example.local_db

import android.content.Context

object CartManager {
    // We'll use the database now instead of SharedPreferences
    
    suspend fun getCartCount(context: Context, email: String): Int {
        val db = AppDatabase.getDatabase(context)
        return db.cartDao().getCartCountByUser(email) ?: 0
    }

    suspend fun addToCart(context: Context, email: String, menuItem: MenuItem, quantity: Int = 1, customization: String = "") {
        val db = AppDatabase.getDatabase(context)
        val existingItems = db.cartDao().getCartItemsByUser(email)
        
        // Find existing item with SAME ID and SAME customization
        val existingItem = existingItems.find { it.id == menuItem.id && it.customization == customization }

        if (existingItem != null) {
            existingItem.quantity += quantity
            db.cartDao().addToCart(existingItem)
        } else {
            val cartItem = CartItem(
                userEmail = email,
                id = menuItem.id,
                name = menuItem.name,
                price = menuItem.price,
                quantity = quantity,
                customization = customization,
                category = menuItem.category,
                imageResId = menuItem.imageResId
            )
            db.cartDao().addToCart(cartItem)
        }
    }

    suspend fun clearCart(context: Context, email: String) {
        val db = AppDatabase.getDatabase(context)
        db.cartDao().clearCartByUser(email)
    }

    // For simplicity, we'll keep order status in memory or just use a simple flag for now
    private var isOrderActiveFlag = false

    fun setOrderActive(active: Boolean) {
        isOrderActiveFlag = active
    }

    fun isOrderActive(): Boolean {
        return isOrderActiveFlag
    }

    /**
     * Automatically calculates and adds reward points based on coffee items in an order.
     * Points are only awarded for items in the "Drinks" category.
     */
    suspend fun addRewardPointsForOrder(context: android.content.Context, email: String, cartItems: List<CartItem>) {
        val db = AppDatabase.getDatabase(context)
        
        // Count total quantity of items in the "Drinks" category
        val coffeePointsEarned = cartItems.filter { item ->
            item.category.contains("Drinks", ignoreCase = true)
        }.sumOf { it.quantity }

        if (coffeePointsEarned > 0) {
            val user = db.userDao().getUserByEmail(email)
            user?.let {
                val updatedUser = it.copy(rewardsPoints = it.rewardsPoints + coffeePointsEarned)
                db.userDao().updateUser(updatedUser)
            }
        }
    }
}
