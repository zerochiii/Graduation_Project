package com.example.graduationproject.fitness

enum class FitnessCategory(val label: String, val apiKey: String) {
    CARDIO("心肺功能", "CARDIO"),
    UPPER_STRENGTH("上肢肌耐力", "UPPER_STRENGTH"),
    LOWER_STRENGTH("下肢肌耐力", "LOWER_STRENGTH"),
    BALANCE("平衡感", "BALANCE"),
    FLEXIBILITY("靈活度", "FLEXIBILITY")
}

object ExerciseCategoryMap {
    private val map: Map<String, FitnessCategory> = mapOf(
        "A1" to FitnessCategory.CARDIO, "B7" to FitnessCategory.CARDIO,
        "C8" to FitnessCategory.CARDIO, "D9" to FitnessCategory.CARDIO,

        "A2" to FitnessCategory.UPPER_STRENGTH, "A3" to FitnessCategory.UPPER_STRENGTH,
        "B1" to FitnessCategory.UPPER_STRENGTH, "B2" to FitnessCategory.UPPER_STRENGTH,
        "C1" to FitnessCategory.UPPER_STRENGTH, "C2" to FitnessCategory.UPPER_STRENGTH,
        "D1" to FitnessCategory.UPPER_STRENGTH, "D2" to FitnessCategory.UPPER_STRENGTH,

        "A4" to FitnessCategory.LOWER_STRENGTH, "A5" to FitnessCategory.LOWER_STRENGTH,
        "B3" to FitnessCategory.LOWER_STRENGTH, "C3" to FitnessCategory.LOWER_STRENGTH,
        "D3" to FitnessCategory.LOWER_STRENGTH, "D4" to FitnessCategory.LOWER_STRENGTH,

        "A6" to FitnessCategory.BALANCE, "B4" to FitnessCategory.BALANCE,
        "C4" to FitnessCategory.BALANCE, "C5" to FitnessCategory.BALANCE,
        "D5" to FitnessCategory.BALANCE, "D6" to FitnessCategory.BALANCE,

        "A7" to FitnessCategory.FLEXIBILITY, "B5" to FitnessCategory.FLEXIBILITY,
        "B6" to FitnessCategory.FLEXIBILITY, "C6" to FitnessCategory.FLEXIBILITY,
        "C7" to FitnessCategory.FLEXIBILITY, "D7" to FitnessCategory.FLEXIBILITY,
        "D8" to FitnessCategory.FLEXIBILITY
    )

    fun categoryOf(exerciseCode: String): FitnessCategory? = map[exerciseCode]
}