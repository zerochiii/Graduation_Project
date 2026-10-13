package com.example.graduationproject.ui.screens

import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardPointRecordTest {
    @Test
    fun snakeCaseIsEarning_isMappedFromPointHistoryJson() {
        val earning = Gson().fromJson(
            """{"title":"訓練獎勵","points":10,"date":"2026-10-13","is_earning":true}""",
            PointRecord::class.java
        )
        val redemption = Gson().fromJson(
            """{"title":"兌換獎勵","points":10,"date":"2026-10-13","is_earning":false}""",
            PointRecord::class.java
        )

        assertTrue(earning.isEarning)
        assertFalse(redemption.isEarning)
    }
}
