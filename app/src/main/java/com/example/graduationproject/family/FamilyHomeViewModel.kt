package com.example.graduationproject.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface FamilyHomeUiState {
    data object Loading : FamilyHomeUiState
    data object Empty : FamilyHomeUiState
    data class Content(val elders: List<FamilyElderSummary>) : FamilyHomeUiState
    data class Error(val message: String) : FamilyHomeUiState
}

class FamilyHomeViewModel(
    private val repository: FamilyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<FamilyHomeUiState>(FamilyHomeUiState.Loading)
    val uiState: StateFlow<FamilyHomeUiState> = _uiState.asStateFlow()

    private var boundAccountId = -1
    private var requestGeneration = 0
    private var loadJob: Job? = null

    fun bindAccount(accountId: Int) {
        if (accountId == boundAccountId && loadJob?.isActive == true) return
        if (accountId == boundAccountId && _uiState.value !is FamilyHomeUiState.Error) return

        boundAccountId = accountId
        startLoad(accountId)
    }

    fun retry() {
        if (boundAccountId <= 0 || loadJob?.isActive == true) return
        startLoad(boundAccountId)
    }

    fun refresh() {
        if (boundAccountId <= 0 || loadJob?.isActive == true) return
        startLoad(boundAccountId)
    }

    private fun startLoad(accountId: Int) {
        loadJob?.cancel()
        val generation = ++requestGeneration
        _uiState.value = FamilyHomeUiState.Loading
        loadJob = viewModelScope.launch {
            load(accountId, generation)
        }
    }

    internal suspend fun loadForTest(accountId: Int) {
        boundAccountId = accountId
        val generation = ++requestGeneration
        _uiState.value = FamilyHomeUiState.Loading
        load(accountId, generation)
    }

    internal suspend fun retryForTest() {
        loadForTest(boundAccountId)
    }

    internal suspend fun refreshForTest() {
        loadForTest(boundAccountId)
    }

    private suspend fun load(accountId: Int, generation: Int) {
        val nextState = runCatching { repository.getElders(accountId) }
            .fold(
                onSuccess = { elders ->
                    if (elders.isEmpty()) FamilyHomeUiState.Empty
                    else FamilyHomeUiState.Content(elders)
                },
                onFailure = { error ->
                    FamilyHomeUiState.Error(
                        error.message?.takeIf { it.isNotBlank() }
                            ?: "長輩資料載入失敗，請稍後再試"
                    )
                }
            )

        if (generation == requestGeneration && accountId == boundAccountId) {
            _uiState.value = nextState
        }
    }
}

class FamilyHomeViewModelFactory(
    private val repository: FamilyRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FamilyHomeViewModel::class.java))
        return FamilyHomeViewModel(repository) as T
    }
}
