package ca.gainzassist.activities.start_workout.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.viewinterop.AndroidView
import ca.gainzassist.R
import ca.gainzassist.activities.start_workout.components.EquipmentView
import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS

private val StaatlichesFontFamily = FontFamily(
    Font(R.font.staatliches, FontWeight.Normal)
)

@Composable
fun ProgressDot(
    number: String,
    status: PROGRESS_STATUS,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val startColor = when (status) {
        PROGRESS_STATUS.UNSELECTED -> Color(0xFFCCCCCC)
        PROGRESS_STATUS.SELECTED -> Color(0xFFCCCCCC)
        PROGRESS_STATUS.SUCCESS -> Color(0xFF16A85A)
        PROGRESS_STATUS.FAIL -> Color(0xFFFF0000)
        PROGRESS_STATUS.SUCCESS_SELECTED -> Color(0xFF16A85A)
        PROGRESS_STATUS.FAIL_SELECTED -> Color(0xFFFF0000)
    }

    val isSelected = when (status) {
        PROGRESS_STATUS.SELECTED,
        PROGRESS_STATUS.SUCCESS_SELECTED,
        PROGRESS_STATUS.FAIL_SELECTED -> true
        else -> false
    }

    Box(
        modifier = modifier
            .size(35.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(startColor, Color.Black),
                    radius = 60f
                )
            )
            .then(
                if (isSelected) {
                    Modifier.border(1.5.dp, Color.Black, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            color = Color.Black,
            fontFamily = StaatlichesFontFamily,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WorkoutProgressHeader(
    exerciseTitle: String,
    setNumText: String,
    exerciseProgress: List<PROGRESS_STATUS>,
    setProgress: List<PROGRESS_STATUS>,
    onExerciseDotClick: (Int) -> Unit,
    onSetDotClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(5.dp))
            .border(0.5.dp, Color.Black, RoundedCornerShape(5.dp))
            .padding(start = 7.dp, end = 7.dp, top = 4.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = exerciseTitle,
            fontFamily = StaatlichesFontFamily,
            fontSize = 36.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.exercise_num),
                fontFamily = StaatlichesFontFamily,
                fontSize = 24.sp,
                color = Color.Black,
                modifier = Modifier.width(60.dp)
            )
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(exerciseProgress.size) { index ->
                    ProgressDot(
                        number = (index.inc()).toString(),
                        status = exerciseProgress[index],
                        onClick = { onExerciseDotClick(index) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = setNumText,
                fontFamily = StaatlichesFontFamily,
                fontSize = 24.sp,
                color = Color.Black,
                modifier = Modifier.width(100.dp),
                maxLines = 2
            )
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(setProgress.size) { index ->
                    ProgressDot(
                        number = (index + 1).toString(),
                        status = setProgress[index],
                        onClick = { onSetDotClick(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutEquipmentTimer(
    equipmentWeight: Float,
    equipmentType: String,
    timerText: String,
    onTimerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            AndroidView(
                factory = { context ->
                    EquipmentView(context).apply {
                        setup(equipmentWeight, equipmentType)
                    }
                },
                update = { view ->
                    view.setup(equipmentWeight, equipmentType)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, RoundedCornerShape(5.dp))
                    .border(0.5.dp, Color.Black, RoundedCornerShape(5.dp))
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color.White, RoundedCornerShape(5.dp))
                .border(0.5.dp, Color.Black, RoundedCornerShape(5.dp))
                .clickable { onTimerClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timerText,
                fontFamily = StaatlichesFontFamily,
                fontSize = 50.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WorkoutRepsWeightControls(
    repsText: String,
    weightText: String,
    onRepsChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onIncReps: () -> Unit,
    onDecReps: () -> Unit,
    onIncWeight: () -> Unit,
    onDecWeight: () -> Unit,
    isDecRepsVisible: Boolean,
    isDecWeightVisible: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(5.dp))
            .border(0.5.dp, Color.Black, RoundedCornerShape(5.dp))
            .padding(5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.reps),
                fontFamily = StaatlichesFontFamily,
                fontSize = 24.sp,
                color = Color.Black
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onDecReps,
                    modifier = Modifier
                        .size(50.dp)
                        .alpha(if (isDecRepsVisible) 1f else 0f),
                    enabled = isDecRepsVisible
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_minus),
                        contentDescription = "Decrease Reps",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                BasicTextField(
                    value = repsText,
                    onValueChange = onRepsChanged,
                    textStyle = TextStyle(
                        fontFamily = StaatlichesFontFamily,
                        fontSize = 35.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.width(60.dp)
                )

                IconButton(
                    onClick = onIncReps,
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "Increase Reps",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(Color.LightGray)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = stringResource(R.string.weight_lbs),
                fontFamily = StaatlichesFontFamily,
                fontSize = 24.sp,
                color = Color.Black
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onDecWeight,
                    modifier = Modifier
                        .size(50.dp)
                        .alpha(if (isDecWeightVisible) 1f else 0f),
                    enabled = isDecWeightVisible
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_minus),
                        contentDescription = "Decrease Weight",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                BasicTextField(
                    value = weightText,
                    onValueChange = onWeightChanged,
                    textStyle = TextStyle(
                        fontFamily = StaatlichesFontFamily,
                        fontSize = 35.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.width(90.dp)
                )

                IconButton(
                    onClick = onIncWeight,
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_plus),
                        contentDescription = "Increase Weight",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutFooterControls(
    isUpdateMode: Boolean,
    onFinishSet: () -> Unit,
    onResumeWorkout: () -> Unit,
    onUpdateSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!isUpdateMode) {
            Button(
                onClick = onFinishSet,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.finish_set),
                    fontSize = 20.sp,
                    fontFamily = StaatlichesFontFamily,
                    color = Color.White
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onResumeWorkout,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(3.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(
                        text = stringResource(R.string.resume_workout),
                        fontSize = 20.sp,
                        fontFamily = StaatlichesFontFamily,
                        color = Color.White
                    )
                }
                Button(
                    onClick = onUpdateSet,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(3.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = stringResource(R.string.update_set),
                        fontSize = 20.sp,
                        fontFamily = StaatlichesFontFamily,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutScreenComposeContent(
    exerciseTitle: String,
    setNumText: String,
    exerciseProgress: List<PROGRESS_STATUS>,
    setProgress: List<PROGRESS_STATUS>,
    timerText: String,
    repsText: String,
    weightText: String,
    equipmentWeight: Float,
    equipmentType: String,
    isUpdateMode: Boolean,
    isDecRepsVisible: Boolean,
    isDecWeightVisible: Boolean,
    onExerciseDotClick: (Int) -> Unit,
    onSetDotClick: (Int) -> Unit,
    onTimerClick: () -> Unit,
    onRepsChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onIncReps: () -> Unit,
    onDecReps: () -> Unit,
    onIncWeight: () -> Unit,
    onDecWeight: () -> Unit,
    onFinishSet: () -> Unit,
    onResumeWorkout: () -> Unit,
    onUpdateSet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(3.dp)
    ) {
        WorkoutProgressHeader(
            exerciseTitle = exerciseTitle,
            setNumText = setNumText,
            exerciseProgress = exerciseProgress,
            setProgress = setProgress,
            onExerciseDotClick = onExerciseDotClick,
            onSetDotClick = onSetDotClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(5f)
                .padding(3.dp)
        )

        WorkoutEquipmentTimer(
            equipmentWeight = equipmentWeight,
            equipmentType = equipmentType,
            timerText = timerText,
            onTimerClick = onTimerClick,
            modifier = Modifier
                .fillMaxWidth()
                .weight(4f)
        )

        WorkoutRepsWeightControls(
            repsText = repsText,
            weightText = weightText,
            onRepsChanged = onRepsChanged,
            onWeightChanged = onWeightChanged,
            onIncReps = onIncReps,
            onDecReps = onDecReps,
            onIncWeight = onIncWeight,
            onDecWeight = onDecWeight,
            isDecRepsVisible = isDecRepsVisible,
            isDecWeightVisible = isDecWeightVisible,
            modifier = Modifier
                .fillMaxWidth()
                .weight(3f)
                .padding(3.dp)
        )

        WorkoutFooterControls(
            isUpdateMode = isUpdateMode,
            onFinishSet = onFinishSet,
            onResumeWorkout = onResumeWorkout,
            onUpdateSet = onUpdateSet,
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutScreenComposeContentPreview() {
    MaterialTheme {
        WorkoutScreenComposeContent(
            exerciseTitle = "Bench Press",
            setNumText = "Set 1: Main",
            exerciseProgress = listOf(
                PROGRESS_STATUS.SUCCESS,
                PROGRESS_STATUS.SELECTED,
                PROGRESS_STATUS.UNSELECTED
            ),
            setProgress = listOf(
                PROGRESS_STATUS.SUCCESS,
                PROGRESS_STATUS.SELECTED,
                PROGRESS_STATUS.UNSELECTED,
                PROGRESS_STATUS.UNSELECTED
            ),
            timerText = "01:30",
            repsText = "10",
            weightText = "135.0",
            equipmentWeight = 135f,
            equipmentType = "barbell",
            isUpdateMode = false,
            isDecRepsVisible = true,
            isDecWeightVisible = true,
            onExerciseDotClick = {},
            onSetDotClick = {},
            onTimerClick = {},
            onRepsChanged = {},
            onWeightChanged = {},
            onIncReps = {},
            onDecReps = {},
            onIncWeight = {},
            onDecWeight = {},
            onFinishSet = {},
            onResumeWorkout = {},
            onUpdateSet = {}
        )
    }
}
