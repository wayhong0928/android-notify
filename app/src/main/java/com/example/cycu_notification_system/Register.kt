package com.example.cycu_notification_system

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class Register : AppCompatActivity() {
    private lateinit var ed_account: EditText
    private lateinit var ed_username: EditText
    private lateinit var ed_password: EditText
    private lateinit var btn_register: Button
    private lateinit var btn_index: Button
    private lateinit var login_link:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        ed_account = findViewById(R.id.ed_account)
        ed_username = findViewById(R.id.ed_username)
        ed_password = findViewById(R.id.ed_password)
        btn_register = findViewById(R.id.btn_register)
        btn_index = findViewById(R.id.btn_index)
        login_link = findViewById(R.id.login_link)


        btn_register.setOnClickListener {
            val account = ed_account.text.toString().trim()
            val username = ed_username.text.toString().trim()
            val password = ed_password.text.toString().trim()

            if (account.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                // 寫入資料庫
                val dbHelper = SetSQL(this)
                val db = dbHelper.writableDatabase

                val contentValues = ContentValues()
                contentValues.put("Account", account)
                contentValues.put("Username", username)
                contentValues.put("Password", password)

                try {
                    val newRowId = db.insert("Members", null, contentValues)
                    if (newRowId != -1L) {
                        Toast.makeText(this, "註冊成功", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        // 註冊失敗
                        Toast.makeText(this, "註冊失敗", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    // 錯誤發生時的處理
                    Toast.makeText(this, "註冊失敗，發生錯誤", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                } finally {
                    db.close()
                }
            } else {
                Toast.makeText(this, "請填寫完整資訊", Toast.LENGTH_SHORT).show()
            }
        }

        btn_index.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        login_link.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}