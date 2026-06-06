package ca.gainzassist.activities.start_workout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.ExerciseSet
import java.util.Locale

private fun numberColumnWidthDp(vararg values: String): Dp {
    val maxChars = values.maxOfOrNull { it.length } ?: 1
    return maxOf(72.dp, (maxChars * 14).dp + 28.dp)
}

@Composable
fun ExerciseSetsListScreen(
    exercises: List<Exercise>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(5.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(exercises) { exercise ->
            ExerciseSetsCard(exercise = exercise)
        }
    }
}

@Composable
fun ExerciseSetsCard(
    exercise: Exercise,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(0.dp), // To match relative layout background border look
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.colorLightBg))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    colorResource(id = R.color.colorDarkText)
                ) // @drawable/border approximation
        ) {
            // Title
            Text(
                text = exercise.name ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.colorBg))
                    .padding(10.dp),
                color = colorResource(id = R.color.colorText),
                fontSize = 30.sp,
                textAlign = TextAlign.Start
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Subtitles (Set, Reps, Weight)
                ExerciseSetSubtitles(
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                )

                // Separator
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .padding(top = 5.dp)
                        .background(colorResource(id = R.color.colorDarkText)) // @drawable/border
                )

                // Horizontal Sets Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp) // space after separator
                ) {
                    items(exercise.setsList) { exerciseSet ->
                        ExerciseSetChip(exerciseSet = exerciseSet)
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseSetSubtitles(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.set_num),
            modifier = Modifier
                .background(colorResource(id = R.color.colorLightBg))
                .height(60.dp)
                .wrapContentHeight(Alignment.CenterVertically),
            color = colorResource(id = R.color.colorBg),
            fontSize = 15.sp,
        )
        Text(
            text = stringResource(id = R.string.reps),
            modifier = Modifier
                .height(60.dp)
                .wrapContentHeight(Alignment.CenterVertically),
            color = colorResource(id = R.color.colorBg),
            fontSize = 15.sp,
        )
        Text(
            text = stringResource(id = R.string.weight_lbs),
            modifier = Modifier
                .height(60.dp)
                .wrapContentHeight(Alignment.CenterVertically),
            color = colorResource(id = R.color.colorBg),
            fontSize = 15.sp,
        )
    }
}

@Composable
private fun ExerciseSetNumberCell(
    text: String,
    width: Dp,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .width(width)
            .height(60.dp)
            .wrapContentHeight(Alignment.CenterVertically),
        color = Color.Black,
        fontSize = 20.sp,
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false
    )
}

@Composable
fun ExerciseSetChip(
    exerciseSet: ExerciseSet,
    modifier: Modifier = Modifier
) {
    val setText = (exerciseSet.setNumber + 1).toString()
    val repsText = exerciseSet.reps.toString()
    val weightText = String.format(Locale.getDefault(), "%.0f", exerciseSet.weight)

    val columnWidth = numberColumnWidthDp(setText, repsText, weightText)

    Column(
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExerciseSetNumberCell(text = setText, width = columnWidth)
        ExerciseSetNumberCell(text = repsText, width = columnWidth)
        ExerciseSetNumberCell(text = weightText, width = columnWidth)
    }
}

// --- Previews ---

@Preview(showBackground = true)
@Composable
fun ExercisesListScreenPreview_Empty() {
    Surface {
        ExerciseSetsListScreen(exercises = emptyList())
    }
}

@Preview(showBackground = true)
@Composable
fun ExercisesListScreenPreview_OneExercise() {
    val exercise = Exercise().apply {
        name = "Bench Press"
        setsList.add(ExerciseSet().apply { setNumber = 0; reps = 10; weight = 135f })
        setsList.add(ExerciseSet().apply { setNumber = 1; reps = 8; weight = 155f })
        setsList.add(ExerciseSet().apply { setNumber = 2; reps = 6; weight = 185f })
    }
    Surface {
        ExerciseSetsListScreen(exercises = listOf(exercise))
    }
}

@Preview(showBackground = true)
@Composable
fun ExercisesListScreenPreview_MultipleExercises() {
    val exercise1 = Exercise().apply {
        name = "Squat"
        setsList.add(ExerciseSet().apply { setNumber = 0; reps = 10; weight = 225f })
        setsList.add(ExerciseSet().apply { setNumber = 1; reps = 8; weight = 275f })
    }
    val exercise2 = Exercise().apply {
        name = "Deadlift"
        setsList.add(ExerciseSet().apply { setNumber = 0; reps = 5; weight = 315f })
    }
    Surface {
        ExerciseSetsListScreen(exercises = listOf(exercise1, exercise2))
    }
}

@Preview(showBackground = true)
@Composable
fun ExercisesListScreenPreview_LongExerciseName() {
    val exercise = Exercise().apply {
        name = "Standing Overhead Barbell Shoulder Press"
        setsList.add(ExerciseSet().apply { setNumber = 0; reps = 10; weight = 95f })
    }
    Surface {
        ExerciseSetsListScreen(exercises = listOf(exercise))
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun ExercisesListScreenPreview_SmallPhone_360x800() {
    val exercise = Exercise().apply {
        name = "Bench Press"
        setsList.add(ExerciseSet().apply { setNumber = 0; reps = 10; weight = 135f })
        setsList.add(ExerciseSet().apply { setNumber = 1; reps = 8; weight = 155f })
        setsList.add(ExerciseSet().apply { setNumber = 2; reps = 6; weight = 185f })
    }
    Surface {
        ExerciseSetsListScreen(exercises = listOf(exercise))
    }
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
fun ExercisesListScreenPreview_FontScaleLarge() {
    val exercise = Exercise().apply {
        name = "Bench Press"
        setsList.add(ExerciseSet().apply { setNumber = 0; reps = 10; weight = 135f })
    }
    Surface {
        ExerciseSetsListScreen(exercises = listOf(exercise))
    }
}

@Preview(showBackground = true)
@Composable
fun ExercisesListScreenPreview_ThreeDigitWeights() {
    val exercise = Exercise().apply {
        name = "Bench Press"
        setsList.add(ExerciseSet().apply { setNumber = 0; reps = 10; weight = 130f })
    }
    Surface {
        ExerciseSetsListScreen(exercises = listOf(exercise))
    }
}

@Preview(showBackground = true)
@Composable
fun ExercisesListScreenPreview_LargeWeights() {
    val exercise = Exercise().apply {
        name = "Leg Press"
        setsList.add(ExerciseSet().apply { setNumber = 12; reps = 100; weight = 315f })
    }
    Surface {
        ExerciseSetsListScreen(exercises = listOf(exercise))
    }
}

@Preview(showBackground = true)
@Composable
fun ExercisesListScreenPreview_MixedDigitWidths() {
    val exercise = Exercise().apply {
        name = "Heavy Lifting"
        setsList.add(ExerciseSet().apply { setNumber = 123; reps = 1000; weight = 10000f })
    }
    Surface {
        ExerciseSetsListScreen(exercises = listOf(exercise))
    }
}
