package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY isFavorite DESC, name ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: Long): Contact?

    @Query("SELECT * FROM contacts WHERE speedDialKey = :key LIMIT 1")
    suspend fun getContactBySpeedDialKey(key: Int): Contact?

    @Query("SELECT * FROM contacts WHERE speedDialKey IS NOT NULL ORDER BY speedDialKey ASC")
    fun getSpeedDialContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchContacts(query: String): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<Contact>)

    @Update
    suspend fun updateContact(contact: Contact)

    @Delete
    suspend fun deleteContact(contact: Contact)

    @Query("UPDATE contacts SET speedDialKey = NULL WHERE speedDialKey = :key")
    suspend fun clearSpeedDialKey(key: Int)

    @Query("UPDATE contacts SET speedDialKey = :key WHERE id = :contactId")
    suspend fun setSpeedDialKey(contactId: Long, key: Int)

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getCount(): Int
}
