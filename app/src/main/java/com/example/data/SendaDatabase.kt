package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfile::class,
        MoodEntry::class,
        JournalEntry::class,
        HabitDefinition::class,
        HabitCompletion::class,
        Course::class,
        Lesson::class,
        LessonProgress::class,
        ChatMessage::class,
        EmergencyResource::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SendaDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun habitDao(): HabitDao
    abstract fun courseDao(): CourseDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun emergencyResourceDao(): EmergencyResourceDao

    companion object {
        @Volatile
        private var INSTANCE: SendaDatabase? = null

        fun getDatabase(context: Context): SendaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SendaDatabase::class.java,
                    "senda_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
