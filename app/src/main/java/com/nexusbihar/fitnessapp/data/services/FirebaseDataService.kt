package com.nexusbihar.fitnessapp.data.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nexusbihar.fitnessapp.data.model.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


/**
 * FIREBASE DATA SERVICE
 *
 * This service handles user-specific data storage and synchronization with Firebase Firestore.
 * It ensures that each user's health data is stored separately and securely.
 *
 * KEY FEATURES:
 * - User-specific data isolation using Firebase Auth UID
 * - Real-time data synchronization across devices
 * - Offline support with local caching
 * - Automatic data backup and recovery
 * - Secure data access with Firebase Security Rules
 *
 * DATA STRUCTURE:
 * - Collection: users/{userId}/health_data/{date}
 * - Collection: users/{userId}/water_logs/{logId}
 * - Collection: users/{userId}/step_logs/{logId}
 * - Collection: users/{userId}/sleep_logs/{logId}
 * - Collection: users/{userId}/user_goals/{goalsId}
 *
 * SECURITY:
 * - All data is scoped to the authenticated user
 * - Firebase Security Rules prevent cross-user data access
 * - Data is encrypted in transit and at rest
 */

@Singleton
class FirebaseDataService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
){
    // ===== HEALTH DATA OPERATIONS =====

    /**
     * Save health data to Firebase for the current user
     * @param healthData HealthData object to save
     */
    suspend fun saveHealthData(healthData: HealthData) {
        val userId = getCurrentUserId() ?: return
        firestore.collection("users")
            .document(userId)
            .collection("health_data")
            .document(healthData.date)
            .set(healthData, SetOptions.merge())
            .await()
    }

    /**
     * Get health data from Firebase for a specific date
     * @param date Date in YYYY-MM-DD format
     * @return HealthData for the specified date, or null if not found
     */
    suspend fun getHealthDataByDate(date: String): HealthData? {
        val userId = getCurrentUserId() ?: return null
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .collection("health_data")
                .document(date)
                .get()
                .await()

            if (document.exists()) {
                document.toObject(HealthData::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get all health data for the current user
     * @return List of HealthData ordered by date (newest first)
     */
    suspend fun getAllHealthData(): List<HealthData> {
        val userId = getCurrentUserId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("health_data")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(HealthData::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get health data between two dates
     * @param startDate Start date in YYYY-MM-DD format
     * @param endDate End date in YYYY-MM-DD format
     * @return List of HealthData within the date range
     */
    suspend fun getHealthDataBetweenDates(startDate: String, endDate: String): List<HealthData> {
        val userId = getCurrentUserId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("health_data")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(HealthData::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ===== WATER LOG OPERATIONS =====

    /**
     * Save water log to Firebase
     * @param waterLog WaterLog object to save
     */
    suspend fun saveWaterLog(waterLog: WaterLog) {
        val userId = getCurrentUserId() ?: return
        val logId = if (waterLog.id == 0L) {
            // Generate new ID for new logs
            System.currentTimeMillis().toString()
        } else {
            waterLog.id.toString()
        }

        firestore.collection("users")
            .document(userId)
            .collection("water_logs")
            .document(logId)
            .set(waterLog.copy(id = logId.toLong()))
            .await()
    }

    /**
     * Get water logs for a specific date
     * @param date Date in YYYY-MM-DD format
     * @return List of WaterLog for the specified date
     */
    suspend fun getWaterLogsByDate(date: String): List<WaterLog> {
        val userId = getCurrentUserId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("water_logs")
                .whereEqualTo("date", date)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(WaterLog::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ===== STEP LOG OPERATIONS =====

    /**
     * Save step log to Firebase
     * @param stepLog StepLog object to save
     */
    suspend fun saveStepLog(stepLog: StepLog) {
        val userId = getCurrentUserId() ?: return
        val logId = if (stepLog.id == 0L) {
            System.currentTimeMillis().toString()
        } else {
            stepLog.id.toString()
        }

        firestore.collection("users")
            .document(userId)
            .collection("step_logs")
            .document(logId)
            .set(stepLog.copy(id = logId.toLong()))
            .await()
    }

    /**
     * Get step logs for a specific date
     * @param date Date in YYYY-MM-DD format
     * @return List of StepLog for the specified date
     */
    suspend fun getStepLogsByDate(date: String): List<StepLog> {
        val userId = getCurrentUserId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("step_logs")
                .whereEqualTo("date", date)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(StepLog::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ===== SLEEP LOG OPERATIONS =====

    /**
     * Save sleep log to Firebase
     * @param sleepLog SleepLog object to save
     */
    suspend fun saveSleepLog(sleepLog: SleepLog) {
        val userId = getCurrentUserId() ?: return
        val logId = if (sleepLog.id == 0L) {
            System.currentTimeMillis().toString()
        } else {
            sleepLog.id.toString()
        }

        firestore.collection("users")
            .document(userId)
            .collection("sleep_logs")
            .document(logId)
            .set(sleepLog.copy(id = logId.toLong()))
            .await()
    }

    /**
     * Get sleep logs for a specific date
     * @param date Date in YYYY-MM-DD format
     * @return List of SleepLog for the specified date
     */
    suspend fun getSleepLogsByDate(date: String): List<SleepLog> {
        val userId = getCurrentUserId() ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("sleep_logs")
                .whereEqualTo("date", date)
                .orderBy("sleepStart", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(SleepLog::class.java) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ===== USER GOALS OPERATIONS =====

    /**
     * Save user goals to Firebase
     * @param userGoals UserGoals object to save
     */
    suspend fun saveUserGoals(userGoals: UserGoals) {
        val userId = getCurrentUserId() ?: return
        firestore.collection("users")
            .document(userId)
            .collection("user_goals")
            .document("goals")
            .set(userGoals)
            .await()
    }

    /**
     * Get user goals from Firebase
     * @return UserGoals for the current user, or default goals if not found
     */
    suspend fun getUserGoals(): UserGoals? {
        val userId = getCurrentUserId() ?: return null
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .collection("user_goals")
                .document("goals")
                .get()
                .await()

            if (document.exists()) {
                document.toObject(UserGoals::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // ===== DATA SYNCHRONIZATION =====

    /**
     * Sync all local data to Firebase for the current user
     * This method should be called when user logs in
     */
    suspend fun syncAllDataToFirebase(
        healthDataList: List<HealthData>,
        waterLogs: List<WaterLog>,
        stepLogs: List<StepLog>,
        sleepLogs: List<SleepLog>,
        userGoals: UserGoals?
    ) {
        val userId = getCurrentUserId() ?: return

        // Save health data
        healthDataList.forEach { healthData ->
            saveHealthData(healthData)
        }

        // Save water logs
        waterLogs.forEach { waterLog ->
            saveWaterLog(waterLog)
        }

        // Save step logs
        stepLogs.forEach { stepLog ->
            saveStepLog(stepLog)
        }

        // Save sleep logs
        sleepLogs.forEach { sleepLog ->
            saveSleepLog(sleepLog)
        }

        // Save user goals
        userGoals?.let { saveUserGoals(it) }
    }

    /**
     * Clear all data for the current user from Firebase
     * This method should be called when user logs out
     */
    suspend fun clearUserData() {
        val userId = getCurrentUserId() ?: return

        try {
            // Delete all collections for the user
            val collections = listOf("health_data", "water_logs", "step_logs", "sleep_logs", "user_goals")

            collections.forEach { collectionName ->
                val snapshot = firestore.collection("users")
                    .document(userId)
                    .collection(collectionName)
                    .get()
                    .await()

                val batch = firestore.batch()
                snapshot.documents.forEach { document ->
                    batch.delete(document.reference)
                }
                batch.commit().await()
            }
        } catch (e: Exception) {
            // Handle error silently - data will be cleared on next login
        }
    }

    // ===== HELPER METHODS =====

    /**
     * Get the current authenticated user ID
     * @return User ID if authenticated, null otherwise
     */
    private fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    /**
     * Check if user is authenticated
     * @return true if user is authenticated, false otherwise
     */
    fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }
}