package com.example.skillsexchange

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class CreatePostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        val toolbar = findViewById<MaterialToolbar>(R.id.createPostToolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val etContent = findViewById<TextInputEditText>(R.id.etPostContent)
        val btnPost = findViewById<Button>(R.id.btnSubmitPost)

        btnPost.setOnClickListener {
            val content = etContent.text.toString()
            if (content.isNotEmpty()) {
                Toast.makeText(this, "Post shared with your connections!", Toast.LENGTH_LONG).show()
                finish()
            } else {
                etContent.error = "Please enter some content"
            }
        }
    }
}
