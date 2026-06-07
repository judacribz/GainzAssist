package ca.gainzassist.activities.start_workout

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import ca.gainzassist.R
import ca.gainzassist.activities.how_to_videos.HowToVideos
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.Workout
import ca.gainzassist.ui.components.GainzTopBar
import ca.gainzassist.util.Misc.readValue
import ca.gainzassist.util.UI.setInitTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import ca.gainzassist.presentation.start_workout.StartWorkoutRestoreDecision
import ca.gainzassist.presentation.start_workout.StartWorkoutViewModel
import ca.gainzassist.presentation.start_workout.StartWorkoutViewModelEvent
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.parceler.Parcels

class StartWorkout : AppCompatActivity(), CurrWorkout.WarmupsListener {

    companion object {
        const val EXTRA_HOW_TO_VID = "ca.gainzassist.activities.start_workout.EXTRA_HOW_TO_VID"
    }

    private val viewModel: StartWorkoutViewModel by viewModel()
    private val currWorkout = CurrWorkout.getInstance()
    var workout: Workout? = null
    var exercises: ArrayList<Exercise>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        workout = Parcels.unwrap(intent.getParcelableExtra(ca.gainzassist.activities.main.Main.EXTRA_WORKOUT))
        val currentWorkout = workout ?: return
        exercises = currentWorkout.exercises
        
        viewModel.initializeFromWorkout(currentWorkout)

        setInitTheme(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is StartWorkoutViewModelEvent.OpenHowToVideos -> {
                            val vidIntent = Intent(this@StartWorkout, HowToVideos::class.java)
                            vidIntent.putExtra(EXTRA_HOW_TO_VID, currWorkout.currExName)
                            startActivity(vidIntent)
                        }
                        is StartWorkoutViewModelEvent.ExitWorkout -> {
                            onBackPressed()
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

        setContent {
            val uiState by viewModel.state.collectAsStateWithLifecycle()

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

                StartWorkoutScreen(
                    uiState = uiState,
                    onTabSelected = { tab ->
                        viewModel.onTabSelected(tab)
                    }
                )
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
    
    private var sessionSet = false

    fun setCurrSession() {
        currWorkout.setDataListener(this)

        val currentWorkout = workout ?: return
        val workoutName = currentWorkout.name ?: return

        lifecycleScope.launch {
            val decision = viewModel.prepareSessionRestore(workoutName)
            when (decision) {
                is StartWorkoutRestoreDecision.StartFresh -> {
                    currWorkout.setCurrWorkout(currentWorkout)
                }
                is StartWorkoutRestoreDecision.RestoreFromJson -> {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        currWorkout.setRetrievedWorkout(
                            readValue(decision.sessionJson) as Map<String, Any?>,
                            currentWorkout
                        )
                    } catch (ex: Exception) {
                        com.orhanobut.logger.Logger.e(ex, "Failed to restore incomplete workout. Starting fresh.")
                        currWorkout.setCurrWorkout(currentWorkout)
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        currWorkout.resetIndices()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        handleLeavingScreen()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        currWorkout.unsetTimer()
        handleLeavingScreen()
    }

    fun handleLeavingScreen() {
        val currentWorkout = workout ?: return
        val workoutName = currentWorkout.name ?: return

        val jsonStr = currWorkout.saveSessionState()
        viewModel.saveLeavingSession(workoutName, jsonStr)
        currWorkout.resetLocks()
    }



    override fun warmupsGenerated(warmups: ArrayList<Exercise>) {
        viewModel.onWarmupsGenerated(warmups)
    }
}
