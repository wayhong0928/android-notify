package com.example.cycu_notification_system
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class GetPostWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private var db: SQLiteDatabase
    private val CHANNEL_ID = "MyNotificationChannel"
    private var getpost: Getpost
    init {
        val dbHelper = SetSQL(context)
        db = dbHelper.writableDatabase
        getpost = Getpost(context)
    }
    @SuppressLint("Range")
    override fun doWork(): Result {
        Log.i("GetPostWorker", "Starting doWork()")
        val cursor = db.rawQuery("SELECT Ann_title FROM Categories;", null)
        val annTitleList = ArrayList<String>()
        while (cursor.moveToNext()) {
            val annTitle = cursor.getString(cursor.getColumnIndex("Ann_title"))
            annTitleList.add(annTitle)
        }

        UpdatedIDsManager.clearUpdatedIDs()

        // 執行 getpost 並更新資料庫
        for (i in 0 until annTitleList.size) {
            getpost.fetchContent(annTitleList[i])
        }
        notifyChanges()
        return Result.success()
    }

    private fun notifyChanges() {
        val notifyManager = NotificationManagerCompat.from(context)
        createNotificationChannel()
        val updatedIDs = UpdatedIDsManager.getUpdatedIDs()
        Log.i("notifyChanges", "notifyChanges updatedIDs = $updatedIDs")

        // 通知有變動的 ID
        for (id in updatedIDs) {
            if (!checkNotificationStatus(id)) {
                val title = "新的公告"
                // 要抓一下名稱
                val content = "類別 $id 有新的公告。"
                sendNotification(id, notifyManager, title, content)
            }
        }
    }

    private fun checkNotificationStatus(categoryID: Int): Boolean {
        val cursor = db.rawQuery("SELECT * FROM SubscriptionCategories WHERE CategoryID = ?", arrayOf(categoryID.toString()))
        val hasNotificationSent = cursor.count > 0
        cursor.close()
        return hasNotificationSent
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My Notification Channel"
            val descriptionText = "Channel Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendNotification(categoryID: Int, notifyManager: NotificationManagerCompat, title: String, content: String) {
        val dbHelper = SetSQL(context)
        val db = dbHelper.readableDatabase

        val categoryName = getCategoryName(db, categoryID)

        db.close()

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("類別 $categoryName 有新的公告。")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notifyManager.notify(categoryID, builder.build())
    }

    // 根據類別 ID 獲取類別名稱
    @SuppressLint("Range")
    private fun getCategoryName(db: SQLiteDatabase, categoryID: Int): String {
        val cursor = db.rawQuery("SELECT Name FROM Categories WHERE ID = ?", arrayOf(categoryID.toString()))
        var categoryName = ""

        if (cursor.moveToFirst()) {
            categoryName = cursor.getString(cursor.getColumnIndex("Name"))
        }

        cursor.close()
        return categoryName
    }

}
