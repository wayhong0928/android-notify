package com.example.cycu_notification_system
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import android.widget.Toast


class MainActivity : AppCompatActivity() {
    private lateinit var spinner: Spinner
    private lateinit var listView: ListView
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化各個視圖
        spinner = findViewById(R.id.spinner)
        listView = findViewById(R.id.listView)

        // 初始化 Retrofit 實例
        apiService = ApiManager.RetrofitClient.retrofit.create(ApiService::class.java)

        // 設置 Spinner 的下拉選項，這裡的 options 是你的 sn_type 選項列表
        val options = arrayOf("option1", "option2", "option3") // 這裡替換成你的選項
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        spinner.adapter = adapter

        // 設置 Spinner 選擇監聽器
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedType = options[position]
                fetchData(selectedType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun fetchData(type: String) {
        val call = apiService.getData(type)
        call.enqueue(object : Callback<List<JsonModel.DataModel>> {
            override fun onResponse(call: Call<List<JsonModel.DataModel>>, response: Response<List<JsonModel.DataModel>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        // 將數據顯示在 ListView 上
                        // 顯示不出來RRRRRRRRRRRRR
                        val titles = data.mapNotNull { it.TITLE }
                        val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, titles)
                        listView.adapter = adapter
                    } else {
                        // 處理無數據返回的情況
                        Toast.makeText(this@MainActivity, "無數據返回", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 處理響應不成功的情況
                    val errorCode = response.code()
                    val errorMessage = response.message()
                    Toast.makeText(this@MainActivity, "Error: $errorCode $errorMessage", Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<List<JsonModel.DataModel>>, t: Throwable) {
                Log.e("API_CALL", "Failed API call: ${t.message}")
                t.printStackTrace()
                // 處理請求失敗的情況
            }
        })
    }
}