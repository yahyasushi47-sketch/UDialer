package com.example.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.example.data.local.CallLogDao
import com.example.data.local.ContactDao
import com.example.data.model.CallLog
import com.example.data.model.CallType
import com.example.data.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class DialerRepository(
    private val contactDao: ContactDao,
    private val callLogDao: CallLogDao
) {
    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()
    val speedDialContacts: Flow<List<Contact>> = contactDao.getSpeedDialContacts()
    val favoriteContacts: Flow<List<Contact>> = contactDao.getFavoriteContacts()
    val allCallLogs: Flow<List<CallLog>> = callLogDao.getAllCallLogs()

    suspend fun getContactBySpeedDialKey(key: Int): Contact? {
        return contactDao.getContactBySpeedDialKey(key)
    }

    suspend fun insertContact(contact: Contact): Long {
        return contactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: Contact) {
        contactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: Contact) {
        contactDao.deleteContact(contact)
    }

    suspend fun assignSpeedDial(contactId: Long, key: Int) {
        contactDao.clearSpeedDialKey(key)
        contactDao.setSpeedDialKey(contactId, key)
    }

    suspend fun removeSpeedDial(key: Int) {
        contactDao.clearSpeedDialKey(key)
    }

    suspend fun logCall(
        name: String,
        phone: String,
        type: CallType = CallType.OUTGOING,
        durationSeconds: Int = 0,
        contactId: Long? = null
    ) {
        callLogDao.insertCallLog(
            CallLog(
                contactId = contactId,
                name = name.ifBlank { phone },
                phone = phone,
                type = type,
                durationSeconds = durationSeconds
            )
        )
    }

    suspend fun deleteCallLog(log: CallLog) {
        callLogDao.deleteCallLog(log)
    }

    suspend fun clearAllCallLogs() {
        callLogDao.clearAllLogs()
    }

    suspend fun importDeviceContacts(context: Context): Int = withContext(Dispatchers.IO) {
        var count = 0
        try {
            val resolver = context.contentResolver
            val cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE
                ),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val typeIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)

                val imported = mutableListOf<Contact>()
                while (c.moveToNext() && imported.size < 50) {
                    val name = if (nameIndex >= 0) c.getString(nameIndex) ?: "" else ""
                    val number = if (numberIndex >= 0) c.getString(numberIndex) ?: "" else ""
                    val type = if (typeIndex >= 0) {
                        when (c.getInt(typeIndex)) {
                            ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "Home"
                            ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "Work"
                            else -> "Mobile"
                        }
                    } else "Mobile"

                    if (name.isNotBlank() && number.isNotBlank()) {
                        imported.add(
                            Contact(
                                name = name,
                                phone = number,
                                label = type,
                                avatarColorIndex = (name.hashCode() and 0x7FFFFFFF) % 8
                            )
                        )
                        count++
                    }
                }
                if (imported.isNotEmpty()) {
                    contactDao.insertAll(imported)
                }
            }
        } catch (_: Exception) {
            // Permission or resolver failure handled gracefully
        }
        count
    }
}
