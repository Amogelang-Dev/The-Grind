package com.example.local_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * AppDatabase is the main entry point for the Room database in this application.
 * It follows the Singleton pattern to ensure only one instance of the database exists at any time.
 * 
 * Entities managed by this database:
 * - User: User account details and reward points.
 * - FavoriteItem: Menu items favorited by users.
 * - PaymentCard: Saved credit/debit card information.
 * - CartItem: Items currently in the user's shopping cart.
 * - OrderHistoryItem: Records of past completed orders.
 */
@Database(entities = [User::class, FavoriteItem::class, PaymentCard::class, CartItem::class, OrderHistoryItem::class], version = 16)
abstract class AppDatabase : RoomDatabase() {
    
    // Data Access Objects (DAOs) for interacting with specific tables
    abstract fun userDao(): UserDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun paymentCardDao(): PaymentCardDao
    abstract fun cartDao(): CartDao
    abstract fun orderHistoryDao(): OrderHistoryDao

    companion object {
        // Volatile ensures that the value of INSTANCE is always up-to-date and the same for all execution threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton instance of the AppDatabase.
         * If it doesn't exist, it builds the database using Room.databaseBuilder.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coffee_shop_db"
                )
                // Wipes and rebuilds the database if the version is incremented without a migration path
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}