package com.seventhelement.fakecallmas

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.seventhelement.fakecallmas.service.FourGroundService

class MyBroadCastReviver:BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(p0: Context?, p1: Intent?) {
        if(p1?.action.equals(Intent.ACTION_BOOT_COMPLETED))
        {
            val serviceIntent=Intent(p0,FourGroundService::class.java)
            p0?.startForegroundService(serviceIntent)
        }
    }
}