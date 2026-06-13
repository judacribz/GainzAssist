package ca.gainzassist.activities.add_workout.exercises_entry.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R
import ca.gainzassist.ui.components.GainzButton
import ca.gainzassist.ui.components.GainzDropdown
import ca.gainzassist.ui.components.GainzOutlinedTextField

data class ExEntryUiState(
    val exerciseName: String,
    val selectedEquipment: String,
    val equipmentOptions: List<String>,
    val weight: String,
    val reps: String,
    val sets: String,
    val showEnter: Boolean,
    val showUpdate: Boolean,
    val showDelete: Boolean,
    val duplicateExerciseError: String?,
    val canDecrementWeight: Boolean,
    val canDecrementReps: Boolean,
    val canDecrementSets: Boolean,
    val exerciseNameError: String?,
    val weightError: String?,
    val repsError: String?,
    val setsError: String?
)

interface ExEntryActions {
    fun onExerciseNameChanged(name: String)
    fun onEquipmentSelected(equipment: String)
    fun onWeightChanged(weight: String)
    fun onRepsChanged(reps: String)
    fun onSetsChanged(sets: String)
    fun onIncrementWeight()
    fun onDecrementWeight()
    fun onIncrementReps()
    fun onDecrementReps()
    fun onIncrementSets()
    fun onDecrementSets()
    fun onEnter()
    fun onUpdate()
    fun onDelete()
}

private val ContainerPadding = 4.dp
private val SectionElevation = 3.dp
private val SectionCornerRadius = 5.dp
private val SectionBorderWidth = 0.5.dp
private val InnerPadding = 4.dp
private val EquipmentTextFontSize = 18.sp
private val EquipmentTextPadding = 5.dp
private val DropdownPadding = 2.dp
private val RowPadding = 2.dp
private val ButtonCornerRadius = 15.dp
private val ButtonElevation = 2.dp
private val PlusMinusFontSize = 40.sp
private val FooterTopPadding = 10.dp
private val FooterBottomPadding = 5.dp
private val FooterHorizontalPadding = 2.dp
private val FooterButtonHorizontalPadding = 2.dp

private const val WeightExerciseName = 4f
private const val WeightEquipment = 3f
private const val WeightMetrics = 10f
private const val WeightFooter = 4f
private const val WeightButton = 1f
private const val WeightValueInput = 1.5f

