package com.example.counter.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.counter.R
import kotlinx.coroutines.delay

class CounterWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    companion object {
        const val WORK_NAME = "counter_worker"
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "counter_channel"
    }
    
    override suspend fun doWork(): Result {
        // جلب العدد الحالي من التخزين
        var count = getCurrentCount()
        
        while (true) {
            // تحديث العدد
            count++
            saveCount(count)
            
            // تحديث الإشعار
            setForeground(createForegroundInfo(count))
            
            // الانتظار ثانية واحدة
            delay(1000)
            
            // التحقق إذا طلب المستخدم إيقاف العمل
            if (isStopped) {
                return Result.success()
            }
        }
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
    
    private val isStopped: Boolean
        get() {
            val prefs = applicationContext.getSharedPreferences("counter_prefs", Context.MODE_PRIVATE)
            return prefs.getBoolean("stop_counter", false)
        }
}
