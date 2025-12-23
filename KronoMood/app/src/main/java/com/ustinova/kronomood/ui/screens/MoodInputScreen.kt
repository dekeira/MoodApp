package com.ustinova.kronomood.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ustinova.kronomood.data.AppDatabase
import com.ustinova.kronomood.data.Mood
import com.ustinova.kronomood.data.UserProfile
import com.ustinova.kronomood.utils.moodColor
import com.ustinova.kronomood.utils.moodEmoji
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MoodInputContent() {
    val scope = rememberCoroutineScope()
    var selectedMood by remember { mutableStateOf(3) }
    var sleepHours by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var user by remember { mutableStateOf<UserProfile?>(null) }

    LaunchedEffect(Unit) {
        val db = AppDatabase.getInstance()
        user = withContext(Dispatchers.IO) {
            db.userDao().getUser()
        }
    }

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val db = AppDatabase.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Привет, ${user!!.name}!",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Как ты себя чувствуешь?",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            for (level in 1..5) {
                MoodButton(
                    level = level,
                    isSelected = selectedMood == level,
                    onClick = { selectedMood = level }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = sleepHours,
            onValueChange = { sleepHours = it.filter { it.isDigit() || it == '.' } },
            label = { Text("Сон, часов (например, 6.5)") },
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Почему? (1 строка)") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                scope.launch {
                    withContext(Dispatchers.IO) {
                        db.moodDao().insertMood(
                            Mood(
                                moodLevel = selectedMood,
                                sleepHours = sleepHours.toFloatOrNull(),
                                noteText = noteText.takeUnless { it.isBlank() }
                            )
                        )
                    }
                    selectedMood = 3
                    sleepHours = ""
                    noteText = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить настроение")
        }
    }
}

@Composable
fun MoodButton(level: Int, isSelected: Boolean, onClick: () -> Unit) {
    val color = level.moodColor
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = level.moodEmoji,
            style = MaterialTheme.typography.displaySmall,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}