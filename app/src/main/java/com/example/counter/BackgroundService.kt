package com.example.counter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class BackgroundService : Service() {
    
    private var isRunning = true
    private var count = 0
    
    companion object {
        const val CHANNEL_ID = "counter_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "STOP_COUNTER"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        startForeground(NOTIFICATION_ID, createNotification(0))
        
        Thread {
            while (isRunning) {
                Thread.sleep(1000)
                count++
                saveCount(count)
                updateNotification(count)
            }
        }.start()
        
        return START_STICKY
    }
    
    private fun createNotification(count: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("عداد الخلفية")
            .setContentText("العدد: $count")
            .setSmallIcon(android.R.drawable.ic_menu_add)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification(count: Int) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification(count))
        saveCount(count)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "عداد الخلفية",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
    
    private fun saveCount(count: Int) {
        getSharedPreferences("counter_prefs", Context.MODE_PRIVATE)
            .edit().putInt("current_count", count).apply()
    }
    
    override fun onDestroy() {
        isRunning = false
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
