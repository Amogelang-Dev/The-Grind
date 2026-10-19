package com.example.local_db

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.local_db.databinding.ActivityCartBinding
import com.example.local_db.databinding.DialogAddCardBinding
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * CartActivity handles the shopping cart experience.
 * Key responsibilities:
 * - Displaying items in the cart (from Room database).
 * - Updating quantities or removing items.
 * - Calculating order totals (subtotal, tax, discounts).
 * - Managing payment methods (Google Pay, saved cards, cash).
 * - Processing the final order and updating user rewards/history.
 */
class CartActivity : AppCompatActivity() {
    
    // View binding and database references
    private lateinit var binding: ActivityCartBinding
    private lateinit var paymentsClient: PaymentsClient
    private lateinit var db: AppDatabase
    
    // State management for payment and discounts
    private var savedCards: List<PaymentCard> = emptyList()
    private var selectedCard: PaymentCard? = null
    private var discountPercentage = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate layout using view binding
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize local Room database
        db = AppDatabase.getDatabase(this)

        // --- GOOGLE PAY INITIALIZATION ---
        // Sets up the environment for mock/test Google Pay payments
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST) 
            .build()
        paymentsClient = Wallet.getPaymentsClient(this, walletOptions)

        // Set 'Orders' tab as selected in bottom navigation
        binding.bottomNav.selectedItemId = R.id.nav_orders
        updateCartBadge()

        // Setup toolbar back navigation
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Initialize cart list and payment data
        setupCartItems()
        loadSavedCards()

        // --- PAYMENT METHOD SELECTION LOGIC ---
        binding.rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbCardPayment) {
                // Show card selection dropdown if Card Payment is selected
                binding.llCardSelection.visibility = View.VISIBLE
                if (savedCards.isEmpty()) {
                    // Prompt to add a card if none are saved
                    showAddCardDialog()
                }
            } else {
                // Hide card selection for other methods (like cash)
                binding.llCardSelection.visibility = View.GONE
            }
        }

        // Allow adding a new card from the cart screen
        binding.tvAddNewCardCart.setOnClickListener {
            showAddCardDialog()
        }

        // --- FINAL CHECKOUT LOGIC ---
        binding.btnPlaceOrder.setOnClickListener {
            val selectedPaymentId = binding.rgPaymentMethod.checkedRadioButtonId
            
            // UI Validation: Ensure payment method is selected
            if (selectedPaymentId == -1) {
                Toast.makeText(this, "Please select a payment method.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // UI Validation: Ensure card is selected if card payment is chosen
            if (selectedPaymentId == R.id.rbCardPayment && selectedCard == null) {
                Toast.makeText(this, "Please select or add a card.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Determine readable payment method name for summary
            val paymentMethod = if (selectedPaymentId == R.id.rbCardPayment) {
                getString(R.string.card_payment)
            } else {
                getString(R.string.pay_upon_pickup)
            }

            // Show loading state while simulating order processing
            binding.llButtons.visibility = View.INVISIBLE
            binding.pbLoading.visibility = View.VISIBLE

            lifecycleScope.launch {
                // Artificial delay to simulate server communication or payment processing
                delay(2500)

                val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val userEmail = sharedPref.getString("userEmail", "") ?: ""

                // Save completed order details to history
                val summary = if (cartItems.size == 1) "1 item" else "${cartItems.size} items"
                val totalStr = binding.tvTotalVal.text.toString()
                
                // Serialize item list to JSON for storage in history
                val gson = com.google.gson.Gson()
                val itemsJson = gson.toJson(cartItems)

                val order = OrderHistoryItem(
                    userEmail = userEmail,
                    date = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date()),
                    totalAmount = totalStr,
                    itemsSummary = summary,
                    status = "Completed",
                    fullItemsJson = itemsJson
                )
                db.orderHistoryDao().addOrder(order)

                // Update user rewards (earn points for coffee purchases)
                CartManager.addRewardPointsForOrder(this@CartActivity, userEmail, cartItems)

                // Mark order as active and clear the temporary cart
                CartManager.setOrderActive(true)
                CartManager.clearCart(this@CartActivity, userEmail)

                // Setup transition to the Order Tracking page
                val intent = Intent(this@CartActivity, OrderStatusActivity::class.java)
                
                // Calculate estimated pickup time (Current + 15 minutes)
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MINUTE, 15)
                val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
                val pickupTime = "${timeFormat.format(calendar.time)} (In 15 mins)"
                
                intent.putExtra("EXTRA_PICKUP_TIME", pickupTime)

                // Enqueue background notifications for order status updates
                scheduleOrderNotifications()

                // Finalize checkout and navigate
                Toast.makeText(this@CartActivity, "Order placed with $paymentMethod", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            }
        }

        // 'Add more items' takes user back to the menu
        binding.tvAddMoreItems.setOnClickListener {
            val intent = Intent(this, BrowseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // 'Continue Shopping' logic (same as add more items)
        binding.btnContinueShopping.setOnClickListener {
            val intent = Intent(this, BrowseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // Button to track an existing active order
        binding.btnTrackOrder.setOnClickListener {
            val intent = Intent(this, OrderStatusActivity::class.java)
            startActivity(intent)
        }

        binding.btnEmptyContinueShopping.setOnClickListener {
            val intent = Intent(this, BrowseActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        // --- DISCOUNT CODE LOGIC ---
        binding.btnApplyDiscount.setOnClickListener {
            val code = binding.etDiscountCode.text.toString().trim().uppercase()
            if (code == "GRIND20") {
                discountPercentage = 0.20
                Toast.makeText(this, "Discount Applied! 20% off", Toast.LENGTH_SHORT).show()
                updateSummary()
                binding.btnApplyDiscount.isEnabled = false
                binding.etDiscountCode.isEnabled = false
            } else if (code.isEmpty()) {
                Toast.makeText(this, "Please enter a code", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Invalid Discount Code", Toast.LENGTH_SHORT).show()
            }
        }

        // --- BOTTOM NAVIGATION HANDLERS ---
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_browse -> {
                    startActivity(Intent(this, BrowseActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_orders -> true
                R.id.nav_account -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> true
            }
        }
    }

    private fun loadSavedCards(autoSelect: PaymentCard? = null) {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        lifecycleScope.launch {
            savedCards = db.paymentCardDao().getCardsByUser(userEmail)
            val cardNames = savedCards.map { card ->
                if (!card.nickname.isNullOrEmpty()) {
                    "${card.nickname} (**** ${card.cardNumber.takeLast(4)})"
                } else {
                    "**** " + card.cardNumber.takeLast(4)
                }
            }
            val adapter = ArrayAdapter(this@CartActivity, android.R.layout.simple_dropdown_item_1line, cardNames)
            binding.actvCardSelector.setAdapter(adapter)

            // If a specific card should be auto-selected (like after adding a new one)
            autoSelect?.let { card ->
                selectedCard = card
                val displayName = if (!card.nickname.isNullOrEmpty()) {
                    "${card.nickname} (**** ${card.cardNumber.takeLast(4)})"
                } else {
                    "**** " + card.cardNumber.takeLast(4)
                }
                binding.actvCardSelector.setText(displayName, false)
            }

            binding.actvCardSelector.setOnItemClickListener { _, _, position, _ ->
                selectedCard = savedCards[position]
            }
        }
    }

    /**
     * Shows a pop-up dialog for entering card details.
     * Includes security recommendations and saves card to account if 'shouldSave' is checked.
     */
    private fun showAddCardDialog() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""
        val dialogBinding = DialogAddCardBinding.inflate(layoutInflater)

        // Default card name
        dialogBinding.etCardName.setText("Customer")

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        // --- CARD NUMBER FORMATTER ---
        dialogBinding.etCardNumber.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val space = ' '

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating || s == null) return
                isUpdating = true

                val originalString = s.toString()
                val digitsOnly = originalString.replace(space.toString(), "")
                
                val formatted = StringBuilder()
                for (i in digitsOnly.indices) {
                    formatted.append(digitsOnly[i])
                    if ((i + 1) % 4 == 0 && (i + 1) < digitsOnly.length) {
                        formatted.append(space)
                    }
                }

                val newString = formatted.toString()
                if (newString != originalString) {
                    // This in-place replacement is safer than setText() for maintaining cursor state
                    s.replace(0, s.length, newString)
                }

                isUpdating = false
            }
        })

        // --- EXPIRY DATE AUTO-FORMATTER ---
        // Automatically adds a '/' after the month is typed (e.g., '12' becomes '12/')
        dialogBinding.etExpiry.addTextChangedListener(object : TextWatcher {
            private var isDeleting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                isDeleting = count > after
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s == null || isDeleting) return
                if (s.length == 2 && !s.contains("/")) {
                    s.append("/")
                }
            }
        })

        // Handle 'Use Card' button click inside the pop-up
        dialogBinding.btnSaveCard.setOnClickListener {
            val cardName = dialogBinding.etCardName.text.toString().trim()
            val cardNickname = dialogBinding.etCardNickname.text.toString().trim()
            val cardNumber = dialogBinding.etCardNumber.text.toString().replace(" ", "").trim()
            val expiry = dialogBinding.etExpiry.text.toString().trim()
            val cvv = dialogBinding.etCvv.text.toString().trim()
            val shouldSave = dialogBinding.cbSaveCard.isChecked

            // Relaxed validation to ensure "Use Card" actually works
            if (cardName.isNotEmpty() && cardNumber.length >= 13 && expiry.length >= 5 && cvv.length in 3..5) {
                lifecycleScope.launch {
                    val newCard = PaymentCard(
                        userEmail = userEmail,
                        nameOnCard = cardName,
                        cardNumber = cardNumber,
                        expiryDate = expiry,
                        cvv = cvv,
                        nickname = if (cardNickname.isNotEmpty()) cardNickname else null
                    )

                    if (shouldSave) {
                        db.paymentCardDao().addCard(newCard)
                        loadSavedCards(newCard) // Refresh list and auto-select
                        Toast.makeText(this@CartActivity, "Card saved and ready for use", Toast.LENGTH_SHORT).show()
                    } else {
                        selectedCard = newCard
                        val displayName = if (!newCard.nickname.isNullOrEmpty()) {
                            "${newCard.nickname} (**** ${newCard.cardNumber.takeLast(4)})"
                        } else {
                            "**** " + newCard.cardNumber.takeLast(4)
                        }
                        binding.actvCardSelector.setText(displayName, false)
                        Toast.makeText(this@CartActivity, "Card ready for one-time use", Toast.LENGTH_SHORT).show()
                    }
                    
                    // Mark card as selected in the main activity UI
                    binding.rbCardPayment.isChecked = true
                    binding.llCardSelection.visibility = View.VISIBLE
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(this, "Please check your details. Card number (13-16 digits), Expiry (MM/YY), and CVV (3-5 digits) are required.", Toast.LENGTH_LONG).show()
            }
        }

        // If user closes the pop-up without saving, deselect the card option
        dialog.setOnCancelListener {
            binding.rgPaymentMethod.clearCheck()
        }

        dialog.show()
    }

    private var cartItems = mutableListOf<CartItem>()

    /**
     * Prepares and displays the items currently in the user's cart.
     * Also updates the summary totals (Subtotal, Tax, and Total).
     */
    private fun setupCartItems() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        lifecycleScope.launch {
            val itemsFromDb = db.cartDao().getCartItemsByUser(userEmail)
            cartItems = itemsFromDb.toMutableList()

            toggleCartVisibility(cartItems.isEmpty())
            
            val adapter = CartAdapter(cartItems, { itemToDelete ->
                removeItem(itemToDelete)
            }, { itemToUpdate, newQuantity ->
                updateItemQuantity(itemToUpdate, newQuantity)
            }, { itemToView ->
                viewItemDetails(itemToView)
            }, { itemToEdit ->
                showSugarEditDialog(itemToEdit)
            })
            binding.rvCartItems.adapter = adapter

            updateSummary()
        }
    }

    private fun toggleCartVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            binding.llEmptyCart.visibility = View.VISIBLE
            binding.nsvCart.visibility = View.GONE
            binding.flBottomActionContainer.visibility = View.GONE
        } else {
            binding.llEmptyCart.visibility = View.GONE
            binding.nsvCart.visibility = View.VISIBLE
            binding.flBottomActionContainer.visibility = View.VISIBLE
            
            // Only show track order button if an order is actually active
            binding.btnTrackOrder.visibility = if (CartManager.isOrderActive()) View.VISIBLE else View.GONE
        }
    }

    private fun updateItemQuantity(item: CartItem, newQuantity: Int) {
        lifecycleScope.launch {
            item.quantity = newQuantity
            db.cartDao().addToCart(item) // Using addToCart which is REPLACE on conflict
            updateSummary()
            updateCartBadge()
        }
    }

    private fun removeItem(item: CartItem) {
        lifecycleScope.launch {
            db.cartDao().removeFromCart(item)
            cartItems.remove(item)
            (binding.rvCartItems.adapter as? CartAdapter)?.updateItems(cartItems)
            updateSummary()
            updateCartBadge()
            
            if (cartItems.isEmpty()) {
                toggleCartVisibility(true)
                Toast.makeText(this@CartActivity, "Cart is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSummary() {
        // --- SUMMARY CALCULATIONS ---
        var subtotal = 0.0
        for (item in cartItems) {
            // Simple parsing of "R32.00" -> 32.0
            val priceValue = item.price.replace("R", "").toDoubleOrNull() ?: 0.0
            subtotal += priceValue * item.quantity
        }
        
        val tax = if (cartItems.isEmpty()) 0.0 else 8.50 // Fixed tax only if cart not empty
        
        val discountAmount = subtotal * discountPercentage
        val total = subtotal + tax - discountAmount

        // Update the UI with formatted currency strings
        binding.tvSubtotalVal.text = String.format(Locale.US, "R%.2f", subtotal)
        binding.tvTaxVal.text = String.format(Locale.US, "R%.2f", tax)
        
        // Show discount if applied
        if (discountAmount > 0) {
            binding.rlDiscountRow.visibility = View.VISIBLE
            binding.tvDiscountVal.text = String.format(Locale.US, "-R%.2f", discountAmount)
        } else {
            binding.rlDiscountRow.visibility = View.GONE
        }

        binding.tvTotalVal.text = String.format(Locale.US, "R%.2f", total)
        
        if (cartItems.isEmpty()) {
            binding.btnPlaceOrder.isEnabled = false
            binding.btnPlaceOrder.alpha = 0.5f
        } else {
            binding.btnPlaceOrder.isEnabled = true
            binding.btnPlaceOrder.alpha = 1.0f
        }
    }

    private fun updateCartBadge() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        lifecycleScope.launch {
            val count = CartManager.getCartCount(this@CartActivity, userEmail)
            val badge = binding.bottomNav.getOrCreateBadge(R.id.nav_orders)
            if (count > 0) {
                badge.isVisible = true
                badge.number = count
            } else {
                badge.isVisible = false
            }
        }
    }

    private fun scheduleOrderNotifications() {
        val workManager = androidx.work.WorkManager.getInstance(this)

        // 1. Notification: 5 minutes from finished (at 10 mins)
        val data5m = androidx.work.workDataOf(
            "title" to "Order Progress",
            "message" to "Your order is 5 minutes from being finished!"
        )
        val request5m = androidx.work.OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(10, java.util.concurrent.TimeUnit.MINUTES)
            .setInputData(data5m)
            .build()

        // 2. Notification: Finally finished (at 15 mins)
        val dataFinished = androidx.work.workDataOf(
            "title" to "Order Ready!",
            "message" to "Your order is finally finished and ready for pickup!"
        )
        val requestFinished = androidx.work.OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(15, java.util.concurrent.TimeUnit.MINUTES)
            .setInputData(dataFinished)
            .build()

        workManager.enqueue(request5m)
        workManager.enqueue(requestFinished)
    }

    private fun viewItemDetails(item: CartItem) {
        val intent = Intent(this, ProductDetailsActivity::class.java).apply {
            putExtra("EXTRA_ID", item.id)
            putExtra("EXTRA_NAME", item.name)
            // Note: This shows base price without customizations for re-ordering
            // We use the original item name to find it in the sample list if needed,
            // but for now we pass what we have.
            putExtra("EXTRA_PRICE", item.price) 
            putExtra("EXTRA_DESC", "") // Desc not stored in cart
            putExtra("EXTRA_CATEGORY", "Drinks") // Assume drinks for sugar editing
            item.imageResId?.let { putExtra("EXTRA_IMAGE", it) }
        }
        startActivity(intent)
    }

    private fun showSugarEditDialog(item: CartItem) {
        val sugars = (0..5).toList().map { "$it Sugar(s)" }
        val types = listOf("White", "Brown")
        
        var selectedSugarCount = 0
        var selectedSugarType = "White"

        // Parse current customization if possible
        try {
            if (item.customization.contains("Sugar")) {
                selectedSugarCount = item.customization.split(" ")[0].toInt()
                selectedSugarType = if (item.customization.contains("Brown")) "Brown" else "White"
            }
        } catch (e: Exception) {}

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Sugar for ${item.name}")

        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.HORIZONTAL
        layout.setPadding(50, 40, 50, 10)
        layout.weightSum = 2f

        val countSpinner = android.widget.Spinner(this)
        val countAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sugars)
        countAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        countSpinner.adapter = countAdapter
        countSpinner.setSelection(selectedSugarCount)
        val countParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        countSpinner.layoutParams = countParams
        layout.addView(countSpinner)

        val typeSpinner = android.widget.Spinner(this)
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = typeAdapter
        typeSpinner.setSelection(if (selectedSugarType == "Brown") 1 else 0)
        val typeParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        typeSpinner.layoutParams = typeParams
        layout.addView(typeSpinner)

        builder.setView(layout)

        builder.setPositiveButton("Update") { _, _ ->
            val newCount = countSpinner.selectedItemPosition
            val newType = typeSpinner.selectedItem.toString()
            
            lifecycleScope.launch {
                val newCustomization = if (newCount > 0) "$newCount $newType Sugar(s)" else ""
                
                // Calculate price change
                val oldSugarCount = selectedSugarCount
                val sugarPriceDiff = (newCount - oldSugarCount) * 5.0
                
                val currentPriceValue = item.price.replace("R", "").toDoubleOrNull() ?: 0.0
                val newPriceValue = currentPriceValue + sugarPriceDiff
                val newPriceStr = String.format(Locale.US, "R%.2f", newPriceValue)

                // We need to update the price in the item too
                val updatedItem = item.copy(customization = newCustomization, price = newPriceStr)
                db.cartDao().addToCart(updatedItem)
                setupCartItems() // Refresh UI
                Toast.makeText(this@CartActivity, "Sugar updated", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}
