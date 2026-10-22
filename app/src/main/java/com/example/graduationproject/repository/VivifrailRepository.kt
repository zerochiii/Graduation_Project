package com.example.graduationproject.repository

import com.example.graduationproject.DataClass.DailyPlan
import com.example.graduationproject.DataClass.Exercise

class VivifrailRepository {

    /**
     * @param level A / B / C / D，也支援 B+、C+，會自動轉成 B、C
     * @param day 1~7，代表週一到週日（六日為休息日）
     * @param week 1~12，代表第幾週；第 7 週後步行訓練會進階
     */
    fun getDailyPlan(level: String, day: Int, week: Int = 1): DailyPlan {
        require(day in 1..7) { "day 必須是 1 到 7，代表週一到週日" }
        require(week in 1..12) { "week 必須是 1 到 12" }

        val baseLevel = level.take(1).uppercase()

        val exercises = when {
            day > 5 -> emptyList()
            else -> when (baseLevel) {
                "A" -> getAExercises()

                "B" -> when {
                    isWheelDay(day) -> getBWheelExercises(week)
                    isWalkingOnlyDay(day) -> listOf(getBWalkingExercise(week))
                    else -> emptyList()
                }

                "C" -> when {
                    isWheelDay(day) -> getCWheelExercises(week)
                    isWalkingOnlyDay(day) -> listOf(getCWalkingExercise(week))
                    else -> emptyList()
                }

                "D" -> when {
                    isWheelDay(day) -> getDWheelExercises(week)
                    isWalkingOnlyDay(day) -> listOf(getDWalkingExercise(week))
                    else -> emptyList()
                }

                else -> emptyList()
            }
        }

        return DailyPlan(day, level, exercises)
    }

    private fun isWheelDay(day: Int): Boolean {
        return day == 1 || day == 3 || day == 5
    }

    private fun isWalkingOnlyDay(day: Int): Boolean {
        return day == 2 || day == 4
    }

    private fun getAExercises(): List<Exercise> {
        return listOf(
            Exercise("A1", "步行", "輕鬆", "5次以上", "5-10秒/次，逐漸增加至可連續走1-2分鐘", "視需要休息"),
            Exercise("A2", "擠壓球", "輕鬆", "3組", "12次", "休息1分鐘"),
            Exercise("A3", "舉水瓶", "中等", "3組", "12次", "休息1分鐘"),
            Exercise("A4", "腳踝負重腿部伸展", "中等", "3組", "12次", "休息1分鐘"),
            Exercise("A5", "在人員輔助下從椅子起身", "中等", "3組", "12次", "休息1分鐘"),
            Exercise("A6", "直線走路", "中等", "3組", "15步", "休息30秒"),
            Exercise("A7", "往上伸展手臂", "輕鬆", "3組", "3次(維持10-12秒)", "休息30秒")
        )
    }

    private fun getBWheelExercises(week: Int): List<Exercise> {
        return listOf(
            getBWalkingExercise(week),
            Exercise("B1", "舉水瓶", "中等", "3組", "12次", "休息1分鐘"),
            Exercise("B2", "擠壓球", "輕鬆", "3組", "12次", "休息1分鐘"),
            Exercise("B3", "模擬坐下動作", "中等", "3組", "12次", "休息1分鐘"),
            Exercise("B4", "用腳尖、腳跟走路", "中等", "3組", "14步", "休息1分鐘"),
            Exercise("B5", "椅上伸展手臂", "輕鬆", "3組", "3次(維持10-12秒)", "休息30秒"),
            Exercise("B6", "往上伸展手臂", "輕鬆", "3組", "3次(維持10-12秒)", "休息30秒"),

            )
    }

    private fun getBWalkingExercise(week: Int): Exercise {
        return if (week >= 7) {
            Exercise("B7", "步行", "中等", "3組", "8分鐘/組", "休息1分鐘，\n結束後慢走2分鐘緩和")
        } else {
            Exercise("B7", "步行", "輕鬆", "5組", "2-5分鐘/組", "休息1分鐘，\n結束後慢走2分鐘緩和")
        }
    }

    private fun getCWheelExercises(week: Int): List<Exercise> {
        return listOf(
            getCWalkingExercise(week),
            Exercise("C1", "扭毛巾", "中等", "3組", "12次(持續2-3秒)", "休息1分鐘"),
            Exercise("C2", "舉水瓶", "中等", "3組", "12次", "休息1分鐘"),
            Exercise("C3", "從椅子起身", "挑戰", "3組", "12次", "休息1分鐘"),
            Exercise("C4", "跨越障礙物", "挑戰", "8組", "每組跨越5次障礙物", "休息1分鐘"),
            Exercise("C5", "走 8 字步", "中等", "3組", "2圈", "休息1分鐘"),
            Exercise("C6", "腿部伸展", "輕鬆", "3組", "每腿6次(維持10-12秒)", "休息1分鐘"),
            Exercise("C7", "往上伸展手臂", "輕鬆", "3組", "3次(維持10-12秒)", "休息1分鐘")
        )
    }

    private fun getCWalkingExercise(week: Int): Exercise {
        return if (week >= 7) {
            Exercise("C8", "步行", "中等", "3組", "15分鐘/組", "休息1分鐘，結束後慢走2分鐘緩和")
        } else {
            Exercise("C8", "步行", "中等", "3組", "10分鐘/組", "休息1分鐘，結束後慢走2分鐘緩和")
        }
    }

    private fun getDWheelExercises(week: Int): List<Exercise> {
        return listOf(
            getDWalkingExercise(week),
            Exercise("D1", "扭毛巾", "中等", "3組", "12次(持續2-3秒)", "休息1分鐘"),
            Exercise("D2", "舉水瓶", "中等", "3組", "12次", "休息1分鐘"),
            Exercise("D3", "從椅子起身", "挑戰", "3組", "12次", "休息1分鐘"),
            Exercise("D4", "上下樓梯", "挑戰", "3組", "20步", "休息1分鐘"),
            Exercise("D5", "邊拍氣球邊走路", "挑戰", "3組", "10步", "休息30秒"),
            Exercise("D6", "走 8 字步", "挑戰", "3組", "2圈", "休息1分鐘"),
            Exercise("D7", "往上伸展手臂", "輕鬆", "3組", "3次(維持10-12秒)", "休息30秒"),
            Exercise("D8", "腿部伸展", "輕鬆", "3組", "每腿6次(維持10-12秒)", "休息30秒")
        )
    }

    private fun getDWalkingExercise(week: Int): Exercise {
        return if (week >= 7) {
            Exercise("D9", "步行", "挑戰", "不分組", "持續走30-45分鐘", "結束後慢走2分鐘緩和")
        } else {
            Exercise("D9", "步行", "挑戰", "2組", "20分鐘/組", "休息1分鐘，結束後慢走2分鐘緩和")
        }
    }
}
