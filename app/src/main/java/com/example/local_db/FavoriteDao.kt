package com.example.local_db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(item: FavoriteItem)

    @Delete
    suspend fun removeFavorite(item: FavoriteItem)

    @Query("DELETE FROM favorites WHERE id = :menuItemId AND userEmail = :email")
    suspend fun removeFavoriteById(menuItemId: Int, email: String)

    @Query("SELECT * FROM favorites WHERE userEmail = :email")
    suspend fun getFavoritesByUser(email: String): List<FavoriteItem>

    @Query("SELECT id FROM favorites WHERE userEmail = :email")
    suspend fun getFavoriteIdsByUser(email: String): List<Int>
}
