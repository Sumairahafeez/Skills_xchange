package com.example.skillxchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skillxchange.model.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class NetworkAdapter(
    private var users: List<User>,
    private var isPendingTab: Boolean,
    private val onAccept: (User) -> Unit,
    private val onDecline: (User) -> Unit,
    private val onMessage: (User) -> Unit
) : RecyclerView.Adapter<NetworkAdapter.NetworkViewHolder>() {

    class NetworkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ShapeableImageView = itemView.findViewById(R.id.ivNetworkUserAvatar)
        val tvName: TextView = itemView.findViewById(R.id.tvNetworkUserName)
        val tvTagline: TextView = itemView.findViewById(R.id.tvNetworkUserTagline)
        val btnMessage: MaterialButton = itemView.findViewById(R.id.btnMessage)
        val btnAccept: ImageButton = itemView.findViewById(R.id.btnAccept)
        val btnDecline: ImageButton = itemView.findViewById(R.id.btnDecline)
        val actionContainer: LinearLayout = itemView.findViewById(R.id.layoutActionButtons)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_network_user, parent, false)
        return NetworkViewHolder(view)
    }

    override fun onBindViewHolder(holder: NetworkViewHolder, position: Int) {
        val user = users[position]
        holder.tvName.text = user.name
        holder.tvTagline.text = user.tagline

        Glide.with(holder.itemView.context)
            .load(user.photoUrl)
            .placeholder(R.drawable.ic_user_placeholder)
            .into(holder.ivAvatar)

        if (isPendingTab) {
            holder.btnMessage.visibility = View.GONE
            holder.btnAccept.visibility = View.VISIBLE
            holder.btnDecline.visibility = View.VISIBLE
        } else {
            holder.btnMessage.visibility = View.VISIBLE
            holder.btnAccept.visibility = View.GONE
            holder.btnDecline.visibility = View.GONE
        }

        holder.btnAccept.setOnClickListener { onAccept(user) }
        holder.btnDecline.setOnClickListener { onDecline(user) }
        holder.btnMessage.setOnClickListener { onMessage(user) }
    }

    override fun getItemCount(): Int = users.size

    fun updateData(newList: List<User>, pending: Boolean) {
        users = newList
        isPendingTab = pending
        notifyDataSetChanged()
    }
}
