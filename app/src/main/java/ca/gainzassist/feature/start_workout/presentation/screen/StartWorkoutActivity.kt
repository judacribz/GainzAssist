package ca.gainzassist.feature.start_workout.presentation.screen

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.gainzassist.R
import ca.gainzassist.activities.base.GainzBaseActivity
import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.model.Workout
import ca.gainzassist.feature.how_to_videos.presentation.screen.HowToVideosActivity
import ca.gainzassist.feature.main.presentation.screen.MainActivity
import ca.gainzassist.feature.start_workout.presentation.viewmodel.StartWorkoutViewModel
import ca.gainzassist.feature.start_workout.presentation.viewmodel.StartWorkoutViewModelEvent
import ca.gainzassist.feature.start_workout.presentation.viewmodel.WorkoutController
import ca.gainzassist.ui.components.GainzTopBar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class StartWorkoutActivity : GainzBaseActivity() {

    private val viewModel: StartWorkoutViewModel by viewModel()
    private val workoutController = WorkoutController
    private var sessionSet = false
    var workout: Workout? = null
    var exercises: ArrayList<Exercise>? = null

    override fun onBeforeSetContent() {
        val intent = intent
        val w = IntentCompat.getParcelableExtra(
            /* in = */ intent,
            /* name = */ MainActivity.EXTRA_WORKOUT,
            /* clazz = */ Workout::class.java
        )
        workout = w
        val currentWorkout = w ?: return
        exercises = currentWorkout.exercises
        viewModel.initializeFromWorkout(currentWorkout)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(enabled = true) {
                override fun handleOnBackPressed() {
                    handleLeavingScreen()
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is StartWorkoutViewModelEvent.OpenHowToVideos -> {
                                val vidIntent =
                                    Intent(
                                        this@StartWorkoutActivity,
                                        HowToVideosActivity::class.java
                                    )
                                vidIntent.putExtra(EXTRA_HOW_TO_VID, workoutController.currExName)
                                startActivity(vidIntent)
                            }

                            is StartWorkoutViewModelEvent.ExitWorkout -> {
                                onBackPressedDispatcher.onBackPressed()
                            }

                            is StartWorkoutViewModelEvent.FinishWorkout -> {
                                // Handled later
                            }

                            is StartWorkoutViewModelEvent.Error -> {
                                // Handle error
                            }
                        }
                    }
                }
                launch {
                    workoutController.events.collect { event ->
                        when (event) {
                            is WorkoutController.WorkoutControllerEvent.WarmupsGenerated -> {
                                viewModel.onWarmupsGenerated(event.warmups)
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }

    @Composable
    override fun InnerContent() {
        val uiState by viewModel.state.collectAsStateWithLifecycle()
        val currentWorkout = workout ?: return
        Column(Modifier.fillMaxSize()) {
            GainzTopBar(
                title = currentWorkout.name.orEmpty(),
                showBack = true,
                onBackClick = { viewModel.onBackClicked() },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.onHowToVideosClicked()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.mipmap.ic_youtube_btn_fg),
                            contentDescription = "How To Videos",
                            tint = Color.Unspecified
                        )
                    }
                }
            )

            if (uiState.isSessionReady) {
                StartWorkoutScreen(
                    uiState = uiState,
                    onTabSelected = { viewModel.onTabSelected(it) }
                )
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Initialize session if not already set. We check if uiState's tabs match a fresh load.
        // But since we removed the pager adapter check, we can use a boolean flag.
        val currentState = viewModel.state.value
        if ((currentState.warmups.isEmpty() && currentState.availableTabs.size == 2) && !sessionSet) {
            setCurrSession()
            sessionSet = true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        workoutController.resetIndices()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        handleLeavingScreen()
    }

    fun setCurrSession() {
        val currentWorkout = workout ?: return
        viewModel.prepareSessionRestore(currentWorkout)
    }

    fun handleLeavingScreen() {
        val currentWorkout = workout ?: return
        val workoutName = currentWorkout.name ?: return
        val jsonStr = workoutController.saveSessionState()
        viewModel.saveLeavingSession(workoutName, jsonStr)
        workoutController.resetLocks()
    }

    companion object {
        const val EXTRA_HOW_TO_VID = "ca.gainzassist.activities.start_workout.EXTRA_HOW_TO_VID"
    }
}
