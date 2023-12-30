package com.example.cycu_notification_system

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class Profile : AppCompatActivity() {

    private lateinit var btn_index: Button
    private lateinit var btn_logout: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        val sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val userName = sharedPrefs.getString("username", "")
        val welcomeTextView: TextView = findViewById(R.id.welcome)
        welcomeTextView.text = "歡迎回來, $userName"



        btn_index = findViewById(R.id.btn_index)
        btn_logout = findViewById(R.id.btn_logout)
        btn_index.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        btn_logout.setOnClickListener {
            val logout = Logout(this)
            logout.logout()
        }
    }
}