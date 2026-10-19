package com.example.local_db

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Base Application class to handle global app initialization, 
 * specifically the Dark Mode preference.
 */
class CoffeeApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Notification Channels
        NotificationHelper.createNotificationChannel(this)
        
        // Load the saved Dark Mode preference from SharedPreferences
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("isDarkMode", false)
        
        // Apply the theme globally as soon as the app starts
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}