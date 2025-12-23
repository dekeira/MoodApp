package com.ustinova.kronomood.utils

import androidx.compose.ui.graphics.Color
import com.ustinova.kronomood.data.Mood
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

val Int.moodColor: Color
    get() = when (this) {
        1 -> Color(0xFFE53935) // 😡 Angry
        2 -> Color(0xFFF44336) // 😞 Sad
        3 -> Color(0xFF9E9E9E) // 😐 Neutral
        4 -> Color(0xFF4CAF50) // 🙂 Happy
        5 -> Color(0xFF2E7D32) // 😌 Calm
        else -> Color.Gray
    }

val Int.moodEmoji: String
    get() = when (this) {
        1 -> "😡"
        2 -> "😞"
        3 -> "😐"
        4 -> "🙂"
        5 -> "😌"
        else -> "❓"
    }

fun List<Mood>.groupByDayOfWeek(): Map<DayOfWeek, List<Mood>> {
    return groupBy { it.timestamp.dayOfWeek }
}

fun List<Mood>.groupByHourOfDay(): Map<Int, List<Mood>> {
    return groupBy { it.timestamp.hour }
}

fun List<Mood>.avgMood(): Double {
    return if (isEmpty()) 0.0 else map { it.moodLevel }.average()
}

fun DayOfWeek.displayName(): String {
    return getDisplayName(TextStyle.SHORT, Locale.getDefault()).replace(".", "")
}

fun LocalDateTime.toDateString(): String {
    return this.toLocalDate().toString()
}

fun LocalDateTime.toTimeString(): String {
    return this.toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
}