package com.example.graduationproject.family

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyBindingViewModelTest {
    @Test
    fun blankCode_disablesSubmit() {
        assertFalse(FamilyBindFormState(bindingCode = "   ", relationship = "母親").canSubmit)
    }

    @Test
    fun missingRelationship_disablesSubmit() {
        assertFalse(FamilyBindFormState(bindingCode = "A7K4M9Q2").canSubmit)
    }

    @Test
    fun codeAndRelationship_enableSubmitWithoutAssumingCodeFormat() {
        assertTrue(FamilyBindFormState(bindingCode = " code-from-server ", relationship = "其他").canSubmit)
    }

    @Test
    fun backendUnavailable_becomesUnavailableInsteadOfSuccess() = runBlocking {
        val viewModel = FamilyBindingViewModel(BackendBlockedFamilyBindingRepository())
        viewModel.updateBindingCode("A7K4M9Q2")
        viewModel.selectRelationship("母親")

        assertTrue(viewModel.submitForTest())
        assertTrue(viewModel.uiState.value is FamilyBindUiState.Unavailable)
        assertFalse(viewModel.uiState.value is FamilyBindUiState.Success)
    }

    @Test
    fun backendUnavailable_preservesInputAndReleasesGuardForRetry() = runBlocking {
        var requestCount = 0
        val repository = object : FamilyBindingRepository {
            override suspend fun bindElder(bindingCode: String, relationship: String): FamilyBindResult {
                requestCount += 1
                return FamilyBindResult.Unavailable("目前暫時無法完成綁定")
            }
        }
        val viewModel = FamilyBindingViewModel(repository)
        viewModel.updateBindingCode("A7K4M9Q2")
        viewModel.selectRelationship("母親")

        assertTrue(viewModel.submitForTest())
        assertTrue(viewModel.submitForTest())

        assertEquals(2, requestCount)
        assertEquals("A7K4M9Q2", viewModel.formState.value.bindingCode)
        assertEquals("母親", viewModel.formState.value.relationship)
        assertTrue(viewModel.uiState.value is FamilyBindUiState.Unavailable)
    }

    @Test
    fun duplicateSubmit_isRejectedWhileFirstMutationIsInFlight() = runBlocking {
        val release = CompletableDeferred<Unit>()
        val repository = object : FamilyBindingRepository {
            override suspend fun bindElder(bindingCode: String, relationship: String): FamilyBindResult {
                release.await()
                return FamilyBindResult.Success
            }
        }
        val viewModel = FamilyBindingViewModel(repository)
        viewModel.updateBindingCode("A7K4M9Q2")
        viewModel.selectRelationship("母親")

        val first = async(start = CoroutineStart.UNDISPATCHED) { viewModel.submitForTest() }
        assertEquals(FamilyBindUiState.Submitting, viewModel.uiState.value)

        assertFalse(viewModel.submitForTest())

        release.complete(Unit)
        assertTrue(first.await())
        assertEquals(FamilyBindUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun submit_trimsOnlyOuterWhitespaceAndPreservesCodeCase() = runBlocking {
        var receivedCode = ""
        val repository = object : FamilyBindingRepository {
            override suspend fun bindElder(bindingCode: String, relationship: String): FamilyBindResult {
                receivedCode = bindingCode
                return FamilyBindResult.Error("preview")
            }
        }
        val viewModel = FamilyBindingViewModel(repository)
        viewModel.updateBindingCode("  AbC-123  ")
        viewModel.selectRelationship("其他")

        viewModel.submitForTest()

        assertEquals("AbC-123", receivedCode)
    }
}
