package ca.gainzassist.feature.start_workout.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R
import ca.gainzassist.feature.start_workout.presentation.state.WorkoutProgressUiItem
import ca.gainzassist.ui.ProgressStatus

data class WorkoutUiState(
    val exerciseTitle: String = "",
    val setNumText: String = "",
    val exerciseProgress: List<WorkoutProgressUiItem> = emptyList(),
    val setProgress: List<WorkoutProgressUiItem> = emptyList(),
    val timerText: String = "",
    val repsText: String = "",
    val weightText: String = "",
    val currentWeight: Float = 0f,
    val currentEquipment: String = "",
    val isMinReps: Boolean = false,
    val isMinWeight: Boolean = false,
    val isFinishSetVisible: Boolean = true,
    val isUpdateSetVisible: Boolean = false,
    val isResumeWorkoutVisible: Boolean = false
)

data class WorkoutUiActions(
    val onTimerClick: () -> Unit = {},
    val onRepsChanged: (String) -> Unit = {},
    val onWeightChanged: (String) -> Unit = {},
    val onRepsFocusLost: () -> Unit = {},
    val onWeightFocusLost: () -> Unit = {},
    val onIncreaseReps: () -> Unit = {},
    val onDecreaseReps: () -> Unit = {},
    val onIncreaseWeight: () -> Unit = {},
    val onDecreaseWeight: () -> Unit = {},
    val onFinishSet: () -> Unit = {},
    val onResumeWorkout: () -> Unit = {},
    val onUpdateSet: () -> Unit = {},
    val onExerciseProgressClick: (Int) -> Unit = {},
    val onSetProgressClick: (Int) -> Unit = {}
)

data class WorkoutNumberControlState(val value: String, val isMin: Boolean, val isDecimal: Boolean = false)

data class WorkoutNumberControlActions(
    val onValueChanged: (String) -> Unit,
    val onFocusLost: () -> Unit,
    val onIncrease: () -> Unit,
    val onDecrease: () -> Unit
)

@Composable
fun WorkoutComposeScreen(
    uiState: WorkoutUiState,
    actions: WorkoutUiActions,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(3.dp)
            .background(colorResource(R.color.colorLightBg))
    ) {
        WorkoutCard(modifier = Modifier
            .weight(5f)
            .fillMaxWidth()) {
            WorkoutProgressHeader(
                title = uiState.exerciseTitle,
                setNumText = uiState.setNumText,
                exerciseProgress = uiState.exerciseProgress,
                setProgress = uiState.setProgress,
                onExerciseClick = actions.onExerciseProgressClick,
                onSetClick = actions.onSetProgressClick,
                modifier = Modifier.fillMaxSize()
            )
        }
        WorkoutEquipmentTimer(
            weight = uiState.currentWeight,
            equipment = uiState.currentEquipment,
            timerText = uiState.timerText,
            onTimerClick = actions.onTimerClick,
            modifier = Modifier.weight(4f)
        )
        WorkoutRepsWeightControls(
            uiState = uiState,
            actions = actions,
            modifier = Modifier.weight(3f)
        )
        WorkoutFooterControls(
            isFinishSetVisible = uiState.isFinishSetVisible,
            isUpdateSetVisible = uiState.isUpdateSetVisible,
            isResumeWorkoutVisible = uiState.isResumeWorkoutVisible,
            onFinishSet = actions.onFinishSet,
            onUpdateSet = actions.onUpdateSet,
            onResumeWorkout = actions.onResumeWorkout,
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
private fun WorkoutProgressHeader(
    title: String,
    setNumText: String,
    exerciseProgress: List<WorkoutProgressUiItem>,
    setProgress: List<WorkoutProgressUiItem>,
    onExerciseClick: (Int) -> Unit,
    onSetClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val staatliches = FontFamily(Font(R.font.staatliches))
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = colorResource(R.color.colorAccent),
            fontSize = 32.sp,
            fontFamily = staatliches,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp)
        )
        WorkoutProgressRow(
            items = exerciseProgress,
            onClick = onExerciseClick,
            modifier = Modifier.padding(vertical = 5.dp)
        )
        Text(
            text = setNumText,
            color = colorResource(R.color.colorAccent),
            fontSize = 32.sp,
            fontFamily = staatliches,
            textAlign = TextAlign.Center
        )
        WorkoutProgressRow(
            items = setProgress,
            onClick = onSetClick,
            modifier = Modifier.padding(vertical = 5.dp)
        )
    }
}

@Composable
private fun WorkoutProgressRow(
    items: List<WorkoutProgressUiItem>,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        itemsIndexed(items) { index, item ->
            WorkoutProgressItem(
                item = item,
                onClick = { onClick(index) }
            )
        }
    }
}

