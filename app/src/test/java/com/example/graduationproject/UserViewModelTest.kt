package com.example.graduationproject

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserViewModelTest {

    @Test
    fun notAssessed_doesNotExposeTrainingGrade() {
        val viewModel = UserViewModel()
        viewModel.bindAccount(1)
        viewModel.updateAssessmentFromDashboard(1, "", null)

        assertFalse(viewModel.isSurveyComplete)
        assertNull(viewModel.currentGrade)
    }

    @Test
    fun gradeD_isPreservedWithoutFallback() {
        val viewModel = UserViewModel()
        viewModel.bindAccount(1)
        viewModel.updateAssessmentFromDashboard(1, "D", 10)

        assertEquals("D", viewModel.currentGrade)
    }

    @Test
    fun blankGradeAndNullScore_isNotAssessed() {
        assertEquals(
            AssessmentState.NotAssessed,
            resolveAssessmentState(accountId = 1, grade = "", sppbScore = null)
        )
    }

    @Test
    fun gradeAAndZeroScore_isAssessed() {
        assertEquals(
            AssessmentState.Assessed(accountId = 1, grade = "A", sppbScore = 0),
            resolveAssessmentState(accountId = 1, grade = "A", sppbScore = 0)
        )
    }

    @Test
    fun gradeDAndTenScore_isAssessed() {
        assertEquals(
            AssessmentState.Assessed(accountId = 1, grade = "D", sppbScore = 10),
            resolveAssessmentState(accountId = 1, grade = "D", sppbScore = 10)
        )
    }

    @Test
    fun gradeWithoutScore_isInconsistent() {
        assertTrue(resolveAssessmentState(1, "D", null) is AssessmentState.Inconsistent)
    }

    @Test
    fun scoreWithoutGrade_isInconsistent() {
        assertTrue(resolveAssessmentState(1, "", 8) is AssessmentState.Inconsistent)
    }

    @Test
    fun unsupportedGrade_isInconsistent() {
        assertTrue(resolveAssessmentState(1, "UNKNOWN", 5) is AssessmentState.Inconsistent)
    }

    @Test
    fun supportedPlusGrade_isAssessed() {
        assertEquals(
            AssessmentState.Assessed(accountId = 1, grade = "B+", sppbScore = 6),
            resolveAssessmentState(accountId = 1, grade = "B+", sppbScore = 6)
        )
    }

    @Test
    fun outOfRangeScore_isInconsistent() {
        assertTrue(resolveAssessmentState(1, "A", -1) is AssessmentState.Inconsistent)
        assertTrue(resolveAssessmentState(1, "D", 13) is AssessmentState.Inconsistent)
    }

    @Test
    fun changingAccount_clearsOldAssessmentAndRejectsStaleResponse() {
        val viewModel = UserViewModel()
        viewModel.bindAccount(1)
        assertTrue(viewModel.updateAssessmentFromDashboard(1, "D", 10))
        assertTrue(viewModel.isSurveyComplete)
        assertEquals("D", viewModel.currentGrade)
        assertEquals(10, viewModel.currentSppbScore)

        assertTrue(viewModel.bindAccount(2))

        assertEquals(2, viewModel.currentAccountId)
        assertEquals(AssessmentState.Loading, viewModel.assessmentState)
        assertFalse(viewModel.isSurveyComplete)
        assertNull(viewModel.currentGrade)
        assertFalse(viewModel.updateAssessmentFromDashboard(1, "D", 10))
        assertEquals(AssessmentState.Loading, viewModel.assessmentState)
    }

    @Test
    fun reset_clearsAccountAndAssessment() {
        val viewModel = UserViewModel()
        viewModel.bindAccount(1)
        viewModel.updateAssessmentFromDashboard(1, "D", 10)

        viewModel.reset()

        assertNull(viewModel.currentAccountId)
        assertEquals(AssessmentState.NotAssessed, viewModel.assessmentState)
        assertFalse(viewModel.isSurveyComplete)
        assertNull(viewModel.currentGrade)
        assertNull(viewModel.currentSppbScore)
    }
}
