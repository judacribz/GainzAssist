package ca.gainzassist.activities.add_workout

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
import androidx.compose.ui.platform.LocalInspectionMode
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

@Composable
fun ExEntryScreen(
    uiState: ExEntryUiState,
    actions: ExEntryActions,
    modifier: Modifier = Modifier
) {
    val staatliches = if (LocalInspectionMode.current) {
        FontFamily.Default
    } else {
        try {
            FontFamily(Font(R.font.staatliches))
        } catch (_: Exception) {
            FontFamily.Default
        }
    }
    val colorBg = colorResource(id = R.color.colorBg) // black
    val colorLightBg = colorResource(id = R.color.colorLightBg) // white
    val colorText = colorResource(id = R.color.colorText) // white
    val colorDarkText = colorResource(id = R.color.colorDarkText) // #333333
    val colorBlue = colorResource(id = R.color.blue)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorLightBg)
            .padding(4.dp)
    ) {
        // Exercise Name Section (weight 4)
        Box(
            modifier = Modifier
                .weight(4f)
                .fillMaxWidth()
                .padding(4.dp)
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(5.dp))
                .background(colorLightBg, RoundedCornerShape(5.dp))
                .border(0.5.dp, colorBg, RoundedCornerShape(5.dp))
                .padding(4.dp)
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
                .weight(3f)
                .fillMaxWidth()
                .padding(4.dp)
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(5.dp))
                .background(colorLightBg, RoundedCornerShape(5.dp))
                .border(0.5.dp, colorBg, RoundedCornerShape(5.dp))
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.equipment),
                    fontFamily = staatliches,
                    fontSize = 18.sp,
                    color = colorDarkText,
                    modifier = Modifier.padding(5.dp),
                    textAlign = TextAlign.Center
                )

                GainzDropdown(
                    selectedValue = uiState.selectedEquipment,
                    options = uiState.equipmentOptions,
                    onOptionSelected = actions::onEquipmentSelected,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                )
            }
        }

        // Weight/Reps/Sets Section (weight 10)
        Column(
            modifier = Modifier
                .weight(10f)
                .fillMaxWidth()
                .padding(4.dp)
                .shadow(elevation = 3.dp, shape = RoundedCornerShape(5.dp))
                .background(colorLightBg, RoundedCornerShape(5.dp))
                .border(0.5.dp, colorBg, RoundedCornerShape(5.dp))
                .padding(4.dp)
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
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Decrement Button
                    if (canDecrement) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(2.dp)
                                .shadow(elevation = 2.dp, shape = RoundedCornerShape(15.dp))
                                .background(colorBlue, RoundedCornerShape(15.dp))
                                .clickable { onDecrement() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", color = colorText, fontSize = 40.sp, fontFamily = staatliches, textAlign = TextAlign.Center)
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Value Input
                    GainzOutlinedTextField(
                        value = value,
                        onValueChange = onValueChange,
                        label = "",
                        modifier = Modifier
                            .weight(1.5f)
                            .fillMaxHeight()
                            .padding(2.dp),
                        textAlign = TextAlign.Center,
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                        isError = errorText != null,
                        errorText = errorText
                    )

                    // Increment Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(2.dp)
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(15.dp))
                            .background(colorBlue, RoundedCornerShape(15.dp))
                            .clickable { onIncrement() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = colorText, fontSize = 40.sp, fontFamily = staatliches, textAlign = TextAlign.Center)
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
                modifier = Modifier.weight(3f)
            )
            NumberRow(
                value = uiState.reps,
                errorText = uiState.repsError,
                onValueChange = actions::onRepsChanged,
                canDecrement = uiState.canDecrementReps,
                onDecrement = actions::onDecrementReps,
                onIncrement = actions::onIncrementReps,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(3f)
            )
            NumberRow(
                value = uiState.sets,
                errorText = uiState.setsError,
                onValueChange = actions::onSetsChanged,
                canDecrement = uiState.canDecrementSets,
                onDecrement = actions::onDecrementSets,
                onIncrement = actions::onIncrementSets,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(3f)
            )
        }

        // Footer Section (weight 4)
        Row(
            modifier = Modifier
                .weight(4f)
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 5.dp, start = 2.dp, end = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (uiState.showDelete) {
                GainzButton(
                    text = stringResource(id = R.string.delete).uppercase(),
                    onClick = actions::onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp),
                    fontFamily = staatliches
                )
            } else {
                Spacer(modifier = Modifier.weight(1f).padding(horizontal = 2.dp))
            }

            if (uiState.showEnter) {
                GainzButton(
                    text = stringResource(id = R.string.enter).uppercase(),
                    onClick = actions::onEnter,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp),
                    fontFamily = staatliches
                )
            } else if (uiState.showUpdate) {
                GainzButton(
                    text = stringResource(id = R.string.update).uppercase(),
                    onClick = actions::onUpdate,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp),
                    fontFamily = staatliches
                )
            } else {
                Spacer(modifier = Modifier.weight(1f).padding(horizontal = 2.dp))
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
    override fun onExerciseNameChanged(name: String) {}
    override fun onEquipmentSelected(equipment: String) {}
    override fun onWeightChanged(weight: String) {}
    override fun onRepsChanged(reps: String) {}
    override fun onSetsChanged(sets: String) {}
    override fun onIncrementWeight() {}
    override fun onDecrementWeight() {}
    override fun onIncrementReps() {}
    override fun onDecrementReps() {}
    override fun onIncrementSets() {}
    override fun onDecrementSets() {}
    override fun onEnter() {}
    override fun onUpdate() {}
    override fun onDelete() {}
}

@Preview(showBackground = true)
@Composable
fun ExEntryScreenPreview_NewExercise_DefaultBarbell() {
    ExEntryScreen(uiState = defaultPreviewState, actions = defaultPreviewActions)
}

@Preview(showBackground = true)
@Composable
fun ExEntryScreenPreview_ExistingExercise_UpdateMode() {
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
fun ExEntryScreenPreview_DeleteHidden() {
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
fun ExEntryScreenPreview_DuplicateExerciseError() {
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
fun ExEntryScreenPreview_Dumbbell() {
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
fun ExEntryScreenPreview_MinWeightMinusHidden() {
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
fun ExEntryScreenPreview_LongExerciseName() {
    ExEntryScreen(
        uiState = defaultPreviewState.copy(
            exerciseName = "A very long exercise name that should fit nicely"
        ),
        actions = defaultPreviewActions
    )
}

@Preview(showBackground = true, device = "id:Nexus 5")
@Composable
fun ExEntryScreenPreview_SmallPhone_360x800() {
    ExEntryScreen(uiState = defaultPreviewState, actions = defaultPreviewActions)
}

@Preview(showBackground = true, device = "id:pixel_4_xl")
@Composable
fun ExEntryScreenPreview_LargePhone_412x915() {
    ExEntryScreen(uiState = defaultPreviewState, actions = defaultPreviewActions)
}

@Preview(showBackground = true, device = "id:automotive_1024p_landscape", widthDp = 800, heightDp = 360)
@Composable
fun ExEntryScreenPreview_Landscape() {
    ExEntryScreen(uiState = defaultPreviewState, actions = defaultPreviewActions)
}

@Preview(showBackground = true, fontScale = 1.5f)
@Composable
fun ExEntryScreenPreview_FontScaleLarge() {
    ExEntryScreen(uiState = defaultPreviewState, actions = defaultPreviewActions)
}

@Preview(showBackground = true, widthDp = 250)
@Composable
fun ExEntryScreenPreview_NarrowWidth() {
    ExEntryScreen(uiState = defaultPreviewState, actions = defaultPreviewActions)
}
