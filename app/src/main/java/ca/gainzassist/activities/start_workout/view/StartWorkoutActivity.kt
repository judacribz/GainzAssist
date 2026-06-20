package ca.gainzassist.activities.start_workout.view

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.gainzassist.R
import ca.gainzassist.activities.base.GainzBaseActivity
import ca.gainzassist.activities.how_to_videos.view.HowToVideosActivity
import ca.gainzassist.activities.main.view.MainActivity
import ca.gainzassist.activities.start_workout.StartWorkoutRestoreDecision
import ca.gainzassist.activities.start_workout.StartWorkoutViewModel
import ca.gainzassist.activities.start_workout.StartWorkoutViewModelEvent
import ca.gainzassist.activities.start_workout.WorkoutController
import ca.gainzassist.core.util.Misc
import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.model.Workout
import ca.gainzassist.ui.components.GainzTopBar
import com.orhanobut.logger.Logger
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class StartWorkoutActivity : GainzBaseActivity(), WorkoutController.WarmupsListener {

    private val viewModel: StartWorkoutViewModel by viewModel()
    private val workoutController = WorkoutController
    private var sessionSet = false
    var workout: Workout? = null
    var exercises: ArrayList<Exercise>? = null

    override fun onBeforeSetContent() {
        val intent = intent
        val w = Parcels.unwrap<Workout>(intent.getParcelableExtra(MainActivity.EXTRA_WORKOUT))
        workout = w
        val currentWorkout = w ?: return
        exercises = currentWorkout.exercises
        viewModel.initializeFromWorkout(currentWorkout)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                workoutController.unsetTimer()
                handleLeavingScreen()
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is StartWorkoutViewModelEvent.OpenHowToVideos -> {
                            val vidIntent =
                                Intent(this@StartWorkoutActivity, HowToVideosActivity::class.java)
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
                    onTabSelected = { tab ->
                        viewModel.onTabSelected(tab)
                    }
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
        if (currentState.warmups.isEmpty() && currentState.availableTabs.size == 2 && !sessionSet) {
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

    override fun warmupsGenerated(
        warmups: ArrayList<Exercise>
    ) = viewModel.onWarmupsGenerated(warmups)

    fun setCurrSession() {
        workoutController.setDataListener(this)
        val currentWorkout = workout ?: return
        val workoutName = currentWorkout.name ?: return
        lifecycleScope.launch {
            when (val decision = viewModel.prepareSessionRestore(workoutName)) {
                is StartWorkoutRestoreDecision.StartFresh -> {
                    workoutController.setCurrWorkout(currentWorkout)
                }

                is StartWorkoutRestoreDecision.RestoreFromJson -> {
                    try {
                        workoutController.setRetrievedWorkout(
                            Misc.readValue(decision.sessionJson),
                            currentWorkout
                        )
                    } catch (ex: Exception) {
                        Logger.e(ex, "Failed to restore incomplete workout. Starting fresh.")
                        workoutController.setCurrWorkout(currentWorkout)
                    }
                }
            }
            viewModel.onSessionReady()
        }
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
