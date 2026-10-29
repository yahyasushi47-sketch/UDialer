package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val label: String = "Mobile", // Mobile, Work, Home, Family, Emergency
    val isFavorite: Boolean = false,
    val speedDialKey: Int? = null, // 1 through 9
    val avatarColorIndex: Int = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
