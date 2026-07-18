package ca.gainzassist.feature.summary.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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
import ca.gainzassist.ui.components.ExerciseSelectionButton
import ca.gainzassist.ui.components.GainzButton
import ca.gainzassist.ui.components.GainzFieldLabelOverlay
import ca.gainzassist.ui.components.GainzMaterialDropdown
import ca.gainzassist.ui.components.GainzMaterialOutlinedTextField
import ca.gainzassist.ui.components.GainzTextFieldState
import androidx.appcompat.R as appCompatR

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
    val canDecrementSets: Boolean,
    val isSaving: Boolean
)

data class SummaryScreenActions(
    val onBack: () -> Unit = {},
    val onWorkoutNameChanged: (String) -> Unit = {},
    val onExerciseNameChanged: (String) -> Unit = {},
    val onEquipmentSelected: (String) -> Unit = {},
    val onWeightChanged: (String) -> Unit = {},
    val onRepsChanged: (String) -> Unit = {},
    val onSetsChanged: (String) -> Unit = {},
    val onIncrementWeight: () -> Unit = {},
    val onDecrementWeight: () -> Unit = {},
    val onIncrementReps: () -> Unit = {},
    val onDecrementReps: () -> Unit = {},
    val onIncrementSets: () -> Unit = {},
    val onDecrementSets: () -> Unit = {},
    val onClearExercise: () -> Unit = {},
    val onAddExercise: () -> Unit = {},
    val onUpdateExercise: () -> Unit = {},
    val onExerciseClicked: (String) -> Unit = {},
    val onDiscardWorkout: () -> Unit = {},
    val onAddOrUpdateWorkout: () -> Unit = {}
)

private val ToolbarHeight = 56.dp
private val ToolbarBorderWidth = 2.dp
private val CardPadding = 4.dp
private val CardElevation = 3.dp
private val CardCornerRadius = 5.dp
private val CardBorderWidth = 0.5.dp
private val StepperCornerRadius = 20.dp
private val StepperBorderWidth = 1.dp
private val StepperPadding = 4.dp
private val IconButtonSize = 40.dp
private val InputFontSize = 35.sp
private val StepperInternalPadding = 4.dp
private val ScreenPadding = 4.dp
private val SectionVerticalPadding = 4.dp
private val MaterialFieldLabelClearance = 12.dp
private val WorkoutNameHeight = 80.dp
private val ExerciseNameHeight = 150.dp
private val InputBottomPadding = 8.dp
private val RowVerticalPadding = 4.dp
private val ColumnEndPadding = 4.dp
private val ColumnStartPadding = 4.dp
private val ButtonTopPadding = 8.dp
private val ButtonHeight = 55.dp
private val FooterButtonHeight = 60.dp
private val ExercisesTitleFontSize = 18.sp
private val ExercisesTitleBottomPadding = 4.dp
private val ExerciseListItemSpacing = 8.dp
private val ExerciseListHorizontalPadding = 4.dp
private const val WeightFull = 1f
private const val BENCH_PRESS = "Bench Press"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryToolbar(onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.title_new_workout_summary),
                fontFamily = StaatlichesFont,
                fontSize = 35.sp,
                color = colorResource(R.color.colorBg),
                style = TextStyle(
                    shadow = Shadow(
                        color = colorResource(R.color.colorBg),
                        blurRadius = 5f,
                        offset = Offset(1f, 1f)
                    )
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(appCompatR.drawable.abc_ic_ab_back_material),
                    contentDescription = stringResource(R.string.cd_back),
                    tint = colorResource(R.color.colorBg)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorResource(R.color.colorLightBg)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(ToolbarHeight)
            .border(
                width = ToolbarBorderWidth,
                color = colorResource(R.color.colorAccent)
            )
    )
}

@Composable
fun SummaryCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .padding(CardPadding)
            .shadow(elevation = CardElevation, shape = RoundedCornerShape(CardCornerRadius))
            .background(
                color = colorResource(R.color.colorLightBg),
                shape = RoundedCornerShape(CardCornerRadius)
            )
            .border(
                width = CardBorderWidth,
                color = colorResource(R.color.colorBg),
                shape = RoundedCornerShape(CardCornerRadius)
            )
            .padding(CardPadding)
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
    isFloat: Boolean = false,
    label: String = ""
) {
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .background(
                    color = colorResource(R.color.colorLightAccent),
                    shape = RoundedCornerShape(StepperCornerRadius)
                )
                .border(
                    width = StepperBorderWidth,
                    color = colorResource(R.color.blueDark),
                    shape = RoundedCornerShape(StepperCornerRadius)
                )
                .padding(StepperPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDecrement,
                enabled = canDecrement,
                modifier = Modifier.size(IconButtonSize)
            ) {
                if (canDecrement) {
                    Icon(
                        painter = painterResource(R.drawable.ic_minus_dark),
                        contentDescription = stringResource(R.string.cd_decrement),
                        tint = Color.Unspecified
                    )
                }
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    fontFamily = StaatlichesFont,
                    fontSize = InputFontSize,
                    color = colorResource(R.color.colorText),
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isFloat) {
                        KeyboardType.Decimal
                    } else {
                        KeyboardType.Number
                    }
                ),
                modifier = Modifier
                    .weight(WeightFull)
                    .padding(horizontal = StepperInternalPadding)
            )

            IconButton(
                onClick = onIncrement,
                modifier = Modifier.size(IconButtonSize)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_plus_dark),
                    contentDescription = stringResource(R.string.cd_increment),
                    tint = Color.Unspecified
                )
            }
        }
        GainzFieldLabelOverlay(label = label)
    }
}


