package com.example.skillsexchange

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText

class ExploreActivity : AppCompatActivity() {

    private lateinit var rvUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var etSearch: TextInputEditText
    private val allUsers = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        rvUsers = findViewById(R.id.rvExploreUsers)
        etSearch = findViewById(R.id.etSearchSkill)

        allUsers.addAll(listOf(
            User("1", "Ali Hassan", "Kotlin Developer", "Loves Android", listOf("Kotlin", "Android"), listOf("UI Design", "Figma")),
            User("2", "Sara Khan", "UI Designer", "Creative designer", listOf("Figma", "UI Design"), listOf("Kotlin", "Firebase")),
            User("3", "Ahmed Raza", "Backend Dev", "Firebase expert", listOf("Firebase", "Node.js"), listOf("Android", "Kotlin")),
            User("4", "Zara Ahmed", "Data Analyst", "Python enthusiast", listOf("Python", "Excel"), listOf("Android", "UI Design")),
            User("5", "Usman Ali", "Public Speaker", "Communication coach", listOf("Communication", "Leadership"), listOf("Python", "Firebase")),
            User("6", "Hina Shah", "Web Developer", "React developer", listOf("React", "JavaScript"), listOf("Kotlin", "Android"))
        ))

        userAdapter = UserAdapter(allUsers) { user ->
            val intent = Intent(this, MatchResultActivity::class.java)
            intent.putExtra("userId", user.id)
            intent.putExtra("userName", user.name)
            intent.putExtra("userTagline", user.tagline)
            startActivity(intent)
        }

        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = userAdapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_connections
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); true }
                R.id.nav_connections -> true
                R.id.nav_create_post -> { startActivity(Intent(this, CreatePostActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
                else -> false
            }
        }
    }

    private fun filterUsers(query: String) {
        val filtered = if (query.isEmpty()) allUsers
        else allUsers.filter { user ->
            user.name.contains(query, ignoreCase = true) ||
                    user.teachSkills.any { it.contains(query, ignoreCase = true) } ||
                    user.learnSkills.any { it.contains(query, ignoreCase = true) }
        }
        userAdapter.updateList(filtered)
    }
}