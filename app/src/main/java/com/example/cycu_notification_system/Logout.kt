package com.example.cycu_notification_system

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast

class Logout(private val context: Context) {
    fun logout() {
        val sharedPrefs = context.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.remove("useraccount")
        editor.remove("username")
        editor.apply()

        UserSession.setLoggedIn(context, false)

        val intent = Intent(context, MainActivity::class.java)
        context.startActivity(intent)

        if (context is Activity) context.finish()

        Toast.makeText(context, "登出成功", Toast.LENGTH_SHORT).show()
    }
}
