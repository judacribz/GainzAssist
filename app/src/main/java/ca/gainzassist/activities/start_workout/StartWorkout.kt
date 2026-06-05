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
import ca.gainzassist.util.Preferences.addIncompleteSessionPref
import ca.gainzassist.util.Preferences.addIncompleteWorkoutPref
import ca.gainzassist.util.Preferences.getIncompleteSessionPref
import ca.gainzassist.util.Preferences.removeIncompleteSessionPref
import ca.gainzassist.util.Preferences.removeIncompleteWorkoutPref
import ca.gainzassist.util.UI.setInitTheme
import org.parceler.Parcels

class StartWorkout : AppCompatActivity(), CurrWorkout.WarmupsListener {

    companion object {
        const val EXTRA_HOW_TO_VID = "ca.gainzassist.activities.start_workout.EXTRA_HOW_TO_VID"
    }

    private val currWorkout = CurrWorkout.getInstance()
    var workout: Workout? = null
    var exercises: ArrayList<Exercise>? = null

    private var uiState by mutableStateOf(
        StartWorkoutUiState(
            selectedTab = StartWorkoutTab.WORKOUT,
            availableTabs = listOf(StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
            exercises = arrayListOf(),
            warmups = arrayListOf()
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        workout = Parcels.unwrap(intent.getParcelableExtra(ca.gainzassist.activities.main.Main.EXTRA_WORKOUT))
        val currentWorkout = workout ?: return
        exercises = currentWorkout.exercises
        
        uiState = uiState.copy(exercises = exercises ?: arrayListOf())

        setInitTheme(this)

        setContent {
            Column(Modifier.fillMaxSize()) {
                GainzTopBar(
                    title = currentWorkout.name.orEmpty(),
                    showBack = true,
                    onBackClick = { onBackPressedDispatcher.onBackPressed() },
                    actions = {
                        IconButton(
                            onClick = {
                                val intent = Intent(this@StartWorkout, HowToVideos::class.java)
                                intent.putExtra(EXTRA_HOW_TO_VID, currWorkout.currExName)
                                startActivity(intent)
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.selector_youtube_btn),
                                contentDescription = "How To Videos",
                                tint = Color.Unspecified
                            )
                        }
                    }
                )

                StartWorkoutScreen(
                    uiState = uiState,
                    onTabSelected = { tab ->
                        uiState = uiState.copy(selectedTab = tab)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Initialize session if not already set. We check if uiState's tabs match a fresh load.
        // But since we removed the pager adapter check, we can use a boolean flag.
        if (uiState.warmups.isEmpty() && uiState.availableTabs.size == 2 && !sessionSet) {
            setCurrSession()
            sessionSet = true
        }
    }
    
    private var sessionSet = false

    fun setCurrSession() {
        currWorkout.setDataListener(this)

        val currentWorkout = workout ?: return
        val workoutName = currentWorkout.name ?: return

        if (removeIncompleteWorkoutPref(this, workoutName)) {
            try {
                val savedSession = getIncompleteSessionPref(this, workoutName)

                if (savedSession.isNullOrEmpty()) {
                    removeIncompleteSessionPref(this, workoutName)
                    currWorkout.setCurrWorkout(currentWorkout)
                    return
                }

                @Suppress("UNCHECKED_CAST")
                currWorkout.setRetrievedWorkout(
                    readValue(savedSession) as Map<String, Any?>,
                    currentWorkout
                )

                removeIncompleteSessionPref(this, workoutName)
            } catch (ex: Exception) {
                com.orhanobut.logger.Logger.e(ex, "Failed to restore incomplete workout. Starting fresh.")
                removeIncompleteSessionPref(this, workoutName)
                currWorkout.setCurrWorkout(currentWorkout)
            }
        } else {
            currWorkout.setCurrWorkout(currentWorkout)
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
        if (jsonStr.isNotEmpty()) {
            addIncompleteSessionPref(
                this,
                workoutName,
                jsonStr
            )
        }
        addIncompleteWorkoutPref(this, workoutName)
        currWorkout.resetLocks()
    }



    override fun warmupsGenerated(warmups: ArrayList<Exercise>) {
        uiState = if (warmups.isEmpty()) {
            uiState.copy(
                availableTabs = listOf(StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
                selectedTab = StartWorkoutTab.WORKOUT,
                warmups = warmups
            )
        } else {
            uiState.copy(
                availableTabs = listOf(StartWorkoutTab.WARMUPS, StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
                selectedTab = StartWorkoutTab.WORKOUT,
                warmups = warmups
            )
        }
    }
}
