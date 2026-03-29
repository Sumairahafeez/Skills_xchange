package com.example.skillsexchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SuggestionAdapter(private val suggestions: List<User>) :
    RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder>() {

    class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvSuggestionName)
        val tvTitle: TextView = itemView.findViewById(R.id.tvSuggestionTitle)
        val tvMutual: TextView = itemView.findViewById(R.id.tvSuggestionMutual)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val user = suggestions[position]
        holder.tvName.text = user.name
        holder.tvTitle.text = user.skillOffered
        holder.tvMutual.text = "${(5..15).random()} mutual skills"
    }

    override fun getItemCount(): Int = suggestions.size
}