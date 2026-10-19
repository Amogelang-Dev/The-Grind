package com.example.local_db

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.local_db.databinding.ActivityOrderDetailBinding
import com.example.local_db.databinding.ItemCartProductBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private var orderId: Int = -1
    private var orderDate: String = ""
    private var totalAmount: String = ""
    private var itemsJson: String = ""
    private var itemsList: List<CartItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }

        // Retrieve data
        orderId = intent.getIntExtra("EXTRA_ORDER_ID", -1)
        orderDate = intent.getStringExtra("EXTRA_DATE") ?: ""
        totalAmount = intent.getStringExtra("EXTRA_TOTAL") ?: ""
        itemsJson = intent.getStringExtra("EXTRA_ITEMS_JSON") ?: ""

        // Populate UI
        binding.tvDetailOrderNumber.text = "Order #$orderId"
        binding.tvDetailOrderDate.text = "Placed on $orderDate"
        binding.tvDetailTotalAmount.text = totalAmount

        setupItemsList()

        binding.btnDownloadReceipt.setOnClickListener {
            generatePdfReceipt()
        }
    }

    private fun setupItemsList() {
        if (itemsJson.isNotEmpty()) {
            val type = object : TypeToken<List<CartItem>>() {}.type
            itemsList = Gson().fromJson(itemsJson, type)

            val inflater = LayoutInflater.from(this)
            for (item in itemsList) {
                val itemView = inflater.inflate(android.R.layout.simple_list_item_2, binding.llOrderItemsList, false)
                val text1 = itemView.findViewById<TextView>(android.R.id.text1)
                val text2 = itemView.findViewById<TextView>(android.R.id.text2)
                
                text1.text = "${item.quantity}x ${item.name}"
                text1.setTextColor(Color.BLACK)
                
                var desc = item.price
                if (item.customization.isNotEmpty()) {
                    desc += " (${item.customization})"
                }
                text2.text = desc
                text2.setTextColor(Color.GRAY)
                
                binding.llOrderItemsList.addView(itemView)
            }
        }
    }

    private fun generatePdfReceipt() {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        var y = 40f
        paint.textSize = 16f
        paint.isFakeBoldText = true
        canvas.drawText("GRIND COFFEE CO.", 80f, y, paint)
        
        y += 30f
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("Receipt for Order #$orderId", 20f, y, paint)
        
        y += 20f
        canvas.drawText("Date: $orderDate", 20f, y, paint)
        
        y += 30f
        paint.isFakeBoldText = true
        canvas.drawText("Items:", 20f, y, paint)
        y += 20f
        paint.isFakeBoldText = false
        
        for (item in itemsList) {
            val line = "${item.quantity}x ${item.name} - ${item.price}"
            canvas.drawText(line, 20f, y, paint)
            y += 15f
            if (item.customization.isNotEmpty()) {
                canvas.drawText("  (${item.customization})", 20f, y, paint)
                y += 15f
            }
        }
        
        y += 20f
        paint.isFakeBoldText = true
        canvas.drawText("Total Paid: $totalAmount", 20f, y, paint)
        
        y += 40f
        paint.isFakeBoldText = false
        paint.textSize = 10f
        canvas.drawText("Thank you for your business!", 70f, y, paint)

        pdfDocument.finishPage(page)

        val fileName = "Receipt_Order_$orderId.pdf"
        val filePath = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(filePath))
            Toast.makeText(this, "Receipt downloaded to Downloads", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}
