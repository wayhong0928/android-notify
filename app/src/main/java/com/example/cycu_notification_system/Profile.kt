package com.example.cycu_notification_system

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast

class Profile : AppCompatActivity() {

    private lateinit var btn_index: Button
    private lateinit var btn_logout: Button
    private lateinit var btn_update: Button
    private lateinit var chec_1: CheckBox
    private lateinit var chec_2: CheckBox
    private lateinit var chec_3: CheckBox
    private lateinit var chec_4: CheckBox
    private lateinit var chec_5: CheckBox
    private lateinit var chec_6: CheckBox
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val sharedPrefs = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val userAccount = sharedPrefs.getString("useraccount", "")
        val userName = sharedPrefs.getString("username", "")
        val welcomeTextView: TextView = findViewById(R.id.welcome)
        welcomeTextView.text = "歡迎回來, $userName"
        chec_1 = findViewById(R.id.chec_1)
        chec_2 = findViewById(R.id.chec_2)
        chec_3 = findViewById(R.id.chec_3)
        chec_4 = findViewById(R.id.chec_4)
        chec_5 = findViewById(R.id.chec_5)
        chec_6 = findViewById(R.id.chec_6)
        btn_index = findViewById(R.id.btn_index)
        btn_logout = findViewById(R.id.btn_logout)
        btn_update = findViewById(R.id.btn_update)

        if (userAccount != null) {
            displayUserSubscribedCategories(userAccount)
        }

        btn_index.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        btn_logout.setOnClickListener {
            val logout = Logout(this)
            logout.logout()
        }
        btn_update.setOnClickListener {
            val useraccount = userAccount.toString()
            updateUserSubscribedCategories(useraccount)
        }
    }
    private fun displayUserSubscribedCategories(userAccount: String) {
        try {
            val setSQL = SetSQL(this)
            val userId = setSQL.getUserIdFromAccount(userAccount)

            // 取得 userID 訂閱的項目
            val subscribedCategories = setSQL.getUserSubscribedCategories(userId)

            for (categoryId in subscribedCategories) {
                when (categoryId) {
                    1 -> chec_1.isChecked = true
                    2 -> chec_2.isChecked = true
                    3 -> chec_3.isChecked = true
                    4 -> chec_4.isChecked = true
                    5 -> chec_5.isChecked = true
                    6 -> chec_6.isChecked = true
                }
            }
        } catch (e: Exception) {
            Log.e("DisplayCategoriesError", "Error displaying user subscribed categories: ${e.message}")
        }
    }
    private fun updateUserSubscribedCategories(userAccount: String): List<Int> {
        val checkedCheckboxIds = mutableListOf<Int>()

        try {
            val setSQL = SetSQL(this)
            val checkboxMapping = mapOf(
                chec_1.id to 1,
                chec_2.id to 2,
                chec_3.id to 3,
                chec_4.id to 4,
                chec_5.id to 5,
                chec_6.id to 6
            )

            // 檢查 checkbox 是否被勾選
            for ((checkboxId, categoryId) in checkboxMapping) {
                val checkbox = findViewById<CheckBox>(checkboxId)
                if (checkbox.isChecked) {
                    checkedCheckboxIds.add(categoryId)
                }
            }

            val updateResult = setSQL.updateSubsrcibedCategories(userAccount, checkedCheckboxIds)
            if (updateResult) {
                Toast.makeText(this, "修改訂閱成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "修改訂閱失敗", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("UpdateCategoriesError", "Error updating user subscribed categories: ${e.message}")
        }
        return checkedCheckboxIds
    }
}