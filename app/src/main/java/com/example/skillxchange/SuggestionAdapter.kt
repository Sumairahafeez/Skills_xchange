package com.example.skillxchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class SuggestionAdapter(
    private val userList: List<User>,
    private val onConnectClick: (User) -> Unit
) : RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder>() {

    class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvSuggestionName)
        val tvTitle: TextView = itemView.findViewById(R.id.tvSuggestionTitle)
        val tvMutual: TextView = itemView.findViewById(R.id.tvSuggestionMutual)
        val btnConnect: MaterialButton = itemView.findViewById(R.id.btnSuggestionConnect)
        val btnMessage: ImageButton = itemView.findViewById(R.id.btnMessageSuggestion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggestion, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val user = userList[position]
        holder.tvName.text = user.name
        holder.tvTitle.text = user.tagline
        holder.tvMutual.text = "12 mutual skills"

        holder.btnConnect.setOnClickListener {
            holder.btnConnect.text = "Requested"
            holder.btnConnect.isEnabled = false
            onConnectClick(user)
        }

        holder.btnMessage.setOnClickListener {
            // Optional: add message functionality later
            Toast.makeText(holder.itemView.context, "Message feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = userList.size
}
