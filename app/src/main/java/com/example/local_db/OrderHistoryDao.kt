package com.example.local_db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface OrderHistoryDao {
    @Insert
    suspend fun addOrder(order: OrderHistoryItem)

    @Query("SELECT * FROM order_history WHERE userEmail = :email ORDER BY orderId DESC")
    suspend fun getOrdersByUser(email: String): List<OrderHistoryItem>

    @Query("DELETE FROM order_history WHERE userEmail = :email")
    suspend fun clearHistoryByUser(email: String)
}