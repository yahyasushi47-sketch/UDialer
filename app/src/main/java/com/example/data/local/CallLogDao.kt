package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CallLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLog): Long

    @Delete
    suspend fun deleteCallLog(callLog: CallLog)

    @Query("DELETE FROM call_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM call_logs")
    suspend fun getCount(): Int
}
