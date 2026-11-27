package com.example.graduationproject.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.graduationproject.ui.util.SingleActionGuard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal val FAMILY_RELATIONSHIP_OPTIONS = listOf("父親", "母親", "祖父", "祖母", "配偶", "其他")

data class FamilyBindFormState(
    val bindingCode: String = "",
    val relationship: String? = null
) {
    val canSubmit: Boolean
        get() = bindingCode.trim().isNotEmpty() && !relationship.isNullOrBlank()
}

sealed interface FamilyBindUiState {
    data object Idle : FamilyBindUiState
    data object Submitting : FamilyBindUiState
    data object Success : FamilyBindUiState
    data class Error(val message: String) : FamilyBindUiState
    data class Unavailable(val message: String) : FamilyBindUiState
}

sealed interface FamilyBindResult {
    data object Success : FamilyBindResult
    data class Error(val message: String) : FamilyBindResult
    data class Unavailable(val message: String) : FamilyBindResult
}

interface FamilyBindingRepository {
    suspend fun bindElder(bindingCode: String, relationship: String): FamilyBindResult
}

/**
 * Production-safe placeholder until the authenticated binding-code contract exists.
 * It deliberately performs no network mutation and can never report a successful binding.
 */
class BackendBlockedFamilyBindingRepository : FamilyBindingRepository {
    override suspend fun bindElder(bindingCode: String, relationship: String): FamilyBindResult =
        FamilyBindResult.Unavailable("目前暫時無法完成綁定，請稍後再試。")
}

class FamilyBindingViewModel(
    private val repository: FamilyBindingRepository
) : ViewModel() {
    private val _formState = MutableStateFlow(FamilyBindFormState())
    val formState: StateFlow<FamilyBindFormState> = _formState.asStateFlow()

    private val _uiState = MutableStateFlow<FamilyBindUiState>(FamilyBindUiState.Idle)
    val uiState: StateFlow<FamilyBindUiState> = _uiState.asStateFlow()

    private val submitGuard = SingleActionGuard()

    fun updateBindingCode(value: String) {
        _formState.value = _formState.value.copy(bindingCode = value)
        clearOperationMessage()
    }

    fun selectRelationship(value: String) {
        _formState.value = _formState.value.copy(relationship = value)
        clearOperationMessage()
    }

    fun submit() {
        viewModelScope.launch { submitInternal() }
    }

    internal suspend fun submitForTest(): Boolean = submitInternal()

    fun consumeSuccess() {
        if (_uiState.value == FamilyBindUiState.Success) {
            _uiState.value = FamilyBindUiState.Idle
        }
    }

    private fun clearOperationMessage() {
        if (_uiState.value is FamilyBindUiState.Error || _uiState.value is FamilyBindUiState.Unavailable) {
            _uiState.value = FamilyBindUiState.Idle
        }
    }

    private suspend fun submitInternal(): Boolean {
        val form = _formState.value
        if (!form.canSubmit || !submitGuard.tryStart()) return false

        _uiState.value = FamilyBindUiState.Submitting
        try {
            _uiState.value = when (
                val result = repository.bindElder(
                    bindingCode = form.bindingCode.trim(),
                    relationship = requireNotNull(form.relationship)
                )
            ) {
                FamilyBindResult.Success -> FamilyBindUiState.Success
                is FamilyBindResult.Error -> FamilyBindUiState.Error(result.message)
                is FamilyBindResult.Unavailable -> FamilyBindUiState.Unavailable(result.message)
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            _uiState.value = FamilyBindUiState.Error(
                error.message?.takeIf { it.isNotBlank() } ?: "綁定失敗，請稍後再試。"
            )
        } finally {
            submitGuard.finish()
        }
        return true
    }
}

class FamilyBindingViewModelFactory(
    private val repository: FamilyBindingRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(FamilyBindingViewModel::class.java))
        return FamilyBindingViewModel(repository) as T
    }
}
