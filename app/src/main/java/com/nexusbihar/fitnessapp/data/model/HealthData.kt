package com.nexusbihar.fitnessapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_data")
data class HealthData(
    @PrimaryKey
    val date: String, // YYYY-MM-DD Format
    val userId: String = "", // Firebase Auth ID for user isolation
    val steps: Int = 0,
    val distance: Float = 0f, // in meters
    val caloriesBurned: Int = 0,
    val waterIntake: Int = 0, // in ml
    val sleepHours: Float = 0f,
    val heartRate: Int = 0, // avg. heart rate
    val healthScore: Int = 0, // calculated score -> 0-100
    val createdAt: String = "", // ISO Date Time String
    val updatedAt: String = "",
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val amount: Int = 0,
    val date: String, // YYYY-MM-DD Format
    val timeStamp: String,
)

@Entity(tableName = "step_logs")
data class StepLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val steps: Int = 0,
    val date: String, // YYYY-MM-DD Format
    val timeStamp: String,
)

@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val sleepStart: String,
    val sleepEnd: String,
    val duration: Float,
    val quality: Int = 0,
    val date: String, // YYYY-MM-DD Format
)

@Entity(tableName = "user_goals")
data class UserGoals(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 1,
    val userId: String = "",
    val dailySteps: Int = 10000,
    val dailyWater: Int = 2000,
    val dailySleep: Float = 8f,
    val weeklyExercise: Int = 150,
)