package com.nexusbihar.fitnessapp.data.services

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * Google Fit Service for integrating with Google Fit API
 *
 * This service handles:
 * - Google Fit authentication and permissions
 * - Reading health data (steps, distance, calories, heart rate)
 * - Data synchronization with Google Fit
 * - Wearable device data integration
 *
 * @param context Android context for accessing Google Fit services
 */
class GoogleFitService(private val context: Context) {

    companion object {
        private const val TAG = "GoogleFitService"
        const val GOOGLE_FIT_PERMISSION_REQUEST_CODE = 1001
    }

    // Google Sign-In configuration for Google Fit
    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestId()
        .requestProfile()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    // Google Fit permissions configuration
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
        .build()

    /**
     * Check if Google Fit is available and user is signed in
     * @return true if Google Fit is available and user is authenticated
     */
    suspend fun isGoogleFitAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                Log.d(TAG, "No Google account signed in")
                return@withContext false
            }

            // Check if Google Fit permissions are granted
            val hasPermissions = GoogleSignIn.hasPermissions(account, fitnessOptions)
            Log.d(TAG, "Google Fit available: $hasPermissions")
            hasPermissions
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Google Fit availability", e)
            false
        }
    }

    /**
     * Request Google Fit permissions from the user
     * @return true if permissions are granted
     */
    fun requestGoogleFitPermissions(activity: Activity, requestCode: Int) {
        val account = GoogleSignIn.getLastSignedInAccount(context)
            ?: throw IllegalStateException("User not signed in")

        GoogleSignIn.requestPermissions(
            activity,
            requestCode,
            account,
            fitnessOptions
        )
    }
    /*suspend fun requestGoogleFitPermissions(): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                Log.d(TAG, "No Google account signed in")
                return@withContext false
            }

            val hasPermissions = GoogleSignIn.hasPermissions(account, fitnessOptions)
            if (hasPermissions) {
                Log.d(TAG, "Google Fit permissions already granted")
                return@withContext true
            }

            Log.d(TAG, "Google Fit permissions not granted - need to request")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting Google Fit permissions", e)
            false
        }
    }
*/
    /**
     * Get the intent to request Google Fit permissions
     * @return Intent for requesting permissions
     */
    fun getGoogleFitPermissionsIntent(): android.content.Intent {
        return GoogleSignIn.getClient(context, googleSignInOptions)
            .signInIntent
    }
    fun hasGoogleFitPermissions(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return false
        return GoogleSignIn.hasPermissions(account, fitnessOptions)
    }


    /**
     * Check if user needs to sign in to Google
     * @return true if user needs to sign in
     */
    fun needsGoogleSignIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        Log.d(TAG, "Checking Google Sign-In status: account = ${account?.email ?: "null"}")
        return account == null
    }

    /**
     * Handle Google Sign-In result
     * @param data Intent data from sign-in result
     * @return true if sign-in was successful
     */
    suspend fun handleSignInResult(data: android.content.Intent?): Boolean = withContext(Dispatchers.IO) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            Log.d(TAG, "Google Sign-In successful: ${account.email}")
            true
        } catch (e: com.google.android.gms.common.api.ApiException) {
            Log.e(TAG, "Google Sign-In failed with status code: ${e.statusCode}", e)
            when (e.statusCode) {
                com.google.android.gms.common.api.CommonStatusCodes.SIGN_IN_REQUIRED -> {
                    Log.e(TAG, "Sign-in required")
                }
                com.google.android.gms.common.api.CommonStatusCodes.INVALID_ACCOUNT -> {
                    Log.e(TAG, "Invalid account")
                }
                com.google.android.gms.common.api.CommonStatusCodes.NETWORK_ERROR -> {
                    Log.e(TAG, "Network error during sign-in")
                }
                else -> {
                    Log.e(TAG, "Unknown sign-in error: ${e.statusCode}")
                }
            }
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            false
        }
    }

    /**
     * Get Google Sign-In intent
     * @return Intent for Google Sign-In
     */
    fun getGoogleSignInIntent(): android.content.Intent {
        return googleSignInClient.signInIntent
    }

    /**
     * Get today's step count from Google Fit
     * @return number of steps taken today
     */
    suspend fun getTodaySteps(): Int = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.d(TAG, "No Google account or permissions for steps")
                return@withContext 0
            }

            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(1)

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(
                Fitness.getHistoryClient(context, account)
                    .readData(readRequest)
            )

            var totalSteps = 0
            for (dataSet in response.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    for (field in dataPoint.dataType.fields) {
                        if (field.name == Field.FIELD_STEPS.name) {
                            totalSteps += dataPoint.getValue(field).asInt()
                        }
                    }
                }
            }

            Log.d(TAG, "Retrieved $totalSteps steps from Google Fit")
            totalSteps
        } catch (e: Exception) {
            Log.e(TAG, "Error reading steps from Google Fit", e)
            0
        }
    }

    /**
     * Get today's distance traveled from Google Fit
     * @return distance in meters
     */
    suspend fun getTodayDistance(): Float = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.d(TAG, "No Google account or permissions for distance")
                return@withContext 0f
            }

            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(1)

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_DISTANCE_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(
                Fitness.getHistoryClient(context, account)
                    .readData(readRequest)
            )

            var totalDistance = 0f
            for (dataSet in response.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    for (field in dataPoint.dataType.fields) {
                        if (field.name == Field.FIELD_DISTANCE.name) {
                            totalDistance += dataPoint.getValue(field).asFloat()
                        }
                    }
                }
            }

            Log.d(TAG, "Retrieved ${totalDistance}m distance from Google Fit")
            totalDistance
        } catch (e: Exception) {
            Log.e(TAG, "Error reading distance from Google Fit", e)
            0f
        }
    }

    /**
     * Get today's calories burned from Google Fit
     * @return calories burned today
     */
    suspend fun getTodayCalories(): Int = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.d(TAG, "No Google account or permissions for calories")
                return@withContext 0
            }

            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(1)

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(
                Fitness.getHistoryClient(context, account)
                    .readData(readRequest)
            )

            var totalCalories = 0f
            for (dataSet in response.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    for (field in dataPoint.dataType.fields) {
                        if (field.name == Field.FIELD_CALORIES.name) {
                            totalCalories += dataPoint.getValue(field).asFloat()
                        }
                    }
                }
            }

            Log.d(TAG, "Retrieved ${totalCalories.toInt()} calories from Google Fit")
            totalCalories.toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading calories from Google Fit", e)
            0
        }
    }

    /**
     * Get today's average heart rate from Google Fit
     * @return average heart rate in BPM
     */
    suspend fun getTodayHeartRate(): Int = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.d(TAG, "No Google account or permissions for heart rate")
                return@withContext 0
            }

            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(1)

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_HEART_RATE_BPM)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(
                Fitness.getHistoryClient(context, account)
                    .readData(readRequest)
            )

            var totalHeartRate = 0f
            var heartRateCount = 0

            for (dataSet in response.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    for (field in dataPoint.dataType.fields) {
                        if (field.name == Field.FIELD_BPM.name) {
                            totalHeartRate += dataPoint.getValue(field).asFloat()
                            heartRateCount++
                        }
                    }
                }
            }

            val averageHeartRate = if (heartRateCount > 0) {
                (totalHeartRate / heartRateCount).toInt()
            } else {
                0
            }

            Log.d(TAG, "Retrieved average heart rate $averageHeartRate BPM from Google Fit")
            averageHeartRate
        } catch (e: Exception) {
            Log.e(TAG, "Error reading heart rate from Google Fit", e)
            0
        }
    }

    /**
     * Get weekly step count from Google Fit (last 7 days)
     * @return list of daily step counts for the last 7 days
     */
    suspend fun getWeeklySteps(): List<Int> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Log.d(TAG, "No Google account or permissions for weekly steps")
                return@withContext List(7) { 0 }
            }

            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(7)

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(
                Fitness.getHistoryClient(context, account)
                    .readData(readRequest)
            )

            val dailySteps = mutableListOf<Int>()
            val calendar = java.util.Calendar.getInstance()

            // Initialize with 0 for each day
            repeat(7) { dailySteps.add(0) }

            for (dataSet in response.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    val timestamp = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                    calendar.timeInMillis = timestamp
                    val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)

                    // Convert to 0-6 index (Sunday = 0, Monday = 1, etc.)
                    val dayIndex = (dayOfWeek - 1) % 7

                    for (field in dataPoint.dataType.fields) {
                        if (field.name == Field.FIELD_STEPS.name) {
                            val steps = dataPoint.getValue(field).asInt()
                            dailySteps[dayIndex] += steps
                        }
                    }
                }
            }

            Log.d(TAG, "Retrieved weekly steps: $dailySteps from Google Fit")
            dailySteps
        } catch (e: Exception) {
            Log.e(TAG, "Error reading weekly steps from Google Fit", e)
            List(7) { 0 }
        }
    }

    /**
     * Sign out from Google Fit
     */
    fun signOut() {
        googleSignInClient.signOut()
    }

    /**
     * Check if wearable devices are connected and syncing data
     * @return true if wearable devices are detected and syncing
     */
    suspend fun isWearableDeviceConnected(): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                return@withContext false
            }

            // Check for recent data from wearable devices
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.HOURS.toMillis(1)

            val readRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            val response = Tasks.await(
                Fitness.getHistoryClient(context, account)
                    .readData(readRequest)
            )

            // Check if data comes from wearable devices
            var hasWearableData = false
            for (dataSet in response.dataSets) {
                for (dataPoint in dataSet.dataPoints) {
                    val dataSource = dataPoint.originalDataSource
                    if (dataSource != null) {
                        val deviceType = dataSource.device?.type
                        if (deviceType == com.google.android.gms.fitness.data.Device.TYPE_WATCH ||
                            deviceType == com.google.android.gms.fitness.data.Device.TYPE_PHONE) {
                            hasWearableData = true
                            break
                        }
                    }
                }
                if (hasWearableData) break
            }

            Log.d(TAG, "Wearable device connected: $hasWearableData")
            hasWearableData
        } catch (e: Exception) {
            Log.e(TAG, "Error checking wearable device connection", e)
            false
        }
    }

    /**
     * Get available data sources (including wearable devices)
     * @return list of data source names
     */
    suspend fun getAvailableDataSources(): List<String> = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                return@withContext emptyList()
            }

            val dataSources = mutableListOf<String>()

            // Get data sources for steps
            val dataSourcesRequest = com.google.android.gms.fitness.request.DataSourcesRequest.Builder()
                .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
                .build()

            val response = Tasks.await(
                Fitness.getSensorsClient(context, account)
                    .findDataSources(dataSourcesRequest)
            )

            for (dataSource in response) {
                val deviceName = dataSource.device?.model ?: "Unknown Device"
                val deviceType = when (dataSource.device?.type) {
                    com.google.android.gms.fitness.data.Device.TYPE_WATCH -> "Smartwatch"
                    com.google.android.gms.fitness.data.Device.TYPE_PHONE -> "Phone"
                    com.google.android.gms.fitness.data.Device.TYPE_TABLET -> "Tablet"
                    else -> "Other"
                }
                dataSources.add("$deviceName ($deviceType)")
            }

            Log.d(TAG, "Available data sources: $dataSources")
            dataSources
        } catch (e: Exception) {
            Log.e(TAG, "Error getting available data sources", e)
            emptyList()
        }
    }
}