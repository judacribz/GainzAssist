package ca.gainzassist.activities.add_workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R
import ca.gainzassist.ui.components.GainzOutlinedTextField
import ca.gainzassist.ui.components.GainzButton

val StaatlichesFont = FontFamily(Font(R.font.staatliches))

data class SummaryUiState(
    val workoutName: String,
    val exerciseName: String,
    val selectedEquipment: String,
    val equipmentOptions: List<String>,
    val weight: String,
    val reps: String,
    val sets: String,
    val exerciseNames: List<String>,
    val selectedExerciseName: String?,
    val showAddExerciseButton: Boolean,
    val showUpdateExerciseButton: Boolean,
    val mainWorkoutButtonText: String,
    val workoutNameError: String?,
    val exerciseNameError: String?,
    val weightError: String?,
    val repsError: String?,
    val setsError: String?,
    val canDecrementWeight: Boolean,
    val canDecrementReps: Boolean,
    val canDecrementSets: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryToolbar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.title_new_workout_summary),
                fontFamily = StaatlichesFont,
                fontSize = 35.sp,
                color = colorResource(id = R.color.colorBg),
                style = TextStyle(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = colorResource(id = R.color.colorBg),
                        blurRadius = 5f,
                        offset = androidx.compose.ui.geometry.Offset(1f, 1f)
                    )
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = androidx.appcompat.R.drawable.abc_ic_ab_back_material),
                    contentDescription = "Back",
                    tint = colorResource(id = R.color.colorBg)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(id = R.color.colorLightBg)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(width = 2.dp, color = colorResource(id = R.color.colorAccent))
    )
}

@Composable
fun SummaryCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .shadow(elevation = 3.dp, shape = RoundedCornerShape(5.dp))
            .background(color = colorResource(id = R.color.colorLightBg), shape = RoundedCornerShape(5.dp))
            .border(width = 0.5.dp, color = colorResource(id = R.color.colorBg), shape = RoundedCornerShape(5.dp))
            .padding(4.dp)
    ) {
        content()
    }
}

@Composable
fun NumericStepperField(
    value: String,
    onValueChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    canDecrement: Boolean,
    modifier: Modifier = Modifier,
    isFloat: Boolean = false
) {
    Row(
        modifier = modifier
            .background(
                color = colorResource(id = R.color.colorLightAccent),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 4.dp,
                color = colorResource(id = R.color.blueDark),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onDecrement,
            enabled = canDecrement,
            modifier = Modifier.size(40.dp)
        ) {
            if (canDecrement) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_minus_dark),
                    contentDescription = "Decrement",
                    tint = Color.Unspecified
                )
            }
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontFamily = StaatlichesFont,
                fontSize = 35.sp,
                color = colorResource(id = R.color.colorText),
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = if (isFloat) KeyboardType.Decimal else KeyboardType.Number),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        )

        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plus_dark),
                contentDescription = "Increment",
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
fun EquipmentDropdown(
    selectedEquipment: String,
    equipmentOptions: List<String>,
    onEquipmentSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedEquipment,
                fontSize = 22.sp,
                color = colorResource(id = R.color.colorText),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_drop_dark),
                contentDescription = "Dropdown",
                tint = Color.Unspecified
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            equipmentOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onEquipmentSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ExerciseChipButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .size(60.dp)
            .background(colorResource(id = R.color.blueDark), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = StaatlichesFont,
            fontSize = 20.sp
        )
    }
}

