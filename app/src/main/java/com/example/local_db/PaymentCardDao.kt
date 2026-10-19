package com.example.local_db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PaymentCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCard(card: PaymentCard)

    @Query("SELECT * FROM payment_cards WHERE userEmail = :email")
    suspend fun getCardsByUser(email: String): List<PaymentCard>

    @androidx.room.Delete
    suspend fun deleteCard(card: PaymentCard)
}
