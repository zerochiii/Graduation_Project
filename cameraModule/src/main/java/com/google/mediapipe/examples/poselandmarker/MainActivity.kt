/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.mediapipe.examples.poselandmarker

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.mediapipe.examples.poselandmarker.databinding.ActivityMainBinding
import com.google.mediapipe.examples.poselandmarker.fragment.PermissionsFragment
import com.google.mediapipe.examples.poselandmarker.fragment.WalkingBWeek7Fragment
import com.google.mediapipe.examples.poselandmarker.fragment.WalkingCWeek7Fragment
import com.google.mediapipe.examples.poselandmarker.fragment.WalkingDWeek7Fragment

internal fun resultContainsExpectedCode(resultCodes: String, expectedCode: String?): Boolean =
    expectedCode != null && resultCodes.split(",").map(String::trim).contains(expectedCode)

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TARGET_FRAGMENT = "extra_target_fragment"

        const val EXTRA_ACCOUNT_ID = "extra_account_id"
        const val EXTRA_USER_LEVEL = "extra_user_level"
        const val EXTRA_REQUESTED_EXERCISE_ID = "extra_requested_exercise_id"
        const val EXTRA_EXPECTED_RESULT_CODE = "extra_expected_result_code"
        const val EXTRA_TRAINING_ATTEMPT_ID = "extra_training_attempt_id"
        const val EXTRA_TRAINING_COMPLETED = "extra_training_completed"
        const val EXTRA_TRAINING_WEEK = "extra_training_week"

        fun resolveTargetDestination(targetFragment: String): Int? {
            return when (targetFragment) {
                "camera_fragment" -> R.id.camera_fragment
                "stretch_fragment" -> R.id.stretch_fragment
                "chair_stand_fragment" -> R.id.chair_stand_fragment
                "walking_fragment" -> R.id.walking_fragment
                "walking_b_fragment" -> R.id.walking_b_fragment
                "walking_c_fragment" -> R.id.walking_c_fragment
                "walking_d_fragment" -> R.id.walking_d_fragment
                "walking_b_week7_fragment" -> R.id.walking_b_fragment
                "walking_c_week7_fragment" -> R.id.walking_c_fragment
                "walking_d_week7_fragment" -> R.id.walking_d_fragment
                "simulated_sitting_fragment" -> R.id.simulated_sitting_fragment
                "toe_heel_walking_fragment" -> R.id.toe_heel_walking_fragment
                "chair_arm_stretch_fragment" -> R.id.chair_arm_stretch_fragment
                "obstacle_crossing_fragment" -> R.id.obstacle_crossing_fragment
                "bottle_lift_fragment" -> R.id.bottle_lift_fragment
                "squeeze_ball_fragment" -> R.id.squeeze_ball_fragment
                "wring_towel_fragment" -> R.id.wring_towel_fragment
                "side_by_side_fragment" -> R.id.side_by_side_fragment
                "semi_tandem_fragment" -> R.id.semi_tandem_fragment
                "tandem_fragment" -> R.id.tandem_fragment
                "figure8_walking_fragment" -> R.id.figure8_walking_fragment
                "leg_stretch_fragment" -> R.id.leg_stretch_fragment
                "weighted_leg_stretch_fragment" -> R.id.weighted_leg_stretch_fragment
                "stair_climbing_fragment" -> R.id.stair_climbing_fragment
                "balloon_walking_fragment" -> R.id.balloon_walking_fragment
                else -> null
            }
        }
    }
    private lateinit var activityMainBinding: ActivityMainBinding
    private val viewModel : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val accountId = intent.getIntExtra(EXTRA_ACCOUNT_ID, -1)
        val userLevel = intent.getStringExtra(EXTRA_USER_LEVEL) ?: ""
        setResult(Activity.RESULT_CANCELED, createTrainingResultIntent(completed = false))
        viewModel.setAccountInfo(
            accountId,
            userLevel,
            intent.getStringExtra(EXTRA_EXPECTED_RESULT_CODE)
        )

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        val targetFragment = intent.getStringExtra(EXTRA_TARGET_FRAGMENT)

        // new
        val startDestination = targetFragment?.let(::resolveTargetDestination)
            ?.takeIf { PermissionsFragment.hasPermissions(this) }
            ?: R.id.permissions_fragment

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        val advancedFragment = when (targetFragment) {
            "walking_b_week7_fragment" -> WalkingBWeek7Fragment::class.java.name
            "walking_c_week7_fragment" -> WalkingCWeek7Fragment::class.java.name
            "walking_d_week7_fragment" -> WalkingDWeek7Fragment::class.java.name
            else -> null
        }
        if (advancedFragment != null) {
            val destination = targetFragment?.let(::resolveTargetDestination)
            (destination?.let(navGraph::findNode) as? FragmentNavigator.Destination)
                ?.setClassName(advancedFragment)
        }
        navGraph.setStartDestination(startDestination)
        navController.graph = navGraph

        activityMainBinding.navigation.setupWithNavController(navController)
        activityMainBinding.navigation.setOnNavigationItemReselectedListener {
            // ignore the reselection
        }
        viewModel.lastResult.observe(this) { result ->
            val expectedResultCode = intent.getStringExtra(EXTRA_EXPECTED_RESULT_CODE)
            if (resultContainsExpectedCode(result.exerciseId, expectedResultCode)) {
                setResult(Activity.RESULT_OK, createTrainingResultIntent(completed = true))
            }
            android.widget.Toast.makeText(
                this,
                "運動完成：${result.exerciseName}\n準確度：${String.format("%.1f", result.accuracy)}%",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }

        viewModel.saveStatus.observe(this) { status ->
            if (status == SaveStatus.FAILED) {
                android.widget.Toast.makeText(
                    this,
                    "訓練成績同步失敗，請確認網路連線",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun createTrainingResultIntent(completed: Boolean) = Intent().apply {
        putExtra(EXTRA_REQUESTED_EXERCISE_ID, intent.getStringExtra(EXTRA_REQUESTED_EXERCISE_ID))
        putExtra(EXTRA_TRAINING_ATTEMPT_ID, intent.getLongExtra(EXTRA_TRAINING_ATTEMPT_ID, -1L))
        putExtra(EXTRA_TRAINING_COMPLETED, completed)
    }
}
