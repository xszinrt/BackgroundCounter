package com.example.counter.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import kotlinx.coroutines.delay

class CounterWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "counter_worker"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "counter_channel"
    }
    
    override suspend fun doWork(): Result {
        var count = getCurrentCount()
        
        while (!isStopped()) {
            count++
            saveCount(count)
            setForeground(createForegroundInfo(count))
            delay(1000)
        }
        
        return Result.success()
    }
    
    private fun createForegroundInfo(count: Int): ForegroundInfo {
        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("عداد الخلفية")
            .setContentText("العدد الحالي: $count")
            .setSmallIcon(android.R.drawable.ic_menu_add)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        return ForegroundInfo(NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "عداد الخلفية",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "يعرض العداد أثناء العمل في الخلفية"
            }
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun getCurrentCount(): Int {
        val prefs = applicationContext.getSharedPreferences("counter_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("current_count", 0)
    }
    
    private fun saveCount(count: Int) {
        val prefs = applicationContext.getSharedPreferences("counter_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("current_count", count).apply()
    }
    
    private fun isStopped(): Boolean {
        val prefs = applicationContext.getSharedPreferences("counter_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("stop_counter", false)
    }
}
