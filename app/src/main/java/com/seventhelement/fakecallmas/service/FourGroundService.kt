package com.seventhelement.fakecallmas.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.seventhelement.fakecallmas.MainActivity
import com.seventhelement.fakecallmas.R

val CHANNEL_ID = "ForegroundService"
var name: String = ""
var phone: String = ""

class FourGroundService : Service(), ShakeDetector.OnShakeListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var shakeDetector: ShakeDetector

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "shake_detection_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        shakeDetector = ShakeDetector(this)

        createNotificationChannel()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)
        name = intent?.getStringExtra("name").toString()
        phone = intent?.getStringExtra("number").toString()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(shakeDetector)
    }

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null

    override fun onShake() {
        Log.d("ForegroundService", "Shake detected!")

        // Check for overlay permission
        if (!Settings.canDrawOverlays(this)) {
            val overlayIntent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(overlayIntent)
            return
        }

        // Fetch name and number from SharedPreferences (same as in CallActivity)
        val sharedPreferences = getSharedPreferences("FakeCallPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putString("phone", phone)
        editor.apply()

        val savedName = sharedPreferences.getString("name", "Unknown Caller")
        val savedPhone = sharedPreferences.getString("phone", "Unknown Number")
        // Inflate CallActivity layout
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val callView = inflater.inflate(R.layout.activity_call, null)

        // Set caller name and number
        val tvCallerName = callView.findViewById<TextView>(R.id.tv_caller_name)
        val tvCallerInfo = callView.findViewById<TextView>(R.id.tv_caller_info)
        tvCallerName.text = savedName
        tvCallerInfo.text = savedPhone

        // Set layout parameters
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        )

        // Get WindowManager and add view
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(callView, params)

        // Play Ringtone
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
        ringtone.play()

        // Start Vibration
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val vibrationPattern = longArrayOf(0, 500, 500)
        vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0))

        // Apply wave animation to buttons
        val btnAccept = callView.findViewById<ImageButton>(R.id.btn_accept_call)
        val btnReject = callView.findViewById<ImageButton>(R.id.btn_reject_call)
        val waveAnimation = AnimationUtils.loadAnimation(this, R.anim.wave_animation)
        btnAccept.startAnimation(waveAnimation)
        btnReject.startAnimation(waveAnimation)

        // Handle Call Accept/Reject with Swipe Gesture
        setSwipeListener(btnAccept, true) {
            ringtone.stop()
            vibrator.cancel()
            windowManager.removeView(callView)
            Log.d("ForegroundService", "Call Accepted")
        }

        setSwipeListener(btnReject, false) {
            ringtone.stop()
            vibrator.cancel()
            windowManager.removeView(callView)
            Log.d("ForegroundService", "Call Rejected")
        }
    }

    // Swipe Gesture Function
    private fun setSwipeListener(view: View, isAccept: Boolean, onSwipe: () -> Unit) {
        var initialY = 0f
        var isSwiping = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialY = event.rawY
                    isSwiping = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaY = event.rawY - initialY
                    if (Math.abs(deltaY) > 100) {
                        isSwiping = true
                        view.y = view.y + deltaY
                        initialY = event.rawY
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isSwiping) {
                        onSwipe()
                    }
                    view.y = 0f
                }
            }
            true
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Shake Detection Service")
            .setContentText("Listening for shake gestures...")
            .setSmallIcon(R.drawable.ic_notification) // Replace with your own icon
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Shake Detection Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf() // Ensure the service stops when the app is removed
        super.onTaskRemoved(rootIntent)
    }
}
