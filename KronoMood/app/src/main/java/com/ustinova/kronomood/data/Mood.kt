package com.ustinova.kronomood.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "mood_entries")
data class Mood(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "timestamp") val timestamp: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "mood_level") val moodLevel: Int, // 1=😡, 2=😞, 3=😐, 4=🙂, 5=😌
    @ColumnInfo(name = "sleep_hours") val sleepHours: Float? = null,
    @ColumnInfo(name = "note_text") val noteText: String? = null,
    @ColumnInfo(name = "note_voice_path") val noteVoicePath: String? = null
)