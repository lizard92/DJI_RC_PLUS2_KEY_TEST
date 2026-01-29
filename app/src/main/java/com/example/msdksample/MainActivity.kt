package com.example.msdksample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 直接跳转到摇杆测试页面
        val intent = Intent(this, JoystickTestActivity::class.java)
        startActivity(intent)
        finish()
    }
}