package com.example.local_db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_cards")
data class PaymentCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val nameOnCard: String,
    val cardNumber: String,
    val expiryDate: String,
    val cvv: String,
    val nickname: String? = null
)
