package com.example.msdksample

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dji.sdk.keyvalue.key.RemoteControllerKey
import dji.v5.et.create
import dji.v5.et.listen
import dji.v5.utils.common.LogUtils
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

/**
 * ESP32 舵机控制页面Activity
 * 通过左摇杆水平值控制舵机角度
 */
class TabPageActivity : AppCompatActivity() {

    private val tag = LogUtils.getTag(this)
    private val handler = Handler(Looper.getMainLooper())
    
    // UI组件
    private lateinit var btnBack: Button
    private lateinit var btnConnect: Button
    private lateinit var etEsp32Ip: EditText
    private lateinit var tvConnectionStatus: TextView
    private lateinit var tvServoAngle: TextView
    private lateinit var tvJoystickValue: TextView
    
    // Socket连接
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var isConnected = false
    private val socketPort = 8888
    
    // 摇杆值
    private var leftHorizontal = 0
    private var lastSentAngle = -1 // 上次发送的角度，用于避免重复发送
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_tab_page)
        
        // 设置全屏
        try {
            window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
                android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        initViews()
        startListeningJoystick()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        disconnectSocket()
        // 取消监听
        dji.v5.manager.KeyManager.getInstance().cancelListen(this)
    }

    private fun initViews() {
        try {
            btnBack = findViewById(R.id.btn_back)
            btnConnect = findViewById(R.id.btn_connect)
            etEsp32Ip = findViewById(R.id.et_esp32_ip)
            tvConnectionStatus = findViewById(R.id.tv_connection_status)
            tvServoAngle = findViewById(R.id.tv_servo_angle)
            tvJoystickValue = findViewById(R.id.tv_joystick_value)
            
            // 设置默认IP（可以根据实际情况修改）
            etEsp32Ip.setText("192.168.1.100")
            
            // 设置返回按钮点击事件
            btnBack.setOnClickListener {
                finish() // 返回到上一个页面
            }
            
            // 设置连接按钮点击事件
            btnConnect.setOnClickListener {
                if (isConnected) {
                    disconnectSocket()
                } else {
                    val ip = etEsp32Ip.text.toString().trim()
                    if (ip.isNotEmpty()) {
                        connectSocket(ip)
                    } else {
                        updateConnectionStatus("请输入ESP32 IP地址", false)
                    }
                }
            }
            
            updateConnectionStatus("未连接", false)
        } catch (e: Exception) {
            e.printStackTrace()
            LogUtils.e(tag, "初始化视图失败: ${e.message}")
        }
    }
    
    /**
     * 开始监听左摇杆水平值
     */
    private fun startListeningJoystick() {
        RemoteControllerKey.KeyStickLeftHorizontal.create().listen(this) { value ->
            value?.let {
                leftHorizontal = it
                handler.post {
                    updateJoystickDisplay()
                    if (isConnected) {
                        sendServoAngle()
                    }
                }
                LogUtils.d(tag, "左摇杆水平值: $it")
            }
        }
    }
    
    /**
     * 更新摇杆显示
     */
    private fun updateJoystickDisplay() {
        tvJoystickValue.text = "左摇杆水平值: $leftHorizontal"
    }
    
    /**
     * 将摇杆值转换为舵机角度（0-180度）
     * 摇杆值范围通常是-660到660，映射到0-180度
     */
    private fun joystickToAngle(joystickValue: Int): Int {
        // 摇杆值范围：-660 到 660
        // 映射到角度：0 到 180
        val minJoystick = -660
        val maxJoystick = 660
        val minAngle = 0
        val maxAngle = 180
        
        // 限制摇杆值在范围内
        val clampedValue = joystickValue.coerceIn(minJoystick, maxJoystick)
        
        // 线性映射
        val angle = ((clampedValue - minJoystick).toFloat() / (maxJoystick - minJoystick) * (maxAngle - minAngle) + minAngle).toInt()
        
        return angle.coerceIn(0, 180)
    }
    
    /**
     * 发送舵机角度到ESP32
     */
    private fun sendServoAngle() {
        val angle = joystickToAngle(leftHorizontal)
        
        // 避免频繁发送相同角度（只有当角度变化超过2度时才发送）
        if (kotlin.math.abs(angle - lastSentAngle) < 2) {
            return
        }
        
        lastSentAngle = angle
        
        thread {
            try {
                writer?.let { pw ->
                    pw.println(angle.toString())
                    pw.flush()
                    handler.post {
                        tvServoAngle.text = "舵机角度: ${angle}°"
                        LogUtils.d(tag, "发送舵机角度: $angle")
                    }
                } ?: run {
                    LogUtils.e(tag, "Socket writer为空")
                }
            } catch (e: Exception) {
                LogUtils.e(tag, "发送角度失败: ${e.message}")
                handler.post {
                    updateConnectionStatus("发送失败: ${e.message}", false)
                    disconnectSocket()
                }
            }
        }
    }
    
    /**
     * 连接到ESP32 Socket服务器
     */
    private fun connectSocket(ip: String) {
        thread {
            try {
                updateConnectionStatus("正在连接...", false)
                socket = Socket(ip, socketPort)
                socket?.let {
                    writer = PrintWriter(it.getOutputStream(), true)
                    isConnected = true
                    handler.post {
                        updateConnectionStatus("已连接", true)
                        btnConnect.text = "断开"
                        etEsp32Ip.isEnabled = false
                    }
                    LogUtils.d(tag, "成功连接到ESP32: $ip:$socketPort")
                }
            } catch (e: Exception) {
                LogUtils.e(tag, "连接ESP32失败: ${e.message}")
                handler.post {
                    updateConnectionStatus("连接失败: ${e.message}", false)
                }
                disconnectSocket()
            }
        }
    }
    
    /**
     * 断开Socket连接
     */
    private fun disconnectSocket() {
        try {
            writer?.close()
            socket?.close()
            writer = null
            socket = null
            isConnected = false
            handler.post {
                updateConnectionStatus("未连接", false)
                btnConnect.text = "连接"
                etEsp32Ip.isEnabled = true
                tvServoAngle.text = "舵机角度: --"
            }
            LogUtils.d(tag, "已断开ESP32连接")
        } catch (e: Exception) {
            LogUtils.e(tag, "断开连接失败: ${e.message}")
        }
    }
    
    /**
     * 更新连接状态显示
     */
    private fun updateConnectionStatus(status: String, connected: Boolean) {
        tvConnectionStatus.text = "状态: $status"
        tvConnectionStatus.setTextColor(
            if (connected) getColor(android.R.color.holo_green_dark)
            else getColor(android.R.color.holo_red_dark)
        )
    }
}






