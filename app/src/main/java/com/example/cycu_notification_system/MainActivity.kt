package com.example.cycu_notification_system

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.cycu_notification_system.R
import org.json.JSONException

class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var spinner: Spinner
    private lateinit var queue: RequestQueue
    private val BASE_URL = "https://itouch.cycu.edu.tw/home/mvc"
    private val header: HashMap<String, String> = HashMap()
    private val catalog: MutableMap<Int, HashMap<String, String>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)
        spinner = findViewById(R.id.spinner)
        queue = Volley.newRequestQueue(this)

        header["User-Agent"] =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.192 Safari/537.36"

        // 初始化載入內容
        fetchDataBasedOnInputId(0)

        // Get title
        val titleUrl = "$BASE_URL/ann.Model.jsp?method=title"
        val titleRequest = JsonObjectRequest(Request.Method.POST, titleUrl, null,
            { response ->
                try {
                    val annTitle = response.getJSONArray("ann_title")
                    for (i in 0 until annTitle.length()) {
                        val item = annTitle.getJSONObject(i)
                        val title = item.getString("name")
                        val id = item.getString("id")
                        catalog[i] = hashMapOf("id" to id, "title" to title)
                        println("$i - $title")
                    }

                    val spinnerOptions = ArrayList<String>()
                    catalog.forEach { (_, value) ->
                        spinnerOptions.add(value["title"] ?: "")
                    }

                    // Populate Spinner with options
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerOptions)
                    spinner.adapter = adapter

                    // Set up Spinner listener to handle item selection
                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            fetchDataBasedOnInputId(position)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // Handle situation where nothing is selected
                        }
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                println("無法取得標題。$error")
            }
        )
        queue.add(titleRequest)
    }

    private fun fetchDataBasedOnInputId(inputId: Int) {
        val contentUrl = "$BASE_URL/ann.Model.jsp?method=query"
        val contentParams: MutableMap<String, String> = mutableMapOf()
        val selectedItem = spinner.selectedItem as? String ?: ""
        Toast.makeText(this, "您選擇了：$selectedItem", Toast.LENGTH_SHORT).show()
        contentParams["sn_type"] = catalog[inputId]?.get("id") ?: ""
        contentParams["perPage"] = "50"

        val contentRequest = JsonObjectRequest(Request.Method.POST, contentUrl, null,
            { contentResponse ->
                try {
                    val contentList: MutableList<String> = mutableListOf()
                    val content = contentResponse.getJSONArray("content")
                    for (i in 0 until content.length()) {
                        val item = content.getJSONObject(i)
                        val contentTitle = item.getString("TITLE")
                        println(contentTitle)
                        contentList.add(contentTitle)
                    }
                    val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, contentList)
                    listView.adapter = adapter
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                println("無法取得內容。$error")
            }
        )
        queue.add(contentRequest)
    }
}
