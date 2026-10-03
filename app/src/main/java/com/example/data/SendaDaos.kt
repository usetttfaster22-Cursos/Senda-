package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: UserProfile)
}

@Dao
interface MoodEntryDao {
    @Query("SELECT * FROM mood_entries ORDER BY dateStr DESC")
    fun getAllMoodEntries(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE dateStr = :date LIMIT 1")
    suspend fun getMoodEntryByDate(date: String): MoodEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodEntry(entry: MoodEntry)

    @Query("DELETE FROM mood_entries WHERE dateStr = :date")
    suspend fun deleteMoodEntryByDate(date: String)

    @Query("DELETE FROM mood_entries")
    suspend fun deleteAllMoodEntries()
}

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries ORDER BY dateStr DESC, timestamp DESC")
    fun getAllJournalEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :id LIMIT 1")
    suspend fun getJournalEntryById(id: Int): JournalEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteJournalEntryById(id: Int)

    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllJournalEntries()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habit_definitions WHERE isDeleted = 0")
    fun getAllHabitsFlow(): Flow<List<HabitDefinition>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitDefinition): Long

    @Query("UPDATE habit_definitions SET isDeleted = 1 WHERE id = :habitId")
    suspend fun softDeleteHabit(habitId: Int)

    @Query("SELECT * FROM habit_completions WHERE dateStr = :date")
    fun getCompletionsForDateFlow(date: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions")
    fun getAllCompletionsFlow(): Flow<List<HabitCompletion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND dateStr = :date")
    suspend fun removeCompletion(habitId: Int, date: String)

    @Query("DELETE FROM habit_definitions")
    suspend fun deleteAllHabits()

    @Query("DELETE FROM habit_completions")
    suspend fun deleteAllCompletions()
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCoursesFlow(): Flow<List<Course>>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun getLessonsForCourseFlow(courseId: Int): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId LIMIT 1")
    suspend fun getLessonById(lessonId: Int): Lesson?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)

    @Query("SELECT * FROM lesson_progress")
    fun getCompletedLessonsFlow(): Flow<List<LessonProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markLessonCompleted(progress: LessonProgress)

    @Query("DELETE FROM lesson_progress WHERE lessonId = :lessonId")
    suspend fun removeLessonProgress(lessonId: Int)

    @Query("DELETE FROM lesson_progress")
    suspend fun deleteAllLessonProgress()
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatMessagesFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()
}

@Dao
interface EmergencyResourceDao {
    @Query("SELECT * FROM emergency_resources WHERE country = :country")
    suspend fun getResourcesForCountry(country: String): List<EmergencyResource>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resources: List<EmergencyResource>)
}