@Composable
private fun WorkoutProgressItem(
    item: WorkoutProgressUiItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unselectedBrush = Brush.radialGradient(
        colors = listOf(colorResource(R.color.colorLightAccent), colorResource(R.color.colorBg)),
        radius = 60f
    )
    val successBrush = Brush.radialGradient(
        colors = listOf(colorResource(R.color.green), colorResource(R.color.colorBg)),
        radius = 60f
    )
    val failBrush = Brush.radialGradient(
        colors = listOf(colorResource(R.color.red), colorResource(R.color.colorBg)),
        radius = 60f
    )
    val selectedBrush = Brush.radialGradient(
        colorStops = arrayOf(
            0.0f to colorResource(R.color.colorBg),
            0.6f to colorResource(R.color.colorBg),
            0.85f to Color.White,
            1.0f to colorResource(R.color.colorBg)
        ),
        radius = 60f
    )
    val successSelectedBrush = Brush.radialGradient(
        colorStops = arrayOf(
            0.0f to colorResource(R.color.green),
            0.6f to colorResource(R.color.green),
            0.85f to Color.White,
            1.0f to colorResource(R.color.colorBg)
        ),
        radius = 60f
    )
    val failSelectedBrush = Brush.radialGradient(
        colorStops = arrayOf(
            0.0f to colorResource(R.color.red),
            0.6f to colorResource(R.color.red),
            0.85f to Color.White,
            1.0f to colorResource(R.color.colorBg)
        ),
        radius = 60f
    )

    val brush = when (item.status) {
        ProgressStatus.SUCCESS -> successBrush
        ProgressStatus.SUCCESS_SELECTED -> successSelectedBrush
        ProgressStatus.FAIL -> failBrush
        ProgressStatus.FAIL_SELECTED -> failSelectedBrush
        ProgressStatus.SELECTED -> selectedBrush
        else -> unselectedBrush
    }

    val textColor = if (item.status == ProgressStatus.SELECTED) {
        Color.White
    } else {
        colorResource(R.color.colorBg)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(5.dp)
            .size(35.dp)
            .clickable(onClick = onClick)
            .background(brush = brush, shape = CircleShape)
    ) {
        Text(
            text = item.number.toString(),
            color = textColor,
            fontSize = 18.sp,
            fontFamily = FontFamily(Font(R.font.staatliches))
        )
    }
}

