package com.example.local_db

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.local_db.databinding.ActivityOrderHistoryBinding
import kotlinx.coroutines.launch
import java.util.*

/**
 * OrderHistoryActivity allows users to view and filter their previous coffee orders.
 */
class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        // Toolbar back navigation
        binding.toolbar.setNavigationOnClickListener { finish() }

        // Handle Date Filtering
        binding.btnFilterDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnGoToMenu.setOnClickListener {
            val intent = Intent(this, BrowseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        loadOrderHistory()
    }

    private fun loadOrderHistory() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        lifecycleScope.launch {
            val orders = db.orderHistoryDao().getOrdersByUser(userEmail)
            if (orders.isEmpty()) {
                binding.llNoOrders.visibility = View.VISIBLE
                binding.rvOrderHistory.visibility = View.GONE
            } else {
                binding.llNoOrders.visibility = View.GONE
                binding.rvOrderHistory.visibility = View.VISIBLE
                binding.rvOrderHistory.adapter = OrderHistoryAdapter(orders)
            }
        }
    }

    /**
     * Shows a date picker dialog to filter orders.
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Filtering logic based on selected date would go here
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.btnFilterDate.text = selectedDate
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}