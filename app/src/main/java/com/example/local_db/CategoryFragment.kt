package com.example.local_db

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.local_db.databinding.FragmentCategoryBinding
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: String): CategoryFragment {
            val fragment = CategoryFragment()
            val args = Bundle()
            args.putString(ARG_CATEGORY, category)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val category = arguments?.getString(ARG_CATEGORY) ?: ""
        
        loadItems(category)
    }

    private fun loadItems(category: String) {
        val sharedPref = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        lifecycleScope.launch {
            val favoriteIds = db.favoriteDao().getFavoriteIdsByUser(userEmail)
            val filteredItems = MenuProvider.allItems.filter { it.category == category }
            
            filteredItems.forEach { item ->
                item.isFavorite = favoriteIds.contains(item.id)
            }
            
            binding.rvItems.adapter = MenuAdapter(filteredItems, { menuItem ->
                toggleFavorite(menuItem, userEmail)
            }) {
                (activity as? BrowseActivity)?.updateCartBadge()
            }
        }
    }

    private fun toggleFavorite(item: MenuItem, userEmail: String) {
        lifecycleScope.launch {
            if (item.isFavorite) {
                val favoriteItem = FavoriteItem(
                    userEmail = userEmail,
                    id = item.id,
                    name = item.name,
                    description = item.description,
                    price = item.price,
                    category = item.category,
                    imageResId = item.imageResId
                )
                db.favoriteDao().addFavorite(favoriteItem)
            } else {
                db.favoriteDao().removeFavoriteById(item.id, userEmail)
            }
        }
    }

    // Removed getSampleItems() as data is now in MenuProvider

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
