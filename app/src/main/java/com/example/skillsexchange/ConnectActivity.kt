package com.example.skillsexchange

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class ConnectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarNetwork)
        toolbar.setNavigationOnClickListener { finish() }

        val rvSuggestions = findViewById<RecyclerView>(R.id.rvSuggestions)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationNetwork)

        val suggestions = listOf(
            User("Emily Davis", "Data Science Expert", "Loves Python"),
            User("Chris Wilson", "Video Editor", "Adobe Premiere Pro"),
            User("Anna Smith", "Marketing Strategist", "Digital Marketing"),
            User("John Lee", "Full Stack Developer", "Node.js & React")
        )

        rvSuggestions.layoutManager = GridLayoutManager(this, 2)
        rvSuggestions.adapter = SuggestionAdapter(suggestions)

        bottomNavigation.selectedItemId = R.id.nav_connections
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_connections -> true
                R.id.nav_create_post -> {
                    startActivity(Intent(this, CreatePostActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