@Composable
private fun WorkoutEquipmentTimer(
    weight: Float,
    equipment: String,
    timerText: String,
    onTimerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        WorkoutCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            WorkoutEquipmentCanvas(
                weight = weight,
                equipment = equipment,
                modifier = Modifier.fillMaxSize()
            )
        }
        WorkoutCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onTimerClick)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = timerText,
                    color = colorResource(R.color.colorAccent),
                    fontSize = 50.sp,
                    fontFamily = FontFamily(Font(R.font.staatliches)),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun WorkoutRepsWeightControls(
    uiState: WorkoutUiState,
    actions: WorkoutUiActions,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        WorkoutCard(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()) {
            WorkoutNumberControl(
                state = WorkoutNumberControlState(
                    value = uiState.repsText,
                    isMin = uiState.isMinReps
                ),
                actions = WorkoutNumberControlActions(
                    onValueChanged = actions.onRepsChanged,
                    onFocusLost = actions.onRepsFocusLost,
                    onIncrease = actions.onIncreaseReps,
                    onDecrease = actions.onDecreaseReps
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
        WorkoutCard(modifier = Modifier
            .weight(1f)
            .fillMaxHeight()) {
            WorkoutNumberControl(
                state = WorkoutNumberControlState(
                    value = uiState.weightText,
                    isMin = uiState.isMinWeight,
                    isDecimal = true
                ),
                actions = WorkoutNumberControlActions(
                    onValueChanged = actions.onWeightChanged,
                    onFocusLost = actions.onWeightFocusLost,
                    onIncrease = actions.onIncreaseWeight,
                    onDecrease = actions.onDecreaseWeight
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun WorkoutNumberControl(
    state: WorkoutNumberControlState,
    actions: WorkoutNumberControlActions,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (!state.isMin) {
                IconButton(onClick = actions.onDecrease) {
                    Icon(
                        painter = painterResource(R.drawable.ic_minus),
                        contentDescription = "Decrease",
                        tint = colorResource(R.color.colorAccent),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(2f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = state.value,
                onValueChange = { newText ->
                    handleValueChange(newText, state.isDecimal, actions.onValueChanged)
                },
                textStyle = TextStyle(
                    color = colorResource(R.color.colorAccent),
                    fontSize = 35.sp,
                    fontFamily = FontFamily(Font(R.font.staatliches)),
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (state.isDecimal) KeyboardType.Decimal else KeyboardType.Number
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused) {
                            actions.onFocusLost()
                        }
                    }
            )
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            IconButton(onClick = actions.onIncrease) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus),
                    contentDescription = "Increase",
                    tint = colorResource(R.color.colorAccent),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

private fun handleValueChange(
    newText: String,
    isDecimal: Boolean,
    onValueChanged: (String) -> Unit
) {
    if (isDecimal) {
        if (newText.length <= 7 && newText.count { it == '.' } <= 1) {
            onValueChanged(newText)
        }
    } else {
        if (newText.length <= 4 && newText.all { it.isDigit() }) {
            onValueChanged(newText)
        }
    }
}

@Composable
private fun WorkoutFooterControls(
    isFinishSetVisible: Boolean,
    isUpdateSetVisible: Boolean,
    isResumeWorkoutVisible: Boolean,
    onFinishSet: () -> Unit,
    onUpdateSet: () -> Unit,
    onResumeWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val staatliches = FontFamily(Font(R.font.staatliches))
    val gainzButtonBrush = Brush.verticalGradient(
        colors = listOf(colorResource(R.color.blue), colorResource(R.color.blueDark))
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(3.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isFinishSetVisible) {
            Button(
                onClick = onFinishSet,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = gainzButtonBrush, shape = RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FINISH SET",
                        color = colorResource(R.color.colorText),
                        fontSize = 32.sp,
                        fontFamily = staatliches
                    )
                }
            }
        } else {
            if (isResumeWorkoutVisible) {
                Button(
                    onClick = onResumeWorkout,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush = gainzButtonBrush, shape = RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "RESUME WORKOUT",
                            color = colorResource(R.color.colorText),
                            fontSize = 32.sp,
                            fontFamily = staatliches,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            if (isUpdateSetVisible) {
                Button(
                    onClick = onUpdateSet,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush = gainzButtonBrush, shape = RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "UPDATE SET",
                            color = colorResource(R.color.colorText),
                            fontSize = 32.sp,
                            fontFamily = staatliches,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.padding(3.dp),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(0.5.dp, colorResource(R.color.colorBg))
    ) {
        content()
    }
}

// Previews
@Preview(showBackground = true)
@Composable
private fun WorkoutComposeScreenPreviewDefault() {
    WorkoutComposeScreen(
        uiState = WorkoutUiState(
            exerciseTitle = "Bench Press",
            setNumText = "Set 1 / 5",
            exerciseProgress = listOf(
                WorkoutProgressUiItem(1, ProgressStatus.SUCCESS),
                WorkoutProgressUiItem(2, ProgressStatus.SELECTED),
                WorkoutProgressUiItem(3, ProgressStatus.UNSELECTED)
            ),
            setProgress = listOf(
                WorkoutProgressUiItem(1, ProgressStatus.SELECTED),
                WorkoutProgressUiItem(2, ProgressStatus.UNSELECTED)
            ),
            timerText = "01:30",
            repsText = "5",
            weightText = "135.0",
            currentWeight = 135f,
            currentEquipment = "barbell",
            isMinReps = false,
            isMinWeight = false,
            isFinishSetVisible = true
        ),
        actions = WorkoutUiActions()
    )
}

@Preview(showBackground = true)
@Composable
private fun WorkoutComposeScreenPreviewUpdateMode() {
    WorkoutComposeScreen(
        uiState = WorkoutUiState(
            exerciseTitle = "Squat",
            setNumText = "Set 3 / 3",
            exerciseProgress = listOf(
                WorkoutProgressUiItem(1, ProgressStatus.SUCCESS)
            ),
            setProgress = listOf(
                WorkoutProgressUiItem(1, ProgressStatus.SUCCESS),
                WorkoutProgressUiItem(2, ProgressStatus.SUCCESS),
                WorkoutProgressUiItem(3, ProgressStatus.SUCCESS_SELECTED)
            ),
            timerText = "00:00",
            repsText = "5",
            weightText = "225.0",
            currentWeight = 225f,
            currentEquipment = "barbell",
            isMinReps = false,
            isMinWeight = false,
            isFinishSetVisible = false,
            isResumeWorkoutVisible = true
        ),
        actions = WorkoutUiActions()
    )
}

@Preview(showBackground = true)
@Composable
private fun WorkoutComposeScreenPreviewLargeWeight() {
    WorkoutComposeScreen(
        uiState = WorkoutUiState(
            exerciseTitle = "Deadlift",
            setNumText = "Set 1 / 1",
            exerciseProgress = emptyList(),
            setProgress = emptyList(),
            timerText = "05:00",
            repsText = "1",
            weightText = "495.0",
            currentWeight = 495f,
            currentEquipment = "barbell",
            isMinReps = false,
            isMinWeight = false,
            isFinishSetVisible = true
        ),
        actions = WorkoutUiActions()
    )
}
