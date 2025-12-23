package com.ustinova.kronomood

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ustinova.kronomood.data.AppDatabase
import com.ustinova.kronomood.ui.screens.*
import com.ustinova.kronomood.ui.theme.KronoMoodTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoodChronicleApp()
        }
    }
}

@Composable
fun MoodChronicleApp() {
    KronoMoodTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    var isOnboarded by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        AppDatabase.getInstance().userDao().observeUser()
            .flowOn(Dispatchers.IO)
            .collect { user ->
                isOnboarded = user != null
            }
    }

    when (isOnboarded) {
        null -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        false -> NavHost(navController = navController, startDestination = "onboarding") {
            composable("onboarding") {
                OnboardingScreen(
                    onDone = {
                    }
                )
            }
        }

        true -> {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        val items = listOf(
                            Triple("mood", "Настроение", Icons.Default.Mood),
                            Triple("history", "История", Icons.Default.History),
                            Triple("stats", "Анализ", Icons.Default.BarChart),
                            Triple("settings", "Профиль", Icons.Default.Person)
                        )
                        items.forEach { (route, label, icon) ->
                            NavigationBarItem(
                                selected = navController.currentBackStackEntry?.destination?.route == route,
                                onClick = {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            ) { padding ->
                NavHost(
                    navController = navController,
                    startDestination = "mood",
                    modifier = Modifier.padding(padding)
                ) {
                    composable("mood") { MoodInputContent() }
                    composable("history") { HistoryScreen(navController) }
                    composable("stats") { StatsScreen(navController) }
                    composable("settings") { SettingsScreen(navController) }
                }
            }
        }
    }
}