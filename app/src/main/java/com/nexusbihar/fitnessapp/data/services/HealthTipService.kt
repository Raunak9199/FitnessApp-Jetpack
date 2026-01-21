package com.nexusbihar.fitnessapp.data.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


data class HealthTip(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val source: String = "Health API"
)

class HealthTipsService {

    suspend fun getHealthTips(category: String = "general"): List<HealthTip> = withContext(
        Dispatchers.IO) {
        try {
            // Return curated health tips
            // TODO: In future versions, integrate with health API for dynamic tips
            getStaticHealthTips(category)
        } catch (e: Exception) {
            // Return fallback tips if service fails
            getFallbackTips()
        }
    }

    private fun getStaticHealthTips(category: String): List<HealthTip> {
        return when (category.lowercase()) {
            "hydration" -> listOf(
                HealthTip(
                    id = "1",
                    title = "Stay Hydrated",
                    description = "Drink at least 8 glasses of water daily. Start your morning with a glass of water to kickstart your metabolism.",
                    category = "hydration"
                ),
                HealthTip(
                    id = "2",
                    title = "Water-Rich Foods",
                    description = "Include water-rich foods like cucumbers, watermelon, and oranges in your diet to boost hydration.",
                    category = "hydration"
                ),
                HealthTip(
                    id = "3",
                    title = "Hydration Timing",
                    description = "Drink water 30 minutes before meals to aid digestion and prevent overeating.",
                    category = "hydration"
                )
            )
            "exercise" -> listOf(
                HealthTip(
                    id = "4",
                    title = "Daily Movement",
                    description = "Aim for at least 10,000 steps daily. Take short walks every hour to break up sedentary time.",
                    category = "exercise"
                ),
                HealthTip(
                    id = "5",
                    title = "Stair Climbing",
                    description = "Take the stairs instead of elevators. It's a great way to incorporate cardio into your daily routine.",
                    category = "exercise"
                ),
                HealthTip(
                    id = "6",
                    title = "Morning Stretches",
                    description = "Start your day with 10 minutes of stretching to improve flexibility and reduce muscle tension.",
                    category = "exercise"
                )
            )
            "sleep" -> listOf(
                HealthTip(
                    id = "7",
                    title = "Consistent Sleep Schedule",
                    description = "Go to bed and wake up at the same time every day, even on weekends, to regulate your body clock.",
                    category = "sleep"
                ),
                HealthTip(
                    id = "8",
                    title = "Screen-Free Bedroom",
                    description = "Keep electronic devices out of the bedroom. The blue light can interfere with melatonin production.",
                    category = "sleep"
                ),
                HealthTip(
                    id = "9",
                    title = "Relaxing Bedtime Routine",
                    description = "Create a calming pre-sleep routine with activities like reading, meditation, or gentle stretching.",
                    category = "sleep"
                )
            )
            "nutrition" -> listOf(
                HealthTip(
                    id = "10",
                    title = "Balanced Meals",
                    description = "Include protein, healthy fats, and complex carbohydrates in each meal for sustained energy.",
                    category = "nutrition"
                ),
                HealthTip(
                    id = "11",
                    title = "Colorful Vegetables",
                    description = "Eat a rainbow of vegetables daily. Different colors provide different essential nutrients.",
                    category = "nutrition"
                ),
                HealthTip(
                    id = "12",
                    title = "Mindful Eating",
                    description = "Eat slowly and without distractions. This helps with portion control and digestion.",
                    category = "nutrition"
                )
            )
            else -> getGeneralHealthTips()
        }
    }

    private fun getGeneralHealthTips(): List<HealthTip> {
        return listOf(
            HealthTip(
                id = "13",
                title = "Regular Health Checkups",
                description = "Schedule annual health checkups and screenings to catch potential health issues early.",
                category = "general"
            ),
            HealthTip(
                id = "14",
                title = "Stress Management",
                description = "Practice stress-reduction techniques like deep breathing, meditation, or yoga to improve overall well-being.",
                category = "general"
            ),
            HealthTip(
                id = "15",
                title = "Social Connections",
                description = "Maintain strong social relationships. Social connections are crucial for mental and emotional health.",
                category = "general"
            ),
            HealthTip(
                id = "16",
                title = "Limit Processed Foods",
                description = "Reduce intake of processed and ultra-processed foods. Focus on whole, natural foods instead.",
                category = "general"
            ),
            HealthTip(
                id = "17",
                title = "Regular Hand Washing",
                description = "Wash your hands frequently with soap and water for at least 20 seconds to prevent illness.",
                category = "general"
            ),
            HealthTip(
                id = "18",
                title = "Sun Protection",
                description = "Use sunscreen daily, even on cloudy days, to protect your skin from harmful UV rays.",
                category = "general"
            )
        )
    }

    private fun getFallbackTips(): List<HealthTip> {
        return listOf(
            HealthTip(
                id = "fallback1",
                title = "Stay Active",
                description = "Regular physical activity is essential for maintaining good health and preventing chronic diseases.",
                category = "general"
            ),
            HealthTip(
                id = "fallback2",
                title = "Eat Well",
                description = "A balanced diet rich in fruits, vegetables, and whole grains supports overall health and well-being.",
                category = "general"
            ),
            HealthTip(
                id = "fallback3",
                title = "Get Enough Sleep",
                description = "Aim for 7-9 hours of quality sleep each night to support physical and mental health.",
                category = "general"
            )
        )
    }

    suspend fun getRandomTip(): HealthTip = withContext(Dispatchers.IO) {
        val allTips = getStaticHealthTips("general") +
                getStaticHealthTips("hydration") +
                getStaticHealthTips("exercise") +
                getStaticHealthTips("sleep") +
                getStaticHealthTips("nutrition")
        allTips.random()
    }
}