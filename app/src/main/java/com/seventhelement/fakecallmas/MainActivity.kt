package com.seventhelement.fakecallmas

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.seventhelement.fakecallmas.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val SYSTEM_ALERT_WINDOW_PERMISSION = 2084
    private val REQUEST_PERMISSIONS = 1

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                showPermissionExplanationDialog()
            } else {
                // Permission granted, you can display overlay
                startOverlayService()
            }

            requestPermissions()
        } else {
            // No need to request permission below Android M
            startOverlayService()
        }

        binding.startButton.setOnClickListener {
            val intent = Intent(this, FirsstActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showPermissionExplanationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Overlay Permission Required")
        builder.setMessage("This app requires permission to display over other apps. Please grant the permission in the next screen.")
        builder.setPositiveButton("OK") { dialog, _ ->
            requestOverlayPermission()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
            Toast.makeText(this, "Permission is required to display over other apps", Toast.LENGTH_SHORT).show()
        }
        builder.create().show()
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SYSTEM_ALERT_WINDOW_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // Permission granted, you can display overlay
                    startOverlayService()
                } else {
                    // Permission not granted, show a message to the user
                    Toast.makeText(this, "Permission is required to display over other apps", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startOverlayService() {

    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.CALL_PHONE,
            android.Manifest.permission.PROCESS_OUTGOING_CALLS,
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.RECEIVE_MMS,
            android.Manifest.permission.RECEIVE_WAP_PUSH,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.WRITE_CALL_LOG,
            android.Manifest.permission.POST_NOTIFICATIONS
        )

        val permissionsNeeded = permissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            permissions.forEachIndexed { index, permission ->
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    // Handle permission denial
                    Toast.makeText(this, "Permission $permission denied.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
