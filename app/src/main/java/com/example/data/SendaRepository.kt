package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class SendaRepository(private val db: SendaDatabase) {

    private val userProfileDao = db.userProfileDao()
    private val moodEntryDao = db.moodEntryDao()
    private val journalEntryDao = db.journalEntryDao()
    private val habitDao = db.habitDao()
    private val courseDao = db.courseDao()
    private val chatMessageDao = db.chatMessageDao()
    private val emergencyResourceDao = db.emergencyResourceDao()

    // Flow observations
    val userProfile: Flow<UserProfile?> = userProfileDao.getProfileFlow()
    val allMoodEntries: Flow<List<MoodEntry>> = moodEntryDao.getAllMoodEntries()
    val allJournalEntries: Flow<List<JournalEntry>> = journalEntryDao.getAllJournalEntries()
    val allHabits: Flow<List<HabitDefinition>> = habitDao.getAllHabitsFlow()
    val allCourses: Flow<List<Course>> = courseDao.getAllCoursesFlow()
    val completedLessons: Flow<List<LessonProgress>> = courseDao.getCompletedLessonsFlow()
    val chatMessages: Flow<List<ChatMessage>> = chatMessageDao.getChatMessagesFlow()

    // Direct Operations
    suspend fun getProfileDirect(): UserProfile? = userProfileDao.getProfileDirect()
    suspend fun saveProfile(profile: UserProfile) = userProfileDao.saveProfile(profile)

    suspend fun insertMoodEntry(entry: MoodEntry) = moodEntryDao.insertMoodEntry(entry)
    suspend fun deleteMoodEntry(dateStr: String) = moodEntryDao.deleteMoodEntryByDate(dateStr)

    suspend fun insertJournalEntry(entry: JournalEntry) = journalEntryDao.insertJournalEntry(entry)
    suspend fun deleteJournalEntry(id: Int) = journalEntryDao.deleteJournalEntryById(id)

    suspend fun insertHabit(habit: HabitDefinition) = habitDao.insertHabit(habit)
    suspend fun softDeleteHabit(habitId: Int) = habitDao.softDeleteHabit(habitId)

    fun getCompletionsForDate(dateStr: String): Flow<List<HabitCompletion>> =
        habitDao.getCompletionsForDateFlow(dateStr)

    fun getAllCompletions(): Flow<List<HabitCompletion>> =
        habitDao.getAllCompletionsFlow()

    suspend fun toggleHabitCompletion(habitId: Int, dateStr: String) {
        val completions = habitDao.getCompletionsForDateFlow(dateStr).firstOrNull() ?: emptyList()
        val exists = completions.any { it.habitId == habitId }
        if (exists) {
            habitDao.removeCompletion(habitId, dateStr)
        } else {
            habitDao.insertCompletion(HabitCompletion(habitId = habitId, dateStr = dateStr))
        }
    }

    fun getLessonsForCourse(courseId: Int): Flow<List<Lesson>> =
        courseDao.getLessonsForCourseFlow(courseId)

    suspend fun toggleLessonProgress(lessonId: Int, completed: Boolean) {
        if (completed) {
            courseDao.markLessonCompleted(LessonProgress(lessonId = lessonId))
        } else {
            courseDao.removeLessonProgress(lessonId)
        }
    }

    suspend fun addChatMessage(msg: ChatMessage) = chatMessageDao.insertMessage(msg)
    suspend fun clearChat() = chatMessageDao.clearChat()

    suspend fun getEmergencyResources(country: String): List<EmergencyResource> =
        emergencyResourceDao.getResourcesForCountry(country)

    // Data Export & Deletion
    suspend fun deleteAllUserData() {
        db.runInTransaction {
            // Delete personal entries
            // We can delete profile, moods, journals, habit completions, lesson progress, chat
            // To keep app running, we just recreate an empty profile
        }
        moodEntryDao.deleteAllMoodEntries()
        journalEntryDao.deleteAllJournalEntries()
        habitDao.deleteAllCompletions()
        courseDao.deleteAllLessonProgress()
        chatMessageDao.clearChat()
        // Reset profile
        val oldProfile = getProfileDirect()
        val newProfile = UserProfile(
            id = 1,
            name = oldProfile?.name ?: "",
            country = oldProfile?.country ?: "México",
            onboardingCompleted = false,
            consentGiven = false,
            isPremium = false,
            privacyAccepted = false
        )
        saveProfile(newProfile)
    }

    // Call Gemini for Nova
    suspend fun getNovaResponse(conversation: List<ChatMessage>, userCountry: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "¡Hola! Para poder conversar conmigo, por favor configura tu clave de Gemini API en el panel de secretos de AI Studio. De momento estaré disponible en modo de demostración local."
        }

        // Build context & system instruction
        val systemPrompt = """
            Eres NOVA, un asistente de bienestar emocional y reflexión personal para adultos de habla hispana en América Latina.
            Idioma principal: Español latinoamericano, empático, cálido, respetuoso y breve. Max 2-3 párrafos por respuesta.
            
            NORMAS ÉTICAS Y DE SEGURIDAD (CRÍTICAS):
            1. No eres un terapeuta, psicólogo, médico ni un ser humano real. Nunca pretendas serlo.
            2. Tu fin es ofrecer orientación de bienestar, no diagnosticar ni sustituir a profesionales.
            3. No fomentes la dependencia emocional ni uses frases románticas o promesas de exclusividad.
            4. Si el usuario muestra síntomas persistentes de tristeza profunda, ansiedad incapacitante, violencia, abuso, consumo riesgoso, duelo complicado o problemas de sueño graves, debes recomendarle de manera respetuosa pero clara buscar apoyo profesional humano.
            5. Si detectas ideación suicida, autolesión, violencia extrema o psicosis, debes detener el flujo conversacional habitual y recomendarle acudir a la sección "Ayuda Urgente" de la app o contactar inmediatamente a las líneas de crisis o emergencias de su país ($userCountry) o seres queridos. No hagas terapia de crisis extensa.
            
            ESTILO CONVERSACIONAL:
            - Usa preguntas abiertas y el reflejo empático para que el usuario explore sus propias soluciones.
            - No impongas conclusiones. Propón acciones pequeñas y hábitos sencillos de bajo esfuerzo de manera opcional.
            - Sé breve y claro.
        """.trimIndent()

        // Map ChatMessage objects to GeminiContent
        // We limit history to last 10 messages to save context windows
        val contents = conversation.takeLast(10).map { msg ->
            val role = if (msg.sender == "user") "user" else "model"
            GeminiContent(role = role, parts = listOf(GeminiPart(text = msg.messageText)))
        }

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig(temperature = 0.7)
        )

        try {
            val response = RetrofitClient.api.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No he podido procesar la respuesta. Por favor, inténtalo de nuevo."
        } catch (e: Exception) {
            Log.e("SendaRepository", "Error calling Gemini", e)
            "Lo siento, he tenido un problema de conexión temporal. Recuerda que puedes escribir en tu diario o completar tus hábitos mientras vuelvo a estar disponible."
        }
    }

    // Call Gemini to generate journal summary
    suspend fun generateJournalSummary(journalText: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext """
                [Resumen AI de Demostración]
                • HECHOS: Escribiste en tu diario reflexivo.
                • EMOCIONES: Expresión emocional libre.
                • PENSAMIENTOS: Enfoque y reflexiones de la sesión.
                • NECESIDADES: Pausa y autocuidado.
                • PRÓXIMO PASO: Realizar una respiración profunda de 2 minutos.
            """.trimIndent()
        }

        val systemPrompt = """
            Eres un analizador de bienestar digital privado de Senda.
            Tu tarea es leer una entrada de diario libre y generar un resumen reflexivo breve y estructurado en español.
            Debes separar estrictamente el resumen en las siguientes categorías con viñetas:
            • HECHOS: (Resumen objetivo de qué ocurrió)
            • EMOCIONES: (Emociones detectadas de forma no diagnóstica)
            • PENSAMIENTOS: (Creencias o ideas predominantes mencionadas)
            • NECESIDADES: (Necesidad subyacente identificada, ej. descanso, límites, etc.)
            • PRÓXIMO PASO: (Una pequeña acción de bajo esfuerzo recomendada para el día de hoy)
            
            No uses lenguaje clínico ni des diagnósticos como "depresión" o "ansiedad clínica". Sé sumamente empático y breve.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = journalText)))),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = GeminiGenerationConfig(temperature = 0.4)
        )

        try {
            val response = RetrofitClient.api.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No fue posible generar el resumen. Tu texto permanece totalmente privado y seguro."
        } catch (e: Exception) {
            Log.e("SendaRepository", "Error summarizing", e)
            "Tu texto permanece totalmente guardado de forma privada. El resumen inteligente de IA no se pudo generar debido a una falla de conexión."
        }
    }

    // Initialize/Pre-populate database
    suspend fun initializeDataIfNeeded() = withContext(Dispatchers.IO) {
        // 1. Prepopulate Emergency Resources
        val existingResources = emergencyResourceDao.getResourcesForCountry("México")
        if (existingResources.isEmpty()) {
            val resourcesList = listOf(
                EmergencyResource(country = "México", serviceName = "Línea de la Vida", phoneNumber = "8009112000", descriptionStr = "Apoyo emocional especializado 24/7 y prevención del suicidio de la Secretaría de Salud."),
                EmergencyResource(country = "México", serviceName = "SAPTEL", phoneNumber = "5552598121", descriptionStr = "Servicio de Medicina Crítica y de Salud Mental vía telefónica."),
                EmergencyResource(country = "Colombia", serviceName = "Línea de la Esperanza", phoneNumber = "106", descriptionStr = "Atención en salud mental, apoyo en crisis emocionales y acompañamiento de la Secretaría de Salud."),
                EmergencyResource(country = "Colombia", serviceName = "Línea de Orientación", phoneNumber = "192", descriptionStr = "Opción 4: Soporte en salud mental a nivel nacional."),
                EmergencyResource(country = "Argentina", serviceName = "Centro de Asistencia al Suicida", phoneNumber = "135", descriptionStr = "Línea gratuita de prevención del suicidio y asistencia telefónica de emergencia."),
                EmergencyResource(country = "Argentina", serviceName = "Línea de Salud Mental", phoneNumber = "08003331665", descriptionStr = "Atención y contención en crisis emocionales y consumos problemáticos."),
                EmergencyResource(country = "Chile", serviceName = "Salud Responde", phoneNumber = "6003607777", descriptionStr = "Línea de orientación en salud pública y apoyo psicosocial."),
                EmergencyResource(country = "Chile", serviceName = "Fono Salud Mental", phoneNumber = "4141", descriptionStr = "Línea del Ministerio de Salud específica para prevención del suicidio."),
                EmergencyResource(country = "Perú", serviceName = "Línea de Infosalud", phoneNumber = "113", descriptionStr = "Opción 5: Consejería psicológica y apoyo emocional en crisis, gratuito."),
                EmergencyResource(country = "Uruguay", serviceName = "Línea de Vida Prevención", phoneNumber = "08000767", descriptionStr = "Línea especializada de prevención del suicidio del ASSE, disponible 24 horas."),
                EmergencyResource(country = "España", serviceName = "Teléfono de la Esperanza", phoneNumber = "717003717", descriptionStr = "Orientación telefónica de urgencia y apoyo en crisis emocionales.")
            )
            emergencyResourceDao.insertResources(resourcesList)
            Log.d("SendaRepository", "Emergency resources prepopulated.")
        }

        // 2. Prepopulate Default Habits
        val habits = habitDao.getAllHabitsFlow().firstOrNull() ?: emptyList()
        if (habits.isEmpty()) {
            habitDao.insertHabit(HabitDefinition(name = "Respirar profundamente 2 min", iconName = "spa", isCustom = false))
            habitDao.insertHabit(HabitDefinition(name = "Caminar 10 minutos", iconName = "directions_walk", isCustom = false))
            habitDao.insertHabit(HabitDefinition(name = "Beber un vaso de agua", iconName = "local_drink", isCustom = false))
            habitDao.insertHabit(HabitDefinition(name = "Escribir una preocupación", iconName = "edit", isCustom = false))
            habitDao.insertHabit(HabitDefinition(name = "Llamar a una persona de confianza", iconName = "call", isCustom = false))
            habitDao.insertHabit(HabitDefinition(name = "Realizar pausa sin pantallas (15 min)", iconName = "phonelink_off", isCustom = false))
            Log.d("SendaRepository", "Default habits prepopulated.")
        }

        // 3. Prepopulate Courses & Lessons
        val courses = courseDao.getAllCoursesFlow().firstOrNull() ?: emptyList()
        if (courses.isEmpty()) {
            val coursesList = listOf(
                Course(1, "Manejo del Estrés Cotidiano", "Aprende a comprender la respuesta biológica de tu cuerpo al estrés y gestionarla con micro-ejercicios prácticos.", 15, false),
                Course(2, "Límites Saludables", "Desarrolla la habilidad de proteger tu energía, decir no asertivamente y comunicar tus necesidades sin culpa.", 15, false),
                Course(3, "Hábitos y Disciplina", "Cómo construir hábitos de bajo esfuerzo que duren a largo plazo utilizando el poder de los pequeños pasos.", 20, true),
                Course(4, "Comunicación Asertiva", "Mejora tus relaciones expresando lo que sientes de forma directa, empática y respetuosa.", 25, true)
            )
            courseDao.insertCourses(coursesList)

            val lessonsList = listOf(
                // Curso 1: Estrés
                Lesson(101, 1, "Comprendiendo la respuesta al estrés", 1, 
                    "Entender la respuesta natural del estrés en el cuerpo.",
                    "El estrés es una reacción de supervivencia diseñada para huir de peligros. Hoy en día, se activa ante plazos de trabajo, correos o tráfico.",
                    "Al llegar tarde al trabajo, tu corazón late rápido, exactamente igual que si un animal salvaje te persiguiera.",
                    "Cierra los ojos. Inhala en 4 tiempos, mantén el aire en 4 tiempos, y exhala lentamente en 4 tiempos. Repítelo 3 veces.",
                    "¿En qué parte de mi cuerpo siento la tensión cuando me estreso?",
                    "Hoy, haz una pausa de un minuto cuando notes prisa y suelta conscientemente los hombros.",
                    5),
                Lesson(102, 1, "Identificando tus desencadenantes", 2,
                    "Reconocer las situaciones específicas que elevan tu estrés.",
                    "No a todos nos estresa lo mismo. Al identificar tus gatilladores, puedes planificar respuestas y reducir su impacto emocional.",
                    "Revisar mensajes de trabajo tarde en la noche o comer con prisas frente a una computadora.",
                    "Anota mentalmente los dos momentos que te resultaron más abrumadores el día de ayer.",
                    "¿Qué parte de mis estresores cotidianos puedo controlar y qué parte corresponde a mi reacción?",
                    "Hoy, define una hora límite para dejar de revisar notificaciones del trabajo.",
                    5),
                Lesson(103, 1, "La pausa sagrada de descompresión", 3,
                    "Aprender a detener el ritmo de forma consciente entre tareas.",
                    "Hacer micro-pausas evita que el estrés se acumule como agua en un vaso. Permite que el sistema nervioso vuelva a regularse.",
                    "Pararte un momento a estirarte tras terminar un reporte y antes de empezar una videollamada.",
                    "Realiza una torsión suave de tu espalda a la izquierda y derecha mientras respiras hondo.",
                    "¿Permito que mi mente descanse al menos 2 minutos entre mis deberes diarios?",
                    "Hoy, entre cada actividad, regálate 60 segundos de silencio sin mirar el teléfono.",
                    5),

                // Curso 2: Límites
                Lesson(201, 2, "Decir NO es decir SÍ a ti mismo", 1,
                    "Comprender que establecer límites es un acto de respeto mutuo.",
                    "Muchos tememos decir no por miedo al rechazo. Pero un no honesto es mejor que un sí resentido o forzado.",
                    "Un amigo te invita a salir cuando estás físicamente agotado por la jornada, y decides quedarte a descansar.",
                    "Practica verbalizar en privado: 'Agradezco mucho la invitación, pero hoy necesito quedarme a descansar en casa'.",
                    "¿A qué le digo que sí por compromiso mientras me digo que no a mí mismo?",
                    "Hoy, niega amablemente una solicitud que exceda tu capacidad de tiempo o energía.",
                    5),
                Lesson(202, 2, "La asertividad sin disculpas largas", 2,
                    "Aprender a comunicar límites de forma clara y directa sin justificarse en exceso.",
                    "Dar excusas interminables debilita tu límite y genera confusión. El límite por sí solo tiene validez.",
                    "Responder 'No puedo ir hoy, tengo un pendiente familiar' en lugar de contar una historia compleja para justificarte.",
                    "Escribe en tu mente un límite simple que requieras comunicarle a un colega o familiar.",
                    "¿Siento culpa cuando digo lo que necesito con honestidad?",
                    "Comunica hoy una necesidad personal con total claridad y usando frases directas.",
                    5),
                Lesson(203, 2, "Límites con la tecnología", 3,
                    "Establecer barreras saludables con los dispositivos digitales.",
                    "La hiperconectividad nos expone constantemente a demandas externas. Los límites digitales protegen tu paz mental.",
                    "Activar el modo 'No molestar' en tu celular a partir de las 9:00 PM.",
                    "Coloca tu teléfono en un cajón o en otra habitación por los próximos 15 minutos.",
                    "¿Cuánto tiempo libre pierdo respondiendo notificaciones no urgentes?",
                    "Hoy, establece al menos una hora libre de pantallas antes de ir a dormir.",
                    5)
            )
            courseDao.insertLessons(lessonsList)
            Log.d("SendaRepository", "Default courses and lessons prepopulated.")
        }
    }
}
