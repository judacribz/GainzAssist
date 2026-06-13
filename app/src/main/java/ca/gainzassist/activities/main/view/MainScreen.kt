@file:Suppress("kotlin:S107", "kotlin:S109", "kotlin:S1192", "kotlin:S138", "kotlin:S3776", "kotlin:S112", "kotlin:S1874", "DEPRECATION", "HardCodedStringLiteral")
@file:OptIn(ExperimentalFoundationApi::class)

package ca.gainzassist.activities.main.view
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
import ca.gainzassist.activities.main.view.tab_screens.ResumeScreen
import ca.gainzassist.activities.main.view.tab_screens.ResumeUiState
import ca.gainzassist.activities.main.view.tab_screens.SettingsScreen
import ca.gainzassist.activities.main.view.tab_screens.SettingsUiState
import ca.gainzassist.activities.main.view.tab_screens.WorkoutsScreen
import ca.gainzassist.ui.components.GainzTabItem
import ca.gainzassist.ui.components.GainzTabRow

data class MainUiState(
    val selectedTab: MainTab = MainTab.WORKOUTS,
    val resumeWorkoutNames: List<String> = emptyList(),
    val workoutNames: List<String> = emptyList(),
    val selectedWorkoutName: String? = null,
    val settingsUiState: SettingsUiState = SettingsUiState(signedInText = "", versionText = "")
)

@Composable
fun MainScreen(
    uiState: MainUiState,
    onTabSelected: (MainTab) -> Unit,
    onResumeWorkoutClick: (String) -> Unit,
    onWorkoutClick: (String) -> Unit,
    onWorkoutLongClick: (String) -> Unit,
    onDismissWorkoutDialog: () -> Unit,
    onEditWorkout: (String) -> Unit,
    onDeleteWorkout: (String) -> Unit,
    onSettingsSignOutClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onAccountDeletionClick: () -> Unit,
    onContactSupportClick: () -> Unit
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
                onTabSelected(MainTab.entries[pagerState.currentPage])
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
                            onWorkoutClick = onResumeWorkoutClick
                        )
                    }
                    MainTab.WORKOUTS -> {
                        WorkoutsScreen(
                            workoutNames = uiState.workoutNames,
                            selectedWorkoutName = uiState.selectedWorkoutName,
                            onWorkoutClick = onWorkoutClick,
                            onWorkoutLongClick = onWorkoutLongClick,
                            onDismissDialog = onDismissWorkoutDialog,
                            onEditWorkout = onEditWorkout,
                            onDeleteWorkout = onDeleteWorkout
                        )
                    }
                    MainTab.SETTINGS -> {
                        SettingsScreen(
                            uiState = uiState.settingsUiState,
                            onSignOutClick = onSettingsSignOutClick,
                            onPrivacyPolicyClick = onPrivacyPolicyClick,
                            onAccountDeletionClick = onAccountDeletionClick,
                            onContactSupportClick = onContactSupportClick
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
        ),
        onTabSelected = {},
        onResumeWorkoutClick = {},
        onWorkoutClick = {},
        onWorkoutLongClick = {},
        onDismissWorkoutDialog = {},
        onEditWorkout = {},
        onDeleteWorkout = {},
        onSettingsSignOutClick = {},
        onPrivacyPolicyClick = {},
        onAccountDeletionClick = {},
        onContactSupportClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreviewResume() {
    MainScreen(
        uiState = MainUiState(
            selectedTab = MainTab.RESUME,
            resumeWorkoutNames = listOf("Chest Day (In Progress)")
        ),
        onTabSelected = {},
        onResumeWorkoutClick = {},
        onWorkoutClick = {},
        onWorkoutLongClick = {},
        onDismissWorkoutDialog = {},
        onEditWorkout = {},
        onDeleteWorkout = {},
        onSettingsSignOutClick = {},
        onPrivacyPolicyClick = {},
        onAccountDeletionClick = {},
        onContactSupportClick = {}
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
        ),
        onTabSelected = {},
        onResumeWorkoutClick = {},
        onWorkoutClick = {},
        onWorkoutLongClick = {},
        onDismissWorkoutDialog = {},
        onEditWorkout = {},
        onDeleteWorkout = {},
        onSettingsSignOutClick = {},
        onPrivacyPolicyClick = {},
        onAccountDeletionClick = {},
        onContactSupportClick = {}
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
        ),
        onTabSelected = {},
        onResumeWorkoutClick = {},
        onWorkoutClick = {},
        onWorkoutLongClick = {},
        onDismissWorkoutDialog = {},
        onEditWorkout = {},
        onDeleteWorkout = {},
        onSettingsSignOutClick = {},
        onPrivacyPolicyClick = {},
        onAccountDeletionClick = {},
        onContactSupportClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreviewEmpty() {
    MainScreen(
        uiState = MainUiState(
            selectedTab = MainTab.WORKOUTS,
            workoutNames = emptyList()
        ),
        onTabSelected = {},
        onResumeWorkoutClick = {},
        onWorkoutClick = {},
        onWorkoutLongClick = {},
        onDismissWorkoutDialog = {},
        onEditWorkout = {},
        onDeleteWorkout = {},
        onSettingsSignOutClick = {},
        onPrivacyPolicyClick = {},
        onAccountDeletionClick = {},
        onContactSupportClick = {}
    )
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun MainScreenPreviewSmallPhone() {
    MainScreen(
        uiState = MainUiState(
            selectedTab = MainTab.WORKOUTS,
            workoutNames = listOf("Chest", "Back")
        ),
        onTabSelected = {},
        onResumeWorkoutClick = {},
        onWorkoutClick = {},
        onWorkoutLongClick = {},
        onDismissWorkoutDialog = {},
        onEditWorkout = {},
        onDeleteWorkout = {},
        onSettingsSignOutClick = {},
        onPrivacyPolicyClick = {},
        onAccountDeletionClick = {},
        onContactSupportClick = {}
    )
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
fun MainScreenPreviewLargeFont() {
    MainScreen(
        uiState = MainUiState(
            selectedTab = MainTab.WORKOUTS,
            workoutNames = listOf("Chest", "Back")
        ),
        onTabSelected = {},
        onResumeWorkoutClick = {},
        onWorkoutClick = {},
        onWorkoutLongClick = {},
        onDismissWorkoutDialog = {},
        onEditWorkout = {},
        onDeleteWorkout = {},
        onSettingsSignOutClick = {},
        onPrivacyPolicyClick = {},
        onAccountDeletionClick = {},
        onContactSupportClick = {}
    )
}



