@file:Suppress("kotlin:S107", "kotlin:S109", "kotlin:S1192", "kotlin:S138", "kotlin:S3776", "kotlin:S112", "kotlin:S1874", "DEPRECATION", "HardCodedStringLiteral")
package ca.gainzassist.activities.start_workout.view

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
import ca.gainzassist.activities.start_workout.StartWorkoutViewModelState
import ca.gainzassist.activities.start_workout.view.components.ExerciseSetsListScreen
import ca.gainzassist.activities.start_workout.view.components.WarmupsListScreen
import ca.gainzassist.activities.start_workout.workout_screen.view.WorkoutFragment
import ca.gainzassist.ui.components.GainzTabItem
import ca.gainzassist.ui.components.GainzTabRow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StartWorkoutScreen(
    uiState: StartWorkoutViewModelState,
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
            beyondViewportPageCount = 1,
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
            val activity = context as? AppCompatActivity ?: return@AndroidView
            val fragmentManager = activity.supportFragmentManager
            val currentFragment = fragmentManager.findFragmentById(view.id)

            if (currentFragment == null || currentFragment.view == null || currentFragment.view?.parent == null) {
                fragmentManager.commit {
                    replace(view.id, WorkoutFragment.getInstance())
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun StartWorkoutScreenPreviewWithWarmups() {
    MaterialTheme {
        StartWorkoutScreen(
            uiState = StartWorkoutViewModelState(
                selectedTab = StartWorkoutTab.WORKOUT,
                availableTabs = listOf(StartWorkoutTab.WARMUPS, StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
                exercises = emptyList(),
                warmups = emptyList()
            ),
            onTabSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StartWorkoutScreenPreviewNoWarmups() {
    MaterialTheme {
        StartWorkoutScreen(
            uiState = StartWorkoutViewModelState(
                selectedTab = StartWorkoutTab.WORKOUT,
                availableTabs = listOf(StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
                exercises = emptyList(),
                warmups = emptyList()
            ),
            onTabSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StartWorkoutScreenPreviewExercisesSelected() {
    MaterialTheme {
        StartWorkoutScreen(
            uiState = StartWorkoutViewModelState(
                selectedTab = StartWorkoutTab.EXERCISES,
                availableTabs = listOf(StartWorkoutTab.WARMUPS, StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
                exercises = emptyList(),
                warmups = emptyList()
            ),
            onTabSelected = {}
        )
    }
}

@PreviewScreenSizes
@Composable
fun StartWorkoutScreenPreviewSmallPhone() {
    MaterialTheme {
        StartWorkoutScreen(
            uiState = StartWorkoutViewModelState(
                selectedTab = StartWorkoutTab.WORKOUT,
                availableTabs = listOf(StartWorkoutTab.WARMUPS, StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
                exercises = emptyList(),
                warmups = emptyList()
            ),
            onTabSelected = {}
        )
    }
}

@PreviewFontScale
@Composable
fun StartWorkoutScreenPreviewLargeFont() {
    MaterialTheme {
        StartWorkoutScreen(
            uiState = StartWorkoutViewModelState(
                selectedTab = StartWorkoutTab.WORKOUT,
                availableTabs = listOf(StartWorkoutTab.WARMUPS, StartWorkoutTab.WORKOUT, StartWorkoutTab.EXERCISES),
                exercises = emptyList(),
                warmups = emptyList()
            ),
            onTabSelected = {}
        )
    }
}


