package com.example.graduationproject.assessment

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssessmentCompletionNoticeViewModelTest {
    @Test
    fun missingPreference_defaultsToNotPendingForPreviouslyAssessedUser() {
        val viewModel = AssessmentCompletionNoticeViewModel(InMemoryStore())

        viewModel.bindAccount(2)

        assertFalse(viewModel.uiState.pending)
    }

    @Test
    fun successfulSave_marksNoticePending() {
        val viewModel = AssessmentCompletionNoticeViewModel(InMemoryStore())

        assertTrue(shouldMarkAssessmentCompletionNoticePending(true, true))
        assertTrue(viewModel.markPending(2))
        assertTrue(viewModel.uiState.pending)
    }

    @Test
    fun failedSave_doesNotMarkNoticePending() {
        assertFalse(shouldMarkAssessmentCompletionNoticePending(false, true))
        assertFalse(shouldMarkAssessmentCompletionNoticePending(true, false))
        assertFalse(shouldMarkAssessmentCompletionNoticePending(false, false))
    }

    @Test
    fun acknowledge_persistsNotPending() {
        val store = InMemoryStore()
        val viewModel = AssessmentCompletionNoticeViewModel(store)
        viewModel.markPending(2)

        assertTrue(viewModel.acknowledge(2))

        assertFalse(store.isPending(2))
        assertFalse(viewModel.uiState.pending)
    }

    @Test
    fun recreatedStateOwner_doesNotShowAcknowledgedNotice() {
        val store = InMemoryStore()
        AssessmentCompletionNoticeViewModel(store).apply {
            markPending(2)
            acknowledge(2)
        }

        val recreated = AssessmentCompletionNoticeViewModel(store)
        recreated.bindAccount(2)

        assertFalse(recreated.uiState.pending)
    }

    @Test
    fun recreatedStateOwner_restoresUnacknowledgedNotice() {
        val store = InMemoryStore().apply { writePending(2, true) }

        val recreated = AssessmentCompletionNoticeViewModel(store)
        recreated.bindAccount(2)

        assertTrue(recreated.uiState.pending)
    }

    @Test
    fun laterSuccessfulAssessmentShowsNoticeAgainEvenWithSameGrade() {
        val store = InMemoryStore()
        val viewModel = AssessmentCompletionNoticeViewModel(store)
        viewModel.markPending(2)
        viewModel.acknowledge(2)

        viewModel.markPending(2)

        assertTrue(viewModel.uiState.pending)
    }

    @Test
    fun pendingStateIsIsolatedByAccount() {
        val store = InMemoryStore().apply { writePending(23, true) }
        val viewModel = AssessmentCompletionNoticeViewModel(store)

        viewModel.bindAccount(2)
        assertFalse(viewModel.uiState.pending)

        viewModel.bindAccount(23)
        assertTrue(viewModel.uiState.pending)
    }

    @Test
    fun logoutAndReloginKeepsAcknowledgedState() {
        val store = InMemoryStore()
        val viewModel = AssessmentCompletionNoticeViewModel(store)
        viewModel.markPending(2)
        viewModel.acknowledge(2)

        viewModel.reset()
        viewModel.bindAccount(2)

        assertFalse(viewModel.uiState.pending)
    }

    @Test
    fun dialogCopyIsStableAndContainsNoAssessmentValues() {
        assertEquals("評估完成", ASSESSMENT_COMPLETION_NOTICE_TITLE)
        assertEquals(
            "體能評估已完成，系統已為您安排適合的訓練內容。",
            ASSESSMENT_COMPLETION_NOTICE_BODY
        )
        assertEquals("知道了", ASSESSMENT_COMPLETION_NOTICE_ACTION)
        assertFalse(ASSESSMENT_COMPLETION_NOTICE_BODY.contains("級別"))
        assertFalse(ASSESSMENT_COMPLETION_NOTICE_BODY.contains("SPPB"))
    }

    private class InMemoryStore : AssessmentCompletionNoticeStore {
        private val pendingByAccount = mutableMapOf<Int, Boolean>()

        override fun isPending(accountId: Int): Boolean = pendingByAccount[accountId] ?: false

        override fun writePending(accountId: Int, pending: Boolean): Boolean {
            pendingByAccount[accountId] = pending
            return true
        }
    }
}
