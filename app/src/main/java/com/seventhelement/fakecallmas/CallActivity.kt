package com.seventhelement.fakecallmas

import android.animation.ObjectAnimator
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.animation.doOnEnd

class CallActivity : AppCompatActivity() {
    private lateinit var ringtone: Ringtone
    private lateinit var btnAcceptCall: ImageButton
    private lateinit var btnRejectCall: ImageButton
    private lateinit var ivArrowLeft: ImageView
    private lateinit var ivArrowRight: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        btnAcceptCall = findViewById(R.id.btn_accept_call)
        btnRejectCall = findViewById(R.id.btn_reject_call)

        // Play ringtone
        val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
        ringtone.play()

        // Start wave animation around buttons
        val waveAnimation = AnimationUtils.loadAnimation(this, R.anim.wave_animation)
        findViewById<ImageView>(R.id.btn_accept_call).startAnimation(waveAnimation)
        findViewById<ImageView>(R.id.btn_reject_call).startAnimation(waveAnimation)

        // Set swipe listeners
        setSwipeListener(btnAcceptCall, true) { onCallAccepted() }
        setSwipeListener(btnRejectCall, false) { onCallRejected() }
    }

    private fun onCallAccepted() {
        ringtone.stop()
        btnAcceptCall.clearAnimation()
        btnRejectCall.clearAnimation()
        finish()
        // Additional logic to handle call acceptance
    }

    private fun onCallRejected() {
        ringtone.stop()
        btnAcceptCall.clearAnimation()
        btnRejectCall.clearAnimation()
        finish()
        // Additional logic to handle call rejection
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
                        if (isAccept) {
                            // Accept the call
                            onCallAccepted()
                        } else {
                            // Reject the call
                            onCallRejected()
                        }
                    }
                    view.y = 0f // Reset view position
                }
            }
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ringtone.isPlaying) {
            ringtone.stop()
        }
    }
}