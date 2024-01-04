package com.example.cycu_notification_system

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

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
        scheduleWorker()

        val btn_personal = findViewById<Button>(R.id.personal)

        btn_personal.setOnClickListener {
            if (UserSession.isLoggedIn(this)) {
                val intent = Intent(this, Profile::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }
        }
    }
    private fun scheduleWorker() {
        Log.i("scheduleWorker", "Starting scheduleWorker()")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<GetPostWorker>(
            5, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(applicationContext)

        workManager.enqueueUniquePeriodicWork(
            "YourUniqueWorkName",
            ExistingPeriodicWorkPolicy.REPLACE,
            periodicWorkRequest
        )
    }
}

// 登入狀態
object UserSession {
    private const val USER_PREFS = "UserPrefs"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"

    // 取得 SharedPreferences
    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
    }

    // 設定登入狀態
    fun setLoggedIn(context: Context, isLoggedIn: Boolean) {
        val editor = getSharedPreferences(context).edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        editor.apply()
    }

    // 取得登入狀態
    fun isLoggedIn(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }
}
object UpdatedIDsManager {
    private val updatedIDs = mutableListOf<Int>()

    fun addUpdatedID(id: Int) {
        updatedIDs.add(id)
    }

    fun getUpdatedIDs(): List<Int> {
        return updatedIDs.toList()
    }

    fun clearUpdatedIDs() {
        updatedIDs.clear()
    }
}


