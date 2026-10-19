package com.example.local_db

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class CategoryPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val categories = listOf("Drinks", "Food", "Sides")

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        return CategoryFragment.newInstance(categories[position])
    }

    fun getCategoryName(position: Int): String {
        return categories[position]
    }
}
