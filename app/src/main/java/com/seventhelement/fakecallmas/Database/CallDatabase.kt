package com.seventhelement.fakecallmas.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [CallEntity::class], version = 1)
abstract class CallDatabase: RoomDatabase() {
    abstract fun dao():CallDao

    companion object {
        //when instances value is update  then all threads are updated
        @Volatile
        private var INSTANCE:CallDatabase? = null

        fun getinstance(context: Context): CallDatabase{
            //2 thread try to create the wsame object
            synchronized(this) {
                var instances = INSTANCE
                if (instances == null) {
                    instances = Room.databaseBuilder(
                        context.applicationContext,
                        CallDatabase::class.java,
                        "notes_database"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instances
                }
                return instances
            }


        }
    }
}