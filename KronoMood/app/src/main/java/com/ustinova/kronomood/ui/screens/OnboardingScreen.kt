package com.ustinova.kronomood.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ustinova.kronomood.data.AppDatabase
import com.ustinova.kronomood.data.UserProfile
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("other") }

    val ageInt = age.toIntOrNull() ?: 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Хроника настроений",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Имя") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { age = it.filter { it.isDigit() } },
            label = { Text("Возраст") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GenderChip(gender = "male", selected = gender == "male") { gender = "male" }
            GenderChip(gender = "female", selected = gender == "female") { gender = "female" }
            GenderChip(gender = "other", selected = gender == "other") { gender = "other" }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val scope = rememberCoroutineScope()
        Button(
            onClick = {
                scope.launch {
                    if (name.isNotBlank() && ageInt in 10..120) {
                        AppDatabase.getInstance().userDao().insertUser(
                            UserProfile(name = name, age = ageInt, gender = gender)
                        )
                        onDone()
                    }
                }
            },
            enabled = name.isNotBlank() && ageInt in 10..120,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Начать хронику")
        }
    }
}

@Composable
fun GenderChip(gender: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = when (gender) {
                    "male" -> "Мужской"
                    "female" -> "Женский"
                    else -> "Другое"
                },
                style = MaterialTheme.typography.labelMedium
            )
        }
    )
}