package ca.gainzassist.activities.start_workout.view.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import ca.gainzassist.R
import ca.gainzassist.domain.model.Exercise
import ca.gainzassist.domain.model.ExerciseSet

@Composable
fun WarmupsListScreen(warmups: List<Exercise>?) {
    if (warmups.isNullOrEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.no_warmups))
        }
    } else {
        ExerciseSetsListScreen(exercises = warmups)
    }
}

@Preview(showBackground = true)
@Composable
fun WarmupsListScreenPreviewEmpty() {
    WarmupsListScreen(warmups = emptyList())
}

@Preview(showBackground = true)
@Composable
fun WarmupsListScreenPreviewOneWarmup() {
    val exercise = Exercise()
    exercise.name = "Jumping Jacks"
    exercise.setsList = arrayListOf(ExerciseSet(0L, "Jumping Jacks", 0, 10, 0f))
    WarmupsListScreen(warmups = listOf(exercise))
}

@Preview(showBackground = true)
@Composable
fun WarmupsListScreenPreviewMultipleWarmups() {
    val ex1 = Exercise()
    ex1.name = "Jumping Jacks"
    ex1.setsList = arrayListOf(
        ExerciseSet(0L, "Jumping Jacks", 0, 10, 0f),
        ExerciseSet(0L, "Jumping Jacks", 1, 15, 0f)
    )

    val ex2 = Exercise()
    ex2.name = "High Knees"
    ex2.setsList = arrayListOf(ExerciseSet(0L, "High Knees", 0, 20, 0f))

    WarmupsListScreen(warmups = listOf(ex1, ex2))
}

@Preview(showBackground = true)
@Composable
fun WarmupsListScreenPreviewLongWarmupName() {
    val ex = Exercise()
    ex.name = "Very Long Warmup Exercise Name That Wraps To Next Line"
    ex.setsList = arrayListOf(
        ExerciseSet(
            0L,
            "Very Long Warmup Exercise Name That Wraps To Next Line",
            0,
            10,
            0f
        )
    )
    WarmupsListScreen(warmups = listOf(ex))
}

@Preview(showBackground = true, device = Devices.PIXEL_2)
@Composable
fun WarmupsListScreenPreviewSmallPhone360x800() {
    val ex = Exercise()
    ex.name = "Jumping Jacks"
    ex.setsList = arrayListOf(ExerciseSet(0L, "Jumping Jacks", 0, 10, 0f))
    WarmupsListScreen(warmups = listOf(ex))
}

@PreviewFontScale
@Composable
fun WarmupsListScreenPreviewFontScaleLarge() {
    val ex = Exercise()
    ex.name = "Jumping Jacks"
    ex.setsList = arrayListOf(ExerciseSet(0L, "Jumping Jacks", 0, 10, 0f))
    WarmupsListScreen(warmups = listOf(ex))
}