@file:OptIn(ExperimentalFoundationApi::class)

package ca.gainzassist.feature.main.presentation.screen
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ca.gainzassist.ui.components.GainzTabItem
import ca.gainzassist.ui.components.GainzTabRow

data class MainUiState(
    val selectedTab: MainTab = MainTab.WORKOUTS,
    val resumeWorkoutNames: List<String> = emptyList(),
    val workoutNames: List<String> = emptyList(),
    val selectedWorkoutName: String? = null,
    val settingsUiState: SettingsUiState = SettingsUiState(signedInText = "", versionText = "")
)

data class MainScreenActions(
    val onTabSelected: (MainTab) -> Unit = {},
    val onResumeWorkoutClick: (String) -> Unit = {},
    val onWorkoutClick: (String) -> Unit = {},
    val onWorkoutLongClick: (String) -> Unit = {},
    val onDismissWorkoutDialog: () -> Unit = {},
    val onEditWorkout: (String) -> Unit = {},
    val onDeleteWorkout: (String) -> Unit = {},
    val onSettingsSignOutClick: () -> Unit = {},
    val onPrivacyPolicyClick: () -> Unit = {},
    val onAccountDeletionClick: () -> Unit = {},
    val onContactSupportClick: () -> Unit = {}
)

@Composable
fun MainScreen(
    uiState: MainUiState,
    actions: MainScreenActions = MainScreenActions()
) {
    Column(modifier = Modifier.fillMaxSize()) {
        val pagerState = rememberPagerState(
            initialPage = uiState.selectedTab.ordinal,
            pageCount = { MainTab.entries.size }
        )

        LaunchedEffect(uiState.selectedTab) {
            pagerState.animateScrollToPage(uiState.selectedTab.ordinal)
        }

        LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
            if (!pagerState.isScrollInProgress && uiState.selectedTab.ordinal != pagerState.currentPage) {
                actions.onTabSelected(MainTab.entries[pagerState.currentPage])
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (MainTab.entries[page]) {
                    MainTab.RESUME -> {
                        ResumeScreen(
                            uiState = ResumeUiState(workoutNames = uiState.resumeWorkoutNames),
                            onWorkoutClick = actions.onResumeWorkoutClick
                        )
                    }

                    MainTab.WORKOUTS -> {
                        WorkoutsScreen(
                            workoutNames = uiState.workoutNames,
                            selectedWorkoutName = uiState.selectedWorkoutName,
                            actions = WorkoutsScreenActions(
                                onWorkoutClick = actions.onWorkoutClick,
                                onWorkoutLongClick = actions.onWorkoutLongClick,
                                onDismissDialog = actions.onDismissWorkoutDialog,
                                onEditWorkout = actions.onEditWorkout,
                                onDeleteWorkout = actions.onDeleteWorkout
                            )
                        )
                    }
                    MainTab.SETTINGS -> {
                        SettingsScreen(
                            uiState = uiState.settingsUiState,
                            actions = SettingsScreenActions(
                                onSignOutClick = actions.onSettingsSignOutClick,
                                onPrivacyPolicyClick = actions.onPrivacyPolicyClick,
                                onAccountDeletionClick = actions.onAccountDeletionClick,
                                onContactSupportClick = actions.onContactSupportClick
                            )
                        )
                    }
                }
            }
        }

        val tabs = MainTab.entries.map { tab ->
            GainzTabItem(title = tab.title)
        }

        GainzTabRow(
            pagerState = pagerState,
            tabs = tabs
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreviewWorkouts() {
    MainScreen(
        uiState = MainUiState(
            selectedTab = MainTab.WORKOUTS,
            workoutNames = listOf("Chest", "Back", "Legs")
        )
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreviewResume() {
    MainScreen(
        uiState = MainUiState(
            selectedTab = MainTab.RESUME,
            resumeWorkoutNames = listOf("Chest Day (In Progress)")
        )
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreviewSettings() {
    MainScreen(
        uiState = MainUiState(
            selectedTab = MainTab.SETTINGS,
            settingsUiState = SettingsUiState(
                signedInText = "test@example.com",
                versionText = "1.0.0"
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreviewWorkoutDialog() {
    MainScreen(
        uiState = MainUiState(
            selectedTab = MainTab.WORKOUTS,
            workoutNames = listOf("Chest", "Back", "Legs"),
            selectedWorkoutName = "Back"
        )
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreviewEmpty() {
    MainScreen(
        uiState = MainUiState(
            selectedTab = MainTab.WORKOUTS,
            workoutNames = emptyList()
        )
    )
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun MainScreenPreviewSmallPhone() {
    MainScreen(
        uiState = MainUiState(
            selectedTab = MainTab.WORKOUTS,
            workoutNames = listOf("Chest", "Back")
        )
    )
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
fun MainScreenPreviewLargeFont() {
    MainScreen(
        uiState = MainUiState(
            selectedTab = MainTab.WORKOUTS,
            workoutNames = listOf("Chest", "Back")
        )
    )
}
