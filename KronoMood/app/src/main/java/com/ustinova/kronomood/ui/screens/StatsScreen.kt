package com.ustinova.kronomood.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ustinova.kronomood.data.Mood
import kotlinx.coroutines.flow.collectLatest
import com.ustinova.kronomood.data.AppDatabase
import com.ustinova.kronomood.ui.theme.Green
import com.ustinova.kronomood.ui.theme.Red
import com.ustinova.kronomood.utils.avgMood
import com.ustinova.kronomood.utils.displayName
import com.ustinova.kronomood.utils.groupByDayOfWeek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(navController: NavHostController) {
    var moods by remember { mutableStateOf<List<Mood>>(emptyList()) }

    LaunchedEffect(Unit) {
        AppDatabase.getInstance().moodDao().getAllMoods()
            .flowOn(Dispatchers.IO)
            .collectLatest { list ->
                moods = list
            }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(title = { Text("Анализ") })

        if (moods.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет данных для анализа 📊", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    StatCard(title = "Среднее настроение") {
                        val avg = moods.avgMood()
                        val color = when {
                            avg < 2.5 -> Color(0xFFF44336)
                            avg < 3.5 -> Color(0xFFFF9800)
                            else -> Color(0xFF4CAF50)
                        }
                        Text(
                            text = "%.2f".format(avg),
                            style = MaterialTheme.typography.displayMedium.copy(
                                color = color,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    StatCard(title = "Настроение по дням недели") {
                        val byDay = moods.groupByDayOfWeek()
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                DayOfWeek.MONDAY to "Пн",
                                DayOfWeek.TUESDAY to "Вт",
                                DayOfWeek.WEDNESDAY to "Ср",
                                DayOfWeek.THURSDAY to "Чт",
                                DayOfWeek.FRIDAY to "Пт",
                                DayOfWeek.SATURDAY to "Сб",
                                DayOfWeek.SUNDAY to "Вс"
                            ).forEach { (day, shortName) ->
                                val dayMoods = byDay[day].orEmpty()
                                if (dayMoods.isNotEmpty()) {
                                    val avg = dayMoods.avgMood()
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = shortName,
                                            modifier = Modifier.weight(1f),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        MoodBar(
                                            avg = avg,
                                            width = 120.dp,
                                            height = 8.dp
                                        )
                                        Text(
                                            text = "%.1f".format(avg),
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    StatCard(title = "Связь со сном") {
                        val withSleep = moods.filter { it.sleepHours != null }
                        if (withSleep.isEmpty()) {
                            Text("Нет данных о сне", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val shortSleep = withSleep.filter { it.sleepHours!! < 6 }
                            val longSleep = withSleep.filter { it.sleepHours!! >= 6 }

                            val shortAvg = shortSleep.avgMood()
                            val longAvg = longSleep.avgMood()

                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Сон < 6 ч (${shortSleep.size} записей)")
                                MoodBar(avg = shortAvg, width = 200.dp, height = 12.dp)

                                Text("Сон ≥ 6 ч (${longSleep.size} записей)")
                                MoodBar(avg = longAvg, width = 200.dp, height = 12.dp)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Разница: ${"%.2f".format(longAvg - shortAvg)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (longAvg > shortAvg) Green else Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun MoodBar(avg: Double, width: Dp, height: Dp) {
    val color = when {
        avg < 2.5 -> Color(0xFFF44336)
        avg < 3.5 -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .width((width.value * (avg / 5.0)).dp)
                .height(height)
                .background(color)
        )
    }
}