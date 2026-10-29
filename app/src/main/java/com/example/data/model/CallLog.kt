package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CallType {
    OUTGOING,
    INCOMING,
    MISSED
}

@Entity(tableName = "call_logs")
data class CallLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long? = null,
    val name: String,
    val phone: String,
    val type: CallType = CallType.OUTGOING,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)
