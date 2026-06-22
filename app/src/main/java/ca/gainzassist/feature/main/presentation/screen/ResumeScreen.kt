package ca.gainzassist.feature.main.presentation.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import ca.gainzassist.R

data class ResumeUiState(val workoutNames: List<String>, val isEmpty: Boolean = workoutNames.isEmpty())

private const val PREVIEW_CHEST_DAY = "Chest Day"

@Composable
fun ResumeScreen(
    uiState: ResumeUiState,
    onWorkoutClick: (String) -> Unit
) {

    val staatliches = FontFamily(Font(R.font.staatliches))

    SharedWorkoutList(
        titleResId = R.string.incomplete_workouts,
        workoutNames = uiState.workoutNames,
        emptyStateTextResId = R.string.no_workouts_to_resume,
        onWorkoutClick = onWorkoutClick,
        buttonFontFamily = staatliches
    )
}

@Preview(showBackground = true)
@Composable
fun ResumeScreenPreviewEmpty() {
    MaterialTheme {
        ResumeScreen(
            uiState = ResumeUiState(workoutNames = emptyList()),
            onWorkoutClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResumeScreenPreviewOneWorkout() {
    MaterialTheme {
        ResumeScreen(
            uiState = ResumeUiState(workoutNames = listOf(PREVIEW_CHEST_DAY)),
            onWorkoutClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResumeScreenPreviewMultipleWorkouts() {
    MaterialTheme {
        ResumeScreen(
            uiState = ResumeUiState(workoutNames = listOf(PREVIEW_CHEST_DAY, "Legs", "Back & Biceps")),
            onWorkoutClick = {}
        )
    }
}

@PreviewScreenSizes
@Composable
fun ResumeScreenPreviewSmallPhone360x800() {
    MaterialTheme {
        ResumeScreen(
            uiState = ResumeUiState(workoutNames = listOf(PREVIEW_CHEST_DAY, "Legs", "Back & Biceps")),
            onWorkoutClick = {}
        )
    }
}

@PreviewFontScale
@Composable
fun ResumeScreenPreviewFontScaleLarge() {
    MaterialTheme {
        ResumeScreen(
            uiState = ResumeUiState(workoutNames = listOf(PREVIEW_CHEST_DAY, "Legs")),
            onWorkoutClick = {}
        )
    }
}
