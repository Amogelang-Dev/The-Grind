package com.example.local_db

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.local_db.databinding.ActivityEditProfileBinding
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

/**
 * EditProfileActivity allows users to update their information and profile photo.
 * Now includes a section to manage saved payment cards.
 */
class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var db: AppDatabase
    private var selectedImageUri: Uri? = null
    private var currentUser: User? = null
    private lateinit var cardsAdapter: SavedCardsAdapter

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { startCrop(it) }
    }

    private val cropImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val resultUri = result.data?.let { UCrop.getOutput(it) }
            resultUri?.let {
                selectedImageUri = it
                binding.ivEditProfileImage.setImageURI(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupCardsRecyclerView()
        loadUserData()

        binding.tvAddNewCardProfile.setOnClickListener {
            showAddCardDialog()
        }

        binding.ivEditProfileImage.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.tvChangePhoto.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.btnSaveProfile.setOnClickListener { saveProfileChanges() }
    }

    private fun setupCardsRecyclerView() {
        cardsAdapter = SavedCardsAdapter(emptyList()) { card ->
            deleteCard(card)
        }
        binding.rvSavedCards.adapter = cardsAdapter
    }

    private fun deleteCard(card: PaymentCard) {
        lifecycleScope.launch {
            db.paymentCardDao().deleteCard(card)
            Toast.makeText(this@EditProfileActivity, "Card removed", Toast.LENGTH_SHORT).show()
            loadUserCards()
        }
    }

    private fun loadUserData() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        lifecycleScope.launch {
            currentUser = db.userDao().getUserByEmail(userEmail)
            currentUser?.let {
                binding.etEditUsername.setText(it.username)
                binding.etEditFullName.setText(it.fullName)
                it.profileImageUri?.let { uriString ->
                    binding.ivEditProfileImage.setImageURI(Uri.parse(uriString))
                }
            }
            loadUserCards()
        }
    }

    private fun loadUserCards() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""

        lifecycleScope.launch {
            val cards = db.paymentCardDao().getCardsByUser(userEmail)
            if (cards.isEmpty()) {
                binding.tvNoCards.visibility = View.VISIBLE
                binding.rvSavedCards.visibility = View.GONE
            } else {
                binding.tvNoCards.visibility = View.GONE
                binding.rvSavedCards.visibility = View.VISIBLE
                cardsAdapter.updateCards(cards)
            }
        }
    }

    private fun saveProfileChanges() {
        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userEmail = sharedPref.getString("userEmail", "") ?: ""
        val newUsername = binding.etEditUsername.text.toString().trim()
        val newName = binding.etEditFullName.text.toString().trim()
        
        if (userEmail.isEmpty()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newUsername.length < 4) {
            Toast.makeText(this, "Username must be at least 4 characters", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Re-fetch current user to be safe
                val user = db.userDao().getUserByEmail(userEmail)
                
                if (user == null) {
                    Toast.makeText(this@EditProfileActivity, "User not found in database.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                var finalImageUri = user.profileImageUri
                selectedImageUri?.let { uri ->
                    val savedPath = saveImageToInternalStorage(uri)
                    if (savedPath != null) finalImageUri = savedPath
                }

                val updatedUser = user.copy(username = newUsername, fullName = newName, profileImageUri = finalImageUri)
                db.userDao().updateUser(updatedUser)
                
                Toast.makeText(this@EditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e("EditProfile", "Error saving profile", e)
                Toast.makeText(this@EditProfileActivity, "Error saving changes: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startCrop(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image_${System.currentTimeMillis()}.jpg"))
        val options = UCrop.Options().apply {
            val themeColor = ContextCompat.getColor(this@EditProfileActivity, R.color.coffee_primary)
            setToolbarColor(themeColor)
            setStatusBarColor(themeColor)
            setToolbarTitle("Crop Photo")
            setCircleDimmedLayer(true)
            setShowCropFrame(false)
            setShowCropGrid(true)
            setCropGridColor(ContextCompat.getColor(this@EditProfileActivity, R.color.coffee_primary))
            setCropGridStrokeWidth(2)
            // Enhance icon visibility - making them white as requested
            setToolbarWidgetColor(ContextCompat.getColor(this@EditProfileActivity, android.R.color.white))
            setActiveControlsWidgetColor(ContextCompat.getColor(this@EditProfileActivity, R.color.coffee_primary))
        }
        val uCropIntent = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withOptions(options)
            .getIntent(this)
        cropImageLauncher.launch(uCropIntent)
    }

    private fun showAddCardDialog() {
        val dialogBinding = com.example.local_db.databinding.DialogAddCardBinding.inflate(layoutInflater)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        // Card formatting logic (same as CartActivity)
        dialogBinding.etCardNumber.addTextChangedListener(object : android.text.TextWatcher {
            private var isUpdating = false
            private val space = ' '
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (isUpdating || s == null) return
                isUpdating = true
                val originalString = s.toString()
                val digitsOnly = originalString.replace(space.toString(), "")
                val formatted = StringBuilder()
                for (i in digitsOnly.indices) {
                    formatted.append(digitsOnly[i])
                    if ((i + 1) % 4 == 0 && (i + 1) < digitsOnly.length) formatted.append(space)
                }
                val newString = formatted.toString()
                if (newString != originalString) {
                    s.replace(0, s.length, newString)
                }
                isUpdating = false
            }
        })

        dialogBinding.etExpiry.addTextChangedListener(object : android.text.TextWatcher {
            private var isDeleting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { isDeleting = count > after }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (s == null || isDeleting) return
                if (s.length == 2 && !s.contains("/")) s.append("/")
            }
        })

        dialogBinding.btnSaveCard.setOnClickListener {
            val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userEmail = sharedPref.getString("userEmail", "") ?: ""
            
            val cardName = dialogBinding.etCardName.text.toString().trim()
            val cardNickname = dialogBinding.etCardNickname.text.toString().trim()
            val cardNumber = dialogBinding.etCardNumber.text.toString().replace(" ", "").trim()
            val expiry = dialogBinding.etExpiry.text.toString().trim()
            val cvv = dialogBinding.etCvv.text.toString().trim()

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
                    db.paymentCardDao().addCard(newCard)
                    loadUserCards()
                    dialog.dismiss()
                }
            } else {
                Toast.makeText(this, "Please check your details", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(filesDir, "profile_picture_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input -> outputStream.use { output -> input.copyTo(output) } }
            Uri.fromFile(file).toString()
        } catch (e: Exception) { null }
    }
}
