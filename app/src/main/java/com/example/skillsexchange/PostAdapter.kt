package com.example.skillsexchange

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val btnShare: MaterialButton = itemView.findViewById(R.id.btnShare)
        val mediaContainer: View = itemView.findViewById(R.id.postMediaContainer)
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
        holder.mediaContainer.visibility = if (post.hasVideo) View.VISIBLE else View.GONE

        // Like Button
        var isLiked = false
        var likeCount = post.likes
        holder.btnLike.text = "Like (${likeCount})"
        holder.btnLike.setOnClickListener {
            isLiked = !isLiked
            if (isLiked) {
                likeCount++
                holder.btnLike.setIconResource(android.R.drawable.btn_star_big_on)
                holder.btnLike.setIconTintResource(R.color.color_primary)
            } else {
                likeCount--
                holder.btnLike.setIconResource(android.R.drawable.btn_star_big_off)
                holder.btnLike.setIconTintResource(R.color.color_text_secondary)
            }
            holder.btnLike.text = "Like (${likeCount})"
        }

        // Ask Question
        holder.btnAskQuestion.setOnClickListener { onAskQuestion(post) }

        // Share functionality
        holder.btnShare.setOnClickListener {
            val shareText = "Check out this skill post from ${post.userName} on Skills Xchange:\n\n${post.content}"
            
            // Choose sharing method
            val options = arrayOf("Share to Messages (App)", "Share via External App")
            com.google.android.material.dialog.MaterialAlertDialogBuilder(context)
                .setTitle("Share Post")
                .setItems(options) { _, which ->
                    if (which == 0) {
                        // Internal share to Chat
                        val intent = Intent(context, ChatListActivity::class.java)
                        intent.putExtra("sharePostId", post.id)
                        intent.putExtra("sharePostContent", shareText)
                        context.startActivity(intent)
                    } else {
                        // System share
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share post via")
                        context.startActivity(shareIntent)
                    }
                }
                .show()
        }
    }

    override fun getItemCount(): Int = postList.size

    fun updatePosts(newPosts: MutableList<Post>) {
        postList.clear()
        postList.addAll(newPosts)
        notifyDataSetChanged()
    }
}