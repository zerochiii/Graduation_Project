package com.example.graduationproject.assessment

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

internal const val ASSESSMENT_COMPLETION_NOTICE_TITLE = "評估完成"
internal const val ASSESSMENT_COMPLETION_NOTICE_BODY =
    "體能評估已完成，系統已為您安排適合的訓練內容。"
internal const val ASSESSMENT_COMPLETION_NOTICE_ACTION = "知道了"

private const val ASSESSMENT_COMPLETION_NOTICE_PENDING_PREFIX =
    "ASSESSMENT_COMPLETION_NOTICE_PENDING_"

internal fun shouldMarkAssessmentCompletionNoticePending(
    httpSuccessful: Boolean,
    businessSuccessful: Boolean
): Boolean = httpSuccessful && businessSuccessful

internal interface AssessmentCompletionNoticeStore {
    fun isPending(accountId: Int): Boolean
    fun writePending(accountId: Int, pending: Boolean): Boolean
}

internal class SharedPreferencesAssessmentCompletionNoticeStore(
    private val preferences: SharedPreferences
) : AssessmentCompletionNoticeStore {
    override fun isPending(accountId: Int): Boolean =
        preferences.getBoolean(pendingKey(accountId), false)

    override fun writePending(accountId: Int, pending: Boolean): Boolean =
        preferences.edit().putBoolean(pendingKey(accountId), pending).commit()

    private fun pendingKey(accountId: Int) =
        "$ASSESSMENT_COMPLETION_NOTICE_PENDING_PREFIX$accountId"
}

internal data class AssessmentCompletionNoticeState(
    val accountId: Int? = null,
    val pending: Boolean = false
)

internal class AssessmentCompletionNoticeViewModel(
    private val store: AssessmentCompletionNoticeStore
) : ViewModel() {
    var uiState by mutableStateOf(AssessmentCompletionNoticeState())
        private set

    fun bindAccount(accountId: Int) {
        if (accountId <= 0) {
            reset()
            return
        }
        if (uiState.accountId == accountId) return
        uiState = AssessmentCompletionNoticeState(
            accountId = accountId,
            pending = store.isPending(accountId)
        )
    }

    fun markPending(accountId: Int): Boolean {
        if (accountId <= 0 || !store.writePending(accountId, true)) return false
        uiState = AssessmentCompletionNoticeState(accountId = accountId, pending = true)
        return true
    }

    fun acknowledge(accountId: Int): Boolean {
        if (accountId <= 0 || uiState.accountId != accountId) return false
        if (!store.writePending(accountId, false)) return false
        uiState = uiState.copy(pending = false)
        return true
    }

    fun reset() {
        uiState = AssessmentCompletionNoticeState()
    }
}

internal class AssessmentCompletionNoticeViewModelFactory(
    private val store: AssessmentCompletionNoticeStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(AssessmentCompletionNoticeViewModel::class.java))
        return AssessmentCompletionNoticeViewModel(store) as T
    }
}
