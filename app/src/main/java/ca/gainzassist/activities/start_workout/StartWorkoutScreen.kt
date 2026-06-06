package ca.gainzassist.activities.start_workout

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import ca.gainzassist.R
import ca.gainzassist.activities.start_workout.components.ExerciseSetsListScreen
import ca.gainzassist.activities.start_workout.components.WarmupsListScreen
import ca.gainzassist.activities.start_workout.fragments.WorkoutScreen
import ca.gainzassist.components.GainzTabItem
import ca.gainzassist.components.GainzTabRow
import ca.gainzassist.models.Exercise

enum class StartWorkoutTab(val titleResId: Int, val iconResId: Int) {
    WARMUPS(R.string.warmups, R.drawable.ic_warmups),
    WORKOUT(R.string.workout, R.drawable.ic_workout),
    EXERCISES(R.string.exercises, R.drawable.ic_exercises)
}

data class StartWorkoutUiState(
    val selectedTab: StartWorkoutTab,
    val availableTabs: List<StartWorkoutTab>,
    val exercises: ArrayList<Exercise>,
    val warmups: ArrayList<Exercise>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StartWorkoutScreen(
    uiState: StartWorkoutUiState,
    onTabSelected: (StartWorkoutTab) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = uiState.availableTabs.indexOf(uiState.selectedTab).takeIf { it >= 0 } ?: 0,
        pageCount = { uiState.availableTabs.size }
    )

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            val newTab = uiState.availableTabs[pagerState.currentPage]
            if (newTab != uiState.selectedTab) {
                onTabSelected(newTab)
            }
        }
    }

    LaunchedEffect(uiState.selectedTab) {
        val targetPage = uiState.availableTabs.indexOf(uiState.selectedTab)
        if (targetPage >= 0 && pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            when (uiState.availableTabs[page]) {
                StartWorkoutTab.WARMUPS -> {
                    WarmupsListScreen(warmups = uiState.warmups)
                }
                StartWorkoutTab.WORKOUT -> {
                    WorkoutFragmentContainer()
                }
                StartWorkoutTab.EXERCISES -> {
                    ExerciseSetsListScreen(exercises = uiState.exercises)
                }
            }
        }

        val tabs = uiState.availableTabs.map { tab ->
            GainzTabItem(
                title = stringResource(id = tab.titleResId),
                iconResId = tab.iconResId
            )
        }
        
        GainzTabRow(
            pagerState = pagerState,
            tabs = tabs
        )
    }
}

@Composable
fun WorkoutFragmentContainer() {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            FragmentContainerView(ctx).apply {
                id = R.id.workout_fragment_container
            }
        },
        update = { view ->
            val activity = context as? AppCompatActivity
            if (activity != null) {
                val fragmentManager = activity.supportFragmentManager
                if (fragmentManager.findFragmentById(view.id) == null) {
                    fragmentManager.commit {
                        replace(view.id, WorkoutScreen.getInstance())
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun StartWorkoutScreenPreview_WithWarmups() {
    MaterialTheme {
        StartWorkoutScreen(
            uiState = StartWorkoutUiState(
                selectedTab = StartWorkoutTab.WORKOUT,
                availableTabs = listOf(StartWorkoutTab.WARMUPS, StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
                exercises = arrayListOf(),
                warmups = arrayListOf()
            ),
            onTabSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StartWorkoutScreenPreview_NoWarmups() {
    MaterialTheme {
        StartWorkoutScreen(
            uiState = StartWorkoutUiState(
                selectedTab = StartWorkoutTab.WORKOUT,
                availableTabs = listOf(StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
                exercises = arrayListOf(),
                warmups = arrayListOf()
            ),
            onTabSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StartWorkoutScreenPreview_ExercisesSelected() {
    MaterialTheme {
        StartWorkoutScreen(
            uiState = StartWorkoutUiState(
                selectedTab = StartWorkoutTab.EXERCISES,
                availableTabs = listOf(StartWorkoutTab.WARMUPS, StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
                exercises = arrayListOf(),
                warmups = arrayListOf()
            ),
            onTabSelected = {}
        )
    }
}

@PreviewScreenSizes
@Composable
fun StartWorkoutScreenPreview_SmallPhone() {
    MaterialTheme {
        StartWorkoutScreen(
            uiState = StartWorkoutUiState(
                selectedTab = StartWorkoutTab.WORKOUT,
                availableTabs = listOf(StartWorkoutTab.WARMUPS, StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
                exercises = arrayListOf(),
                warmups = arrayListOf()
            ),
            onTabSelected = {}
        )
    }
}

@PreviewFontScale
@Composable
fun StartWorkoutScreenPreview_LargeFont() {
    MaterialTheme {
        StartWorkoutScreen(
            uiState = StartWorkoutUiState(
                selectedTab = StartWorkoutTab.WORKOUT,
                availableTabs = listOf(StartWorkoutTab.WARMUPS, StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
                exercises = arrayListOf(),
                warmups = arrayListOf()
            ),
            onTabSelected = {}
        )
    }
}


