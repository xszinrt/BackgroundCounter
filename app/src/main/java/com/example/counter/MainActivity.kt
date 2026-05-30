package com.example.counter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.example.counter.workers.CounterWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    
    private lateinit var tvCount: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnReset: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        tvCount = findViewById(R.id.tv_count)
        btnStart = findViewById(R.id.btn_start)
        btnStop = findViewById(R.id.btn_stop)
        btnReset = findViewById(R.id.btn_reset)
        
        // طلب صلاحية الإشعارات لأجهزة Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
        
        // عرض العدد الحالي
        updateCountDisplay()
        
        btnStart.setOnClickListener {
            startCounter()
        }
        
        btnStop.setOnClickListener {
            stopCounter()
        }
        
        btnReset.setOnClickListener {
            resetCounter()
        }
    }
    
    private fun startCounter() {
        // إعادة تعيين علامة الإيقاف
        saveStopFlag(false)
        
        // إعداد WorkRequest للتشغيل في الخلفية
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<CounterWorker>()
            .setConstraints(constraints)
            .setInitialDelay(0, TimeUnit.SECONDS)
            .build()
        
        WorkManager.getInstance(this).enqueueUniqueWork(
            CounterWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        
        Toast.makeText(this, "بدأ العد في الخلفية!", Toast.LENGTH_SHORT).show()
        
        // تحديث العرض بشكل دوري
        startUpdatingDisplay()
    }
    
    private fun stopCounter() {
        saveStopFlag(true)
        WorkManager.getInstance(this).cancelUniqueWork(CounterWorker.WORK_NAME)
        Toast.makeText(this, "تم إيقاف العد", Toast.LENGTH_SHORT).show()
        updateCountDisplay()
    }
    
    private fun resetCounter() {
        stopCounter()
        val prefs = getSharedPreferences("counter_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("current_count", 0).apply()
        saveStopFlag(false)
        updateCountDisplay()
        Toast.makeText(this, "تم تصفير العداد", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateCountDisplay() {
        val prefs = getSharedPreferences("counter_prefs", Context.MODE_PRIVATE)
        val count = prefs.getInt("current_count", 0)
        tvCount.text = count.toString()
    }
    
    private fun startUpdatingDisplay() {
        lifecycleScope.launch {
            while (true) {
                kotlinx.coroutines.delay(500)
                updateCountDisplay()
            }
        }
    }
    
    private fun saveStopFlag(stopped: Boolean) {
        val prefs = getSharedPreferences("counter_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("stop_counter", stopped).apply()
    }
    
    override fun onResume() {
        super.onResume()
        updateCountDisplay()
    }
}
