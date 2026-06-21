package ca.gainzassist.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R

private val CornerRadius = 20.dp
private val ShadowElevation = 4.dp
private val OuterBorderWidth = 1.dp
private val InnerBorderWidth = 3.dp
private val BoxPadding = 1.dp
private val InputFontSize = 30.sp
private val LabelFontSize = 16.sp
private val LabelPaddingStart = 16.dp
private val LabelPaddingTop = 8.dp
private val InputPaddingHorizontal = 16.dp
private val InputPaddingVertical = 8.dp
private val ErrorFontSize = 12.sp
private val ErrorPaddingTop = 2.dp

private val StaatlichesFont = FontFamily(
    Font(R.font.staatliches, FontWeight.Normal)
)

data class GainzTextFieldState(
    val value: String,
    val label: String = "",
    val isError: Boolean = false,
    val errorText: String? = null
)

@Composable
fun GainzOutlinedTextField(
    state: GainzTextFieldState,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    textAlign: TextAlign = TextAlign.Center,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    val safeFontFamily = if (androidx.compose.ui.platform.LocalInspectionMode.current) {
        FontFamily.Default
    } else {
        try {
            StaatlichesFont
        } catch (_: Exception) {
            FontFamily.Default
        }
    }
    val blue = colorResource(R.color.blue)
    val grey = colorResource(R.color.grey)
    val colorDarkText = colorResource(R.color.colorDarkText)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isFloating = isFocused || state.value.isNotEmpty()
    val innerBorderColor = if (state.isError) Color.Red else blue
    val alignment = when (textAlign) {
        TextAlign.Start -> Alignment.CenterStart
        TextAlign.End -> Alignment.CenterEnd
        else -> Alignment.Center
    }

    Column(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(CornerRadius),
            shadowElevation = ShadowElevation,
            color = grey
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(OuterBorderWidth, Color.Black, RoundedCornerShape(CornerRadius))
                    .padding(BoxPadding)
                    .border(InnerBorderWidth, innerBorderColor, RoundedCornerShape(CornerRadius))
            ) {
                BasicTextField(
                    value = state.value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxSize(),
                    interactionSource = interactionSource,
                    textStyle = TextStyle(
                        fontFamily = safeFontFamily,
                        fontSize = InputFontSize,
                        textAlign = textAlign,
                        color = colorDarkText,
                    ),
                    singleLine = singleLine,
                    keyboardOptions = keyboardOptions,
                    cursorBrush = SolidColor(colorDarkText),
                    decorationBox = { innerTextField ->
                        GainzTextFieldDecorationBox(
                            isFloating = isFloating,
                            label = state.label,
                            safeFontFamily = safeFontFamily,
                            textAlign = textAlign,
                            alignment = alignment,
                            innerTextField = innerTextField
                        )
                    }
                )
            }
        }

        if (state.isError && !state.errorText.isNullOrBlank()) {
            Text(
                text = state.errorText,
                color = Color.Red,
                fontSize = ErrorFontSize,
                modifier = Modifier.padding(start = LabelPaddingStart, top = ErrorPaddingTop)
            )
        }
    }
}

@Composable
private fun GainzTextFieldDecorationBox(
    isFloating: Boolean,
    label: String,
    safeFontFamily: FontFamily,
    textAlign: TextAlign,
    alignment: Alignment,
    innerTextField: @Composable () -> Unit
) {
    val colorBg = colorResource(R.color.colorBg)
    val colorDarkText = colorResource(R.color.colorDarkText)

    Box(modifier = Modifier.fillMaxSize()) {
        // Floating Label at top-left
        if (isFloating) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = safeFontFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = LabelFontSize,
                    color = colorBg.copy(alpha = 0.7f)
                ),
                modifier = Modifier.padding(start = LabelPaddingStart, top = LabelPaddingTop)
            )
        }

        // Input Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = InputPaddingHorizontal, vertical = InputPaddingVertical),
            contentAlignment = alignment
        ) {
            if (!isFloating) {
                Text(
                    text = label,
                    style = TextStyle(
                        fontFamily = safeFontFamily,
                        fontSize = InputFontSize,
                        textAlign = textAlign,
                        color = colorDarkText.copy(alpha = 0.5f),
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            innerTextField()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GainzOutlinedTextFieldPreview() {
    GainzOutlinedTextField(
        state = GainzTextFieldState(
            value = "Bench Press",
            label = "Exercise Name"
        ),
        onValueChange = {},
        textAlign = TextAlign.Start,
        modifier = Modifier.height(150.dp).padding(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun GainzOutlinedTextFieldEmptyPreview() {
    GainzOutlinedTextField(
        state = GainzTextFieldState(
            value = "",
            label = "Workout Name"
        ),
        onValueChange = {},
        modifier = Modifier.height(150.dp).padding(16.dp)
    )
}