@Composable
fun SummaryScreenContent(
    uiState: SummaryUiState,
    onBack: () -> Unit,
    onWorkoutNameChanged: (String) -> Unit,
    onExerciseNameChanged: (String) -> Unit,
    onEquipmentSelected: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onRepsChanged: (String) -> Unit,
    onSetsChanged: (String) -> Unit,
    onIncrementWeight: () -> Unit,
    onDecrementWeight: () -> Unit,
    onIncrementReps: () -> Unit,
    onDecrementReps: () -> Unit,
    onIncrementSets: () -> Unit,
    onDecrementSets: () -> Unit,
    onClearExercise: () -> Unit,
    onAddExercise: () -> Unit,
    onUpdateExercise: () -> Unit,
    onExerciseClicked: (String) -> Unit,
    onDiscardWorkout: () -> Unit,
    onAddOrUpdateWorkout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(4.dp)
            .verticalScroll(rememberScrollState())
    ) {
        SummaryToolbar(onBack = onBack)

        SummaryCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            GainzOutlinedTextField(
                value = uiState.workoutName,
                onValueChange = onWorkoutNameChanged,
                label = stringResource(id = R.string.hint_workout_name),
                isError = uiState.workoutNameError != null,
                errorText = uiState.workoutNameError,
                modifier = Modifier.padding(4.dp).height(80.dp)
            )
        }

        SummaryCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Column(modifier = Modifier.padding(4.dp)) {
                GainzOutlinedTextField(
                    value = uiState.exerciseName,
                    onValueChange = onExerciseNameChanged,
                    label = stringResource(id = R.string.hint_exercise_name),
                    isError = uiState.exerciseNameError != null,
                    errorText = uiState.exerciseNameError,
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth().height(150.dp).padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumericStepperField(
                        value = uiState.weight,
                        onValueChange = onWeightChanged,
                        onIncrement = onIncrementWeight,
                        onDecrement = onDecrementWeight,
                        canDecrement = uiState.canDecrementWeight,
                        isFloat = true,
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    )
                    EquipmentDropdown(
                        selectedEquipment = uiState.selectedEquipment,
                        equipmentOptions = uiState.equipmentOptions,
                        onEquipmentSelected = onEquipmentSelected,
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumericStepperField(
                        value = uiState.sets,
                        onValueChange = onSetsChanged,
                        onIncrement = onIncrementSets,
                        onDecrement = onDecrementSets,
                        canDecrement = uiState.canDecrementSets,
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    )
                    NumericStepperField(
                        value = uiState.reps,
                        onValueChange = onRepsChanged,
                        onIncrement = onIncrementReps,
                        onDecrement = onDecrementReps,
                        canDecrement = uiState.canDecrementReps,
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GainzButton(
                        text = stringResource(id = R.string.clear),
                        onClick = onClearExercise,
                        fontFamily = StaatlichesFont,
                        modifier = Modifier.weight(1f).padding(end = 4.dp).height(55.dp)
                    )

                    if (uiState.showUpdateExerciseButton) {
                        GainzButton(
                            text = stringResource(id = R.string.update_exercise),
                            onClick = onUpdateExercise,
                            fontFamily = StaatlichesFont,
                            modifier = Modifier.weight(1f).padding(start = 4.dp).height(55.dp)
                        )
                    } else if (uiState.showAddExerciseButton) {
                        GainzButton(
                            text = stringResource(id = R.string.add_exercise),
                            onClick = onAddExercise,
                            fontFamily = StaatlichesFont,
                            modifier = Modifier.weight(1f).padding(start = 4.dp).height(55.dp)
                        )
                    }
                }
            }
        }

        SummaryCard(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Column(modifier = Modifier.padding(4.dp)) {
                Text(
                    text = stringResource(id = R.string.exercises).uppercase(),
                    fontFamily = StaatlichesFont,
                    fontSize = 18.sp,
                    color = colorResource(id = R.color.colorBg),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                LazyRow {
                    items(uiState.exerciseNames) { name ->
                        ExerciseChipButton(
                            text = name,
                            onClick = { onExerciseClicked(name) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GainzButton(
                text = stringResource(id = R.string.discard),
                onClick = onDiscardWorkout,
                fontFamily = StaatlichesFont,
                modifier = Modifier.weight(1f).padding(end = 4.dp).height(60.dp)
            )
            GainzButton(
                text = uiState.mainWorkoutButtonText,
                onClick = onAddOrUpdateWorkout,
                fontFamily = StaatlichesFont,
                modifier = Modifier.weight(1f).padding(start = 4.dp).height(60.dp)
            )
        }
    }
}

@Composable
fun SummaryScreen(
    uiState: SummaryUiState,
    onBack: () -> Unit,
    onWorkoutNameChanged: (String) -> Unit,
    onExerciseNameChanged: (String) -> Unit,
    onEquipmentSelected: (String) -> Unit,
    onWeightChanged: (String) -> Unit,
    onRepsChanged: (String) -> Unit,
    onSetsChanged: (String) -> Unit,
    onIncrementWeight: () -> Unit,
    onDecrementWeight: () -> Unit,
    onIncrementReps: () -> Unit,
    onDecrementReps: () -> Unit,
    onIncrementSets: () -> Unit,
    onDecrementSets: () -> Unit,
    onClearExercise: () -> Unit,
    onAddExercise: () -> Unit,
    onUpdateExercise: () -> Unit,
    onExerciseClicked: (String) -> Unit,
    onDiscardWorkout: () -> Unit,
    onAddOrUpdateWorkout: () -> Unit
) {
    SummaryScreenContent(
        uiState = uiState,
        onBack = onBack,
        onWorkoutNameChanged = onWorkoutNameChanged,
        onExerciseNameChanged = onExerciseNameChanged,
        onEquipmentSelected = onEquipmentSelected,
        onWeightChanged = onWeightChanged,
        onRepsChanged = onRepsChanged,
        onSetsChanged = onSetsChanged,
        onIncrementWeight = onIncrementWeight,
        onDecrementWeight = onDecrementWeight,
        onIncrementReps = onIncrementReps,
        onDecrementReps = onDecrementReps,
        onIncrementSets = onIncrementSets,
        onDecrementSets = onDecrementSets,
        onClearExercise = onClearExercise,
        onAddExercise = onAddExercise,
        onUpdateExercise = onUpdateExercise,
        onExerciseClicked = onExerciseClicked,
        onDiscardWorkout = onDiscardWorkout,
        onAddOrUpdateWorkout = onAddOrUpdateWorkout
    )
}

// -- Previews --
@Preview(showBackground = true)
@Composable
fun SummaryScreenPreview_EmptyInitial() {
    SummaryScreen(
        uiState = SummaryUiState(
            workoutName = "", exerciseName = "", selectedEquipment = "Barbell", equipmentOptions = listOf("Barbell", "Dumbbell", "N/A"),
            weight = "45.0", reps = "10", sets = "3", exerciseNames = emptyList(), selectedExerciseName = null,
            showAddExerciseButton = true, showUpdateExerciseButton = false, mainWorkoutButtonText = "ADD WORKOUT",
            workoutNameError = null, exerciseNameError = null, weightError = null, repsError = null, setsError = null,
            canDecrementWeight = true, canDecrementReps = true, canDecrementSets = true
        ),
        onBack = {}, onWorkoutNameChanged = {}, onExerciseNameChanged = {}, onEquipmentSelected = {}, onWeightChanged = {},
        onRepsChanged = {}, onSetsChanged = {}, onIncrementWeight = {}, onDecrementWeight = {}, onIncrementReps = {},
        onDecrementReps = {}, onIncrementSets = {}, onDecrementSets = {}, onClearExercise = {}, onAddExercise = {},
        onUpdateExercise = {}, onExerciseClicked = {}, onDiscardWorkout = {}, onAddOrUpdateWorkout = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SummaryScreenPreview_WithWorkoutAndExerciseText() {
    SummaryScreen(
        uiState = SummaryUiState(
            workoutName = "Push Day", exerciseName = "Bench Press", selectedEquipment = "Barbell", equipmentOptions = listOf("Barbell", "Dumbbell", "N/A"),
            weight = "135.0", reps = "8", sets = "4", exerciseNames = emptyList(), selectedExerciseName = null,
            showAddExerciseButton = true, showUpdateExerciseButton = false, mainWorkoutButtonText = "ADD WORKOUT",
            workoutNameError = null, exerciseNameError = null, weightError = null, repsError = null, setsError = null,
            canDecrementWeight = true, canDecrementReps = true, canDecrementSets = true
        ),
        onBack = {}, onWorkoutNameChanged = {}, onExerciseNameChanged = {}, onEquipmentSelected = {}, onWeightChanged = {},
        onRepsChanged = {}, onSetsChanged = {}, onIncrementWeight = {}, onDecrementWeight = {}, onIncrementReps = {},
        onDecrementReps = {}, onIncrementSets = {}, onDecrementSets = {}, onClearExercise = {}, onAddExercise = {},
        onUpdateExercise = {}, onExerciseClicked = {}, onDiscardWorkout = {}, onAddOrUpdateWorkout = {}
    )
}
