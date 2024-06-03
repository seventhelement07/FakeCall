package com.seventhelement.fakecallmas.Database

import android.app.Application

class CallApp: Application() {
    // loads our data when ever needed
    val db by lazy {
        CallDatabase.getinstance(this)
    }
}