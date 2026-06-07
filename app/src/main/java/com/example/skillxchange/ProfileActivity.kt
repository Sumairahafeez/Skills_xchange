package com.example.skillxchange

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.skillxchange.model.User
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var ivProfile: ShapeableImageView
    private lateinit var tvName: TextView
    private lateinit var tvTagline: TextView
    private lateinit var tvConnections: TextView
    private lateinit var tvProfileViews: TextView
    private lateinit var tvBio: TextView

    // Split into distinct UI groups matching your teaching vs learning lists
    private lateinit var chipGroupSkillsOffering: ChipGroup
    private lateinit var chipGroupSkillsSeeking: ChipGroup

    private lateinit var btnEdit: MaterialButton
    private lateinit var btnLogout: MaterialButton
    private lateinit var layoutStats: View
    private lateinit var containerViews: View

    private var displayedUser: User? = null
    private var targetUserId: String? = null
    private var currentUserId: String = ""

    private val photoPickerLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { uploadProfileImage(uri) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        CloudinaryConfig.initialize(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        targetUserId = intent.getStringExtra("userId") ?: currentUserId

        val toolbar = findViewById<MaterialToolbar>(R.id.profileToolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        ivProfile = findViewById(R.id.ivProfilePicture)
        tvName = findViewById(R.id.tvProfileNameDetail)
        tvTagline = findViewById(R.id.tvProfileTagline)
        tvConnections = findViewById(R.id.tvConnectionsCount)
        tvProfileViews = findViewById(R.id.tvProfileViews)
        tvBio = findViewById(R.id.tvProfileBio)

        // Map elements to your dual xml configurations
        chipGroupSkillsOffering = findViewById(R.id.chipGroupSkills)
        chipGroupSkillsSeeking = findViewById(R.id.chipGroupSkillsSeeking)

        btnEdit = findViewById(R.id.btnEditProfile)
        btnLogout = findViewById(R.id.btnLogout)
        layoutStats = findViewById(R.id.layoutStats)
        containerViews = findViewById(R.id.containerViews)

        if (targetUserId != currentUserId) {
            btnLogout.visibility = View.GONE
            layoutStats.visibility = View.GONE
            trackProfileView()
            checkConnectionStatus()
        } else {
            layoutStats.visibility = View.VISIBLE
            btnEdit.setOnClickListener { showEditDialog() }
            btnLogout.setOnClickListener { logout() }
            ivProfile.setOnClickListener {
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            containerViews.setOnClickListener {
                startActivity(Intent(this, ProfileViewersActivity::class.java))
            }
        }

        loadUserData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (targetUserId == currentUserId) {
            menuInflater.inflate(R.menu.profile_menu, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                showSettingsBottomSheet()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSettingsBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null)
        dialog.setContentView(view)

        view.findViewById<View>(R.id.btnViewers)?.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, ProfileViewersActivity::class.java))
        }

        view.findViewById<View>(R.id.btnSettingsSignOut)?.setOnClickListener {
            dialog.dismiss()
            logout()
        }
        dialog.show()
    }

    private fun trackProfileView() {
        val targetId = targetUserId ?: return
        if (targetId == currentUserId) return

        val userRef = db.collection("users").document(targetId)
        userRef.update("viewedBy", FieldValue.arrayUnion(currentUserId))
            .addOnFailureListener {
                userRef.set(mapOf("viewedBy" to listOf(currentUserId)), SetOptions.merge())
            }

        db.collection("users").document(currentUserId).get().addOnSuccessListener { doc ->
            if (isFinishing || isDestroyed) return@addOnSuccessListener
            val me = doc.toObject(User::class.java)
            NotificationHelper.createNotification(
                toUserId = targetId,
                fromUserId = currentUserId,
                fromUserName = me?.name ?: auth.currentUser?.displayName ?: "Someone",
                fromUserProfileUrl = me?.photoUrl ?: "",
                message = "viewed your profile",
                type = "PROFILE_VIEW",
                relatedId = currentUserId
            )
        }
    }

    private fun checkConnectionStatus() {
        val targetId = targetUserId ?: return
        val connectionId = if (currentUserId < targetId) "${currentUserId}_${targetId}" else "${targetId}_${currentUserId}"

        db.collection("connections").document(connectionId).addSnapshotListener { snapshot, _ ->
            if (isFinishing || isDestroyed) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                val status = snapshot.getString("status")
                val senderId = snapshot.getString("requestSenderId")

                btnEdit.visibility = View.VISIBLE
                btnEdit.isEnabled = true

                when (status) {
                    "accepted" -> {
                        btnEdit.text = "Message"
                        btnEdit.setOnClickListener {
                            val intent = Intent(this@ProfileActivity, ChatActivity::class.java).apply {
                                putExtra("userId", targetId)
                                putExtra("userName", displayedUser?.name ?: "Chat")
                            }
                            startActivity(intent)
                        }
                    }
                    "pending" -> {
                        if (senderId == currentUserId) {
                            btnEdit.text = "Pending"
                            btnEdit.isEnabled = false
                            btnEdit.setOnClickListener(null)
                        } else {
                            btnEdit.text = "Accept Request"
                            btnEdit.setOnClickListener { handleAccept() }
                        }
                    }
                    else -> {
                        btnEdit.text = "Connect"
                        btnEdit.setOnClickListener { handleConnect() }
                    }
                }
            } else {
                btnEdit.visibility = View.VISIBLE
                btnEdit.text = "Connect"
                btnEdit.setOnClickListener { handleConnect() }
            }
        }
    }

    private fun handleConnect() {
        val targetId = targetUserId ?: return
        val connectionId = if (currentUserId < targetId) "${currentUserId}_${targetId}" else "${targetId}_${currentUserId}"
        val data = mapOf(
            "id" to connectionId,
            "users" to listOf(currentUserId, targetId),
            "status" to "pending",
            "requestSenderId" to currentUserId,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        db.collection("connections").document(connectionId).set(data, SetOptions.merge()).addOnSuccessListener {
            if (!isFinishing && !isDestroyed) {
                Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleAccept() {
        val targetId = targetUserId ?: return
        val connectionId = if (currentUserId < targetId) "${currentUserId}_${targetId}" else "${targetId}_${currentUserId}"

        db.runBatch { batch ->
            batch.update(db.collection("connections").document(connectionId),
                mapOf("status" to "accepted", "updatedAt" to FieldValue.serverTimestamp()))

            batch.set(db.collection("users").document(currentUserId),
                mapOf("connections" to FieldValue.arrayUnion(targetId)), SetOptions.merge())
            batch.set(db.collection("users").document(targetId),
                mapOf("connections" to FieldValue.arrayUnion(currentUserId)), SetOptions.merge())
        }.addOnSuccessListener {
            if (!isFinishing && !isDestroyed) {
                Toast.makeText(this, "Connected successfully!", Toast.LENGTH_SHORT).show()

                db.collection("users").document(currentUserId).get().addOnSuccessListener { doc ->
                    if (isFinishing || isDestroyed) return@addOnSuccessListener
                    val me = doc.toObject(User::class.java)
                    NotificationHelper.createNotification(
                        toUserId = targetId,
                        fromUserId = currentUserId,
                        fromUserName = me?.name ?: auth.currentUser?.displayName ?: "Someone",
                        fromUserProfileUrl = me?.photoUrl ?: "",
                        message = "accepted your connection request",
                        type = "CONNECTION_ACCEPT",
                        relatedId = connectionId
                    )
                }
            }
        }.addOnFailureListener { e ->
            if (!isFinishing && !isDestroyed) {
                Toast.makeText(this, "Failed to connect: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserData() {
        val uid = targetUserId ?: return
        db.collection("users").document(uid).addSnapshotListener { snapshot, e ->
            if (isFinishing || isDestroyed || e != null || snapshot == null) return@addSnapshotListener

            displayedUser = snapshot.toObject(User::class.java)
            displayedUser?.let { user ->
                tvName.text = user.name.ifEmpty { "Member" }
                tvTagline.text = user.tagline.ifEmpty { "SkillXchange Explorer" }
                tvBio.text = user.bio.ifEmpty { "Passionate about skill sharing." }
                tvConnections.text = user.connections.size.toString()
                tvProfileViews.text = user.viewedBy.size.toString()

                if (!isFinishing && !isDestroyed) {
                    Glide.with(this)
                        .load(user.photoUrl)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(ivProfile)
                }

                // Bind your correct list fields to their relative UI groups
                displaySkills(user.teachSkills, chipGroupSkillsOffering)
                displaySkills(user.learnSkills, chipGroupSkillsSeeking)
            }
        }
    }

    private fun displaySkills(skills: List<String>, chipGroup: ChipGroup) {
        chipGroup.removeAllViews()
        for (skill in skills) {
            val chip = Chip(this).apply {
                text = skill
                setChipBackgroundColorResource(android.R.color.white)
            }
            chipGroup.addView(chip)
        }
    }

    private fun logout() {
        auth.signOut()
        Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showEditDialog() {
        if (displayedUser == null) return
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null)
        dialog.setContentView(view)

        val etName = view.findViewById<EditText>(R.id.etEditName)
        val etTagline = view.findViewById<EditText>(R.id.etEditTagline)
        val etBio = view.findViewById<EditText>(R.id.etEditBio)

        // Grab fields for separated arrays
        val etTeachSkills = view.findViewById<EditText>(R.id.etEditSkills)
        val etLearnSkills = view.findViewById<EditText>(R.id.etEditSkillsSeeking)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveProfile)

        etName?.setText(displayedUser?.name)
        etTagline?.setText(displayedUser?.tagline)
        etBio?.setText(displayedUser?.bio)
        etTeachSkills?.setText(displayedUser?.teachSkills?.joinToString(", "))
        etLearnSkills?.setText(displayedUser?.learnSkills?.joinToString(", "))

        btnSave?.setOnClickListener {
            val nameStr = etName?.text?.toString()?.trim() ?: ""
            val teachInput = etTeachSkills?.text?.toString()?.trim() ?: ""
            val learnInput = etLearnSkills?.text?.toString()?.trim() ?: ""

            val regex = Regex("[,#]")
            val teachList = if (teachInput.isEmpty()) emptyList() else teachInput.split(regex).map { it.trim().removePrefix("#") }.filter { it.isNotEmpty() }
            val learnList = if (learnInput.isEmpty()) emptyList() else learnInput.split(regex).map { it.trim().removePrefix("#") }.filter { it.isNotEmpty() }

            if (nameStr.isNotEmpty()) {
                val updates = mapOf(
                    "name" to nameStr,
                    "tagline" to etTagline?.text?.toString()?.trim(),
                    "bio" to etBio?.text?.toString()?.trim(),
                    "teachSkills" to teachList,
                    "learnSkills" to learnList
                )
                db.collection("users").document(currentUserId).update(updates).addOnSuccessListener {
                    if (!isFinishing && !isDestroyed) {
                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun uploadProfileImage(uri: Uri) {
        val uid = currentUserId
        if (uid.isEmpty()) return

        Toast.makeText(this, "Updating profile picture...", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(uri).unsigned("ml_default").callback(object : UploadCallback {
            override fun onStart(requestId: String) {}
            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                val url = resultData["secure_url"] as String
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) {
                        db.collection("users").document(uid).update("photoUrl", url)
                            .addOnSuccessListener {
                                if (!isFinishing && !isDestroyed) {
                                    Toast.makeText(this@ProfileActivity, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                                    updateAllMyPostsPhoto(url)
                                }
                            }
                    }
                }
            }
            override fun onError(requestId: String, error: ErrorInfo) {
                runOnUiThread {
                    if (!isFinishing && !isDestroyed) {
                        Toast.makeText(this@ProfileActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            override fun onReschedule(requestId: String, error: ErrorInfo) {}
        }).dispatch()
    }

    private fun updateAllMyPostsPhoto(newUrl: String) {
        db.collection("posts")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    db.runBatch { batch ->
                        for (doc in snapshot.documents) {
                            batch.update(doc.reference, "userPhotoUrl", newUrl)
                        }
                    }
                }
            }
    }
}