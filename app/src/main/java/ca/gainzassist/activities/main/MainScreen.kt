@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package ca.gainzassist.activities.main
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import ca.gainzassist.R
import ca.gainzassist.activities.main.fragments.resume.ResumeScreen
import ca.gainzassist.activities.main.fragments.settings.SettingsScreen
import ca.gainzassist.activities.main.fragments.settings.SettingsUiState
import ca.gainzassist.activities.main.fragments.workouts.WorkoutsScreen
import kotlinx.coroutines.launch
import kotlin.math.abs
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.indication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.remember
import androidx.compose.foundation.LocalIndication

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
        val coroutineScope = rememberCoroutineScope()

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
        
        TabRow(
            selectedTabIndex = uiState.selectedTab.ordinal,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorResource(id = R.color.blue),
                            colorResource(id = R.color.colorBg),
                            colorResource(id = R.color.colorBg)
                        )
                    )
                ),
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                val page = pagerState.currentPage
                val fraction = pagerState.currentPageOffsetFraction
                val targetPage = if (fraction > 0) page + 1 else page - 1

                val currentTab = tabPositions.getOrNull(page)
                val targetTab = tabPositions.getOrNull(targetPage)

                if (currentTab != null) {
                    val indicatorWidth = if (targetTab != null) {
                        lerp(currentTab.width, targetTab.width, abs(fraction))
                    } else currentTab.width

                    val indicatorOffset = if (targetTab != null) {
                        lerp(currentTab.left, targetTab.left, abs(fraction))
                    } else currentTab.left

                    TabRowDefaults.Indicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentSize(Alignment.BottomStart)
                            .offset(x = indicatorOffset)
                            .width(indicatorWidth),
                        color = colorResource(id = R.color.blue)
                    )
                }
            },
            divider = {
                androidx.compose.material3.HorizontalDivider(color = Color.Black)
            }
        ) {
            MainTab.entries.forEach { tab ->
                val pageOffset = ((pagerState.currentPage - tab.ordinal) + pagerState.currentPageOffsetFraction)
                val distance = abs(pageOffset).coerceIn(0f, 1f)
                val color = androidx.compose.ui.graphics.lerp(colorResource(id = R.color.blue), Color.White, distance)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .instantClickable { 
                            onTabSelected(tab)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(tab.ordinal)
                            }
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.title,
                        color = color
                    )
                }
            }
        }
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

@Composable
fun Modifier.instantClickable(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = LocalIndication.current
    val coroutineScope = rememberCoroutineScope()
    
    return this
        .indication(interactionSource, indication)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { offset ->
                    val press = PressInteraction.Press(offset)
                    val rippleJob = coroutineScope.launch {
                        interactionSource.emit(press)
                        kotlinx.coroutines.delay(100) // Minimum ripple visibility duration
                    }
                    
                    val released = tryAwaitRelease()
                    if (released) {
                        coroutineScope.launch { 
                            rippleJob.join()
                            interactionSource.emit(PressInteraction.Release(press)) 
                        }
                    } else {
                        coroutineScope.launch { 
                            rippleJob.join()
                            interactionSource.emit(PressInteraction.Cancel(press)) 
                        }
                    }
                },
                onTap = {
                    onClick()
                }
            )
        }
}

