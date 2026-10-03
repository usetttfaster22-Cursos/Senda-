@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.*
import com.example.viewmodel.SendaScreen
import com.example.viewmodel.SendaViewModel
import java.util.Locale

@Composable
fun SendaApp(viewModel: SendaViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // Ensure we send users to Welcome or Onboarding on startup if not completed
    LaunchedEffect(profile) {
        profile?.let {
            if (!it.onboardingCompleted && currentScreen != SendaScreen.ONBOARDING) {
                viewModel.navigateTo(SendaScreen.WELCOME)
            } else if (it.onboardingCompleted && currentScreen == SendaScreen.WELCOME) {
                viewModel.navigateTo(SendaScreen.DASHBOARD)
            }
        }
    }

    if (currentScreen == SendaScreen.WELCOME) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            WelcomeScreen(viewModel)
        }
    } else if (currentScreen == SendaScreen.ONBOARDING) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            OnboardingScreen(viewModel)
        }
    } else {
        Row(modifier = Modifier.fillMaxSize()) {
            // Screen Width adaptation (Navigation Rail for Tablets/Foldables)
            if (isTablet) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("tablet_nav_rail")
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    IconButton(
                        onClick = { viewModel.navigateTo(SendaScreen.DASHBOARD) },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Spa, contentDescription = "Inicio", tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    
                    val items = listOf(
                        Triple(SendaScreen.DASHBOARD, Icons.Outlined.Home, "Inicio"),
                        Triple(SendaScreen.HABITS, Icons.Outlined.CheckCircle, "Hábitos"),
                        Triple(SendaScreen.JOURNAL, Icons.Outlined.Book, "Diario"),
                        Triple(SendaScreen.COURSES, Icons.Outlined.School, "Cursos"),
                        Triple(SendaScreen.TRENDS, Icons.Outlined.TrendingUp, "Patrones"),
                        Triple(SendaScreen.PROFILE, Icons.Outlined.Person, "Perfil")
                    )

                    items.forEach { (screen, icon, label) ->
                        NavigationRailItem(
                            selected = currentScreen == screen,
                            onClick = { viewModel.navigateTo(screen) },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.testTag("rail_item_${screen.name.lowercase()}")
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Spa, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Senda", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        actions = {
                            // HELP NOW BUTTON (Centro de Seguridad - visible always)
                            Button(
                                onClick = { viewModel.navigateTo(SendaScreen.EMERGENCY) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .testTag("help_now_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Necesito ayuda ahora", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (profile?.isPremium == true) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("Premium", color = Color.White, modifier = Modifier.padding(4.dp), fontSize = 10.sp)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                bottomBar = {
                    if (!isTablet) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            val items = listOf(
                                Triple(SendaScreen.DASHBOARD, Icons.Outlined.Home, "Inicio"),
                                Triple(SendaScreen.HABITS, Icons.Outlined.CheckCircle, "Hábitos"),
                                Triple(SendaScreen.JOURNAL, Icons.Outlined.Book, "Diario"),
                                Triple(SendaScreen.COURSES, Icons.Outlined.School, "Cursos"),
                                Triple(SendaScreen.TRENDS, Icons.Outlined.TrendingUp, "Patrones"),
                                Triple(SendaScreen.PROFILE, Icons.Outlined.Person, "Perfil")
                            )

                            items.forEach { (screen, icon, label) ->
                                NavigationBarItem(
                                    selected = currentScreen == screen,
                                    onClick = { viewModel.navigateTo(screen) },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                    modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        SendaScreen.DASHBOARD -> DashboardScreen(viewModel)
                        SendaScreen.MOOD_CHECKIN -> MoodCheckinScreen(viewModel)
                        SendaScreen.NOVA_CHAT -> NovaChatScreen(viewModel)
                        SendaScreen.JOURNAL -> JournalScreen(viewModel)
                        SendaScreen.TRENDS -> TrendsScreen(viewModel)
                        SendaScreen.COURSES -> CoursesScreen(viewModel)
                        SendaScreen.HABITS -> HabitsScreen(viewModel)
                        SendaScreen.EMERGENCY -> EmergencyScreen(viewModel)
                        SendaScreen.PROFILE -> ProfileScreen(viewModel)
                        SendaScreen.PREMIUM -> PremiumScreen(viewModel)
                        else -> DashboardScreen(viewModel)
                    }
                }
            }
        }
    }
}

// 1. WELCOME SCREEN
@Composable
fun WelcomeScreen(viewModel: SendaViewModel) {
    val scrollState = rememberScrollState()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Senda",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Tu camino al bienestar emocional",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Hero Image loaded dynamically from our generated asset!
        Image(
            painter = painterResource(id = R.drawable.img_senda_hero_1791036675683),
            contentDescription = "Senda Hero Illustration",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(24.dp))

        // CLINICAL ADVISORY (CRITICAL)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = "Aviso importante", tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aviso Importante", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Este asistente ofrece orientación de bienestar y reflexión personal. No diagnostica, no sustituye a un profesional de salud mental y no es un servicio de emergencia.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (profile?.onboardingCompleted == true) {
                    viewModel.navigateTo(SendaScreen.DASHBOARD)
                } else {
                    viewModel.navigateTo(SendaScreen.ONBOARDING)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("welcome_start_button"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Comenzar mi camino", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// 2. ONBOARDING SCREEN
@Composable
fun OnboardingScreen(viewModel: SendaViewModel) {
    val scrollState = rememberScrollState()
    var preferredName by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("México") }
    var consentGiven by remember { mutableStateOf(false) }

    val countriesList = listOf("México", "Colombia", "Argentina", "Chile", "Perú", "Uruguay", "España", "Otro")
    var countryDropdownExpanded by remember { mutableStateOf(false) }

    // Focus preference checkboxes
    val goalsList = listOf(
        "Manejar el estrés cotidiano",
        "Establecer límites saludables",
        "Organización y disciplina",
        "Mejorar hábitos diarios",
        "Autoconocimiento",
        "Conciliar mejor el sueño"
    )
    val selectedGoals = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(24.dp)
    ) {
        Text(
            text = "Personaliza tu Senda",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Queremos conocer tus objetivos para ofrecerte un espacio personalizado de reflexión.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Name input
        Text("¿Cómo te gustaría que te llamemos?", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        TextField(
            value = preferredName,
            onValueChange = { preferredName = it },
            placeholder = { Text("Tu nombre o pseudónimo") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_name_input"),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Country selection (CRITICAL FOR EMERGENCY HELPLINES)
        Text("¿En qué país te encuentras?", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp))
        Text(
            "Esto nos permite mostrarte recursos y líneas de ayuda de emergencia locales si los necesitas.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { countryDropdownExpanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("onboarding_country_select"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedCountry, color = MaterialTheme.colorScheme.onBackground)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Seleccionar país")
                }
            }
            DropdownMenu(
                expanded = countryDropdownExpanded,
                onDismissRequest = { countryDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                countriesList.forEach { country ->
                    DropdownMenuItem(
                        text = { Text(country) },
                        onClick = {
                            selectedCountry = country
                            countryDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Preferences Checklist
        Text("¿Cuáles son tus áreas de interés principales?", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
        goalsList.forEach { goal ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (selectedGoals.contains(goal)) {
                            selectedGoals.remove(goal)
                        } else {
                            selectedGoals.add(goal)
                        }
                    }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedGoals.contains(goal),
                    onCheckedChange = {
                        if (selectedGoals.contains(goal)) {
                            selectedGoals.remove(goal)
                        } else {
                            selectedGoals.add(goal)
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(goal, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // CONSENT & PRIVACY (CRITICAL)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Consentimiento de Privacidad",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "En Senda, tu privacidad es primero. Los datos de tu estado de ánimo, diario y metas se guardan localmente en tu dispositivo. Al utilizar el chat con NOVA, tus mensajes se envían de forma confidencial al modelo de IA y nunca se venderán ni se usarán para publicidad conductual.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Switch(
                        checked = consentGiven,
                        onCheckedChange = { consentGiven = it },
                        modifier = Modifier.testTag("onboarding_consent_switch")
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Doy mi consentimiento para procesar mis datos emocionales para mi bienestar.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (preferredName.isBlank()) {
                    preferredName = "Viajero"
                }
                viewModel.completeOnboarding(preferredName, selectedCountry, consentGiven)
            },
            enabled = consentGiven,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_complete_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Guardar y Comenzar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

// 3. DASHBOARD SCREEN
@Composable
fun DashboardScreen(viewModel: SendaViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val moods by viewModel.moodEntries.collectAsStateWithLifecycle()
    val todayDate = viewModel.getTodayDateString()
    val todayMood = moods.find { it.dateStr == todayDate }
    val todayCompletions by viewModel.habitCompletionsForSelectedDate.collectAsStateWithLifecycle()
    val habitsList by viewModel.habits.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Welcome Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hola, ${profile?.name ?: "Viajero"}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Que tengas un día consciente",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            // Mood Button
            IconButton(
                onClick = { viewModel.navigateTo(SendaScreen.MOOD_CHECKIN) },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .testTag("dashboard_checkin_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Check-In",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // MOOD STATUS CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tu estado de ánimo hoy",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (todayMood != null) {
                    val moodNames = listOf("", "Muy mal", "Mal", "Neutral", "Bien", "Muy bien")
                    val moodEmotions = todayMood.emotions.split(",").filter { it.isNotBlank() }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (todayMood.score) {
                                1 -> "😢"
                                2 -> "😟"
                                3 -> "😐"
                                4 -> "🙂"
                                5 -> "😊"
                                else -> "😐"
                            },
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = moodNames.getOrElse(todayMood.score) { "Desconocido" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (moodEmotions.isNotEmpty()) {
                                Text(
                                    text = "Sientes: " + moodEmotions.joinToString(", "),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        "Aún no has registrado cómo te sientes hoy.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Button(
                        onClick = { viewModel.navigateTo(SendaScreen.MOOD_CHECKIN) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hacer Check-in diario")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // NOVA CHAT BANNER
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.navigateTo(SendaScreen.NOVA_CHAT) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Conversa con NOVA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Tu guía de reflexión e inteligencia emocional breve, atenta y respetuosa.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                Icon(Icons.Filled.ChevronRight, contentDescription = "Ir al chat", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // QUICK HABITS TRACKER
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tus hábitos de hoy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { viewModel.navigateTo(SendaScreen.HABITS) }) {
                        Text("Ver todos", fontSize = 12.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                if (habitsList.isEmpty()) {
                    Text("Cargando hábitos iniciales...", fontSize = 12.sp)
                } else {
                    habitsList.take(3).forEach { habit ->
                        val isCompleted = todayCompletions.any { it.habitId == habit.id }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleHabit(habit.id) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isCompleted,
                                onCheckedChange = { viewModel.toggleHabit(habit.id) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            val icon = when (habit.iconName) {
                                "spa" -> Icons.Filled.Spa
                                "directions_walk" -> Icons.Filled.DirectionsWalk
                                "local_drink" -> Icons.Filled.LocalDrink
                                "edit" -> Icons.Filled.Edit
                                "call" -> Icons.Filled.Call
                                else -> Icons.Filled.Star
                            }
                            
                            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(habit.name, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MICRO COURSES PREVIEW
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Microcursos recomendados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { viewModel.navigateTo(SendaScreen.COURSES) }) {
                        Text("Explorar", fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                        .clickable { viewModel.navigateTo(SendaScreen.COURSES) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Manejo del Estrés Cotidiano", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("3 lecciones rápidas de 5 minutos con ejercicios prácticos.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// 4. MOOD CHECK-IN SCREEN
@Composable
fun MoodCheckinScreen(viewModel: SendaViewModel) {
    BackHandler { viewModel.goBack() }

    var selectedScore by remember { mutableStateOf(3) }
    val selectedEmotions = remember { mutableStateListOf<String>() }
    var energyLevel by remember { mutableStateOf(3f) }
    var sleepHours by remember { mutableStateOf(7f) }
    var physicalActivity by remember { mutableStateOf("Media") }
    var notes by remember { mutableStateOf("") }

    val emotionsList = listOf("ansiedad", "calma", "tristeza", "alegría", "enojo", "cansancio", "frustración", "esperanza", "soledad", "gratitud")

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { viewModel.goBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
            }
            Text("Registrar Estado de Ánimo", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mood scale
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("¿Cómo calificarías tu ánimo general de hoy?", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val faces = listOf("😢", "😟", "😐", "🙂", "😊")
                    val names = listOf("Muy mal", "Mal", "Neutral", "Bien", "Muy bien")
                    
                    faces.forEachIndexed { index, face ->
                        val scoreValue = index + 1
                        val isSelected = selectedScore == scoreValue
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedScore = scoreValue }
                                .padding(8.dp)
                        ) {
                            Text(
                                face, 
                                fontSize = if (isSelected) 42.sp else 30.sp,
                                modifier = Modifier.testTag("mood_face_$scoreValue")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                names[index], 
                                fontSize = 10.sp, 
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emotion chips selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Selecciona las emociones que sientes hoy:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emotionsList.forEach { emotion ->
                        val isSelected = selectedEmotions.contains(emotion)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedEmotions.remove(emotion)
                                else selectedEmotions.add(emotion)
                            },
                            label = { Text(emotion.replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.testTag("emotion_chip_$emotion")
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Details (Sleep, Energy, Activity)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Detalles adicionales", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                // Energy slider
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Nivel de energía física:", fontSize = 14.sp)
                    Text(energyLevel.toInt().toString() + " / 5", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = energyLevel,
                    onValueChange = { energyLevel = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.testTag("energy_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sleep hours
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Horas de sueño anoche:", fontSize = 14.sp)
                    Text(String.format(Locale.getDefault(), "%.1f hrs", sleepHours), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = sleepHours,
                    onValueChange = { sleepHours = it },
                    valueRange = 2f..12f,
                    steps = 20,
                    modifier = Modifier.testTag("sleep_slider")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Physical activity
                Text("Actividad física realizada hoy:", fontSize = 14.sp)
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    listOf("Ninguna", "Baja", "Media", "Alta").forEach { level ->
                        val isSelected = physicalActivity == level
                        OutlinedButton(
                            onClick = { physicalActivity = level },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                            ),
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        ) {
                            Text(level, fontSize = 12.sp, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Personal notes
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Notas personales / Reflexiones del día:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Escribe de forma libre qué ocurrió hoy, qué te motivó, etc.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("mood_notes_input"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.checkInMood(
                    score = selectedScore,
                    emotions = selectedEmotions,
                    energyLevel = energyLevel.toInt(),
                    sleepHours = sleepHours,
                    physicalActivity = physicalActivity,
                    notes = notes
                )
                viewModel.navigateTo(SendaScreen.DASHBOARD)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("mood_save_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Guardar Registro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// 5. NOVA CHAT ASSISTANT SCREEN
@Composable
fun NovaChatScreen(viewModel: SendaViewModel) {
    BackHandler { viewModel.goBack() }

    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isTyping by viewModel.isNovaTyping.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    
    var textInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    // Scroll to bottom on new messages
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    // Load initial greeting if empty
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            viewModel.resetNovaConversation()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Chat Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            IconButton(onClick = { viewModel.goBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Conversa con NOVA", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Asistente de bienestar y autoconocimiento", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
            }
            
            // Restart button
            TextButton(
                onClick = { viewModel.resetNovaConversation() },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Reiniciar", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reiniciar", fontSize = 11.sp)
            }
        }

        // CLINICAL DISCLAIMER BANNER (MANDATORY)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "“Este asistente ofrece orientación de bienestar y reflexión personal. No diagnostica, no sustituye a un profesional de salud mental y no es un servicio de emergencia.”",
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp)
            )
        }

        // Messages area
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                val alignment = if (isUser) Alignment.End else Alignment.Start
                val containerColor = if (isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                }
                val textColor = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = alignment
                ) {
                    Text(
                        text = if (isUser) (profile?.name ?: "Tú") else "NOVA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 16.dp
                                )
                            )
                            .background(containerColor)
                            .padding(12.dp)
                            .widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = msg.messageText,
                            color = textColor,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            if (isTyping) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("NOVA está reflexionando...", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        // Text input area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Escribe un mensaje empático a NOVA...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("nova_message_input"),
                maxLines = 3,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendChatMessage(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .testTag("nova_send_button")
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Enviar", tint = Color.White)
            }
        }
    }
}

// 6. GUIDED DIARY SCREEN
@Composable
fun JournalScreen(viewModel: SendaViewModel) {
    BackHandler { viewModel.goBack() }

    val entries by viewModel.journalEntries.collectAsStateWithLifecycle()
    val isSummaryLoading by viewModel.journalAILoading.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("history") } // "write" or "history"
    var selectedTemplate by remember { mutableStateOf("Escritura Libre") }
    var freeText by remember { mutableStateOf("") }
    var runAISummary by remember { mutableStateOf(true) }

    // Guided template variables
    var q1 by remember { mutableStateOf("") }
    var q2 by remember { mutableStateOf("") }
    var q3 by remember { mutableStateOf("") }
    var q4 by remember { mutableStateOf("") }
    var q5 by remember { mutableStateOf("") }

    val templates = listOf("Escritura Libre", "¿Qué siento ahora?", "Tres cosas de Gratitud")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Diario Reflexivo", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Organiza tus pensamientos, emociones y acciones.", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs switcher
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { activeTab = "history" },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (activeTab == "history") MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
                ),
                border = BorderStroke(1.dp, if (activeTab == "history") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
            ) {
                Text("Mis Memorias", color = if (activeTab == "history") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            }
            Button(
                onClick = { activeTab = "write" },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeTab == "write") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Escribir entrada")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeTab == "write") {
            // Write Mode
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Text("Selecciona una plantilla guiada:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        templates.forEach { temp ->
                            val isSel = selectedTemplate == temp
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedTemplate = temp },
                                label = { Text(temp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedTemplate == "Escritura Libre") {
                        Text("Escribe libremente todo lo que ronde tu mente:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = freeText,
                            onValueChange = { freeText = it },
                            placeholder = { Text("Hoy ha sido un día lleno de...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .testTag("journal_free_text"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    } else if (selectedTemplate == "¿Qué siento ahora?") {
                        // Guided Prompts
                        Column {
                            Text("1. ¿Qué emoción está predominando en mi cuerpo justo ahora?", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            TextField(
                                value = q1,
                                onValueChange = { q1 = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                singleLine = true
                            )

                            Text("2. ¿Qué ocurrió o detonó que me sienta de esta manera?", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            TextField(
                                value = q2,
                                onValueChange = { q2 = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                singleLine = true
                            )

                            Text("3. ¿Qué pensamiento recurrente está ocupando más espacio?", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            TextField(
                                value = q3,
                                onValueChange = { q3 = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                singleLine = true
                            )

                            Text("4. De todo esto, ¿qué parte o decisión depende de mí?", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            TextField(
                                value = q4,
                                onValueChange = { q4 = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                singleLine = true
                            )

                            Text("5. ¿Cuál es una acción pequeña y viable para hoy?", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            TextField(
                                value = q5,
                                onValueChange = { q5 = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                singleLine = true
                            )
                        }
                    } else {
                        // Gratitud
                        Column {
                            Text("Anota tres cosas específicas por las que sientas gratitud hoy:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = q1,
                                onValueChange = { q1 = it },
                                label = { Text("1. Agradezco...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                            TextField(
                                value = q2,
                                onValueChange = { q2 = it },
                                label = { Text("2. Agradezco...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                            TextField(
                                value = q3,
                                onValueChange = { q3 = it },
                                label = { Text("3. Agradezco...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // AI SUMMARY TOGGLE
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { runAISummary = !runAISummary }
                    ) {
                        Checkbox(checked = runAISummary, onCheckedChange = { runAISummary = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Resumen reflexivo privado de IA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("NOVA creará un resumen privado separando: Hechos, Emociones, Pensamientos y próximo paso.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (isSummaryLoading) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Generando resumen con IA...", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = {
                                val fullContent = when (selectedTemplate) {
                                    "Escritura Libre" -> freeText
                                    "¿Qué siento ahora?" -> {
                                        """
                                            1. ¿Qué siento?: $q1
                                            2. ¿Qué lo causó?: $q2
                                            3. Pensamiento: $q3
                                            4. Depende de mí: $q4
                                            5. Acción pequeña: $q5
                                        """.trimIndent()
                                    }
                                    else -> {
                                        """
                                            Tres cosas de Gratitud:
                                            • $q1
                                            • $q2
                                            • $q3
                                        """.trimIndent()
                                    }
                                }

                                if (fullContent.isNotBlank()) {
                                    viewModel.addJournalEntry(selectedTemplate, fullContent, runAISummary)
                                    // Reset inputs
                                    freeText = ""
                                    q1 = ""
                                    q2 = ""
                                    q3 = ""
                                    q4 = ""
                                    q5 = ""
                                    activeTab = "history"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_journal_button")
                        ) {
                            Text("Guardar Memorias", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        } else {
            // History list
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No hay entradas en tu diario aún.", fontWeight = FontWeight.SemiBold)
                        Text("Haz clic en 'Escribir entrada' para registrar tu mente.", fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries) { entry ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(entry.dateStr, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                                        Text(entry.templateName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                    
                                    IconButton(onClick = { viewModel.deleteJournalEntry(entry.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(entry.content, fontSize = 13.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)

                                if (entry.aiSummary.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Resumen Reflexivo Senda AI", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(entry.aiSummary, fontSize = 12.sp, lineHeight = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 7. PATTERNS AND TRENDS SCREEN
@Composable
fun TrendsScreen(viewModel: SendaViewModel) {
    BackHandler { viewModel.goBack() }

    val moodEntries by viewModel.moodEntries.collectAsStateWithLifecycle()
    val patterns = viewModel.getNonClinicalPatterns()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Tendencias y Patrones", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Visualiza tus fluctuaciones de ánimo y descubre hábitos de bienestar.", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
        }

        // MOOD CHART SECTION
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Historial de Ánimo (Últimos check-ins)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (moodEntries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Registra al menos un check-in de ánimo para ver el gráfico.", fontSize = 12.sp)
                        }
                    } else {
                        // Drawing a beautiful custom mood bar chart using native Row columns and Canvas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            moodEntries.take(7).reversed().forEach { entry ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val barHeightMultiplier = entry.score / 5f
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight(barHeightMultiplier)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primary,
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                    )
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (entry.score) {
                                            1 -> "😢"
                                            2 -> "😟"
                                            3 -> "😐"
                                            4 -> "🙂"
                                            5 -> "😊"
                                            else -> "😐"
                                        },
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = entry.dateStr.substringAfterLast("-"),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // NON-CLINICAL PATTERNS (CRITICAL)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Patrones y Correlaciones Identificadas", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    patterns.forEach { pattern ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("•", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                            Text(pattern, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }

        // PRIVACY STATEMENT
        item {
            Text(
                text = "Estos patrones se generan estrictamente de forma algorítmica local en tu teléfono y no son diagnósticos médicos ni clínicos. Están diseñados para fomentar tu autoreflexión y hábitos conscientes.",
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// 8. LIBRARY OF MICRO COURSES SCREEN
@Composable
fun CoursesScreen(viewModel: SendaViewModel) {
    BackHandler { viewModel.goBack() }

    val coursesList by viewModel.courses.collectAsStateWithLifecycle()
    val completedProgress by viewModel.completedLessons.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    var activeCourse by remember { mutableStateOf<Course?>(null) }
    var currentLessonList = remember { mutableStateListOf<Lesson>() }
    var selectedLesson by remember { mutableStateOf<Lesson?>(null) }

    // Observe active course lessons
    LaunchedEffect(activeCourse) {
        activeCourse?.let { course ->
            viewModel.getLessonsForCourse(course.id).collect { lessons ->
                currentLessonList.clear()
                currentLessonList.addAll(lessons)
            }
        }
    }

    if (selectedLesson != null) {
        // LESSON READER COMPOSABLE
        val lesson = selectedLesson!!
        BackHandler { selectedLesson = null }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selectedLesson = null }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver a lecciones")
                }
                Text("Lección: ${lesson.title}", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Objective Card
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Stars, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Objetivo de la sesión", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text(lesson.objective, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text Explanation
            Text("1. Explicación Sencilla", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Text(lesson.explanation, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(vertical = 4.dp))

            Spacer(modifier = Modifier.height(12.dp))

            // Daily Example
            Text("2. Ejemplo Cotidiano", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Text(lesson.example, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(vertical = 4.dp))

            Spacer(modifier = Modifier.height(12.dp))

            // 2 Minute Exercise
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("🛠️ Ejercicio Práctico de 2 minutos", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(lesson.exercise, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reflection Question
            Text("3. Pregunta de Reflexión", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Text(lesson.reflection, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))

            Spacer(modifier = Modifier.height(12.dp))

            // Small Action
            Text("4. Acción Pequeña para Hoy", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Text(lesson.action, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))

            Spacer(modifier = Modifier.height(24.dp))

            val isDone = completedProgress.any { it.lessonId == lesson.id }
            Button(
                onClick = {
                    viewModel.toggleLessonCompletion(lesson.id, !isDone)
                    selectedLesson = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("complete_lesson_button")
            ) {
                Text(if (isDone) "Completada ✓ (Haz clic para desmarcar)" else "Marcar como leída y completada", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    } else if (activeCourse != null) {
        // COURSE LESSONS DRAWER
        val course = activeCourse!!
        BackHandler { activeCourse = null }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { activeCourse = null }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                }
                Text(course.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
            }

            Text(course.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(16.dp))

            Text("Sesiones del curso", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(currentLessonList) { lesson ->
                    val isCompleted = completedProgress.any { it.lessonId == lesson.id }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedLesson = lesson },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.PlayCircle,
                                contentDescription = null,
                                tint = if (isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(lesson.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("${lesson.durationMinutes} min de lectura", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Icon(Icons.Filled.ChevronRight, contentDescription = "Iniciar", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    } else {
        // LIBRARIES LIST
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Biblioteca de Cursos", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Adquiere habilidades y herramientas prácticas para tu bienestar.", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
            }

            items(coursesList) { course ->
                val isPremiumUser = profile?.isPremium == true
                val isLocked = course.isPremium && !isPremiumUser

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isLocked) {
                                viewModel.navigateTo(SendaScreen.PREMIUM)
                            } else {
                                activeCourse = course
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLocked) MaterialTheme.colorScheme.surface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(course.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            if (isLocked) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Lock, contentDescription = "Premium", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Premium", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(course.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${course.durationMinutes} minutos de estudio", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

// 9. HABITS AND RECOLLECTORS SCREEN
@Composable
fun HabitsScreen(viewModel: SendaViewModel) {
    BackHandler { viewModel.goBack() }

    val habitsList by viewModel.habits.collectAsStateWithLifecycle()
    val todayCompletions by viewModel.habitCompletionsForSelectedDate.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var habitNameInput by remember { mutableStateOf("") }
    var habitIconInput by remember { mutableStateOf("spa") }

    val availableIcons = listOf(
        Pair("spa", "Bienestar"),
        Pair("directions_walk", "Caminar"),
        Pair("local_drink", "Hidratación"),
        Pair("edit", "Escribir"),
        Pair("call", "Llamar"),
        Pair("phonelink_off", "Desconexión")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Tus Microhábitos", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Hábitos pequeños de bajísimo esfuerzo.", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
            }

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                    .testTag("add_habit_button")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar hábito", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (habitsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(habitsList) { habit ->
                    val isDone = todayCompletions.any { it.habitId == habit.id }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleHabit(habit.id) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isDone,
                                onCheckedChange = { viewModel.toggleHabit(habit.id) },
                                modifier = Modifier.testTag("habit_check_${habit.id}")
                            )
                            Spacer(modifier = Modifier.width(12.dp))

                            val icon = when (habit.iconName) {
                                "spa" -> Icons.Filled.Spa
                                "directions_walk" -> Icons.Filled.DirectionsWalk
                                "local_drink" -> Icons.Filled.LocalDrink
                                "edit" -> Icons.Filled.Edit
                                "call" -> Icons.Filled.Call
                                "phonelink_off" -> Icons.Filled.PhonelinkOff
                                else -> Icons.Filled.CheckCircle
                            }

                            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = habit.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )

                            if (habit.isCustom) {
                                IconButton(onClick = { viewModel.deleteCustomHabit(habit.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ADD CUSTOM DIALOG MOCK
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Nuevo Hábito") },
                text = {
                    Column {
                        Text("Ingresa una acción pequeña que tome menos de 2 minutos y requiera bajo esfuerzo:", fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        TextField(
                            value = habitNameInput,
                            onValueChange = { habitNameInput = it },
                            placeholder = { Text("Ej: Respirar profundo 3 veces") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_habit_name")
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Elige un ícono de bienestar:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableIcons.forEach { (key, name) ->
                                val isSel = habitIconInput == key
                                OutlinedButton(
                                    onClick = { habitIconInput = key },
                                    border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                ) {
                                    Text(name, fontSize = 11.sp, color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (habitNameInput.isNotBlank()) {
                                viewModel.createCustomHabit(habitNameInput, habitIconInput)
                                habitNameInput = ""
                                showAddDialog = false
                            }
                        },
                        modifier = Modifier.testTag("dialog_confirm_add_habit")
                    ) {
                        Text("Agregar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

// 10. CRISIS & SAFETY CENTER SCREEN
@Composable
fun EmergencyScreen(viewModel: SendaViewModel) {
    BackHandler { viewModel.goBack() }

    val context = LocalContext.current
    val resourceList by viewModel.emergencyResources.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.goBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
            }
            Text("Centro de Apoyo Urgente", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // WARNING BANNER (CRITICAL)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = "Atención", tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Líneas de Crisis y Apoyo Humano", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Senda NO es un servicio médico ni de terapia de crisis. Si estás sufriendo ideación suicida, autolesiones o sufres violencia familiar inmediata, te instamos a contactar de inmediato a los números a continuación o comunicarte con alguien de absoluta confianza.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Recursos locales para ${profile?.country ?: "México"}:",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (resourceList.isEmpty()) {
            Text("No se cargaron recursos específicos. Te aconsejamos llamar al número unificado de emergencias nacional (911 / 112) de tu localidad.", fontSize = 13.sp)
        } else {
            resourceList.forEach { res ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(res.serviceName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(res.descriptionStr, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        // TELEMETRY/REAL CALL TRIGGER - Compliance dial
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${res.phoneNumber}"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No se pudo iniciar la llamada: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Llamar ahora: ${res.phoneNumber}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// 11. PROFILE SCREEN AND PRIVACY CENTRE
@Composable
fun ProfileScreen(viewModel: SendaViewModel) {
    BackHandler { viewModel.goBack() }

    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val moods by viewModel.moodEntries.collectAsStateWithLifecycle()
    val journals by viewModel.journalEntries.collectAsStateWithLifecycle()
    val completions by viewModel.allHabitCompletions.collectAsStateWithLifecycle()

    var editMode by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(profile?.name ?: "") }
    var countryInput by remember { mutableStateOf(profile?.country ?: "México") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Configuración y Privacidad", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Control total de tus datos y preferencias.", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)

        Spacer(modifier = Modifier.height(20.dp))

        // Profile details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tus datos personales", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = {
                        if (editMode) {
                            // save changes
                            val current = profile ?: return@IconButton
                            viewModel.completeOnboarding(nameInput, countryInput, current.consentGiven)
                            editMode = false
                        } else {
                            nameInput = profile?.name ?: ""
                            countryInput = profile?.country ?: "México"
                            editMode = true
                        }
                    }) {
                        Icon(if (editMode) Icons.Filled.Save else Icons.Filled.Edit, contentDescription = "Editar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (editMode) {
                    TextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nombre preferido") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("Nombre: ${profile?.name ?: "No configurado"}", fontSize = 15.sp)
                    Text("País registrado: ${profile?.country ?: "No configurado"}", fontSize = 15.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Senda Premium Plan
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Senda Premium", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                Text(
                    text = if (profile?.isPremium == true) "¡Eres un miembro Premium activo! Gracias por apoyar nuestro trabajo de bienestar responsable."
                    else "Desbloquea todos los microcursos de disciplina y comunicación asertiva, además de resúmenes AI de tu diario ilimitados.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.navigateTo(SendaScreen.PREMIUM) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text(if (profile?.isPremium == true) "Gestionar Plan" else "Obtener Premium")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy from the design block
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Privacidad desde el Diseño", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Senda no vende tus datos emocionales.\n• Senda encripta toda tu base de datos Room de forma segura en local.\n• Los prompts del chat con NOVA se procesan respetando la confidencialidad absoluta.\n• No enviamos contenido sensible a motores de analíticas externas.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Export and delete data (CRITICAL PRIVACY CONTROL)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tus Derechos y Datos", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))

                // EXPORT BUTTON - Generates pure JSON content
                Button(
                    onClick = {
                        val exportedJson = """
                            {
                              "profile": {
                                "name": "${profile?.name}",
                                "country": "${profile?.country}",
                                "consent": ${profile?.consentGiven}
                              },
                              "checkins_mood_total": ${moods.size},
                              "journal_entries_total": ${journals.size},
                              "habits_completed_total": ${completions.size}
                            }
                        """.trimIndent()
                        
                        // We share/show a Toast for proof, and we can also use an Intent to share!
                        try {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Senda Data Export")
                                putExtra(Intent.EXTRA_TEXT, exportedJson)
                            }
                            context.startActivity(Intent.createChooser(intent, "Exportar datos Senda"))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Exportado localmente:\n$exportedJson", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Exportar toda mi información (JSON)", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // DELETE ACCOUNT BUTTON - Full compliance wipes Room
                Button(
                    onClick = {
                        viewModel.wipeAllUserData()
                        Toast.makeText(context, "Todos los datos de Senda fueron eliminados permanentemente.", Toast.LENGTH_SHORT).show()
                        viewModel.navigateTo(SendaScreen.WELCOME)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar mi cuenta y borrar todo", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// 12. PREMIUM PLAN billing SCREEN
@Composable
fun PremiumScreen(viewModel: SendaViewModel) {
    BackHandler { viewModel.goBack() }

    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Stars, contentDescription = "Senda Premium", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Senda Premium",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Tu inversión en disciplina, equilibrio y tranquilidad mental.",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Beneficios de Senda Premium:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))

                val benefits = listOf(
                    "Cursos completos de Disciplina, Comunicación y Sueño.",
                    "Análisis de diario con IA ilimitado (NOVA Hechos, Emociones y Tareas).",
                    "Acompañamiento por NOVA sin límites diarios.",
                    "Prioridad de soporte local por correo."
                )

                benefits.forEach { benefit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Beneficio", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(benefit, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Transparency (Billing & Cancel Terms)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Senda Premium",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "$4.99 USD / mes",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "Facturación recurrente mensual, renovación automática con total transparencia. Cancela en un solo clic desde tu perfil en cualquier momento, sin cargos ocultos ni sorpresas.",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val isActive = profile?.isPremium == true
        Button(
            onClick = {
                viewModel.togglePremiumStatus()
                viewModel.goBack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("premium_upgrade_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isActive) "Cancelar mi Plan Premium" else "Activar Senda Premium ($4.99/mes)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { viewModel.goBack() }) {
            Text("Volver por ahora", color = MaterialTheme.colorScheme.secondary)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
