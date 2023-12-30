package com.example.cycu_notification_system

import SetSQL
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private lateinit var getpost: Getpost
    private lateinit var dbHelper: SetSQL
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)

        sharedPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // 檢查是否是第一次開啟應用程式
        val isFirstRun = sharedPrefs.getBoolean("isFirstRun", true)
        if (isFirstRun) {
            // 第一次開啟應用程式，初始化
            dbHelper = SetSQL(this)

            // 不再是第一次開啟應用程式
            val editor = sharedPrefs.edit()
            editor.putBoolean("isFirstRun", false)
            editor.apply()
        }
        getpost = Getpost(this)
        getpost.initializeViews()

//        通知的東西再想想吧
//        val notify = Notify(this)
//        notify.startNotifyAfterDelay()

        val btn_personal = findViewById<Button>(R.id.personal)
        btn_personal.setOnClickListener {
            if (UserSession.isLoggedIn) {
                // 使用者已登入，導航到 profile.kt
                val intent = Intent(this, Profile::class.java)
                startActivity(intent)
            } else {
                // 使用者未登入，導航到 login.kt
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }
        }
    }
}
// 登入狀態預設false
object UserSession {
    var isLoggedIn: Boolean = false
}
