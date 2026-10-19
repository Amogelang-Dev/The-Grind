package com.example.local_db

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.local_db.databinding.ItemPaymentCardV2Binding

/**
 * Adapter for displaying saved payment cards in the Edit Profile screen.
 * Provides functionality to delete a card.
 */
class SavedCardsAdapter(
    private var cards: List<PaymentCard>,
    private val onDeleteClick: (PaymentCard) -> Unit
) : RecyclerView.Adapter<SavedCardsAdapter.CardViewHolder>() {

    class ViewHolder(val binding: ItemPaymentCardV2Binding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemPaymentCardV2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        holder.binding.apply {
            tvCardName.text = if (!card.nickname.isNullOrEmpty()) card.nickname else card.nameOnCard
            // Masking card number for security: e.g., •••• 1234
            val maskedNumber = "•••• ${card.cardNumber.takeLast(4)}"
            tvCardInfo.text = if (!card.nickname.isNullOrEmpty()) {
                "${card.nameOnCard} | $maskedNumber"
            } else {
                maskedNumber
            }
            
            btnDeleteCard.setOnClickListener {
                onDeleteClick(card)
            }
        }
    }

    override fun getItemCount(): Int = cards.size

    fun updateCards(newCards: List<PaymentCard>) {
        this.cards = newCards
        notifyDataSetChanged()
    }

    inner class CardViewHolder(val binding: ItemPaymentCardV2Binding) : RecyclerView.ViewHolder(binding.root)
}