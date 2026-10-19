package com.example.local_db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * User represents a customer account in the system.
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"], unique = true)
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val username: String,
    val email: String,
    val password: String,
    val profileImageUri: String? = null,
    val rewardsPoints: Int = 0
)