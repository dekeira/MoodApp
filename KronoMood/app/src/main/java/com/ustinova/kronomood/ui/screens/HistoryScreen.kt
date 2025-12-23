package com.ustinova.kronomood.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ustinova.kronomood.data.AppDatabase
import com.ustinova.kronomood.data.Mood
import com.ustinova.kronomood.utils.moodColor
import com.ustinova.kronomood.utils.moodEmoji
import com.ustinova.kronomood.utils.toDateString
import com.ustinova.kronomood.utils.toTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavHostController) {
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
        TopAppBar(title = { Text("История") })

        if (moods.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Ещё нет записей 😌", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(moods) { mood ->
                    MoodCard(mood = mood)
                }
            }
        }
    }
}

@Composable
fun MoodCard(mood: Mood) {
    var showFirstDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var moodToDelete by remember { mutableStateOf<Long?>(null) }

    val scope = rememberCoroutineScope()
    val db = AppDatabase.getInstance()

    // Сбрасываем состояние диалогов при смене mood.id (важно в LazyColumn)
    LaunchedEffect(mood.id) {
        showFirstDialog = false
        showDeleteConfirmation = false
        moodToDelete = null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = mood.moodLevel.moodColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${mood.timestamp.toDateString()} в ${mood.timestamp.toTimeString()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = mood.moodLevel.moodEmoji + " " + (mood.noteText ?: "Без заметки"),
                    style = MaterialTheme.typography.bodyLarge
                )
                if (mood.sleepHours != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Сон: ${"%.1f".format(mood.sleepHours)} ч",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = {
                    showFirstDialog = true
                }
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    }

    // Первый диалог: подтверждение открытия удаления
    if (showFirstDialog) {
        AlertDialog(
            onDismissRequest = { showFirstDialog = false },
            confirmButton = {
                Button(onClick = {
                    showFirstDialog = false
                    showDeleteConfirmation = true
                    moodToDelete = mood.id
                }) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                Button(onClick = { showFirstDialog = false }) {
                    Text("Отмена")
                }
            },
            title = { Text("Подтвердите") },
            text = { Text("Вы уверены?") }
        )
    }

    // Второй диалог: окончательное подтверждение
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                moodToDelete = null
            },
            confirmButton = {
                Button(onClick = {
                    val idToDelete = moodToDelete
                    if (idToDelete != null) {
                        scope.launch {
                            db.moodDao().deleteMood(idToDelete)
                        }
                    }
                    showDeleteConfirmation = false
                    moodToDelete = null
                }) {
                    Text("Да")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDeleteConfirmation = false
                    moodToDelete = null
                }) {
                    Text("Нет")
                }
            },
            title = { Text("Удалить запись?") },
            text = { Text("Это действие нельзя отменить.") }
        )
    }
}