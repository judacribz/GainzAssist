package ca.gainzassist.activities.add_workout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R
import ca.gainzassist.constants.ExerciseConst
import ca.gainzassist.ui.components.GainzButton
import ca.gainzassist.ui.components.GainzOutlinedTextField

val Staatliches = FontFamily(
    Font(R.font.staatliches, FontWeight.Normal)
)

data class WorkoutEntryUiState(
    val workoutName: String = "",
    val numExercises: Int = 3
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEntryScreen(
    uiState: WorkoutEntryUiState,
    onWorkoutNameChanged: (String) -> Unit,
    onNumExercisesChanged: (Int) -> Unit,
    onIncrementExercises: () -> Unit,
    onDecrementExercises: () -> Unit,
    onCancel: () -> Unit,
    onEnter: () -> Unit,
    onBack: () -> Unit
) {
    val blue = colorResource(id = R.color.blue)
    val grey = colorResource(id = R.color.grey)
    val colorLightBg = colorResource(id = R.color.colorLightBg)
    val colorBg = colorResource(id = R.color.colorBg)

    val safeFontFamily = if (LocalInspectionMode.current) {
        FontFamily.Default
    } else {
        try {
            Staatliches
        } catch (_: Exception) {
            FontFamily.Default
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorLightBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            // Toolbar
            CustomToolbar(onBack = onBack, fontFamily = safeFontFamily)

            Spacer(modifier = Modifier.height(4.dp))

            // Workout Name Section
            WorkoutNameSection(
                modifier = Modifier.weight(0.25f), // Matches ll_md_weight
                workoutName = uiState.workoutName,
                onWorkoutNameChanged = onWorkoutNameChanged,
                colorBg = colorBg
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Number of Exercises Section
            NumExercisesSection(
                modifier = Modifier.weight(0.5f), // Matches ll_lg_weight
                numExercises = uiState.numExercises,
                onNumExercisesChanged = onNumExercisesChanged,
                onIncrementExercises = onIncrementExercises,
                onDecrementExercises = onDecrementExercises,
                grey = grey,
                blue = blue,
                colorBg = colorBg,
                fontFamily = safeFontFamily
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Footer Section
            FooterSection(
                modifier = Modifier.weight(0.25f), // Matches ll_md_weight
                isNameEmpty = uiState.workoutName.trim().isEmpty(),
                onCancel = onCancel,
                onEnter = onEnter,
                fontFamily = safeFontFamily
            )
        }
    }
}

@Composable
fun CustomToolbar(onBack: () -> Unit, fontFamily: FontFamily) {
    val colorBg = colorResource(id = R.color.colorBg)
    val colorLightAccent = colorResource(id = R.color.colorLightAccent)
    val colorLightBg = colorResource(id = R.color.colorLightBg)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(colorLightAccent, RoundedCornerShape(3.dp))
            .padding(bottom = 2.dp)
            .background(colorLightBg, RoundedCornerShape(2.dp))
            .border(1.dp, colorBg, RoundedCornerShape(2.dp))
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colorBg
            )
        }
        Text(
            text = if (LocalInspectionMode.current) "Add Workout" else stringResource(
                id = R.string.add_workout
            ),
            style = TextStyle(
                fontFamily = fontFamily,
                fontSize = 35.sp,
                color = colorBg,
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = colorBg,
                    offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                    blurRadius = 5f
                )
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 48.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun WorkoutNameSection(
    modifier: Modifier = Modifier,
    workoutName: String,
    onWorkoutNameChanged: (String) -> Unit,
    colorBg: Color
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .shadow(3.dp, RoundedCornerShape(5.dp)),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, colorBg)
    ) {
        GainzOutlinedTextField(
            value = workoutName,
            onValueChange = onWorkoutNameChanged,
            label = if (LocalInspectionMode.current) "Workout Name" else stringResource(
                id = R.string.hint_workout_name
            ).trim(),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        )
    }
}

@Composable
fun NumExercisesSection(
    modifier: Modifier = Modifier,
    numExercises: Int,
    onNumExercisesChanged: (Int) -> Unit,
    onIncrementExercises: () -> Unit,
    onDecrementExercises: () -> Unit,
    grey: Color,
    blue: Color,
    colorBg: Color,
    fontFamily: FontFamily
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .shadow(3.dp, RoundedCornerShape(5.dp)),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, colorBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = if (LocalInspectionMode.current) "# OF EXERCISES" else stringResource(
                    id = R.string.num_of_exercises
                ).uppercase(),
                style = TextStyle(
                    fontFamily = fontFamily,
                    fontSize = 20.sp,
                    color = colorBg,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = colorResource(id = R.color.greenDark),
                        offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                        blurRadius = 2f
                    )
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Minus Button - Hidden when disabled to match legacy View.GONE behavior
                if (numExercises > ExerciseConst.MIN_INT) {
                    GainzButton(
                        text = "-",
                        onClick = onDecrementExercises,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(80.dp)
                            .padding(vertical = 10.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                // Number Display
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(150.dp)
                        .background(grey, RoundedCornerShape(20.dp))
                        .border(4.dp, blue, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = numExercises.toString(),
                        onValueChange = {
                            val newValueStr = it.filter { char -> char.isDigit() }.take(3)
                            if (newValueStr.isEmpty()) {
                                // Restore MIN_INT if field is cleared
                                onNumExercisesChanged(ExerciseConst.MIN_INT)
                            } else {
                                val newValue = newValueStr.toInt()
                                onNumExercisesChanged(Math.max(ExerciseConst.MIN_INT, newValue))
                            }
                        },
                        textStyle = TextStyle(
                            fontFamily = fontFamily,
                            fontSize = 35.sp,
                            color = colorBg,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        cursorBrush = SolidColor(colorBg)
                    )
                }

                // Plus Button
                GainzButton(
                    text = "+",
                    onClick = onIncrementExercises,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(80.dp)
                        .padding(vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
fun FooterSection(
    modifier: Modifier = Modifier,
    isNameEmpty: Boolean,
    onCancel: () -> Unit,
    onEnter: () -> Unit,
    fontFamily: FontFamily
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 2.dp, end = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        GainzButton(
            text = if (LocalInspectionMode.current) "CANCEL" else stringResource(
                id = R.string.cancel
            ),
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(bottom = 5.dp),
            fontFamily = fontFamily
        )

        GainzButton(
            text = if (LocalInspectionMode.current) {
                if (isNameEmpty) "SKIP" else "ENTER"
            } else {
                if (isNameEmpty) stringResource(id = R.string.skip) else stringResource(id = R.string.enter)
            },
            onClick = onEnter,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(bottom = 5.dp),
            fontFamily = fontFamily
        )
    }
}

@Preview(showBackground = true, name = "Empty Name, Skip, 3 Exercises")
@Composable
fun WorkoutEntryScreenPreview_EmptyName_Skip_ThreeExercises() {
    WorkoutEntryScreen(
        uiState = WorkoutEntryUiState(workoutName = "", numExercises = 3),
        onWorkoutNameChanged = {},
        onNumExercisesChanged = {},
        onIncrementExercises = {},
        onDecrementExercises = {},
        onCancel = {},
        onEnter = {},
        onBack = {}
    )
}

@Preview(showBackground = true, name = "With Name, Enter, 5 Exercises")
@Composable
fun WorkoutEntryScreenPreview_WithName_Enter_FiveExercises() {
    WorkoutEntryScreen(
        uiState = WorkoutEntryUiState(workoutName = "Push Day", numExercises = 5),
        onWorkoutNameChanged = {},
        onNumExercisesChanged = {},
        onIncrementExercises = {},
        onDecrementExercises = {},
        onCancel = {},
        onEnter = {},
        onBack = {}
    )
}

@Preview(showBackground = true, name = "Min Exercise Count, Disabled Minus")
@Composable
fun WorkoutEntryScreenPreview_MinExerciseCount_DisabledMinus() {
    WorkoutEntryScreen(
        uiState = WorkoutEntryUiState(workoutName = "", numExercises = 1),
        onWorkoutNameChanged = {},
        onNumExercisesChanged = {},
        onIncrementExercises = {},
        onDecrementExercises = {},
        onCancel = {},
        onEnter = {},
        onBack = {}
    )
}

@Preview(showBackground = true, name = "Long Workout Name")
@Composable
fun WorkoutEntryScreenPreview_LongWorkoutName() {
    WorkoutEntryScreen(
        uiState = WorkoutEntryUiState(
            workoutName = "Very Long Workout Name to Test Layout",
            numExercises = 3
        ),
        onWorkoutNameChanged = {},
        onNumExercisesChanged = {},
        onIncrementExercises = {},
        onDecrementExercises = {},
        onCancel = {},
        onEnter = {},
        onBack = {}
    )
}

@Preview(showBackground = true, device = "spec:width=360dp,height=800dp", name = "Small Phone")
@Composable
fun WorkoutEntryScreenPreview_SmallPhone_360x800() {
    WorkoutEntryScreenPreview_EmptyName_Skip_ThreeExercises()
}

@Preview(showBackground = true, device = "spec:width=412dp,height=915dp", name = "Large Phone")
@Composable
fun WorkoutEntryScreenPreview_LargePhone_412x915() {
    WorkoutEntryScreenPreview_EmptyName_Skip_ThreeExercises()
}

@Preview(
    showBackground = true,
    device = "spec:width=800dp,height=360dp,orientation=landscape",
    name = "Landscape"
)
@Composable
fun WorkoutEntryScreenPreview_Landscape() {
    WorkoutEntryScreenPreview_EmptyName_Skip_ThreeExercises()
}

@Preview(showBackground = true, fontScale = 1.5f, name = "Large Font Scale")
@Composable
fun WorkoutEntryScreenPreview_FontScaleLarge() {
    WorkoutEntryScreenPreview_EmptyName_Skip_ThreeExercises()
}

@Preview(showBackground = true, device = "spec:width=320dp,height=640dp", name = "Narrow Width")
@Composable
fun WorkoutEntryScreenPreview_NarrowWidth() {
    WorkoutEntryScreenPreview_EmptyName_Skip_ThreeExercises()
}

@Preview(showBackground = true, device = "spec:width=360dp,height=1000dp", name = "Tall Phone")
@Composable
fun WorkoutEntryScreenPreview_TallPhone() {
    WorkoutEntryScreenPreview_EmptyName_Skip_ThreeExercises()
}
