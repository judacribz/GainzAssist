package ca.gainzassist.activities.main.fragments.resume

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R
import ca.gainzassist.ui.components.GainzButton

data class ResumeUiState(
    val workoutNames: List<String>,
    val isEmpty: Boolean = workoutNames.isEmpty()
)

@Composable
fun ResumeScreen(
    uiState: ResumeUiState,
    onWorkoutClick: (String) -> Unit
) {
    val staatliches = FontFamily(Font(R.font.staatliches))

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
        ) {
            Text(
                text = stringResource(R.string.incomplete_workouts),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                fontSize = 16.sp
            )

            if (uiState.isEmpty) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_workouts_to_resume),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.workoutNames) { workoutName ->
                        GainzButton(
                            text = workoutName,
                            onClick = { onWorkoutClick(workoutName) },
                            modifier = Modifier
                                .fillMaxWidth(),
                            fontFamily = staatliches
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResumeScreenPreview_Empty() {
    MaterialTheme {
        ResumeScreen(
            uiState = ResumeUiState(workoutNames = emptyList()),
            onWorkoutClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResumeScreenPreview_OneWorkout() {
    MaterialTheme {
        ResumeScreen(
            uiState = ResumeUiState(workoutNames = listOf("Chest Day")),
            onWorkoutClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResumeScreenPreview_MultipleWorkouts() {
    MaterialTheme {
        ResumeScreen(
            uiState = ResumeUiState(workoutNames = listOf("Chest Day", "Legs", "Back & Biceps")),
            onWorkoutClick = {}
        )
    }
}

@PreviewScreenSizes
@Composable
fun ResumeScreenPreview_SmallPhone_360x800() {
    MaterialTheme {
        ResumeScreen(
            uiState = ResumeUiState(workoutNames = listOf("Chest Day", "Legs", "Back & Biceps")),
            onWorkoutClick = {}
        )
    }
}

@PreviewFontScale
@Composable
fun ResumeScreenPreview_FontScaleLarge() {
    MaterialTheme {
        ResumeScreen(
            uiState = ResumeUiState(workoutNames = listOf("Chest Day", "Legs")),
            onWorkoutClick = {}
        )
    }
}
