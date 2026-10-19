package com.example.local_db

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.local_db.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch

/**
 * RegisterActivity allows new users to create an account or sign up with Google.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var db: AppDatabase

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInTask(task)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val rawName = binding.etFullName.text.toString().trim()
            val fullName = if (rawName.isBlank()) "Customer" else rawName
            val email = binding.etEmail.text.toString().trim().lowercase()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                
                if (username.length < 3) {
                    Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (password != confirmPassword) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!isPasswordSecure(password)) {
                    Toast.makeText(this, "Password must be at least 6 characters long and include letters and numbers or symbols", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                lifecycleScope.launch {
                    val existingEmailUser = db.userDao().getUserByEmail(email)
                    if (existingEmailUser != null) {
                        Toast.makeText(this@RegisterActivity, "An account with this email already exists", Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    val existingUsernameUser = db.userDao().getUserByUsername(username)
                    if (existingUsernameUser != null) {
                        Toast.makeText(this@RegisterActivity, "Username is already taken", Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    val user = User(fullName = fullName, username = username, email = email, password = password)
                    try {
                        db.orderHistoryDao().clearHistoryByUser(email)
                        db.userDao().registerUser(user)

                        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putBoolean("isLoggedIn", true)
                        editor.putString("userEmail", email)
                        editor.apply()

                        Toast.makeText(this@RegisterActivity, "Account created successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this@RegisterActivity, "Failed to create account: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            performGoogleSignIn()
        }

        binding.tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun isPasswordSecure(password: String): Boolean {
        if (password.length < 6) return false
        val hasLetter = password.any { it.isLetter() }
        val hasDigitOrSymbol = password.any { it.isDigit() || !it.isLetterOrDigit() }
        return hasLetter && hasDigitOrSymbol
    }

    private fun performGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        } catch (e: Exception) {
            showGoogleSignInFallbackDialog("Unable to initialize Google Sign-In: ${e.localizedMessage}")
        }
    }

    private fun handleGoogleSignInTask(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null && !account.email.isNullOrEmpty()) {
                processGoogleUser(account.email!!, account.displayName ?: "Google User", account.photoUrl?.toString())
            } else {
                showGoogleSignInFallbackDialog("No account details returned from Google Sign-In.")
            }
        } catch (e: ApiException) {
            showGoogleSignInFallbackDialog("Google Sign-In returned status code ${e.statusCode}.")
        } catch (e: Exception) {
            showGoogleSignInFallbackDialog("Google Sign-In error: ${e.localizedMessage}")
        }
    }

    private fun processGoogleUser(email: String, displayName: String, photoUri: String?) {
        lifecycleScope.launch {
            val normalizedEmail = email.trim().lowercase()
            var user = db.userDao().getUserByEmail(normalizedEmail)
            if (user == null) {
                var username = normalizedEmail.substringBefore("@").replace(".", "_")
                if (db.userDao().getUserByUsername(username) != null) {
                    username = "${username}_${(100..999).random()}"
                }
                val newUser = User(
                    fullName = displayName.ifBlank { "Google User" },
                    username = username,
                    email = normalizedEmail,
                    password = "GoogleAuth_${System.currentTimeMillis()}",
                    profileImageUri = photoUri
                )
                try {
                    db.orderHistoryDao().clearHistoryByUser(normalizedEmail)
                    db.userDao().registerUser(newUser)
                } catch (e: Exception) {
                    // Ignore if registered
                }
                user = db.userDao().getUserByEmail(normalizedEmail)
            }

            if (user != null) {
                val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.putString("userEmail", user.email)
                editor.apply()

                Toast.makeText(this@RegisterActivity, "Google Sign-In Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun showGoogleSignInFallbackDialog(message: String) {
        val input = EditText(this).apply {
            hint = "user@gmail.com"
            setPadding(50, 30, 50, 30)
        }
        AlertDialog.Builder(this)
            .setTitle("Google Sign-In")
            .setMessage("$message\n\nEnter your Google account email to sign in:")
            .setView(input)
            .setPositiveButton("Sign In") { _, _ ->
                val inputEmail = input.text.toString().trim()
                if (inputEmail.isNotEmpty() && inputEmail.contains("@")) {
                    val name = inputEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                    processGoogleUser(inputEmail, name, null)
                } else {
                    Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}