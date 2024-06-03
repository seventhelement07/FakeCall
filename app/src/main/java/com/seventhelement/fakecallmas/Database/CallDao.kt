package com.seventhelement.fakecallmas.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {
    @Insert
    suspend fun insert(entity:CallEntity)
    @Update
    suspend fun update(entity:CallEntity)

    @Query("SELECT * FROM `call`")
    fun fetchallTable(): Flow<List<CallEntity>>
}