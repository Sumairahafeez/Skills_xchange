package com.example.skillsexchange

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ExploreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        findViewById<Button>(R.id.btnGoToConnect).setOnClickListener {
            startActivity(Intent(this, ConnectActivity::class.java))
        }

        findViewById<Button>(R.id.btnBackFromExplore).setOnClickListener {
            finish()
        }
    }
}