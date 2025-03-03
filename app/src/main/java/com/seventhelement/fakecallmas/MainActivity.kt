package com.seventhelement.fakecallmas

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.seventhelement.fakecallmas.databinding.ActivityMainBinding
import com.seventhelement.fakecallmas.service.FourGroundService

class MainActivity : AppCompatActivity() {

    private val SYSTEM_ALERT_WINDOW_PERMISSION = 2084
    private val REQUEST_PERMISSIONS = 1

    lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION)
        }
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
}
