package com.nexusbihar.fitnessapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nexusbihar.fitnessapp.data.model.StepLog
import kotlinx.coroutines.flow.Flow

@Dao
interface StepLogDao {

    @Query("SELECT * FROM step_logs WHERE date = :date ORDER BY timeStamp DESC")
    fun getStepLogsByDate(date: String): Flow<List<StepLog>>

    @Query("SELECT SUM(steps) FROM step_logs WHERE date = :date")
    suspend fun getTotalStepsForDate(date: String): Int?

    @Query("SELECT * FROM step_logs ORDER BY timeStamp DESC LIMIT :limit")
    fun getRecentStepLogs(limit: Int = 50): Flow<List<StepLog>>

    @Insert
    suspend fun insertStepLog(stepLog: StepLog)

    @Update
    suspend fun updateStepLog(stepLog: StepLog)

    @Delete
    suspend fun deleteStepLog(stepLog: StepLog)

    @Query("DELETE FROM step_logs WHERE id = :id")
    suspend fun deleteStepLogById(id: Long)

    @Query("DELETE FROM step_logs")
    suspend fun deleteAllStepLogs()

    @Query("SELECT * FROM step_logs WHERE userId = :userId ORDER BY timeStamp DESC")
    suspend fun getAllStepLogsForUser(userId: String): List<StepLog>

    @Query("DELETE FROM step_logs WHERE userId = :userId")
    suspend fun deleteAllStepLogsForUser(userId: String)


}