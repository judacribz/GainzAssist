package ca.gainzassist.activities.start_workout.fragments

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R
import ca.gainzassist.adapters.SingleItemAdapter.PROGRESS_STATUS
import java.util.Locale

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

data class WorkoutProgressUiItem(
    val number: Int,
    val status: PROGRESS_STATUS
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
            .background(colorResource(id = R.color.colorLightBg))
    ) {
        WorkoutCard(modifier = Modifier.weight(5f).fillMaxWidth()) {
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
            repsText = uiState.repsText,
            weightText = uiState.weightText,
            isMinReps = uiState.isMinReps,
            isMinWeight = uiState.isMinWeight,
            onRepsChanged = actions.onRepsChanged,
            onWeightChanged = actions.onWeightChanged,
            onRepsFocusLost = actions.onRepsFocusLost,
            onWeightFocusLost = actions.onWeightFocusLost,
            onIncreaseReps = actions.onIncreaseReps,
            onDecreaseReps = actions.onDecreaseReps,
            onIncreaseWeight = actions.onIncreaseWeight,
            onDecreaseWeight = actions.onDecreaseWeight,
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
fun WorkoutProgressHeader(
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
            color = colorResource(id = R.color.colorAccent),
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
            color = colorResource(id = R.color.colorAccent),
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
fun WorkoutProgressRow(
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
fun WorkoutProgressItem(
    item: WorkoutProgressUiItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unselectedBrush = androidx.compose.ui.graphics.Brush.radialGradient(
        colors = listOf(colorResource(id = R.color.colorLightAccent), colorResource(id = R.color.colorBg)),
        radius = 60f
    )
    val successBrush = androidx.compose.ui.graphics.Brush.radialGradient(
        colors = listOf(colorResource(id = R.color.green), colorResource(id = R.color.colorBg)),
        radius = 60f
    )
    val failBrush = androidx.compose.ui.graphics.Brush.radialGradient(
        colors = listOf(colorResource(id = R.color.red), colorResource(id = R.color.colorBg)),
        radius = 60f
    )
    val selectedBrush = androidx.compose.ui.graphics.Brush.radialGradient(
        colorStops = arrayOf(
            0.0f to colorResource(id = R.color.colorBg),
            0.6f to colorResource(id = R.color.colorBg),
            0.85f to Color.White,
            1.0f to colorResource(id = R.color.colorBg)
        ),
        radius = 60f
    )
    val successSelectedBrush = androidx.compose.ui.graphics.Brush.radialGradient(
        colorStops = arrayOf(
            0.0f to colorResource(id = R.color.green),
            0.6f to colorResource(id = R.color.green),
            0.85f to Color.White,
            1.0f to colorResource(id = R.color.colorBg)
        ),
        radius = 60f
    )
    val failSelectedBrush = androidx.compose.ui.graphics.Brush.radialGradient(
        colorStops = arrayOf(
            0.0f to colorResource(id = R.color.red),
            0.6f to colorResource(id = R.color.red),
            0.85f to Color.White,
            1.0f to colorResource(id = R.color.colorBg)
        ),
        radius = 60f
    )
    
    val brush = when (item.status) {
        PROGRESS_STATUS.SUCCESS -> successBrush
        PROGRESS_STATUS.SUCCESS_SELECTED -> successSelectedBrush
        PROGRESS_STATUS.FAIL -> failBrush
        PROGRESS_STATUS.FAIL_SELECTED -> failSelectedBrush
        PROGRESS_STATUS.SELECTED -> selectedBrush
        else -> unselectedBrush
    }

    val textColor = if (item.status == PROGRESS_STATUS.SELECTED) {
        Color.White
    } else {
        colorResource(id = R.color.colorBg)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(5.dp)
            .size(35.dp)
            .clickable(onClick = onClick)
            .background(brush = brush, shape = androidx.compose.foundation.shape.CircleShape)
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
fun WorkoutEquipmentTimer(
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
                color = colorResource(id = R.color.colorAccent),
                fontSize = 50.sp,
                fontFamily = FontFamily(Font(R.font.staatliches)),
                textAlign = TextAlign.Center
            )
            }
        }
    }
}

@Composable
fun WorkoutEquipmentCanvas(
    weight: Float,
    equipment: String,
    modifier: Modifier = Modifier
) {
    val barbellName = stringResource(id = R.string.barbell).lowercase(Locale.getDefault())
    val eqLower = equipment.lowercase(Locale.getDefault())
    
    Canvas(modifier = modifier.padding(8.dp)) {
        val width = size.width
        val height = size.height
        
        // Prevent drawing crash during layout phase when size is too small
        if (width <= 0f || height <= 40f) return@Canvas
        
        if (eqLower == barbellName) {
            var barbellWeight = (weight - 45f) / 2f
            barbellWeight *= 10f
            var startX = 20f
            var newWeight = barbellWeight.toInt()
            val diam45 = height - 20f
            
            val weights = intArrayOf(450, 250, 100, 50, 25)
            val numWeights = IntArray(5)
            val plateWidth = 22f
            
            // Calculate total plates for sleeve width
            var totalPlates = 0
            var tempWeight = newWeight
            for (j in weights.indices) {
                val qty = tempWeight / weights[j]
                val limit = if (j == 0) kotlin.math.min(qty, 5) else qty
                totalPlates += limit
                tempWeight -= weights[j] * qty
            }
            val sleeveWidth = 20f + (totalPlates * plateWidth) + 15f
            val sleeveHeight = 16f
            
            // Draw barbell sleeve
            drawRect(
                color = Color.DarkGray,
                topLeft = androidx.compose.ui.geometry.Offset(0f, (height - sleeveHeight) / 2f),
                size = androidx.compose.ui.geometry.Size(sleeveWidth, sleeveHeight),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
            drawRect(
                color = Color.Black,
                topLeft = androidx.compose.ui.geometry.Offset(0f, (height - sleeveHeight) / 2f),
                size = androidx.compose.ui.geometry.Size(sleeveWidth, sleeveHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            // Draw plates
            for (j in weights.indices) {
                numWeights[j] = newWeight / weights[j]
                for (i in 0 until numWeights[j]) {
                    if (j == 0 && i > 4) continue // Max 5 x 45 plates
                    
                    val ratio = weights[j] / 450f
                    val r = diam45 * (0.4f + 0.6f * ratio) // 45lb is full diam45, smaller weights are smaller
                    val startY = (height - r) / 2f
                    
                    // Fill grey
                    drawRect(
                        color = Color.Gray,
                        topLeft = androidx.compose.ui.geometry.Offset(startX, startY),
                        size = androidx.compose.ui.geometry.Size(plateWidth, r)
                    )
                    // Stroke black
                    drawRect(
                        color = Color.Black,
                        topLeft = androidx.compose.ui.geometry.Offset(startX, startY),
                        size = androidx.compose.ui.geometry.Size(plateWidth, r),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                    startX += plateWidth
                }
                newWeight -= weights[j] * numWeights[j]
            }
            
            // Draw Text
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.DKGRAY
                textSize = 45f
                textAlign = android.graphics.Paint.Align.RIGHT
                isAntiAlias = true
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            }
            
            var distinctWeights = 0
            for (w in numWeights) if (w > 0) distinctWeights++
            
            if (distinctWeights > 0) {
                val textSpacing = 60f
                var textY = (height - (distinctWeights * textSpacing)) / 2f + 45f
                
                for (j in numWeights.indices) {
                    if (numWeights[j] > 0) {
                        val lbs = weights[j] / 10f
                        val label = "${numWeights[j]} x ${if(lbs % 1 == 0f) lbs.toInt().toString() else lbs.toString()} lbs"
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(
                                label,
                                width - 10f,
                                textY,
                                textPaint
                            )
                        }
                        textY += textSpacing
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutRepsWeightControls(
    repsText: String,
    weightText: String,
    isMinReps: Boolean,
    isMinWeight: Boolean,
    onRepsChanged: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onRepsFocusLost: () -> Unit,
    onWeightFocusLost: () -> Unit,
    onIncreaseReps: () -> Unit,
    onDecreaseReps: () -> Unit,
    onIncreaseWeight: () -> Unit,
    onDecreaseWeight: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        WorkoutCard(modifier = Modifier.weight(1f).fillMaxHeight()) {
            WorkoutNumberControl(
                value = repsText,
                onValueChanged = onRepsChanged,
                onFocusLost = onRepsFocusLost,
                onIncrease = onIncreaseReps,
                onDecrease = onDecreaseReps,
                isMin = isMinReps,
                modifier = Modifier.fillMaxSize()
            )
        }
        WorkoutCard(modifier = Modifier.weight(1f).fillMaxHeight()) {
            WorkoutNumberControl(
                value = weightText,
                onValueChanged = onWeightChanged,
                onFocusLost = onWeightFocusLost,
                onIncrease = onIncreaseWeight,
                onDecrease = onDecreaseWeight,
                isMin = isMinWeight,
                isDecimal = true,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun WorkoutNumberControl(
    value: String,
    onValueChanged: (String) -> Unit,
    onFocusLost: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    isMin: Boolean,
    isDecimal: Boolean = false,
    @SuppressLint("ModifierParameter")
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (!isMin) {
                IconButton(onClick = onDecrease) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_minus),
                        contentDescription = "Decrease",
                        tint = colorResource(id = R.color.colorAccent),
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
                value = value,
                onValueChange = { newText ->
                    if (isDecimal) {
                        if (newText.length <= 7 && newText.count { it == '.' } <= 1) {
                            onValueChanged(newText)
                        }
                    } else {
                        if (newText.length <= 4 && newText.all { it.isDigit() }) {
                            onValueChanged(newText)
                        }
                    }
                },
                textStyle = TextStyle(
                    color = colorResource(id = R.color.colorAccent),
                    fontSize = 35.sp,
                    fontFamily = FontFamily(Font(R.font.staatliches)),
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { state ->
                        if (!state.isFocused) {
                            onFocusLost()
                        }
                    }
            )
        }
        
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            IconButton(onClick = onIncrease) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_plus),
                    contentDescription = "Increase",
                    tint = colorResource(id = R.color.colorAccent),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun WorkoutFooterControls(
    isFinishSetVisible: Boolean,
    isUpdateSetVisible: Boolean,
    isResumeWorkoutVisible: Boolean,
    onFinishSet: () -> Unit,
    onUpdateSet: () -> Unit,
    onResumeWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val staatliches = FontFamily(Font(R.font.staatliches))
    val gainzButtonBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
        colors = listOf(colorResource(id = R.color.blue), colorResource(id = R.color.blueDark))
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
                        color = colorResource(id = R.color.colorText),
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
                            color = colorResource(id = R.color.colorText),
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
                            color = colorResource(id = R.color.colorText),
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

// Previews
@Preview(showBackground = true)
@Composable
fun WorkoutComposeScreenPreview_Default() {
    WorkoutComposeScreen(
        uiState = WorkoutUiState(
            exerciseTitle = "Bench Press",
            setNumText = "Set 1 / 5",
            exerciseProgress = listOf(
                WorkoutProgressUiItem(1, PROGRESS_STATUS.SUCCESS),
                WorkoutProgressUiItem(2, PROGRESS_STATUS.SELECTED),
                WorkoutProgressUiItem(3, PROGRESS_STATUS.UNSELECTED)
            ),
            setProgress = listOf(
                WorkoutProgressUiItem(1, PROGRESS_STATUS.SELECTED),
                WorkoutProgressUiItem(2, PROGRESS_STATUS.UNSELECTED)
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
fun WorkoutComposeScreenPreview_UpdateMode() {
    WorkoutComposeScreen(
        uiState = WorkoutUiState(
            exerciseTitle = "Squat",
            setNumText = "Set 3 / 3",
            exerciseProgress = listOf(
                WorkoutProgressUiItem(1, PROGRESS_STATUS.SUCCESS)
            ),
            setProgress = listOf(
                WorkoutProgressUiItem(1, PROGRESS_STATUS.SUCCESS),
                WorkoutProgressUiItem(2, PROGRESS_STATUS.SUCCESS),
                WorkoutProgressUiItem(3, PROGRESS_STATUS.SUCCESS_SELECTED)
            ),
            timerText = "00:00",
            repsText = "5",
            weightText = "225.0",
            currentWeight = 225f,
            currentEquipment = "barbell",
            isMinReps = false,
            isMinWeight = false,
            isFinishSetVisible = false,
            isUpdateSetVisible = true,
            isResumeWorkoutVisible = true
        ),
        actions = WorkoutUiActions()
    )
}

@Preview(showBackground = true)
@Composable
fun WorkoutComposeScreenPreview_LargeWeight() {
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


@Composable
fun WorkoutCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.padding(3.dp),
        shape = RoundedCornerShape(5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, colorResource(id = R.color.colorBg))
    ) {
        content()
    }
}
