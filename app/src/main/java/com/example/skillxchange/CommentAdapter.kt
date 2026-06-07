package com.example.skillxchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skillxchange.model.Comment
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Locale

class CommentAdapter(private var commentList: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ShapeableImageView = itemView.findViewById(R.id.ivCommentUserAvatar)
        val tvUserName: TextView = itemView.findViewById(R.id.tvCommentUserName)
        val tvCommentText: TextView = itemView.findViewById(R.id.tvCommentText)
        val tvTime: TextView = itemView.findViewById(R.id.tvCommentTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]

        holder.tvUserName.text = comment.userName
        holder.tvCommentText.text = comment.text

        val date = comment.timestamp?.toDate()
        if (date != null) {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            holder.tvTime.text = sdf.format(date)
        } else {
            holder.tvTime.text = "Just now"
        }

        Glide.with(holder.itemView.context)
            .load(comment.userPhotoUrl)
            .placeholder(R.drawable.ic_user_placeholder)
            .into(holder.ivAvatar)
    }

    override fun getItemCount(): Int = commentList.size

    fun updateComments(newList: List<Comment>) {
        commentList = newList
        notifyDataSetChanged()
    }
}
