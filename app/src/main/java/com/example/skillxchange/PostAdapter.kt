package com.example.skillxchange

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.Locale

class PostAdapter(
    private var postList: List<Post>,
    private val currentUserId: String,
    private val onLikeClicked: (Post) -> Unit,
    private val onCommentClicked: (Post) -> Unit,
    private val onDeleteClicked: (Post) -> Unit,
    private val onAskQuestion: (Post) -> Unit,
    private val onRepostClicked: (Post) -> Unit,
    private val onUserClicked: (String) -> Unit // Added callback for name click
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ShapeableImageView = itemView.findViewById(R.id.ivPostUserAvatar)
        val tvUserName: TextView = itemView.findViewById(R.id.tvPostUserName)
        val tvUserTitle: TextView = itemView.findViewById(R.id.tvPostUserTitle)
        val tvTime: TextView = itemView.findViewById(R.id.tvPostTime)
        val tvContent: TextView = itemView.findViewById(R.id.tvPostContent)
        val mediaContainer: View = itemView.findViewById(R.id.postMediaContainer)
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivPostImage)
        val btnLike: MaterialButton = itemView.findViewById(R.id.btnLike)
        val btnComments: MaterialButton = itemView.findViewById(R.id.btnComments)
        val btnAskQuestion: MaterialButton = itemView.findViewById(R.id.btnAskQuestion)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeletePost)
        val btnShare: MaterialButton = itemView.findViewById(R.id.btnShare)
        val btnRepost: MaterialButton = itemView.findViewById(R.id.btnRepost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        holder.tvUserName.text = post.userName
        holder.tvUserTitle.text = post.userTitle
        
        // Navigation to profile on name click
        holder.tvUserName.setOnClickListener { onUserClicked(post.userId) }
        holder.ivAvatar.setOnClickListener { onUserClicked(post.userId) }

        val date = post.timestamp?.toDate()
        if (date != null) {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            holder.tvTime.text = sdf.format(date)
        } else {
            holder.tvTime.text = "Just now"
        }

        Glide.with(holder.itemView.context)
            .load(post.userPhotoUrl)
            .placeholder(R.drawable.ic_user_placeholder)
            .into(holder.ivAvatar)

        if (!post.imageUrl.isNullOrEmpty()) {
            holder.mediaContainer.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .into(holder.ivPostImage)
        } else {
            holder.mediaContainer.visibility = View.GONE
        }

        val isLiked = post.likedBy.contains(currentUserId)
        holder.btnLike.text = "Like (${post.likesCount})"
        
        if (isLiked) {
            holder.btnLike.setIconResource(R.drawable.ic_heart_filled)
            holder.btnLike.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.color_primary))
            holder.btnLike.setIconTintResource(R.color.color_primary)
        } else {
            holder.btnLike.setIconResource(R.drawable.ic_heart_outline)
            holder.btnLike.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.color_accent))
            holder.btnLike.setIconTintResource(R.color.color_accent)
        }

        holder.btnComments.text = "Comments (${post.commentsCount})"

        holder.btnLike.setOnClickListener { onLikeClicked(post) }
        holder.btnComments.setOnClickListener { onCommentClicked(post) }
        holder.btnAskQuestion.setOnClickListener { onAskQuestion(post) }
        holder.btnRepost.setOnClickListener { onRepostClicked(post) }
        
        holder.btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Check out this skill on SkillXchange")
                putExtra(Intent.EXTRA_TEXT, "Read this post by ${post.userName}: https://skillxchange.app/post/${post.id}")
            }
            holder.itemView.context.startActivity(Intent.createChooser(shareIntent, "Share post via"))
        }

        if (post.userId == currentUserId) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener { onDeleteClicked(post) }
        } else {
            holder.btnDelete.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = postList.size

    fun updatePosts(newList: List<Post>) {
        postList = newList
        notifyDataSetChanged()
    }
}