@Composable
fun ExEntryScreen(
    uiState: ExEntryUiState,
    actions: ExEntryActions,
    modifier: Modifier = Modifier
) {
    val staatliches = FontFamily(Font(R.font.staatliches))
    val colorBg = colorResource(id = R.color.colorBg) // black
    val colorLightBg = colorResource(id = R.color.colorLightBg) // white
    val colorText = colorResource(id = R.color.colorText) // white
    val colorDarkText = colorResource(id = R.color.colorDarkText) // #333333
    val colorBlue = colorResource(id = R.color.blue)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorLightBg)
            .padding(ContainerPadding)
    ) {
        // Exercise Name Section (weight 4)
        Box(
            modifier = Modifier
                .weight(WeightExerciseName)
                .fillMaxWidth()
                .padding(ContainerPadding)
                .shadow(elevation = SectionElevation, shape = RoundedCornerShape(SectionCornerRadius))
                .background(colorLightBg, RoundedCornerShape(SectionCornerRadius))
                .border(SectionBorderWidth, colorBg, RoundedCornerShape(SectionCornerRadius))
                .padding(InnerPadding)
        ) {
            val errorText = uiState.duplicateExerciseError ?: uiState.exerciseNameError
            GainzOutlinedTextField(
                value = uiState.exerciseName,
                onValueChange = actions::onExerciseNameChanged,
                label = stringResource(id = R.string.hint_exercise_name),
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Start,
                isError = errorText != null,
                errorText = errorText
            )
        }

        // Equipment Section (weight 3)
        Box(
            modifier = Modifier
                .weight(WeightEquipment)
                .fillMaxWidth()
                .padding(ContainerPadding)
                .shadow(elevation = SectionElevation, shape = RoundedCornerShape(SectionCornerRadius))
                .background(colorLightBg, RoundedCornerShape(SectionCornerRadius))
                .border(SectionBorderWidth, colorBg, RoundedCornerShape(SectionCornerRadius))
                .padding(InnerPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.equipment),
                    fontFamily = staatliches,
                    fontSize = EquipmentTextFontSize,
                    color = colorDarkText,
                    modifier = Modifier.padding(EquipmentTextPadding),
                    textAlign = TextAlign.Center
                )

                GainzDropdown(
                    selectedValue = uiState.selectedEquipment,
                    options = uiState.equipmentOptions,
                    onOptionSelected = actions::onEquipmentSelected,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(DropdownPadding)
                )
            }
        }

        // Weight/Reps/Sets Section (weight 10)
        Column(
            modifier = Modifier
                .weight(WeightMetrics)
                .fillMaxWidth()
                .padding(ContainerPadding)
                .shadow(elevation = SectionElevation, shape = RoundedCornerShape(SectionCornerRadius))
                .background(colorLightBg, RoundedCornerShape(SectionCornerRadius))
                .border(SectionBorderWidth, colorBg, RoundedCornerShape(SectionCornerRadius))
                .padding(InnerPadding)
        ) {
            // Reusable row for Weight, Reps, Sets
            @Composable
            fun NumberRow(
                value: String,
                errorText: String?,
                onValueChange: (String) -> Unit,
                canDecrement: Boolean,
                onDecrement: () -> Unit,
                onIncrement: () -> Unit,
                keyboardType: KeyboardType,
                modifier: Modifier = Modifier
            ) {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(RowPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Decrement Button
                    if (canDecrement) {
                        Box(
                            modifier = Modifier
                                .weight(WeightButton)
                                .fillMaxHeight()
                                .padding(RowPadding)
                                .shadow(elevation = ButtonElevation, shape = RoundedCornerShape(ButtonCornerRadius))
                                .background(colorBlue, RoundedCornerShape(ButtonCornerRadius))
                                .clickable { onDecrement() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", color = colorText, fontSize = PlusMinusFontSize, fontFamily = staatliches, textAlign = TextAlign.Center)
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(WeightButton))
                    }

                    // Value Input
                    GainzOutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        label = "",
                        modifier = Modifier
                            .weight(WeightValueInput)
                            .fillMaxHeight()
                            .padding(RowPadding),
                        textAlign = TextAlign.Center,
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        isError = errorText != null,
                        errorText = errorText
                    )

                    // Increment Button
                    Box(
                        modifier = Modifier
                            .weight(WeightButton)
                            .fillMaxHeight()
                            .padding(RowPadding)
                            .shadow(elevation = ButtonElevation, shape = RoundedCornerShape(ButtonCornerRadius))
                            .background(colorBlue, RoundedCornerShape(ButtonCornerRadius))
                            .clickable { onIncrement() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = colorText, fontSize = PlusMinusFontSize, fontFamily = staatliches, textAlign = TextAlign.Center)
                    }
                }
            }

            NumberRow(
                value = uiState.weight,
                errorText = uiState.weightError,
                onValueChange = actions::onWeightChanged,
                canDecrement = uiState.canDecrementWeight,
                onDecrement = actions::onDecrementWeight,
                onIncrement = actions::onIncrementWeight,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(WeightEquipment)
            )
            NumberRow(
                value = uiState.reps,
                errorText = uiState.repsError,
                onValueChange = actions::onRepsChanged,
                canDecrement = uiState.canDecrementReps,
                onDecrement = actions::onDecrementReps,
                onIncrement = actions::onIncrementReps,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(WeightEquipment)
            )
            NumberRow(
                value = uiState.sets,
                errorText = uiState.setsError,
                onValueChange = actions::onSetsChanged,
                canDecrement = uiState.canDecrementSets,
                onDecrement = actions::onDecrementSets,
                onIncrement = actions::onIncrementSets,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(WeightEquipment)
            )
        }

        // Footer Section (weight 4)
        Row(
            modifier = Modifier
                .weight(WeightFooter)
                .fillMaxWidth()
                .padding(top = FooterTopPadding, bottom = FooterBottomPadding, start = FooterHorizontalPadding, end = FooterHorizontalPadding),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.showDelete) {
                GainzButton(
                    text = stringResource(id = R.string.delete).uppercase(),
                    onClick = actions::onDelete,
                    modifier = Modifier
                        .weight(WeightButton)
                        .fillMaxHeight()
                        .padding(horizontal = FooterButtonHorizontalPadding),
                    fontFamily = staatliches
                )
            } else {
                Spacer(modifier = Modifier.weight(WeightButton).padding(horizontal = FooterButtonHorizontalPadding))
            }

            if (uiState.showEnter) {
                GainzButton(
                    text = stringResource(id = R.string.enter).uppercase(),
                    onClick = actions::onEnter,
                    modifier = Modifier
                        .weight(WeightButton)
                        .fillMaxHeight()
                        .padding(horizontal = FooterButtonHorizontalPadding),
                    fontFamily = staatliches
                )
            } else if (uiState.showUpdate) {
                GainzButton(
                    text = stringResource(id = R.string.update).uppercase(),
                    onClick = actions::onUpdate,
                    modifier = Modifier
                        .weight(WeightButton)
                        .fillMaxHeight()
                        .padding(horizontal = FooterButtonHorizontalPadding),
                    fontFamily = staatliches
                )
            } else {
                Spacer(modifier = Modifier.weight(WeightButton).padding(horizontal = FooterButtonHorizontalPadding))
            }
        }
    }
}

