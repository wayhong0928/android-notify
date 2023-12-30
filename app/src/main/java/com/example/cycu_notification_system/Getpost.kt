package com.example.cycu_notification_system

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class Getpost(private val mainActivity: MainActivity) {

    private val BASE_URL = "https://itouch.cycu.edu.tw/home/mvc"
    private val header = Headers.Builder()
        .add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.192 Safari/537.36")
        .build()

    private lateinit var listView: ListView
    private var catalog: HashMap<Int, CatalogItem> = HashMap()
    private lateinit var buttonItems: ArrayList<Button>

    fun initializeViews() {
        listView = mainActivity.findViewById(R.id.listView)
        buttonItems = arrayListOf()

        val button1 = mainActivity.findViewById<Button>(R.id.button1)
        val button2 = mainActivity.findViewById<Button>(R.id.button2)
        val button3 = mainActivity.findViewById<Button>(R.id.button3)
        val button4 = mainActivity.findViewById<Button>(R.id.button4)
        val button5 = mainActivity.findViewById<Button>(R.id.button5)
        val button6 = mainActivity.findViewById<Button>(R.id.button6)
        buttonItems.apply {
            add(button1)
            add(button2)
            add(button3)
            add(button4)
            add(button5)
            add(button6)
        }

        fetchDataAndNotify()
    }

    private fun fetchDataAndNotify() {
        val client = OkHttpClient()

        val requestTitle = Request.Builder()
            .url("$BASE_URL/ann.Model.jsp?method=title")
            .post(RequestBody.create(MediaType.parse("application/json"), "{\"perPage\":\"50\"}"))
            .headers(header)
            .build()

        client.newCall(requestTitle).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FetchData", "無法取得標題。", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body()?.string()
                responseData?.let {
                    val jsonResponse = JSONObject(it)
                    val titleArray = jsonResponse.getJSONArray("ann_title")

                    mainActivity.runOnUiThread {
                        val buttonContainer = mainActivity.findViewById<LinearLayout>(R.id.buttonContainer)
                        buttonContainer.removeAllViews()

                        for (i in 0 until titleArray.length()) {
                            val item = titleArray.getJSONObject(i)
                            val id = item.getString("id")
                            val name = item.getString("name")

                            catalog[i] = CatalogItem(id, name)
                            val button = Button(mainActivity)
                            button.text = "$i - $name"
                            button.setOnClickListener {
                                fetchContent(id, mainActivity)
                            }
                            buttonContainer.addView(button)
                        }
                    }
                }
            }
        })
    }

    private fun fetchContent(id: String, context: MainActivity) {
        val client = OkHttpClient()

        val requestBody = JSONObject()
            .put("sn_type", id)
            .put("perPage", "50")
            .toString()

        val requestContent = Request.Builder()
            .url("$BASE_URL/ann.Model.jsp?method=query")
            .post(RequestBody.create(MediaType.parse("application/json"), requestBody))
            .headers(header)
            .build()

        data class Content(
            val title: String,
            val sn: String
        )

        // 第一次安裝的時候都是0，後面如果有更新就要發送對應的通知給user。

        client.newCall(requestContent).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FetchContent", "無法取得內容。", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body()?.string()
                responseData?.let {
                    val jsonResponse = JSONObject(it)
                    val contentArray = jsonResponse.getJSONArray("content")

                    val contentList = arrayListOf<Content>()

                    for (i in 0 until contentArray.length()) {
                        val item = contentArray.getJSONObject(i)
                        val title = item.getString("TITLE")
                        val sn = item.getString("SN")
                        contentList.add(Content(title, sn))
                    }

                    mainActivity.runOnUiThread {
                        val contentAdapter = ArrayAdapter(mainActivity, android.R.layout.simple_list_item_1, contentList.map { it.title })
                        listView.adapter = contentAdapter

                        listView.setOnItemClickListener { _, _, position, _ ->
                            val selectedContent = contentList[position]
                            val url = "https://ann.cycu.edu.tw/aa/frontend/AnnItem.jsp?sn=${selectedContent.sn}"

                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            try {
                                context.startActivity(browserIntent)
                            } catch (e: ActivityNotFoundException) {
                                Log.e("Error", "Activity not found to handle Intent.")
                            }
                        }
                    }
                }
            }
        })
    }

    data class CatalogItem(val id: String, val title: String)
}
