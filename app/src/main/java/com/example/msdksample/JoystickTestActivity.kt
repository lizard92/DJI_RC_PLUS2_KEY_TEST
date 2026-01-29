package com.example.msdksample

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.view.KeyEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import dji.sdk.keyvalue.key.RemoteControllerKey
import dji.sdk.keyvalue.value.remotecontroller.FiveDimensionPressedStatus
import dji.v5.et.create
import dji.v5.et.listen
import dji.v5.manager.KeyManager
import dji.v5.manager.SDKManager
import dji.v5.utils.common.LogUtils

/**
 * 摇杆测试Activity
 * 用于测试和显示遥控器摇杆和所有按键的实时数值
 */
class JoystickTestActivity : AppCompatActivity() {

    private val tag = LogUtils.getTag(this)
    private val handler = Handler(Looper.getMainLooper())
    private var checkCount = 0
    private val maxCheckCount = 10 // 最多检查10次，每次2秒，总共20秒

    // 摇杆
    private lateinit var tvLeftHorizontal: TextView
    private lateinit var tvLeftVertical: TextView
    private lateinit var tvRightHorizontal: TextView
    private lateinit var tvRightVertical: TextView

    // 按键
    private lateinit var tvShutterButton: TextView
    private lateinit var tvRecordButton: TextView
    private lateinit var tvGoHomeButton: TextView
    private lateinit var tvPauseButton: TextView
    private lateinit var tvCustomButton1: TextView
    private lateinit var tvCustomButton2: TextView
    private lateinit var tvCustomButton3: TextView
    private lateinit var tvAuthLedButton: TextView
    private lateinit var tvFlightModeSwitch: TextView

    // 拨轮
    private lateinit var tvLeftDial: TextView
    private lateinit var tvRightDial: TextView
    private lateinit var tvScrollWheel: TextView

    // 五维按键
    private lateinit var tvFiveDimension: TextView

    // 连接状态
    private lateinit var tvConnectionStatus: TextView
    private lateinit var btnTab: Button

    // 外设按键 (通过KeyEvent检测)
    private lateinit var tvPhysicalButton1: TextView
    private lateinit var tvPhysicalButton2: TextView
    private lateinit var tvPhysicalButton3: TextView
    private lateinit var tvPhysicalButton4: TextView
    private lateinit var tvPhysicalButton5: TextView
    private lateinit var tvPhysicalButton6: TextView
    
    // 存储按键的keyCode
    private val physicalButtonKeyCodes = mutableListOf<Int>()
    
    // 按键映射：keyCode -> 按键名称
    private val keyCodeToName = mapOf(
        131 to "L1",
        132 to "L2",
        133 to "L3",
        134 to "R1",
        135 to "R2",
        136 to "R3"
    )