// Previews
val defaultPreviewState = ExEntryUiState(
    exerciseName = "",
    selectedEquipment = "Barbell",
    equipmentOptions = listOf("Barbell", "Dumbbell", "N/A"),
    weight = "45.0",
    reps = "10",
    sets = "3",
    showEnter = true,
    showUpdate = false,
    showDelete = true,
    duplicateExerciseError = null,
    canDecrementWeight = false, // min weight
    canDecrementReps = true,
    canDecrementSets = true,
    exerciseNameError = null,
    weightError = null,
    repsError = null,
    setsError = null
)

val defaultPreviewActions = object : ExEntryActions {
    override fun onExerciseNameChanged(name: String) {} // Preview
    override fun onEquipmentSelected(equipment: String) {} // Preview
    override fun onWeightChanged(weight: String) {} // Preview
    override fun onRepsChanged(reps: String) {} // Preview
    override fun onSetsChanged(sets: String) {} // Preview
    override fun onIncrementWeight() {} // Preview
    override fun onDecrementWeight() {} // Preview
    override fun onIncrementReps() {} // Preview
    override fun onDecrementReps() {} // Preview
    override fun onIncrementSets() {} // Preview
    override fun onDecrementSets() {} // Preview
    override fun onEnter() {} // Preview
    override fun onUpdate() {} // Preview
    override fun onDelete() {} // Preview
}

@Preview(showBackground = true)
@Composable
fun ExEntryScreenPreviewNewExerciseDefaultBarbell() {
    ExEntryScreen(uiState = defaultPreviewState, actions = defaultPreviewActions)
}

@Preview(showBackground = true)
@Composable
fun ExEntryScreenPreviewExistingExerciseUpdateMode() {
    ExEntryScreen(
        uiState = defaultPreviewState.copy(
            exerciseName = "Bench Press",
            showEnter = false,
            showUpdate = true,
            canDecrementWeight = true,
            weight = "135.0"
        ),
        actions = defaultPreviewActions
    )
}

@Preview(showBackground = true)
@Composable
fun ExEntryScreenPreviewDeleteHidden() {
    ExEntryScreen(
        uiState = defaultPreviewState.copy(
            exerciseName = "Squat",
            showEnter = false,
            showUpdate = true,
            showDelete = false
        ),
        actions = defaultPreviewActions
    )
}

@Preview(showBackground = true)
@Composable
fun ExEntryScreenPreviewDuplicateExerciseError() {
    ExEntryScreen(
        uiState = defaultPreviewState.copy(
            exerciseName = "Deadlift",
            duplicateExerciseError = "Deadlift exists"
        ),
        actions = defaultPreviewActions
    )
}

@Preview(showBackground = true)
@Composable
fun ExEntryScreenPreviewDumbbell() {
    ExEntryScreen(
        uiState = defaultPreviewState.copy(
            selectedEquipment = "Dumbbell",
            weight = "10.0"
        ),
        actions = defaultPreviewActions
    )
}

@Preview(showBackground = true)
@Composable
fun ExEntryScreenPreviewMinWeightMinusHidden() {
    ExEntryScreen(
        uiState = defaultPreviewState.copy(
            canDecrementWeight = false,
            weight = "45.0"
        ),
        actions = defaultPreviewActions
    )
}

@Preview(showBackground = true)
@Composable
fun ExEntryScreenPreviewLongExerciseName() {
    ExEntryScreen(
        uiState = defaultPreviewState.copy(
            exerciseName = "A very long exercise name that should fit nicely"
        ),
        actions = defaultPreviewActions
    )
}

@Preview(showBackground = true, device = "id:Nexus 5")
@Composable
fun ExEntryScreenPreviewSmallPhone360x800() {
    ExEntryScreen(uiState = defaultPreviewState, actions = defaultPreviewActions)
}

@Preview(showBackground = true, device = "id:pixel_4_xl")
@Composable
fun ExEntryScreenPreviewLargePhone412x915() {
    ExEntryScreen(uiState = defaultPreviewState, actions = defaultPreviewActions)
}

@Preview(showBackground = true, device = "id:automotive_1024p_landscape", widthDp = 800, heightDp = 360)
@Composable
fun ExEntryScreenPreviewLandscape() {
    ExEntryScreen(uiState = defaultPreviewState, actions = defaultPreviewActions)
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
fun ExEntryScreenPreviewFontScaleLarge() {
    ExEntryScreen(uiState = defaultPreviewState, actions = defaultPreviewActions)
}

@Preview(showBackground = true, widthDp = 250)
@Composable
fun ExEntryScreenPreviewNarrowWidth() {
    ExEntryScreen(uiState = defaultPreviewState, actions = defaultPreviewActions)
}
