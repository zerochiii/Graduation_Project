package com.example.graduationproject.family

import com.example.graduationproject.DataClass.FamilyElderRecord
import com.example.graduationproject.api.ApiService

data class FamilyElderSummary(
    val displayName: String,
    val realName: String?,
    val sppbScore: Int?,
    val hasFallRisk: Boolean?,
    val testDate: String?
)

interface FamilyRepository {
    suspend fun getElders(familyAccountId: Int): List<FamilyElderSummary>
}

class ApiFamilyRepository(
    private val apiService: ApiService
) : FamilyRepository {
    override suspend fun getElders(familyAccountId: Int): List<FamilyElderSummary> {
        require(familyAccountId > 0) { "家屬帳號資料錯誤" }

        val response = apiService.getFamilyElders(familyAccountId)
        if (!response.isSuccessful) {
            throw FamilyDataException("無法取得長輩資料（HTTP ${response.code()}）")
        }

        val body = response.body()
            ?: throw FamilyDataException("伺服器回應內容為空")
        if (!body.success) {
            throw FamilyDataException(body.message?.takeIf { it.isNotBlank() } ?: "無法取得長輩資料")
        }

        return body.data.map(FamilyElderRecord::toSummary)
    }
}

class FamilyDataException(message: String) : Exception(message)

internal fun FamilyElderRecord.toSummary(): FamilyElderSummary {
    val normalizedNickname = nickname?.trim().orEmpty()
    val normalizedRealName = realName?.trim().orEmpty()
    val displayName = normalizedNickname.ifBlank {
        normalizedRealName.ifBlank { "長輩" }
    }

    return FamilyElderSummary(
        displayName = displayName,
        realName = normalizedRealName
            .takeIf { it.isNotBlank() && it != displayName },
        sppbScore = sppbScore,
        hasFallRisk = when (hasFallRisk) {
            0 -> false
            1 -> true
            else -> null
        },
        testDate = testDate?.trim()?.takeIf { it.isNotEmpty() }
    )
}
