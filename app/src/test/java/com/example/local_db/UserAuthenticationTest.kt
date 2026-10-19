package com.example.local_db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserAuthenticationTest {

    private fun isPasswordSecure(password: String): Boolean {
        if (password.length < 6) return false
        val hasLetter = password.any { it.isLetter() }
        val hasDigitOrSymbol = password.any { it.isDigit() || !it.isLetterOrDigit() }
        return hasLetter && hasDigitOrSymbol
    }

    @Test
    fun passwordSecurityValidation() {
        assertTrue(isPasswordSecure("Pass123"))
        assertTrue(isPasswordSecure("Secret!"))
        assertTrue(isPasswordSecure("p@ssword99"))
        assertFalse(isPasswordSecure("12345"))
        assertFalse(isPasswordSecure("abcde"))
        assertFalse(isPasswordSecure("abcdef"))
    }

    @Test
    fun emailAndUsernameNormalization() {
        val rawEmail = "  Test.User@Example.COM  "
        val normalizedEmail = rawEmail.trim().lowercase()
        assertEquals("test.user@example.com", normalizedEmail)

        val usernameFromEmail = normalizedEmail.substringBefore("@").replace(".", "_")
        assertEquals("test_user", usernameFromEmail)
    }

    @Test
    fun userCreationFromGoogleProfile() {
        val googleEmail = "john.doe@gmail.com"
        val displayName = "John Doe"
        val username = googleEmail.substringBefore("@").replace(".", "_")

        val user = User(
            fullName = displayName,
            username = username,
            email = googleEmail.lowercase(),
            password = "GoogleAuth_12345"
        )

        assertEquals("John Doe", user.fullName)
        assertEquals("john_doe", user.username)
        assertEquals("john.doe@gmail.com", user.email)
        assertTrue(user.password.startsWith("GoogleAuth_"))
    }
}
