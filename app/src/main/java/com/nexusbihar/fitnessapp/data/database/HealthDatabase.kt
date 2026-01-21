package com.nexusbihar.fitnessapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nexusbihar.fitnessapp.data.dao.HealthDataDao
import com.nexusbihar.fitnessapp.data.dao.SleepLogDao
import com.nexusbihar.fitnessapp.data.dao.StepLogDao
import com.nexusbihar.fitnessapp.data.dao.UserGoalsDao
import com.nexusbihar.fitnessapp.data.dao.WaterLogDao
import com.nexusbihar.fitnessapp.data.model.HealthData
import com.nexusbihar.fitnessapp.data.model.SleepLog
import com.nexusbihar.fitnessapp.data.model.StepLog
import com.nexusbihar.fitnessapp.data.model.UserGoals
import com.nexusbihar.fitnessapp.data.model.WaterLog

@Database(
    entities = [
        HealthData::class, // daily health metrics aggregation
        WaterLog::class, // individual water intake logs
        StepLog::class, // individual step count logs
        SleepLog::class, // individual sleep session logs
        UserGoals::class, // user-defined health goals
    ],
    version = 2,
    exportSchema = false // disable schema export for production
)

@TypeConverters(Converters::class)
abstract class HealthDatabase : RoomDatabase() {
    // === DAO Access Methods ===
    // These methods provide access to DAOs
    // Each DAO handles CRUD operations for specific entities

    abstract fun healthDataDao(): HealthDataDao // Daily health data operations
    abstract fun waterLogDao(): WaterLogDao // Water intake log
    abstract fun stepLogDao(): StepLogDao // Step counting operations
    abstract fun sleepLogDao(): SleepLogDao // Sleep tracking operations
    abstract fun userGoalsDao(): UserGoalsDao // User goals management

    companion object {
        @Volatile
        private var INSTANCE: HealthDatabase? = null

        /**
         * SINGLETON DATABASE ACCESS
         *
         * This method ensures only one database instance exists throughout the app's lifecycle.
         * Uses double-checked locking pattern for thread safety.
         *
         * @param context Application context for database creation
         * @param userId Optional user ID for user-specific database isolation
         * @return  HealthDatabase instance
         */

        fun getDatabase(context: Context, userId: String? = null): HealthDatabase {
            val databaseName = if (userId != null) {
                "health_database_${userId}"
            } else {
                "health_database"
            }

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, // use applicationContext to prevent memory leaks
                    HealthDatabase::class.java, // DB class
                    databaseName // user-specific DB file name
                ).build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Clear the current database instance
         * This should be called when user logs out to ensure fresh DB for next user
         */

        fun clearInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }

}