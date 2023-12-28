package com.example.cycu_notification_system

import SetSQL
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.cycu_notification_system.UserSession.isLoggedIn

class Login : AppCompatActivity() {
    private lateinit var useraccount: EditText
    private lateinit var pwd: EditText
    private lateinit var btn_login: Button
    private lateinit var btn_register: Button
    private lateinit var db: SQLiteDatabase
    private lateinit var userAccount: SharedPreferences
    private lateinit var userName: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        useraccount = findViewById(R.id.ed_account)
        pwd = findViewById(R.id.pwd)
        btn_login = findViewById(R.id.btn_login)
        btn_register = findViewById(R.id.btn_register)
        userAccount = getSharedPreferences("useraccount", MODE_PRIVATE)
        userName = getSharedPreferences("username", MODE_PRIVATE)

        val setSQL = SetSQL(this)
        db = setSQL.readableDatabase

        btn_login.setOnClickListener {
            val username = useraccount.text.toString().trim()
            val password = pwd.text.toString().trim()

            // Log 登入開始
            Log.d("LoginStatus", "登入開始")

            if (validateLogin(username, password)) {
                // 登入成功
                Toast.makeText(this, "登入成功", Toast.LENGTH_SHORT).show()
                isLoggedIn = true
                // 從資料庫取得名稱
                val name = getNameFromDatabase(username)

                // 設定 SharedPreferences
                val editorID = userAccount.edit()
                editorID.putString("useraccount", username)
                editorID.apply()

                val editorName = userName.edit()
                editorName.putString("username", name)
                editorName.apply()

                // Log 登入成功
                Log.d("LoginStatus", "登入成功")

                // Intent 到首頁
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // 結束登入頁面
            } else {
                // 登入失敗
                Toast.makeText(this, "登入失敗", Toast.LENGTH_SHORT).show()

                // Log 登入失敗
                Log.d("LoginStatus", "登入失敗")
            }
        }

        btn_register.setOnClickListener {
            // 跳轉到註冊頁面
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    private fun validateLogin(username: String, password: String): Boolean {
        val query = "SELECT * FROM Members WHERE Account = ? AND Password = ?"
        val cursor: Cursor? = db.rawQuery(query, arrayOf(username, password))
        val count = cursor?.count ?: 0
        cursor?.close()
        return count > 0
    }

    @SuppressLint("Range")
    private fun getNameFromDatabase(username: String): String {
        var name = ""
        val query = "SELECT Username FROM Members WHERE Account = ?"
        val cursor: Cursor? = db.rawQuery(query, arrayOf(username))
        cursor?.use {
            if (it.moveToFirst()) {
                name = it.getString(it.getColumnIndex("Username"))
            }
        }
        cursor?.close()
        return name
    }
}
