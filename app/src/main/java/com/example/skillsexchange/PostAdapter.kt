package com.example.skillsexchange

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class PostAdapter(
    private val postList: MutableList<Post>,
    private val onAskQuestion: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvPostUserName)
        val tvUserTitle: TextView = itemView.findViewById(R.id.tvPostUserTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvPostContent)
        val tvTime: TextView = itemView.findViewById(R.id.tvPostTime)
        val btnAskQuestion: MaterialButton = itemView.findViewById(R.id.btnAskQuestion)
        val mediaContainer: View = itemView.findViewById(R.id.postMediaContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.tvUserName.text = post.userName
        holder.tvUserTitle.text = post.userTitle
        holder.tvContent.text = post.content
        holder.tvTime.text = post.timestamp
        holder.mediaContainer.visibility = if (post.hasVideo) View.VISIBLE else View.GONE
        holder.btnAskQuestion.setOnClickListener { onAskQuestion(post) }
    }

    override fun getItemCount(): Int = postList.size

    /** Replaces the current list and redraws the RecyclerView */
    fun updatePosts(newPosts: MutableList<Post>) {
        postList.clear()
        postList.addAll(newPosts)
        notifyDataSetChanged()
    }
}