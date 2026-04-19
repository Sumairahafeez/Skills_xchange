package com.example.skillxchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InvitationAdapter(
    private var invitations: MutableList<Invitation>,
    private val onAccept: (Invitation) -> Unit,
    private val onDecline: (Invitation, Int) -> Unit
) : RecyclerView.Adapter<InvitationAdapter.InvitationViewHolder>() {

    class InvitationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvInviterName)
        val tvTitle: TextView = itemView.findViewById(R.id.tvInviterTitle)
        val tvReason: TextView = itemView.findViewById(R.id.tvInviterReason)
        val btnAccept: ImageButton = itemView.findViewById(R.id.btnAcceptInvitation)
        val btnDecline: ImageButton = itemView.findViewById(R.id.btnDeclineInvitation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_invitation, parent, false)
        return InvitationViewHolder(view)
    }

    override fun onBindViewHolder(holder: InvitationViewHolder, position: Int) {
        val invitation = invitations[position]
        holder.tvName.text = invitation.name
        holder.tvTitle.text = invitation.title
        holder.tvReason.text = invitation.reason

        holder.btnAccept.setOnClickListener { onAccept(invitation) }
        holder.btnDecline.setOnClickListener { onDecline(invitation, holder.adapterPosition) }
    }

    override fun getItemCount(): Int = invitations.size

    fun removeAt(position: Int) {
        invitations.removeAt(position)
        notifyItemRemoved(position)
    }
}