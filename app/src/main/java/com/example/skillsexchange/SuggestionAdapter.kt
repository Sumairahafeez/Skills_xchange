package com.example.skillsexchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
        holder.tvMutual.text = "${user.teachSkills.size} skills to teach"
        holder.btnConnect.setOnClickListener { onConnectClick(user) }
    }

    override fun getItemCount(): Int = userList.size
}