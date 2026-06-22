package ca.gainzassist.feature.workout_entry.presentation.screen

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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
import ca.gainzassist.core.constants.ExerciseConst
import ca.gainzassist.ui.components.GainzButton
import ca.gainzassist.ui.components.GainzOutlinedTextField
import ca.gainzassist.ui.components.GainzTextFieldState

val Staatliches = FontFamily(
    Font(R.font.staatliches, FontWeight.Normal)
)

data class WorkoutEntryScreenActions(
    val onWorkoutNameChanged: (String) -> Unit = {},
    val onNumberOfExercisesChanged: (String) -> Unit = {},
    val onIncrementExercises: () -> Unit = {},
    val onDecrementExercises: () -> Unit = {},
    val onCancel: () -> Unit = {},
    val onContinueClicked: () -> Unit = {},
    val onBack: () -> Unit = {}
)

private val ScreenPadding = 4.dp
private val SectionSpacing = 4.dp
private val ToolbarHeight = 56.dp
private val BorderWidthLarge = 4.dp
private val BorderWidthSmall = 1.dp
private val CornerRadiusSmall = 2.dp
private val CornerRadiusMedium = 3.dp
private val CornerRadiusLarge = 5.dp
private val CornerRadiusXLarge = 20.dp
private val ShadowElevationMedium = 3.dp
private val TextShadowOffset = 1f
private val TextShadowBlurLarge = 5f
private val TextShadowBlurSmall = 2f
private val FontSizeTitle = 35.sp
private val FontSizeSubtitle = 20.sp
private val FontSizeError = 12.sp
private val ToolbarTextPaddingStart = 48.dp
private val CardPadding = 4.dp
private val ContentPadding = 8.dp
private val ButtonWidth = 80.dp
private val InputBoxWidth = 150.dp
private val ButtonVerticalPadding = 10.dp
private val FooterTopPadding = 10.dp
private val FooterHorizontalPadding = 2.dp
private val FooterSpacing = 4.dp
private val FooterButtonPaddingBottom = 5.dp
private const val WeightMd = 0.25f
private const val WeightLg = 0.5f
private const val WeightFull = 1f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutEntryScreen(
    workoutName: String,
    numberOfExercises: String,
    workoutNameErrorResId: Int?,
    numberOfExercisesErrorResId: Int?,
    actions: WorkoutEntryScreenActions = WorkoutEntryScreenActions()
) {
    val workoutNameError = workoutNameErrorResId?.let { stringResource(it) }
    val numberOfExercisesError = numberOfExercisesErrorResId?.let { stringResource(it) }
    val blue = colorResource(R.color.blue)
    val grey = colorResource(R.color.grey)
    val colorLightBg = colorResource(R.color.colorLightBg)
    val colorBg = colorResource(R.color.colorBg)

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
                .padding(ScreenPadding)
        ) {
            // Toolbar
            CustomToolbar(onBack = actions.onBack, fontFamily = safeFontFamily)

            Spacer(modifier = Modifier.height(SectionSpacing))

            // Workout Name Section
            WorkoutNameSection(
                modifier = Modifier.weight(WeightMd), // Matches ll_md_weight
                workoutName = workoutName,
                workoutNameError = workoutNameError,
                onWorkoutNameChanged = actions.onWorkoutNameChanged,
                colorBg = colorBg
            )

            Spacer(modifier = Modifier.height(SectionSpacing))

            // Number of Exercises Section
            NumExercisesSection(
                modifier = Modifier.weight(WeightLg), // Matches ll_lg_weight
                state = NumExercisesState(
                    numberOfExercises = numberOfExercises,
                    numberOfExercisesError = numberOfExercisesError,
                    grey = grey,
                    blue = blue,
                    colorBg = colorBg,
                    fontFamily = safeFontFamily
                ),
                actions = NumExercisesActions(
                    onNumberOfExercisesChanged = actions.onNumberOfExercisesChanged,
                    onIncrementExercises = actions.onIncrementExercises,
                    onDecrementExercises = actions.onDecrementExercises
                )
            )

            Spacer(modifier = Modifier.height(SectionSpacing))

            // Footer Section
            FooterSection(
                modifier = Modifier.weight(WeightMd), // Matches ll_md_weight
                isNameEmpty = workoutName.trim().isEmpty(),
                onCancel = actions.onCancel,
                onEnter = actions.onContinueClicked,
                fontFamily = safeFontFamily
            )
        }
    }
}

