package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CallLog
import com.example.data.model.CallType
import com.example.data.model.Contact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Contact::class, CallLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun callLogDao(): CallLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pulsedial_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.contactDao(), database.callLogDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(contactDao: ContactDao, callLogDao: CallLogDao) {
            val sampleContacts = listOf(
                Contact(
                    name = "Sarah Miller",
                    phone = "(555) 234-8901",
                    email = "sarah.m@example.com",
                    label = "Family",
                    isFavorite = true,
                    speedDialKey = 2,
                    avatarColorIndex = 0,
                    notes = "Sister - weekend cabin trips"
                ),
                Contact(
                    name = "Mom",
                    phone = "(555) 987-6543",
                    email = "mom@example.com",
                    label = "Family",
                    isFavorite = true,
                    speedDialKey = 1,
                    avatarColorIndex = 1,
                    notes = "Call on Sundays"
                ),
                Contact(
                    name = "David Chen",
                    phone = "(555) 345-6789",
                    email = "david.chen@work.com",
                    label = "Work",
                    isFavorite = true,
                    speedDialKey = 3,
                    avatarColorIndex = 2,
                    notes = "Product Lead"
                ),
                Contact(
                    name = "Dr. Emily Hayes",
                    phone = "(555) 789-0123",
                    email = "care@clinicmed.org",
                    label = "Medical",
                    isFavorite = false,
                    speedDialKey = 4,
                    avatarColorIndex = 3,
                    notes = "Primary care clinic #402"
                ),
                Contact(
                    name = "Alex Johnson",
                    phone = "(555) 456-7890",
                    email = "alex.j@gymfit.io",
                    label = "Friend",
                    isFavorite = true,
                    speedDialKey = 5,
                    avatarColorIndex = 4,
                    notes = "Morning workout partner"
                ),
                Contact(
                    name = "Jessica Taylor",
                    phone = "(555) 567-8901",
                    email = "jtaylor@designstudio.co",
                    label = "Work",
                    isFavorite = false,
                    speedDialKey = 6,
                    avatarColorIndex = 5,
                    notes = "Lead UI Designer"
                ),
                Contact(
                    name = "Emergency Services",
                    phone = "911",
                    email = "",
                    label = "Emergency",
                    isFavorite = true,
                    speedDialKey = 9,
                    avatarColorIndex = 6,
                    notes = "Police / Fire / Ambulance"
                ),
                Contact(
                    name = "Marcus Vance",
                    phone = "(555) 890-1234",
                    email = "marcus.vance@techhub.net",
                    label = "Mobile",
                    isFavorite = false,
                    speedDialKey = null,
                    avatarColorIndex = 7,
                    notes = "Audio tech collaborator"
                ),
                Contact(
                    name = "Elena Rostova",
                    phone = "(555) 678-9012",
                    email = "elena.r@architects.com",
                    label = "Home",
                    isFavorite = false,
                    speedDialKey = null,
                    avatarColorIndex = 0,
                    notes = "Interior architect"
                )
            )
            contactDao.insertAll(sampleContacts)

            val now = System.currentTimeMillis()
            val sampleLogs = listOf(
                CallLog(
                    name = "Mom",
                    phone = "(555) 987-6543",
                    type = CallType.INCOMING,
                    timestamp = now - 15 * 60 * 1000, // 15 mins ago
                    durationSeconds = 245
                ),
                CallLog(
                    name = "David Chen",
                    phone = "(555) 345-6789",
                    type = CallType.OUTGOING,
                    timestamp = now - 2 * 3600 * 1000, // 2 hrs ago
                    durationSeconds = 112
                ),
                CallLog(
                    name = "Dr. Emily Hayes",
                    phone = "(555) 789-0123",
                    type = CallType.MISSED,
                    timestamp = now - 5 * 3600 * 1000, // 5 hrs ago
                    durationSeconds = 0
                ),
                CallLog(
                    name = "Sarah Miller",
                    phone = "(555) 234-8901",
                    type = CallType.OUTGOING,
                    timestamp = now - 24 * 3600 * 1000, // Yesterday
                    durationSeconds = 480
                )
            )
            sampleLogs.forEach { callLogDao.insertCallLog(it) }
        }
    }
}
