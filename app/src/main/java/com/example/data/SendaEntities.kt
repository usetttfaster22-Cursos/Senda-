package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val country: String = "México",
    val onboardingCompleted: Boolean = false,
    val consentGiven: Boolean = false,
    val isPremium: Boolean = false,
    val privacyAccepted: Boolean = false,
    val preferredNotificationTime: String = "20:00"
)

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey val dateStr: String, // yyyy-MM-dd
    val score: Int, // 1 to 5 (muy mal, mal, neutral, bien, muy bien)
    val emotions: String, // Comma separated list, e.g., "ansiedad,cansancio"
    val energyLevel: Int, // 1 to 5
    val sleepHours: Float,
    val physicalActivity: String = "Ninguna", // "Baja", "Media", "Alta"
    val notes: String = ""
)

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateStr: String, // yyyy-MM-dd
    val templateName: String, // "Escritura Libre" or Guided templates
    val content: String, // Full text or formatted guided text
    val answersJson: String = "", // Holds guided prompt answers if any
    val aiSummary: String = "", // summary in parts: hechos, emociones, pensamientos, necesidades, próximo paso
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_definitions")
data class HabitDefinition(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconName: String, // e.g. "spa", "directions_walk", "water_drop"
    val frequency: String = "Diario",
    val isCustom: Boolean = false,
    val isDeleted: Boolean = false
)

@Entity(tableName = "habit_completions")
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val habitId: Int,
    val dateStr: String, // yyyy-MM-dd
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val isPremium: Boolean = false
)

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey val id: Int,
    val courseId: Int,
    val title: String,
    val orderIndex: Int,
    val objective: String,
    val explanation: String,
    val example: String,
    val exercise: String,
    val reflection: String,
    val action: String,
    val durationMinutes: Int = 5
)

@Entity(tableName = "lesson_progress")
data class LessonProgress(
    @PrimaryKey val lessonId: Int,
    val isCompleted: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user", "nova", "system"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "emergency_resources")
data class EmergencyResource(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val country: String, // México, Colombia, Argentina, etc.
    val serviceName: String,
    val phoneNumber: String,
    val descriptionStr: String
)
