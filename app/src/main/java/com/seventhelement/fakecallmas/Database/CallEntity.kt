package com.seventhelement.fakecallmas.Database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "call")
data class CallEntity(
    @PrimaryKey(autoGenerate = true)
    val Id:Int=0,
    val name:String="",
    val number:Long=0L
)