@Composable
fun SummaryScreenContent(
    uiState: SummaryUiState,
    actions: SummaryScreenActions
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SummaryToolbar(onBack = actions.onBack)
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ButtonTopPadding),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GainzButton(
                    text = stringResource(R.string.discard),
                    onClick = actions.onDiscardWorkout,
                    fontFamily = StaatlichesFont,
                    modifier = Modifier
                        .weight(WeightFull)
                        .padding(end = ColumnEndPadding)
                        .height(FooterButtonHeight)
                )
                GainzButton(
                    text = uiState.mainWorkoutButtonText,
                    enabled = !uiState.isSaving,
                    onClick = actions.onAddOrUpdateWorkout,
                    fontFamily = StaatlichesFont,
                    modifier = Modifier
                        .weight(WeightFull)
                        .padding(start = ColumnStartPadding)
                        .height(FooterButtonHeight)
                )
            }
        },
        contentColor = colorResource(R.color.colorLightBg),
        containerColor = colorResource(R.color.colorLightBg)
    ) {
        Column(
            Modifier
                .padding(it)
                .padding(ScreenPadding)
                .verticalScroll(rememberScrollState())
        ) {
            SummaryCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SectionVerticalPadding)
            ) {
                GainzMaterialOutlinedTextField(
                    state = GainzTextFieldState(
                        value = uiState.workoutName,
                        label = stringResource(R.string.hint_workout_name),
                        isError = uiState.workoutNameError != null,
                        errorText = uiState.workoutNameError
                    ),
                    onValueChange = actions.onWorkoutNameChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = CardPadding,
                            top = MaterialFieldLabelClearance,
                            end = CardPadding,
                            bottom = CardPadding
                        )
                        .height(WorkoutNameHeight)
                )
            }

            SummaryCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SectionVerticalPadding)
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = CardPadding,
                        top = MaterialFieldLabelClearance,
                        end = CardPadding,
                        bottom = CardPadding
                    )
                ) {
                    GainzMaterialOutlinedTextField(
                        state = GainzTextFieldState(
                            value = uiState.exerciseName,
                            label = stringResource(R.string.hint_exercise_name),
                            isError = uiState.exerciseNameError != null,
                            errorText = uiState.exerciseNameError
                        ),
                        onValueChange = actions.onExerciseNameChanged,
                        singleLine = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ExerciseNameHeight)
                            .padding(bottom = InputBottomPadding)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = RowVerticalPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumericStepperField(
                            value = uiState.weight,
                            label = stringResource(R.string.hint_weight),
                            onValueChange = actions.onWeightChanged,
                            onIncrement = actions.onIncrementWeight,
                            onDecrement = actions.onDecrementWeight,
                            canDecrement = uiState.canDecrementWeight,
                            isFloat = true,
                            modifier = Modifier
                                .weight(WeightFull)
                                .padding(end = ColumnEndPadding)
                        )
                        GainzMaterialDropdown(
                            selectedValue = uiState.selectedEquipment,
                            label = stringResource(R.string.hint_equipment),
                            options = uiState.equipmentOptions,
                            onOptionSelected = actions.onEquipmentSelected,
                            modifier = Modifier
                                .weight(WeightFull)
                                .padding(start = ColumnStartPadding)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = RowVerticalPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumericStepperField(
                            value = uiState.sets,
                            label = stringResource(R.string.hint_sets),
                            onValueChange = actions.onSetsChanged,
                            onIncrement = actions.onIncrementSets,
                            onDecrement = actions.onDecrementSets,
                            canDecrement = uiState.canDecrementSets,
                            modifier = Modifier
                                .weight(WeightFull)
                                .padding(end = ColumnEndPadding)
                        )
                        NumericStepperField(
                            value = uiState.reps,
                            label = stringResource(R.string.hint_reps),
                            onValueChange = actions.onRepsChanged,
                            onIncrement = actions.onIncrementReps,
                            onDecrement = actions.onDecrementReps,
                            canDecrement = uiState.canDecrementReps,
                            modifier = Modifier
                                .weight(WeightFull)
                                .padding(start = ColumnStartPadding)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = ButtonTopPadding),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        GainzButton(
                            text = stringResource(R.string.clear),
                            onClick = actions.onClearExercise,
                            fontFamily = StaatlichesFont,
                            modifier = Modifier
                                .weight(WeightFull)
                                .padding(end = ColumnEndPadding)
                                .height(ButtonHeight)
                        )

                        if (uiState.showUpdateExerciseButton) {
                            GainzButton(
                                text = stringResource(R.string.update_exercise),
                                onClick = actions.onUpdateExercise,
                                fontFamily = StaatlichesFont,
                                modifier = Modifier
                                    .weight(WeightFull)
                                    .padding(start = ColumnStartPadding)
                                    .height(ButtonHeight)
                            )
                        } else if (uiState.showAddExerciseButton) {
                            GainzButton(
                                text = stringResource(R.string.add_exercise),
                                onClick = actions.onAddExercise,
                                fontFamily = StaatlichesFont,
                                modifier = Modifier
                                    .weight(WeightFull)
                                    .padding(start = ColumnStartPadding)
                                    .height(ButtonHeight)
                            )
                        }
                    }
                }
            }

            SummaryCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = SectionVerticalPadding)
            ) {
                Column(modifier = Modifier.padding(CardPadding)) {
                    Text(
                        text = stringResource(R.string.exercises).uppercase(),
                        fontFamily = StaatlichesFont,
                        fontSize = ExercisesTitleFontSize,
                        color = colorResource(R.color.colorBg),
                        modifier = Modifier.padding(bottom = ExercisesTitleBottomPadding)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(ExerciseListItemSpacing),
                        contentPadding = PaddingValues(horizontal = ExerciseListHorizontalPadding)
                    ) {
                        items(uiState.exerciseNames) { name ->
                            ExerciseSelectionButton(
                                text = name,
                                onClick = { actions.onExerciseClicked(name) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(WeightFull))
        }
    }
}

@Composable
fun SummaryScreen(
    uiState: SummaryUiState,
    actions: SummaryScreenActions = SummaryScreenActions()
) {
    SummaryScreenContent(
        uiState = uiState,
        actions = actions
    )
}

// -- Previews --
val summaryPreviewState = SummaryUiState(
    workoutName = "Push Day",
    exerciseName = "",
    selectedEquipment = "Barbell",
    equipmentOptions = listOf("Barbell", "Dumbbell", "N/A"),
    weight = "45.0",
    reps = "10",
    sets = "3",
    exerciseNames = listOf(BENCH_PRESS, "Overhead Press"),
    selectedExerciseName = null,
    showAddExerciseButton = true,
    showUpdateExerciseButton = false,
    mainWorkoutButtonText = "ADD WORKOUT",
    workoutNameError = null,
    exerciseNameError = null,
    weightError = null,
    repsError = null,
    setsError = null,
    canDecrementWeight = true,
    canDecrementReps = true,
    canDecrementSets = true,
    isSaving = false
)

@Preview(showBackground = true)
@Composable
private fun SummaryScreenPreviewEmptyInitial() {
    SummaryScreen(
        uiState = summaryPreviewState.copy(
            workoutName = "",
            exerciseNames = emptyList()
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SummaryScreenPreviewWithWorkoutAndExerciseText() {
    SummaryScreen(
        uiState = summaryPreviewState.copy(
            exerciseName = "Squat",
            weight = "225.0"
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SummaryScreenPreviewWithOneExercise() {
    SummaryScreen(
        uiState = summaryPreviewState.copy(
            exerciseNames = listOf(BENCH_PRESS)
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SummaryScreenPreviewUpdateWorkoutMode() {
    SummaryScreen(
        uiState = summaryPreviewState.copy(
            mainWorkoutButtonText = "UPDATE WORKOUT"
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SummaryScreenPreviewUpdateExerciseMode() {
    SummaryScreen(
        uiState = summaryPreviewState.copy(
            exerciseName = BENCH_PRESS,
            selectedExerciseName = BENCH_PRESS,
            showAddExerciseButton = false,
            showUpdateExerciseButton = true,
            weight = "135.0"
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SummaryScreenPreviewDuplicateExerciseError() {
    SummaryScreen(
        uiState = summaryPreviewState.copy(
            exerciseName = BENCH_PRESS,
            exerciseNameError = "$BENCH_PRESS exists"
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SummaryScreenPreviewMinWeight() {
    SummaryScreen(
        uiState = summaryPreviewState.copy(
            weight = "45.0",
            canDecrementWeight = false
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun SummaryScreenPreviewDumbbellSelected() {
    SummaryScreen(
        uiState = summaryPreviewState.copy(
            exerciseName = "Lateral Raise",
            selectedEquipment = "Dumbbell",
            weight = "15.0"
        ),
    )
}

@Preview(showBackground = true, device = "spec:width=360dp,height=800dp,dpi=411")
@Composable
private fun SummaryScreenPreviewSmallPhone360x800() {
    SummaryScreen(
        uiState = summaryPreviewState,
    )
}

@Preview(showBackground = true, device = "spec:width=412dp,height=915dp,dpi=411")
@Composable
private fun SummaryScreenPreviewLargePhone412x915() {
    SummaryScreen(
        uiState = summaryPreviewState,
    )
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
private fun SummaryScreenPreviewFontScaleLarge() {
    SummaryScreen(
        uiState = summaryPreviewState,
    )
}
