package com.example.local_db

import androidx.room.*

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(item: CartItem)

    @Delete
    suspend fun removeFromCart(item: CartItem)

    @Query("SELECT * FROM cart_items WHERE userEmail = :email")
    suspend fun getCartItemsByUser(email: String): List<CartItem>

    @Query("DELETE FROM cart_items WHERE userEmail = :email")
    suspend fun clearCartByUser(email: String)

    @Query("SELECT SUM(quantity) FROM cart_items WHERE userEmail = :email")
    suspend fun getCartCountByUser(email: String): Int?
}