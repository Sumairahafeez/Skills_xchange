package com.example.skillsexchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private val userList: List<User>, private var isGridLayout: Boolean) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvUserSkill: TextView = itemView.findViewById(R.id.tvUserSkill)
        val tvRating: TextView = itemView.findViewById(R.id.tvRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.tvUserName.text = user.name
        holder.tvUserSkill.text = "Offering: ${user.skillOffered}"
        holder.tvRating.text = user.rating.toString()
    }

    override fun getItemCount(): Int = userList.size

    fun updateLayout(isGrid: Boolean) {
        this.isGridLayout = isGrid
        notifyDataSetChanged()
    }
}