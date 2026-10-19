package com.example.local_db

data class MenuItem(
    val id: Int,
    val name: String,
    val description: String = "",
    val price: String = "",
    val originalPrice: String? = null,
    val category: String = "",
    val imageResId: Int? = null,
    var isFavorite: Boolean = false,
    val isHeader: Boolean = false
)