@Composable
fun CustomToolbar(onBack: () -> Unit, fontFamily: FontFamily) {
    val colorBg = colorResource(R.color.colorBg)
    val colorLightAccent = colorResource(R.color.colorLightAccent)
    val colorLightBg = colorResource(R.color.colorLightBg)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ToolbarHeight)
            .background(colorLightAccent, RoundedCornerShape(CornerRadiusMedium))
            .padding(bottom = CornerRadiusSmall)
            .background(colorLightBg, RoundedCornerShape(CornerRadiusSmall))
            .border(BorderWidthSmall, colorBg, RoundedCornerShape(CornerRadiusSmall))
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                tint = colorBg
            )
        }
        Text(
            text = stringResource(R.string.add_workout),
            style = TextStyle(
                fontFamily = fontFamily,
                fontSize = FontSizeTitle,
                color = colorBg,
                shadow = Shadow(
                    color = colorBg,
                    offset = Offset(TextShadowOffset, TextShadowOffset),
                    blurRadius = TextShadowBlurLarge
                )
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = ToolbarTextPaddingStart)
                .fillMaxWidth(),
            textAlign = TextAlign.Start
        )
    }
}

@Composable
fun WorkoutNameSection(
    modifier: Modifier = Modifier,
    workoutName: String,
    workoutNameError: String?,
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
            state = GainzTextFieldState(
                value = workoutName,
                label = stringResource(R.string.hint_workout_name).trim(),
                isError = workoutNameError != null,
                errorText = workoutNameError
            ),
            onValueChange = onWorkoutNameChanged,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxSize()
                .padding(ContentPadding)
        )
    }
}

data class NumExercisesState(
    val numberOfExercises: String,
    val numberOfExercisesError: String?,
    val grey: Color,
    val blue: Color,
    val colorBg: Color,
    val fontFamily: FontFamily
)

data class NumExercisesActions(
    val onNumberOfExercisesChanged: (String) -> Unit,
    val onIncrementExercises: () -> Unit,
    val onDecrementExercises: () -> Unit
)

