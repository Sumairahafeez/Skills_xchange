package com.example.skillxchange

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class PostAdapter(
    private val postList: MutableList<Post>,
    private val onAskQuestion: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ShapeableImageView = itemView.findViewById(R.id.ivPostUserAvatar)
        val tvUserName: TextView = itemView.findViewById(R.id.tvPostUserName)
        val tvUserTitle: TextView = itemView.findViewById(R.id.tvPostUserTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvPostContent)
        val tvTime: TextView = itemView.findViewById(R.id.tvPostTime)
        val btnLike: MaterialButton = itemView.findViewById(R.id.btnLike)
        val btnAskQuestion: MaterialButton = itemView.findViewById(R.id.btnAskQuestion)
        val btnComments: MaterialButton = itemView.findViewById(R.id.btnComments)
        val mediaContainer: View = itemView.findViewById(R.id.postMediaContainer)
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        val context = holder.itemView.context

        holder.tvUserName.text = post.userName
        holder.tvUserTitle.text = post.userTitle
        holder.tvContent.text = post.content
        holder.tvTime.text = post.timestamp
        
        // Handle post image
        if (!post.imageUri.isNullOrEmpty()) {
            holder.mediaContainer.visibility = View.VISIBLE
            try {
                holder.ivPostImage.setImageURI(Uri.parse(post.imageUri))
            } catch (e: Exception) {
                holder.mediaContainer.visibility = View.GONE
            }
        } else {
            holder.mediaContainer.visibility = if (post.hasVideo) View.VISIBLE else View.GONE
        }

        // Like Button - Synced with Firebase
        holder.btnLike.text = "Like (${post.likes})"
        holder.btnLike.setOnClickListener {
            // Toggle like logic: here we assume a simple increment for demo.
            // In a real app, you'd track if the specific user has liked it.
            PostCache.toggleLike(post.id, post.likes, true)
        }

        // Ask Question
        holder.btnAskQuestion.setOnClickListener { onAskQuestion(post) }

        // Comments functionality - Synced with Firebase
        holder.btnComments.text = "Comments (${post.comments})"
        
        holder.btnComments.setOnClickListener {
            PostCache.listenToComments(post.id) { commentsList ->
                if (commentsList.isEmpty()) {
                    Toast.makeText(context, "No public questions yet.", Toast.LENGTH_SHORT).show()
                } else {
                    val commentsText = commentsList.joinToString("\n\n")
                    com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                        .setTitle("Public Questions")
                        .setMessage(commentsText)
                        .setPositiveButton("Close", null)
                        .show()
                }
            }
        }
    }

    override fun getItemCount(): Int = postList.size

    fun updatePosts(newPosts: MutableList<Post>) {
        postList.clear()
        postList.addAll(newPosts)
        notifyDataSetChanged()
    }
}
