package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class SendaScreen {
    WELCOME,
    ONBOARDING,
    DASHBOARD,
    MOOD_CHECKIN,
    NOVA_CHAT,
    JOURNAL,
    TRENDS,
    COURSES,
    HABITS,
    EMERGENCY,
    PROFILE,
    PREMIUM
}

class SendaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SendaDatabase.getDatabase(application)
    private val repository = SendaRepository(database)

    // Navigation state
    private val _currentScreen = MutableStateFlow(SendaScreen.WELCOME)
    val currentScreen: StateFlow<SendaScreen> = _currentScreen.asStateFlow()

    private val screenHistory = mutableListOf<SendaScreen>()

    fun navigateTo(screen: SendaScreen) {
        if (_currentScreen.value != screen) {
            screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun goBack() {
        if (screenHistory.isNotEmpty()) {
            _currentScreen.value = screenHistory.removeAt(screenHistory.size - 1)
        } else {
            val isCompleted = userProfile.value?.onboardingCompleted ?: false
            _currentScreen.value = if (isCompleted) SendaScreen.DASHBOARD else SendaScreen.WELCOME
        }
    }

    // State flows
    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val moodEntries: StateFlow<List<MoodEntry>> = repository.allMoodEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val journalEntries: StateFlow<List<JournalEntry>> = repository.allJournalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitDefinition>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courses: StateFlow<List<Course>> = repository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedLessons: StateFlow<List<LessonProgress>> = repository.completedLessons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live list of completions for the current date
    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val habitCompletionsForSelectedDate: StateFlow<List<HabitCompletion>> = _selectedDate
        .flatMapLatest { date -> repository.getCompletionsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabitCompletions: StateFlow<List<HabitCompletion>> = repository.getAllCompletions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Emergency resources state for selected profile country
    private val _emergencyResources = MutableStateFlow<List<EmergencyResource>>(emptyList())
    val emergencyResources: StateFlow<List<EmergencyResource>> = _emergencyResources.asStateFlow()

    // Transient UI States
    private val _isNovaTyping = MutableStateFlow(false)
    val isNovaTyping: StateFlow<Boolean> = _isNovaTyping.asStateFlow()

    private val _journalAILoading = MutableStateFlow(false)
    val journalAILoading: StateFlow<Boolean> = _journalAILoading.asStateFlow()

    init {
        viewModelScope.launch {
            // Populate database tables if they are empty
            repository.initializeDataIfNeeded()
            // Load resources for user's country
            userProfile.collect { profile ->
                profile?.let {
                    loadEmergencyResources(it.country)
                }
            }
        }
    }

    fun setSelectedDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    // Helper to get today's date
    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getTodayDayOfWeek(): String {
        return SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
    }

    private suspend fun loadEmergencyResources(country: String) {
        val resources = repository.getEmergencyResources(country)
        _emergencyResources.value = resources
    }

    // 1. Onboarding & Consent
    fun completeOnboarding(name: String, country: String, privacyAccepted: Boolean) {
        viewModelScope.launch {
            val profile = UserProfile(
                id = 1,
                name = name,
                country = country,
                onboardingCompleted = true,
                consentGiven = privacyAccepted,
                privacyAccepted = privacyAccepted,
                isPremium = false
            )
            repository.saveProfile(profile)
            loadEmergencyResources(country)
        }
    }

    // 2. Mood check-in
    fun checkInMood(
        score: Int,
        emotions: List<String>,
        energyLevel: Int,
        sleepHours: Float,
        physicalActivity: String,
        notes: String
    ) {
        viewModelScope.launch {
            val entry = MoodEntry(
                dateStr = getTodayDateString(),
                score = score,
                emotions = emotions.joinToString(","),
                energyLevel = energyLevel,
                sleepHours = sleepHours,
                physicalActivity = physicalActivity,
                notes = notes
            )
            repository.insertMoodEntry(entry)
        }
    }

    // 3. Journal operations
    fun addJournalEntry(templateName: String, content: String, runAISummary: Boolean) {
        viewModelScope.launch {
            _journalAILoading.value = true
            var aiSummary = ""
            if (runAISummary && content.isNotBlank()) {
                aiSummary = repository.generateJournalSummary(content)
            }
            val entry = JournalEntry(
                dateStr = getTodayDateString(),
                templateName = templateName,
                content = content,
                aiSummary = aiSummary
            )
            repository.insertJournalEntry(entry)
            _journalAILoading.value = false
        }
    }

    fun deleteJournalEntry(id: Int) {
        viewModelScope.launch {
            repository.deleteJournalEntry(id)
        }
    }

    // 4. Custom Habits
    fun createCustomHabit(name: String, iconName: String) {
        viewModelScope.launch {
            repository.insertHabit(
                HabitDefinition(name = name, iconName = iconName, isCustom = true)
            )
        }
    }

    fun toggleHabit(habitId: Int) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, _selectedDate.value)
        }
    }

    fun deleteCustomHabit(habitId: Int) {
        viewModelScope.launch {
            repository.softDeleteHabit(habitId)
        }
    }

    // 5. Lesson actions
    fun toggleLessonCompletion(lessonId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleLessonProgress(lessonId, isCompleted)
        }
    }

    fun getLessonsForCourse(courseId: Int): Flow<List<Lesson>> =
        repository.getLessonsForCourse(courseId)

    // 6. Nova Chat
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return
        viewModelScope.launch {
            // Add user message
            val userMsg = ChatMessage(sender = "user", messageText = userText)
            repository.addChatMessage(userMsg)

            // Trigger typing indicator
            _isNovaTyping.value = true

            // Send full conversation history to Gemini
            val currentHistory = chatMessages.value + userMsg
            val country = userProfile.value?.country ?: "México"
            val replyText = repository.getNovaResponse(currentHistory, country)

            // Add Nova message
            repository.addChatMessage(ChatMessage(sender = "nova", messageText = replyText))
            _isNovaTyping.value = false
        }
    }

    fun resetNovaConversation() {
        viewModelScope.launch {
            repository.clearChat()
            repository.addChatMessage(
                ChatMessage(
                    sender = "nova",
                    messageText = "Hola. Soy NOVA, tu asistente de bienestar de Senda. Estoy aquí para escucharte sin juzgar, ayudarte a ordenar tus pensamientos o sugerirte pequeñas acciones cotidianas. ¿Cómo te sientes hoy?"
                )
            )
        }
    }

    // 7. Security Center Trigger Crisis Event
    fun insertCrisisLog(alertText: String) {
        viewModelScope.launch {
            // For audit/safety logs, we add an empathetic response immediately
            repository.addChatMessage(
                ChatMessage(
                    sender = "system",
                    messageText = "Senda detectó una necesidad de apoyo inmediato: $alertText"
                )
            )
        }
    }

    // 8. Monetization (Become Premium Mock Simulator)
    fun togglePremiumStatus() {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            val updated = current.copy(isPremium = !current.isPremium)
            repository.saveProfile(updated)
        }
    }

    // 9. Data Control (Privacy Center)
    fun wipeAllUserData() {
        viewModelScope.launch {
            repository.deleteAllUserData()
        }
    }

    // 10. Pattern Analysis (Non-clinical insights generator)
    fun getNonClinicalPatterns(): List<String> {
        val entries = moodEntries.value
        if (entries.size < 3) {
            return listOf("Necesitamos al menos 3 check-ins de ánimo para identificar patrones significativos.")
        }

        val patterns = mutableListOf<String>()

        // 1. Fatigue & Sleep correlation
        val lowSleepDays = entries.filter { it.sleepHours > 0 && it.sleepHours < 6 }
        if (lowSleepDays.isNotEmpty()) {
            val lowSleepAndHighStress = lowSleepDays.filter {
                it.emotions.contains("cansancio") || it.emotions.contains("ansiedad") || it.emotions.contains("frustración")
            }
            if (lowSleepAndHighStress.size >= lowSleepDays.size / 2) {
                patterns.add("Dormir menos de 6 horas coincide frecuentemente con reportes de cansancio o frustración en tus registros.")
            }
        }

        // 2. Day of the week fatigue (e.g. Monday stress)
        // Let's parse day of week if entries can give us it, or let's look at average energy per mood
        val averageEnergy = entries.map { it.energyLevel }.average()
        if (averageEnergy < 2.5) {
            patterns.add("Has reportado niveles de energía física bajos en promedio durante los últimos días. Considera integrar un hábito de pausa corta sin pantallas.")
        }

        // 3. Exercise correlation
        val highExerciseDays = entries.filter { it.physicalActivity == "Alta" || it.physicalActivity == "Media" }
        if (highExerciseDays.isNotEmpty()) {
            val highExerciseAndGoodMood = highExerciseDays.filter { it.score >= 4 }
            if (highExerciseAndGoodMood.size >= highExerciseDays.size / 2) {
                patterns.add("¡Excelente patrón! Realizar actividad física moderada o alta coincide con tus días más alegres o con mayor calma.")
            }
        }

        // 4. Common emotions
        val emotionCounts = mutableMapOf<String, Int>()
        entries.forEach { entry ->
            entry.emotions.split(",").filter { it.isNotBlank() }.forEach { emotion ->
                emotionCounts[emotion] = (emotionCounts[emotion] ?: 0) + 1
            }
        }
        val topEmotion = emotionCounts.maxByOrNull { it.value }
        if (topEmotion != null && topEmotion.value >= 3) {
            patterns.add("La emoción que más has registrado recientemente es '${topEmotion.key}'. Sigue explorándola de forma amable.")
        }

        if (patterns.isEmpty()) {
            patterns.add("Tu estado de ánimo promedio se mantiene equilibrado. Continúa registrando tus días para conocerte mejor.")
        }

        return patterns
    }
}
