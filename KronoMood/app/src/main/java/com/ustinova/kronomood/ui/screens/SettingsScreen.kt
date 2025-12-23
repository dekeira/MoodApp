package com.ustinova.kronomood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ustinova.kronomood.data.AppDatabase
import com.ustinova.kronomood.data.UserProfile
import com.ustinova.kronomood.ui.theme.Gray
import com.ustinova.kronomood.ui.theme.Red
import com.ustinova.kronomood.ui.theme.Red2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val db = AppDatabase.getInstance()
    var user by remember { mutableStateOf<UserProfile?>(null) }
    val scope = rememberCoroutineScope()

    var showClearDialog by remember { mutableStateOf(false) }
    var isClearing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(title = { Text("Настройки") })

        Spacer(modifier = Modifier.height(24.dp))

        SettingsSection(title = "Профиль") {
            ProfileItem(label = "Имя:", value = user!!.name)
            Spacer(Modifier.height(10.dp))
            ProfileItem(label = "Возраст:", value = user!!.age.toString())
            Spacer(Modifier.height(10.dp))
            ProfileItem(label = "Пол:", value = when (user!!.gender) {
                "male" -> "Мужской"
                "female" -> "Женский"
                else -> "Другое"
            })
        }


        Spacer(modifier = Modifier.weight(1f))

        DangerButton("Очистить всю историю") {
            showClearDialog = true
        }

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Версия 1.0 • Локальное хранилище",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isClearing = true
                            try {
                                withContext(Dispatchers.IO) {
                                    db.moodDao().deleteAllMoods()
                                }
                            } finally {
                                isClearing = false
                                showClearDialog = false
                            }
                        }
                    },
                    enabled = !isClearing
                ) {
                    if (isClearing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    } else {
                        Text("Да, удалить")
                    }
                }
            },
            dismissButton = {
                Button(onClick = { showClearDialog = false }) {
                    Text("Отмена")
                }
            },
            title = { Text("Внимание!") },
            text = { Text("Вся история будет безвозвратно удалена.") }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
            color = Color.Black
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 16.sp,
            color = Color.DarkGray
        )
    }
}

@Composable
fun DangerButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Red2),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.padding(end = 4.dp), tint = Color.White)
        Text(text, color = Color.White)
    }
}