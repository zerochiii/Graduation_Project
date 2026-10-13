package com.example.graduationproject.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class VivifrailRepositoryTest {
    private val repository = VivifrailRepository()

    @Test
    fun weeksOneAndSix_useBaseWalkingPlans() {
        assertEquals("2-5分鐘/組", walking("B", "B7", 1))
        assertEquals("10分鐘/組", walking("C", "C8", 6))
        assertEquals("20分鐘/組", walking("D", "D9", 6))
    }

    @Test
    fun weeksSevenAndTwelve_useAdvancedWalkingPlans() {
        assertEquals("8分鐘/組", walking("B", "B7", 7))
        assertEquals("15分鐘/組", walking("C", "C8", 12))
        assertEquals("持續走30-45分鐘", walking("D", "D9", 7))
    }

    @Test
    fun invalidWeek_isRejectedInsteadOfFallingBack() {
        assertThrows(IllegalArgumentException::class.java) {
            repository.getDailyPlan(level = "B", day = 1, week = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            repository.getDailyPlan(level = "B", day = 1, week = 13)
        }
    }

    private fun walking(level: String, exerciseId: String, week: Int): String =
        repository.getDailyPlan(level = level, day = 1, week = week)
            .exercises.first { it.id == exerciseId }.repsOrTime
}
