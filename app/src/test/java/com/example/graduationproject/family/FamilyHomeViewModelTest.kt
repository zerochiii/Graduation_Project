package com.example.graduationproject.family

import com.example.graduationproject.DataClass.FamilyElderRecord
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyHomeViewModelTest {

    @Test
    fun successfulLoadShowsContent() = runBlocking {
        val elder = elder("家中長輩")
        val viewModel = FamilyHomeViewModel(FakeFamilyRepository(result = Result.success(listOf(elder))))

        viewModel.loadForTest(accountId = 12)

        val state = viewModel.uiState.value
        assertTrue(state is FamilyHomeUiState.Content)
        assertEquals(listOf(elder), (state as FamilyHomeUiState.Content).elders)
    }

    @Test
    fun emptyLoadShowsEmptyInsteadOfContentOrError() = runBlocking {
        val viewModel = FamilyHomeViewModel(FakeFamilyRepository(result = Result.success(emptyList())))

        viewModel.loadForTest(accountId = 12)

        assertEquals(FamilyHomeUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun failedLoadShowsErrorInsteadOfEmpty() = runBlocking {
        val viewModel = FamilyHomeViewModel(
            FakeFamilyRepository(result = Result.failure(FamilyDataException("後端暫時無法使用")))
        )

        viewModel.loadForTest(accountId = 12)

        val state = viewModel.uiState.value
        assertTrue(state is FamilyHomeUiState.Error)
        assertEquals("後端暫時無法使用", (state as FamilyHomeUiState.Error).message)
    }

    @Test
    fun retryRequestsAgainAndCanRecover() = runBlocking {
        val repository = SequencedFamilyRepository(
            mutableListOf(
                Result.failure(FamilyDataException("第一次失敗")),
                Result.success(listOf(elder("重試成功")))
            )
        )
        val viewModel = FamilyHomeViewModel(repository)

        viewModel.loadForTest(accountId = 12)
        assertTrue(viewModel.uiState.value is FamilyHomeUiState.Error)

        viewModel.retryForTest()

        assertTrue(viewModel.uiState.value is FamilyHomeUiState.Content)
        assertEquals(2, repository.requestCount)
    }

    @Test
    fun refreshAfterEmpty_requestsAgainAndCanRevealNewBinding() = runBlocking {
        val repository = SequencedFamilyRepository(
            mutableListOf(
                Result.success(emptyList()),
                Result.success(listOf(elder("新綁定長輩")))
            )
        )
        val viewModel = FamilyHomeViewModel(repository)

        viewModel.loadForTest(accountId = 12)
        assertEquals(FamilyHomeUiState.Empty, viewModel.uiState.value)

        viewModel.refreshForTest()

        val state = viewModel.uiState.value as FamilyHomeUiState.Content
        assertEquals("新綁定長輩", state.elders.single().displayName)
        assertEquals(2, repository.requestCount)
    }

    @Test
    fun accountChangeUsesNewAccountId() = runBlocking {
        val repository = RecordingFamilyRepository()
        val viewModel = FamilyHomeViewModel(repository)

        viewModel.loadForTest(accountId = 12)
        viewModel.loadForTest(accountId = 33)

        assertEquals(listOf(12, 33), repository.accountIds)
        val state = viewModel.uiState.value as FamilyHomeUiState.Content
        assertEquals("帳號 33", state.elders.single().displayName)
    }

    @Test
    fun nullableApiFieldsMapSafely() {
        val summary = FamilyElderRecord().toSummary()

        assertEquals("長輩", summary.displayName)
        assertNull(summary.realName)
        assertNull(summary.sppbScore)
        assertNull(summary.hasFallRisk)
        assertNull(summary.testDate)
    }

    @Test
    fun numericFallRiskMapsToBooleanMeaning() {
        assertEquals(true, FamilyElderRecord(hasFallRisk = 1).toSummary().hasFallRisk)
        assertEquals(false, FamilyElderRecord(hasFallRisk = 0).toSummary().hasFallRisk)
        assertNull(FamilyElderRecord(hasFallRisk = 2).toSummary().hasFallRisk)
    }

    @Test
    fun assessmentDateUsesReadableFormatAndNullFallback() {
        assertEquals("2026/10/20", formatAssessmentDate("2026-10-20 09:30:00"))
        assertEquals("尚無評估日期", formatAssessmentDate(null))
    }

    private fun elder(name: String) = FamilyElderSummary(
        displayName = name,
        realName = null,
        sppbScore = 8,
        hasFallRisk = false,
        testDate = "2026-10-21 10:30:00"
    )
}

private class FakeFamilyRepository(
    private val result: Result<List<FamilyElderSummary>>
) : FamilyRepository {
    override suspend fun getElders(familyAccountId: Int): List<FamilyElderSummary> = result.getOrThrow()
}

private class SequencedFamilyRepository(
    private val results: MutableList<Result<List<FamilyElderSummary>>>
) : FamilyRepository {
    var requestCount = 0
        private set

    override suspend fun getElders(familyAccountId: Int): List<FamilyElderSummary> {
        requestCount += 1
        return results.removeAt(0).getOrThrow()
    }
}

private class RecordingFamilyRepository : FamilyRepository {
    val accountIds = mutableListOf<Int>()

    override suspend fun getElders(familyAccountId: Int): List<FamilyElderSummary> {
        accountIds += familyAccountId
        return listOf(
            FamilyElderSummary(
                displayName = "帳號 $familyAccountId",
                realName = null,
                sppbScore = null,
                hasFallRisk = null,
                testDate = null
            )
        )
    }
}
