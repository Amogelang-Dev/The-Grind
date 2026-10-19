package com.example.local_db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites", primaryKeys = ["id", "userEmail"])
data class FavoriteItem(
    val userEmail: String,
    val id: Int, // MenuItem ID
    val name: String,
    val description: String,
    val price: String,
    val category: String,
    val imageResId: Int? = null
)
