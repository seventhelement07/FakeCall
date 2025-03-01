package com.seventhelement.fakecallmas

import android.animation.ObjectAnimator
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.seventhelement.fakecallmas.databinding.ActivityCallBinding

class CallActivity : AppCompatActivity() {
    private lateinit var ringtone: Ringtone
    private lateinit var vibrator: Vibrator
    private lateinit var btnAcceptCall: ImageButton
    private lateinit var btnRejectCall: ImageButton
    private lateinit var binding: ActivityCallBinding
    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        sharedPreferences = getSharedPreferences("FakeCallPreferences", Context.MODE_PRIVATE)

        val name = intent.getStringExtra("name1")
        val phone = intent.getStringExtra("number1")

        val editor = sharedPreferences.edit()
        if (name != null) editor.putString("name", name)
        if (phone != null) editor.putString("phone", phone)
        editor.apply()

        val savedName = sharedPreferences.getString("name", "Unknown Caller")
        val savedPhone = sharedPreferences.getString("phone", "Unknown Number")

        binding.tvCallerName.text = savedName
        binding.tvCallerInfo.text = savedPhone

        btnAcceptCall = binding.btnAcceptCall
        btnRejectCall = binding.btnRejectCall

        // Play ringtone
        val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
        ringtone.play()

        // Start vibration
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val vibrationPattern = longArrayOf(0, 500, 500) // Vibrate for 500ms, pause for 500ms
        vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0)) // Repeat indefinitely

        // Start wave animation
        val waveAnimation = AnimationUtils.loadAnimation(this, R.anim.wave_animation)
        btnAcceptCall.startAnimation(waveAnimation)
        btnRejectCall.startAnimation(waveAnimation)

        setSwipeListener(btnAcceptCall, true) { onCallAccepted() }
        setSwipeListener(btnRejectCall, false) { onCallRejected() }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }
    }

    private fun onCallAccepted() {
        stopRingtoneAndVibration()
        finish()
        // Additional logic for accepting the call
    }

    private fun onCallRejected() {
        stopRingtoneAndVibration()
        finish()
        // Additional logic for rejecting the call
    }

    private fun stopRingtoneAndVibration() {
        if (ringtone.isPlaying) ringtone.stop()
        vibrator.cancel()
        btnAcceptCall.clearAnimation()
        btnRejectCall.clearAnimation()
    }

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
                        if (isAccept) onCallAccepted() else onCallRejected()
                    }
                    view.y = 0f
                }
            }
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtoneAndVibration()
    }
}
