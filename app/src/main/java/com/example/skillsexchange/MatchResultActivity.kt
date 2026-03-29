package com.example.skillsexchange

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MatchResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_result)

        findViewById<Button>(R.id.btnBackFromMatch).setOnClickListener {
            finish()
        }
    }
}