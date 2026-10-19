package com.example.local_db

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.local_db.databinding.ActivityHelpBinding

class HelpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupStyledContactDetails()

        binding.tvPhoneNumber.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+27727548705")
            }
            startActivity(intent)
        }

        binding.tvEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:info@thegrindcoffeecompany.co.za")
            }
            startActivity(intent)
        }

        binding.tvWebsite.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://grindcoffeecompany.co.za/"))
            startActivity(intent)
        }
    }

    private fun setupStyledContactDetails() {
        val blackColor = ContextCompat.getColor(this, R.color.black)
        val accentColor = ContextCompat.getColor(this, R.color.coffee_accent)

        // WhatsApp
        val whatsappText = "WhatsApp Number: 0727548705"
        val whatsappSpannable = SpannableString(whatsappText)
        whatsappSpannable.setSpan(ForegroundColorSpan(blackColor), 0, 16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        whatsappSpannable.setSpan(ForegroundColorSpan(accentColor), 16, whatsappText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvPhoneNumber.text = whatsappSpannable

        // Email
        val emailText = "Email: info@thegrindcoffeecompany.co.za"
        val emailSpannable = SpannableString(emailText)
        emailSpannable.setSpan(ForegroundColorSpan(blackColor), 0, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        emailSpannable.setSpan(ForegroundColorSpan(accentColor), 6, emailText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvEmail.text = emailSpannable

        // Website
        val websiteText = "Website: https://grindcoffeecompany.co.za/"
        val websiteSpannable = SpannableString(websiteText)
        websiteSpannable.setSpan(ForegroundColorSpan(blackColor), 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        websiteSpannable.setSpan(ForegroundColorSpan(accentColor), 8, websiteText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.tvWebsite.text = websiteSpannable
    }
}