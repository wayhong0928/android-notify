package com.example.cycu_notification_system

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    private lateinit var getpost: Getpost
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)
        FirebaseApp.initializeApp(this)
        getpost = Getpost(this)
        getpost.initializeViews()
        val notify = Notify(this)
        notify.startNotifyAfterDelay()
    }

}
