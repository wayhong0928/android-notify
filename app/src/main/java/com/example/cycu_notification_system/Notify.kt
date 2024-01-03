package com.example.cycu_notification_system

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.provider.Settings
import androidx.core.net.toUri


class Notify(private val context: Context) {

    private val CHANNEL_ID = "MyNotificationChannel"
    private val notificationId = 100

    fun startNotifyAfterDelay(title: String, content: String) {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            sendNotification(title, content)
        }, 10000) // 10 秒後發送通知
    }

    private fun sendNotification(title: String, content: String) {
        createNotificationChannel()

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationPermissionGranted = checkNotificationPermission()

        if (notificationPermissionGranted) {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } else {
            showNotificationPermissionDialog()
        }

    }

    private fun checkNotificationPermission(): Boolean {
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        return notificationManagerCompat.areNotificationsEnabled()
    }

    private fun showNotificationPermissionDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("通知權限未開啟")
        builder.setMessage("應用程式需要通知權限以發送通知。請開啟通知權限。")

        builder.setPositiveButton("前往設定") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
            openNotificationSettings()
        }

        builder.setNegativeButton("取消") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }


    private fun openNotificationSettings() {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.data = Settings.ACTION_APPLICATION_DETAILS_SETTINGS.toUri()
            intent.putExtra("package", context.packageName)
        }
        context.startActivity(intent)
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
}