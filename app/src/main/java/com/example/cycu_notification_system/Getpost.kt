package com.example.cycu_notification_system

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.math.log

class Getpost(private val context: Context) {
    private lateinit var db: SQLiteDatabase
    private val BASE_URL = "https://itouch.cycu.edu.tw/home/mvc"
    private val header = Headers.Builder()
        .add("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.192 Safari/537.36")
        .build()

    private var catalog: HashMap<Int, CatalogItem> = HashMap()
    private lateinit var buttonContainer: LinearLayout

    fun initializeViews() {
        buttonContainer = (context as MainActivity).findViewById(R.id.buttonContainer)
        fetchDataAndNotify()
        fetchContent("ann_2")
    }
    fun initializeDatabase() {
        val dbHelper = SetSQL(context)
        db = dbHelper.writableDatabase
    }

    fun fetchDataAndNotify() {
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

                    (context as MainActivity).runOnUiThread {
                        val buttonContainer = context.findViewById<LinearLayout>(R.id.buttonContainer)
                        buttonContainer.removeAllViews()

                        for (i in 0 until titleArray.length()) {
                            val item = titleArray.getJSONObject(i)
                            val id = item.getString("id")
                            val name = item.getString("name")

                            catalog[i] = CatalogItem(id, name)
                            val button = Button(context)
                            button.text = "$i - $name"
                            button.setOnClickListener {
                                fetchContent(id)
                            }
                            buttonContainer.addView(button)
                        }
                    }
                }
            }
        })
    }
    data class Content(
        val title: String,
        val sn: String
    )

    data class CatalogItem(val id: String, val title: String)
    fun fetchContent(id: String) {
        initializeDatabase()
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

        client.newCall(requestContent).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FetchContent", "無法取得內容。", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body()?.string()
                responseData?.let {
                    try {
                        val jsonResponse = JSONObject(it)
                        if (jsonResponse.has("content")) {
                            val contentArray = jsonResponse.getJSONArray("content")
                            val contentList = arrayListOf<Content>()

                            for (i in 0 until contentArray.length()) {
                                val item = contentArray.getJSONObject(i)
                                val title = item.optString("TITLE")
                                val sn = item.optString("SN").toIntOrNull()
                                if (sn != null) {
                                    contentList.add(Content(title, sn.toString()))
                                }
                                if ( i == 0) {
                                    val databaseSn = getSnFromDatabase(id)
                                    val databaseId = getIdFromDatabase(id)
                                    Log.i("CHECKLOGSN", "databaseSn = $databaseSn, sn = $sn")
                                    if (databaseSn != null && sn != null && databaseId != null && databaseSn.toInt() != sn) {
                                        UpdatedIDsManager.addUpdatedID(databaseId.toInt())
                                        Log.i("addUpdatedID", "addUpdatedID = $databaseId")
                                        markNotificationSent(id, sn)
                                    }
                                }
                            }

                            (context as? MainActivity)?.runOnUiThread {
                                val listView = context.findViewById<ListView>(R.id.listView)
                                val contentAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, contentList.map { it.title })

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
                        } else {
                            Log.e("FetchContent", "Response does not contain 'content' key.")
                        }
                    } catch (e: JSONException) {
                        Log.e("FetchContent", "Error parsing JSON: ${e.message}")
                    }
                }
            }
        })
    }

    @SuppressLint("Range")
    fun getSnFromDatabase(Ann_title: String): String? {
        val cursor = db.rawQuery("SELECT Sn FROM Categories WHERE Ann_title = ?", arrayOf(Ann_title))
        var sn: String? = null
        if (cursor.moveToFirst()) {
            sn = cursor.getString(cursor.getColumnIndex("Sn"))
        }
        cursor.close()
        return sn
    }
    @SuppressLint("Range")
    fun getIdFromDatabase(Ann_title: String): String? {
        val cursor = db.rawQuery("SELECT ID FROM Categories WHERE Ann_title = ?", arrayOf(Ann_title))
        var ID: String? = null
        if (cursor.moveToFirst()) {
            ID = cursor.getString(cursor.getColumnIndex("ID"))
        }
        cursor.close()
        return ID
    }

    fun markNotificationSent(Ann_title: String, snValue: Int) {
        initializeDatabase()
        val contentValues = ContentValues().apply {
            put("Sn", snValue)
        }
        val whereClause = "Ann_title = ?"
        val whereArgs = arrayOf(Ann_title)

        Log.d("MarkNotification", "Before update: Ann_title: $Ann_title, SnValue: $snValue")
        val affectedRows = db.update("Categories", contentValues, whereClause, whereArgs)
        Log.d("MarkNotification", "After update: Rows affected: $affectedRows")
    }
}
