package com.example.cycu_notification_system
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("Range")
class GetPostWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    private var db: SQLiteDatabase
    private val CHANNEL_ID = "MyNotificationChannel"
    private var getpost: Getpost
    init {
        val dbHelper = SetSQL(context)
        db = dbHelper.writableDatabase
        getpost = Getpost(context)
    }
    val annTitleList: ArrayList<String> by lazy {
        val cursor = db.rawQuery("SELECT Ann_title FROM Categories;", null)
        val titles = ArrayList<String>()
        while (cursor.moveToNext()) {
            val annTitle = cursor.getString(cursor.getColumnIndex("Ann_title"))
            titles.add(annTitle)
        }
        titles
    }

    @SuppressLint("Range")
    override fun doWork(): Result {
        Log.i("GetPostWorker", "Starting doWork()")
        Log.i("annTitleList", "annTitleList = $annTitleList")
        UpdatedIDsManager.clearUpdatedIDs()
        val coroutineScope = CoroutineScope(Dispatchers.IO)

        coroutineScope.launch {
            for (i in 0 until annTitleList.size) {
                fetchContentAndNotify(i)
                delay(1000)
            }
        }
        return Result.success()
    }
    private suspend fun fetchContentAndNotify(index: Int) {
        getpost.fetchContent(annTitleList[index])
        delay(500)
        notifyChanges()
        UpdatedIDsManager.clearUpdatedIDs()
    }

    private fun notifyChanges() {
        val notifyManager = NotificationManagerCompat.from(context)
        createNotificationChannel()
        val updatedIDs = UpdatedIDsManager.getUpdatedIDs()
        Log.i("notifyChanges", "notifyChanges updatedIDs = $updatedIDs")

        // 通知有變動的 ID
        for (id in updatedIDs) {
            // 目前的作法是廣發通知
            //if (!checkNotificationStatus(id)) {
                val title = "新公告！"
                sendNotification(id, notifyManager, title)
            //}
        }
    }
//    應該要抓到登入的人，然後抓取有訂閱的項目才進行通知，如果沒有登入的話就廣發通知
//    private fun checkNotificationStatus(categoryID: Int): Boolean {
//        val cursor = db.rawQuery("SELECT * FROM SubscriptionCategories WHERE CategoryID = ?", arrayOf(categoryID.toString()))
//        val hasNotificationSent = cursor.count > 0
//        cursor.close()
//        return hasNotificationSent
//    }

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
    private fun sendNotification(categoryID: Int, notifyManager: NotificationManagerCompat, title: String) {
        Log.i("sendNotification", "sendNotification title = $title")
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
