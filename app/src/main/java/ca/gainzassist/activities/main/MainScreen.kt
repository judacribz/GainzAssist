@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package ca.gainzassist.activities.main
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ca.gainzassist.activities.main.fragments.resume.ResumeScreen
import ca.gainzassist.activities.main.fragments.settings.SettingsScreen
import ca.gainzassist.activities.main.fragments.settings.SettingsUiState
import ca.gainzassist.activities.main.fragments.workouts.WorkoutsScreen
import ca.gainzassist.components.GainzTabItem
import ca.gainzassist.components.GainzTabRow


enum class MainTab(val title: String) {
    RESUME("RESUME"),
    WORKOUTS("WORKOUTS"),
    SETTINGS("SETTINGS")
}

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
        val pagerState = androidx.compose.foundation.pager.rememberPagerState(
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
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (MainTab.entries[page]) {
                    MainTab.RESUME -> {
                        ResumeScreen(
                            uiState = ca.gainzassist.activities.main.fragments.resume.ResumeUiState(workoutNames = uiState.resumeWorkoutNames),
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
fun MainScreenPreview_Workouts() {
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
fun MainScreenPreview_Resume() {
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
fun MainScreenPreview_Settings() {
    MainScreen(
        uiState = MainUiState(
            selectedTab = MainTab.SETTINGS,
            settingsUiState = SettingsUiState(signedInText = "test@example.com", versionText = "1.0.0")
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
fun MainScreenPreview_WorkoutDialog() {
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
fun MainScreenPreview_Empty() {
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
fun MainScreenPreview_SmallPhone() {
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
fun MainScreenPreview_LargeFont() {
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