@Composable
fun NumExercisesSection(
    state: NumExercisesState,
    actions: NumExercisesActions,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(CardPadding)
            .shadow(ShadowElevationMedium, RoundedCornerShape(CornerRadiusLarge)),
        shape = RoundedCornerShape(CornerRadiusLarge),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(BorderWidthSmall, state.colorBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(ContentPadding)
        ) {
            Text(
                text = stringResource(R.string.num_of_exercises).uppercase(),
                style = TextStyle(
                    fontFamily = state.fontFamily,
                    fontSize = FontSizeSubtitle,
                    color = state.colorBg,
                    shadow = Shadow(
                        color = colorResource(R.color.greenDark),
                        offset = Offset(TextShadowOffset, TextShadowOffset),
                        blurRadius = TextShadowBlurSmall
                    )
                ),
                modifier = Modifier.padding(bottom = ContentPadding)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(WeightFull),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Minus Button - Hidden when disabled to match legacy View.GONE behavior
                val numExercisesInt = state.numberOfExercises.toIntOrNull() ?: ExerciseConst.MIN_INT
                if (numExercisesInt > ExerciseConst.MIN_INT) {
                    GainzButton(
                        text = "-",
                        onClick = actions.onDecrementExercises,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(ButtonWidth)
                            .padding(vertical = ButtonVerticalPadding)
                    )
                } else {
                    Spacer(modifier = Modifier.width(ButtonWidth))
                }

                // Number Display
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(InputBoxWidth)
                        .background(state.grey, RoundedCornerShape(CornerRadiusXLarge))
                        .border(BorderWidthLarge, state.blue, RoundedCornerShape(CornerRadiusXLarge)),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = state.numberOfExercises,
                        onValueChange = {
                            val newValueStr = it.filter { char -> char.isDigit() }.take(3)
                            actions.onNumberOfExercisesChanged(newValueStr)
                        },
                        textStyle = TextStyle(
                            fontFamily = state.fontFamily,
                            fontSize = FontSizeTitle,
                            color = state.colorBg,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        cursorBrush = SolidColor(state.colorBg)
                    )
                }

                // Plus Button
                GainzButton(
                    text = "+",
                    onClick = actions.onIncrementExercises,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(ButtonWidth)
                        .padding(vertical = ButtonVerticalPadding)
                )
            }

            if (state.numberOfExercisesError != null) {
                Text(
                    text = state.numberOfExercisesError,
                    color = Color.Red,
                    fontSize = FontSizeError,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = CardPadding)
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
            .padding(top = FooterTopPadding, start = FooterHorizontalPadding, end = FooterHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(FooterSpacing)
    ) {
        GainzButton(
            text = stringResource(R.string.cancel),
            onClick = onCancel,
            modifier = Modifier
                .weight(WeightFull)
                .fillMaxHeight()
                .padding(bottom = FooterButtonPaddingBottom),
            fontFamily = fontFamily
        )

        GainzButton(
            text = if (isNameEmpty) {
                stringResource(R.string.skip)
            } else {
                stringResource(R.string.enter)
            },
            onClick = onEnter,
            modifier = Modifier
                .weight(WeightFull)
                .fillMaxHeight()
                .padding(bottom = FooterButtonPaddingBottom),
            fontFamily = fontFamily
        )
    }
}

@Preview(showBackground = true, name = "Empty Name, Skip, 3 Exercises")
@Composable
private fun WorkoutEntryScreenPreviewEmptyNameSkipThreeExercises() {
    WorkoutEntryScreen(
        workoutName = "",
        numberOfExercises = "3",
        workoutNameErrorResId = null,
        numberOfExercisesErrorResId = null
    )
}

@Preview(showBackground = true, name = "With Name, Enter, 5 Exercises")
@Composable
private fun WorkoutEntryScreenPreviewWithNameEnterFiveExercises() {
    WorkoutEntryScreen(
        workoutName = "Push Day",
        numberOfExercises = "5",
        workoutNameErrorResId = null,
        numberOfExercisesErrorResId = null
    )
}

@Preview(showBackground = true, name = "Min Exercise Count, Disabled Minus")
@Composable
private fun WorkoutEntryScreenPreviewMinExerciseCountDisabledMinus() {
    WorkoutEntryScreen(
        workoutName = "",
        numberOfExercises = "1",
        workoutNameErrorResId = null,
        numberOfExercisesErrorResId = null
    )
}

@Preview(showBackground = true, name = "Long Workout Name")
@Composable
private fun WorkoutEntryScreenPreviewLongWorkoutName() {
    WorkoutEntryScreen(
        workoutName = "Very Long Workout Name to Test Layout",
        numberOfExercises = "3",
        workoutNameErrorResId = null,
        numberOfExercisesErrorResId = null
    )
}

@Preview(showBackground = true, device = "spec:width=360dp,height=800dp", name = "Small Phone")
@Composable
private fun WorkoutEntryScreenPreviewSmallPhone360x800() {
    WorkoutEntryScreenPreviewEmptyNameSkipThreeExercises()
}

@Preview(showBackground = true, device = "spec:width=412dp,height=915dp", name = "Large Phone")
@Composable
private fun WorkoutEntryScreenPreviewLargePhone412x915() {
    WorkoutEntryScreenPreviewEmptyNameSkipThreeExercises()
}

@Preview(
    showBackground = true,
    device = "spec:width=800dp,height=360dp,orientation=landscape",
    name = "Landscape"
)
@Composable
private fun WorkoutEntryScreenPreviewLandscape() {
    WorkoutEntryScreenPreviewEmptyNameSkipThreeExercises()
}

@Preview(showBackground = true, fontScale = 1.5f, name = "Large Font Scale")
@Composable
private fun WorkoutEntryScreenPreviewFontScaleLarge() {
    WorkoutEntryScreenPreviewEmptyNameSkipThreeExercises()
}

@Preview(showBackground = true, device = "spec:width=320dp,height=640dp", name = "Narrow Width")
@Composable
private fun WorkoutEntryScreenPreviewNarrowWidth() {
    WorkoutEntryScreenPreviewEmptyNameSkipThreeExercises()
}

@Preview(showBackground = true, device = "spec:width=360dp,height=1000dp", name = "Tall Phone")
@Composable
private fun WorkoutEntryScreenPreviewTallPhone() {
    WorkoutEntryScreenPreviewEmptyNameSkipThreeExercises()
}
