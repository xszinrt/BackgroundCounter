package com.example.counter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var tvCount: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        tvCount = findViewById(R.id.tv_count)
        
        findViewById<Button>(R.id.btn_start).setOnClickListener {
            startService(Intent(this, BackgroundService::class.java))
        }
        
        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            val intent = Intent(this, BackgroundService::class.java)
            intent.action = BackgroundService.ACTION_STOP
            startService(intent)
        }
        
        findViewById<Button>(R.id.btn_reset).setOnClickListener {
            getSharedPreferences("counter_prefs", MODE_PRIVATE)
                .edit().putInt("current_count", 0).apply()
            updateCount()
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateCount()
    }
    
    private fun updateCount() {
        val count = getSharedPreferences("counter_prefs", MODE_PRIVATE)
            .getInt("current_count", 0)
        tvCount.text = count.toString()
    }
}