    // 摇杆值
    private var leftHorizontal = 0
    private var leftVertical = 0
    private var rightHorizontal = 0
    private var rightVertical = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_joystick_test)

        initViews()
        
        // 初始化快捷键按键显示
        updatePhysicalButtonsDisplay()
        
        // 确保Activity可以接收按键事件
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        // 开始检查SDK注册状态
        checkSDKRegistration()
    }
    
    override fun onResume() {
        super.onResume()
        // 确保Activity可以获得焦点以接收按键事件
        window.decorView.isFocusable = true
        window.decorView.isFocusableInTouchMode = true
        window.decorView.requestFocus()
    }

    private fun initViews() {
        tvConnectionStatus = findViewById(R.id.tv_connection_status)
        btnTab = findViewById(R.id.btn_tab)
        
        // 设置选项卡按钮点击事件
        btnTab.setOnClickListener {
            val intent = Intent(this, TabPageActivity::class.java)
            startActivity(intent)
        }
        
        // 摇杆
        tvLeftHorizontal = findViewById(R.id.tv_left_horizontal)
        tvLeftVertical = findViewById(R.id.tv_left_vertical)
        tvRightHorizontal = findViewById(R.id.tv_right_horizontal)
        tvRightVertical = findViewById(R.id.tv_right_vertical)
        
        // 按键
        tvShutterButton = findViewById(R.id.tv_shutter_button)
        tvRecordButton = findViewById(R.id.tv_record_button)
        tvGoHomeButton = findViewById(R.id.tv_gohome_button)
        tvPauseButton = findViewById(R.id.tv_pause_button)
        tvCustomButton1 = findViewById(R.id.tv_custom_button1)
        tvCustomButton2 = findViewById(R.id.tv_custom_button2)
        tvCustomButton3 = findViewById(R.id.tv_custom_button3)
        tvAuthLedButton = findViewById(R.id.tv_auth_led_button)
        tvFlightModeSwitch = findViewById(R.id.tv_flight_mode_switch)
        
        // 拨轮
        tvLeftDial = findViewById(R.id.tv_left_dial)
        tvRightDial = findViewById(R.id.tv_right_dial)
        tvScrollWheel = findViewById(R.id.tv_scroll_wheel)
        
        // 五维按键
        tvFiveDimension = findViewById(R.id.tv_five_dimension)
        
        // 外设按键
        tvPhysicalButton1 = findViewById(R.id.tv_physical_button1)
        tvPhysicalButton2 = findViewById(R.id.tv_physical_button2)
        tvPhysicalButton3 = findViewById(R.id.tv_physical_button3)
        tvPhysicalButton4 = findViewById(R.id.tv_physical_button4)
        tvPhysicalButton5 = findViewById(R.id.tv_physical_button5)
        tvPhysicalButton6 = findViewById(R.id.tv_physical_button6)
    }

    /**
     * 检查SDK注册状态，循环检查直到注册成功
     */
    private fun checkSDKRegistration() {
        if (SDKManager.getInstance().isRegistered) {
            LogUtils.d(tag, "SDK已注册，开始监听")
            tvConnectionStatus.text = "MSDK注册: 已注册"
            tvConnectionStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            startListeningAll()
        } else {
            checkCount++
            if (checkCount <= maxCheckCount) {
                tvConnectionStatus.text = "MSDK注册: 等待中... ($checkCount/$maxCheckCount)"
                tvConnectionStatus.setTextColor(getColor(android.R.color.darker_gray))
                LogUtils.d(tag, "SDK未注册，等待中... ($checkCount/$maxCheckCount)")
                // 每2秒检查一次
                handler.postDelayed({
                    checkSDKRegistration()
                }, 2000)
            } else {
                tvConnectionStatus.text = "MSDK注册: 注册超时，请检查网络和API KEY"
                tvConnectionStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                LogUtils.e(tag, "SDK注册超时")
            }
        }
    }

    /**
     * 开始监听所有摇杆和按键
     */
    private fun startListeningAll() {
        LogUtils.d(tag, "开始监听所有摇杆和按键")
        
        // 监听摇杆
        startListeningSticks()
        
        // 监听按键
        startListeningButtons()
        
        // 监听拨轮
        startListeningDials()
        
        // 监听五维按键
        startListeningFiveDimension()
        
        // 监听飞行模式开关状态
        startListeningFlightModeSwitch()
        
        // 检查连接状态
        checkConnectionStatus()
    }

    /**
     * 开始监听摇杆值
     */
    private fun startListeningSticks() {
        // 监听左摇杆水平值
        RemoteControllerKey.KeyStickLeftHorizontal.create().listen(this) { value ->
            value?.let {
                leftHorizontal = it
                updateLeftHorizontalDisplay()
                LogUtils.d(tag, "左摇杆水平值: $it")
            }
        }

        // 监听左摇杆垂直值
        RemoteControllerKey.KeyStickLeftVertical.create().listen(this) { value ->
            value?.let {
                leftVertical = it
                updateLeftVerticalDisplay()
                LogUtils.d(tag, "左摇杆垂直值: $it")
            }
        }

        // 监听右摇杆水平值
        RemoteControllerKey.KeyStickRightHorizontal.create().listen(this) { value ->
            value?.let {
                rightHorizontal = it
                updateRightHorizontalDisplay()
                LogUtils.d(tag, "右摇杆水平值: $it")
            }
        }

        // 监听右摇杆垂直值
        RemoteControllerKey.KeyStickRightVertical.create().listen(this) { value ->
            value?.let {
                rightVertical = it
                updateRightVerticalDisplay()
                LogUtils.d(tag, "右摇杆垂直值: $it")
            }
        }
    }

    /**
     * 开始监听按键
     */
    private fun startListeningButtons() {
        // 快门按钮
        RemoteControllerKey.KeyShutterButtonDown.create().listen(this) { value ->
            runOnUiThread {
                val text = if (value == true) "按下" else "未按下"
                tvShutterButton.text = "快门按钮: $text"
                if (value == true) {
                    LogUtils.d(tag, "快门按钮按下")
                }
            }
        }

        // 录像按钮
        RemoteControllerKey.KeyRecordButtonDown.create().listen(this) { value ->
            runOnUiThread {
                val text = if (value == true) "按下" else "未按下"
                tvRecordButton.text = "录像按钮: $text"
                if (value == true) {
                    LogUtils.d(tag, "录像按钮按下")
                }
            }
        }

        // 返航按钮
        RemoteControllerKey.KeyGoHomeButtonDown.create().listen(this) { value ->
            runOnUiThread {
                val text = if (value == true) "按下" else "未按下"
                tvGoHomeButton.text = "返航按钮: $text"
                if (value == true) {
                    LogUtils.d(tag, "返航按钮按下")
                }
            }
        }

        // 急停按钮
        RemoteControllerKey.KeyPauseButtonDown.create().listen(this) { value ->
            runOnUiThread {
                val text = if (value == true) "按下" else "未按下"
                tvPauseButton.text = "急停按钮: $text"
                if (value == true) {
                    LogUtils.d(tag, "急停按钮按下")
                }
            }
        }

        // 自定义按键C1
        RemoteControllerKey.KeyCustomButton1Down.create().listen(this) { value ->
            runOnUiThread {
                val text = if (value == true) "按下" else "未按下"
                tvCustomButton1.text = "自定义按键C1: $text"
                if (value == true) {
                    LogUtils.d(tag, "自定义按键C1按下")
                }
            }
        }

        // 自定义按键C2
        RemoteControllerKey.KeyCustomButton2Down.create().listen(this) { value ->
            runOnUiThread {
                val text = if (value == true) "按下" else "未按下"
                tvCustomButton2.text = "自定义按键C2: $text"
                if (value == true) {
                    LogUtils.d(tag, "自定义按键C2按下")
                }
            }
        }

        // 自定义按键C3
        RemoteControllerKey.KeyCustomButton3Down.create().listen(this) { value ->
            runOnUiThread {
                val text = if (value == true) "按下" else "未按下"
                tvCustomButton3.text = "自定义按键C3: $text"
                if (value == true) {
                    LogUtils.d(tag, "自定义按键C3按下")
                }
            }
        }

        // 发光按钮 (RCAuthLedButton)
        try {
            RemoteControllerKey.KeyRCAuthLedButtonDown.create().listen(this) { value ->
                runOnUiThread {
                    val text = if (value == true) "按下" else "未按下"
                    tvAuthLedButton.text = "发光按钮: $text"
                    if (value == true) {
                        LogUtils.d(tag, "发光按钮按下")
                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.e(tag, "发光按钮监听失败: ${e.message}")
            runOnUiThread {
                tvAuthLedButton.text = "发光按钮: 不支持"
            }
        }

    }

    /**
     * 开始监听飞行模式开关状态
     * 键值名称：KeyFlightModeSwitchState
     * 返回值：SWITCH_ONE / SWITCH_TWO / SWITCH_THREE
     */
    private fun startListeningFlightModeSwitch() {
        try {
            // 使用官方标准方式：RemoteControllerKey.KeyFlightModeSwitchState.create().listen()
            RemoteControllerKey.KeyFlightModeSwitchState.create().listen(this) { value ->
                val modeText = when (value?.toString()) {
                    "SWITCH_ONE" -> "位置1"
                    "SWITCH_TWO" -> "位置2"
                    "SWITCH_THREE" -> "位置3"
                    else -> value?.toString() ?: "未知"
                }
                runOnUiThread {
                    tvFlightModeSwitch.text = "飞行模式开关: $modeText"
                }
                LogUtils.d(tag, "飞行模式开关状态: $modeText (原始值: ${value?.toString()})")
            }
            LogUtils.d(tag, "成功设置飞行模式开关监听")
        } catch (e: Exception) {
            LogUtils.e(tag, "设置飞行模式开关监听失败: ${e.message}")
            e.printStackTrace()
            runOnUiThread {
                tvFlightModeSwitch.text = "飞行模式开关: 不支持"
            }
        }
    }

    /**
     * 开始监听拨轮
     */
    private fun startListeningDials() {
        // 左拨轮
        RemoteControllerKey.KeyLeftDial.create().listen(this) { value ->
            value?.let {
                runOnUiThread {
                    tvLeftDial.text = "左拨轮: $it"
                }
                LogUtils.d(tag, "左拨轮: $it")
            }
        }

        // 右拨轮
        RemoteControllerKey.KeyRightDial.create().listen(this) { value ->
            value?.let {
                runOnUiThread {
                    tvRightDial.text = "右拨轮: $it"
                }
                LogUtils.d(tag, "右拨轮: $it")
            }
        }

        // 滚轮
        RemoteControllerKey.KeyScrollWheel.create().listen(this) { value ->
            value?.let {
                runOnUiThread {
                    tvScrollWheel.text = "滚轮: $it"
                }
                LogUtils.d(tag, "滚轮: $it")
            }
        }
    }

    /**
     * 开始监听五维按键
     */
    private fun startListeningFiveDimension() {
        RemoteControllerKey.KeyFiveDimensionPressedStatus.create().listen(this) { value ->
            value?.let {
                runOnUiThread {
                    val statusList = mutableListOf<String>()
                    if (it.getUpwards() == true) statusList.add("上")
                    if (it.getDownwards() == true) statusList.add("下")
                    if (it.getLeftwards() == true) statusList.add("左")
                    if (it.getRightwards() == true) statusList.add("右")
                    if (it.getMiddlePressed() == true) statusList.add("中")
                    
                    val status = if (statusList.isEmpty()) "无" else statusList.joinToString("、")
                    tvFiveDimension.text = "五维按键: $status"
                    LogUtils.d(tag, "五维按键: $status")
                }
            }
        }
    }

    /**
     * 检查连接状态
     */
    private fun checkConnectionStatus() {
        // 监听遥控器连接状态
        RemoteControllerKey.KeyConnection.create().listen(this) { isConnected ->
            runOnUiThread {
                if (isConnected == true) {
                    tvConnectionStatus.text = "MSDK注册: 已注册"
                    tvConnectionStatus.setTextColor(getColor(android.R.color.holo_green_dark))
                } else {
                    tvConnectionStatus.text = "MSDK注册: 未注册"
                    tvConnectionStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                }
                LogUtils.d(tag, "遥控器连接状态: $isConnected")
            }
        }
    }

    private fun updateLeftHorizontalDisplay() {
        runOnUiThread {
            tvLeftHorizontal.text = "水平值: $leftHorizontal"
        }
    }

    private fun updateLeftVerticalDisplay() {
        runOnUiThread {
            tvLeftVertical.text = "垂直值: $leftVertical"
        }
    }

    private fun updateRightHorizontalDisplay() {
        runOnUiThread {
            tvRightHorizontal.text = "水平值: $rightHorizontal"
        }
    }

    private fun updateRightVerticalDisplay() {
        runOnUiThread {
            tvRightVertical.text = "垂直值: $rightVertical"
        }
    }

    /**
     * 捕获按键按下事件
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val buttonName = keyCodeToName[keyCode] ?: "未知"
        LogUtils.d(tag, "按键按下: keyCode=$keyCode, 按键名称=$buttonName")
        
        // 如果是快捷键按键，更新显示
        if (keyCodeToName.containsKey(keyCode)) {
            updatePhysicalButtonState(keyCode, true)
        }
        
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 捕获按键释放事件
     */
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        val buttonName = keyCodeToName[keyCode] ?: "未知"
        LogUtils.d(tag, "按键释放: keyCode=$keyCode, 按键名称=$buttonName")
        
        // 如果是快捷键按键，更新显示
        if (keyCodeToName.containsKey(keyCode)) {
            updatePhysicalButtonState(keyCode, false)
        }
        
        return super.onKeyUp(keyCode, event)
    }

    /**
     * 更新外设按键显示
     */
    private fun updatePhysicalButtonsDisplay() {
        runOnUiThread {
            // 按键顺序：L1, L2, L3, R1, R2, R3
            val keyCodes = listOf(131, 132, 133, 134, 135, 136)
            val buttons = listOf(tvPhysicalButton1, tvPhysicalButton2, tvPhysicalButton3, 
                                tvPhysicalButton4, tvPhysicalButton5, tvPhysicalButton6)
            
            keyCodes.forEachIndexed { index, keyCode ->
                if (index < buttons.size) {
                    val buttonName = keyCodeToName[keyCode] ?: "未知"
                    buttons[index].text = "$buttonName: 未按下"
                }
            }
        }
    }

    /**
     * 更新单个外设按键状态
     */
    private fun updatePhysicalButtonState(keyCode: Int, isPressed: Boolean) {
        runOnUiThread {
            // 按键顺序：L1, L2, L3, R1, R2, R3
            val keyCodes = listOf(131, 132, 133, 134, 135, 136)
            val buttons = listOf(tvPhysicalButton1, tvPhysicalButton2, tvPhysicalButton3,
                                tvPhysicalButton4, tvPhysicalButton5, tvPhysicalButton6)
            
            val index = keyCodes.indexOf(keyCode)
            if (index >= 0 && index < buttons.size) {
                val buttonName = keyCodeToName[keyCode] ?: "未知"
                val state = if (isPressed) "按下" else "未按下"
                buttons[index].text = "$buttonName: $state"
                
                // 按下时改变颜色
                if (isPressed) {
                    buttons[index].setTextColor(getColor(android.R.color.holo_green_dark))
                } else {
                    buttons[index].setTextColor(getColor(android.R.color.black))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        // 取消监听
        KeyManager.getInstance().cancelListen(this)
        LogUtils.d(tag, "停止监听所有按键")
    }
}


