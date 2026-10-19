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
import com.example.local_db.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch

/**
 * LoginActivity handles user authentication via local DB and Google Sign-In.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: AppDatabase

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInTask(task)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

        if (isLoggedIn) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getDatabase(this)

        binding.btnLogin.setOnClickListener {
            val rawIdentifier = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (rawIdentifier.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val user = db.userDao().loginUser(rawIdentifier, password)

                    if (user != null) {
                        val editor = sharedPref.edit()
                        editor.putBoolean("isLoggedIn", true)
                        editor.putString("userEmail", user.email)
                        editor.apply()

                        Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            performGoogleSignIn()
        }

        binding.tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
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
                    // Ignore if already registered
                }
                user = db.userDao().getUserByEmail(normalizedEmail)
            }

            if (user != null) {
                val sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putBoolean("isLoggedIn", true)
                editor.putString("userEmail", user.email)
                editor.apply()

                Toast.makeText(this@LoginActivity, "Google Sign-In Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
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