@file:OptIn(ExperimentalFoundationApi::class)

package ca.gainzassist.activities.add_workout

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import ca.gainzassist.R
import ca.gainzassist.components.GainzTabItem
import ca.gainzassist.components.GainzTabRow

data class ExerciseEntryTab(
    val index: Int,
    val title: String,
    val id: Long,
    val isAddTab: Boolean = false
)

data class ExercisesEntryUiState(
    val selectedIndex: Int,
    val tabs: List<ExerciseEntryTab>,
    val numExercises: Int
)

@Composable
fun ExercisesEntryScreen(
    uiState: ExercisesEntryUiState,
    onTabSelected: (Int) -> Unit,
    pageContent: @Composable (Int) -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = uiState.selectedIndex.takeIf { it >= 0 } ?: 0,
        pageCount = { uiState.tabs.size } // Note: we sync page count to total tabs, including the Add Tab
    )

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            if (pagerState.currentPage != uiState.selectedIndex) {
                onTabSelected(pagerState.currentPage)
            }
        }
    }

    LaunchedEffect(uiState.selectedIndex, uiState.tabs.size) {
        if (uiState.selectedIndex >= 0 && uiState.selectedIndex < uiState.tabs.size) {
            if (pagerState.currentPage != uiState.selectedIndex) {
                pagerState.animateScrollToPage(uiState.selectedIndex)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // HorizontalPager should only display real exercise tabs
        // If the pager gets to the Add Tab index somehow, we can display empty content, 
        // but typically the state updates and pushes a new tab before it renders.
        HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 2,
            key = { page -> uiState.tabs.getOrNull(page)?.id ?: page.toLong() },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            if (page < uiState.numExercises) {
                pageContent(page)
            }
        }

        val gainzTabs = uiState.tabs.map { tab ->
            GainzTabItem(
                title = tab.title,
                iconResId = if (tab.isAddTab) R.drawable.ic_plus else null
            )
        }

        GainzTabRow(
            pagerState = pagerState,
            tabs = gainzTabs,
            scrollable = true,
            onTabClick = onTabSelected
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExercisesEntryScreenPreview_OneExercise() {
    MaterialTheme {
        ExercisesEntryScreen(
            uiState = ExercisesEntryUiState(
                selectedIndex = 0,
                tabs = listOf(
                    ExerciseEntryTab(0, "Exercise 1", 1L),
                    ExerciseEntryTab(1, "", 2L, isAddTab = true)
                ),
                numExercises = 1
            ),
            onTabSelected = {},
            pageContent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExercisesEntryScreenPreview_MultipleExercises() {
    MaterialTheme {
        ExercisesEntryScreen(
            uiState = ExercisesEntryUiState(
                selectedIndex = 1,
                tabs = listOf(
                    ExerciseEntryTab(0, "Exercise 1", 1L),
                    ExerciseEntryTab(1, "Exercise 2", 2L),
                    ExerciseEntryTab(2, "Exercise 3", 3L),
                    ExerciseEntryTab(3, "", 4L, isAddTab = true)
                ),
                numExercises = 3
            ),
            onTabSelected = {},
            pageContent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExercisesEntryScreenPreview_WithPlusTab() {
    MaterialTheme {
        ExercisesEntryScreen(
            uiState = ExercisesEntryUiState(
                selectedIndex = 0,
                tabs = listOf(
                    ExerciseEntryTab(0, "Exercise 1", 1L),
                    ExerciseEntryTab(1, "", 2L, isAddTab = true)
                ),
                numExercises = 1
            ),
            onTabSelected = {},
            pageContent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ExercisesEntryScreenPreview_SelectedLastExercise() {
    MaterialTheme {
        ExercisesEntryScreen(
            uiState = ExercisesEntryUiState(
                selectedIndex = 2,
                tabs = listOf(
                    ExerciseEntryTab(0, "Exercise 1", 1L),
                    ExerciseEntryTab(1, "Exercise 2", 2L),
                    ExerciseEntryTab(2, "Exercise 3", 3L),
                    ExerciseEntryTab(3, "", 4L, isAddTab = true)
                ),
                numExercises = 3
            ),
            onTabSelected = {},
            pageContent = {}
        )
    }
}

@PreviewScreenSizes
@Composable
fun ExercisesEntryScreenPreview_SmallPhone() {
    MaterialTheme {
        ExercisesEntryScreen(
            uiState = ExercisesEntryUiState(
                selectedIndex = 0,
                tabs = listOf(
                    ExerciseEntryTab(0, "Exercise 1", 1L),
                    ExerciseEntryTab(1, "Exercise 2", 2L),
                    ExerciseEntryTab(2, "", 3L, isAddTab = true)
                ),
                numExercises = 2
            ),
            onTabSelected = {},
            pageContent = {}
        )
    }
}

@PreviewFontScale
@Composable
fun ExercisesEntryScreenPreview_LargeFont() {
    MaterialTheme {
        ExercisesEntryScreen(
            uiState = ExercisesEntryUiState(
                selectedIndex = 0,
                tabs = listOf(
                    ExerciseEntryTab(0, "Exercise 1", 1L),
                    ExerciseEntryTab(1, "Exercise 2", 2L),
                    ExerciseEntryTab(2, "", 3L, isAddTab = true)
                ),
                numExercises = 2
            ),
            onTabSelected = {},
            pageContent = {}
        )
    }
}
