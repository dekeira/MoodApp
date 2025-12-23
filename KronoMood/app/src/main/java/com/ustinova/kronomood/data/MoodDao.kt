package com.ustinova.kronomood.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllMoods(): Flow<List<Mood>>

    @Query("SELECT * FROM mood_entries WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getMoodsBetween(start: LocalDateTime, end: LocalDateTime): Flow<List<Mood>>

    @Insert
    suspend fun insertMood(mood: Mood)

    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteMood(id: Long)

    @Query("DELETE FROM mood_entries")
    suspend fun deleteAllMoods()
}

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: UserProfile)

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUser(): UserProfile?

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun observeUser(): Flow<UserProfile?>  // ← реактивный метод

    @Update
    suspend fun updateUser(user: UserProfile)
}