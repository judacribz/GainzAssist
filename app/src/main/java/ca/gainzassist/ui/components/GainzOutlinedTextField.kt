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

private val StaatlichesFont = FontFamily(
    Font(R.font.staatliches, FontWeight.Normal)
)

@Composable
fun GainzOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    textAlign: TextAlign = TextAlign.Center,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false,
    errorText: String? = null,
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

    val blue = colorResource(id = R.color.blue)
    val grey = colorResource(id = R.color.grey)
    val colorDarkText = colorResource(id = R.color.colorDarkText)
    val colorBg = colorResource(id = R.color.colorBg)

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isFloating = isFocused || value.isNotEmpty()

    val innerBorderColor = if (isError) Color.Red else blue

    val alignment = when (textAlign) {
        TextAlign.Start -> Alignment.CenterStart
        TextAlign.End -> Alignment.CenterEnd
        else -> Alignment.Center
    }

    Column(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 4.dp,
            color = grey
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color.Black, RoundedCornerShape(20.dp))
                    .padding(1.dp)
                    .border(3.dp, innerBorderColor, RoundedCornerShape(20.dp))
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxSize(),
                    interactionSource = interactionSource,
                    textStyle = TextStyle(
                        fontFamily = safeFontFamily,
                        fontSize = 30.sp,
                        textAlign = textAlign,
                        color = colorDarkText,
                    ),
                    singleLine = singleLine,
                    keyboardOptions = keyboardOptions,
                    cursorBrush = SolidColor(colorDarkText),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Floating Label at top-left
                            if (isFloating) {
                                Text(
                                    text = label,
                                    style = TextStyle(
                                        fontFamily = safeFontFamily,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 16.sp,
                                        color = colorBg.copy(alpha = 0.7f)
                                    ),
                                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                                )
                            }

                            // Input Area
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = alignment
                            ) {
                                if (!isFloating) {
                                    Text(
                                        text = label,
                                        style = TextStyle(
                                            fontFamily = safeFontFamily,
                                            fontSize = 30.sp,
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
                )
            }
        }

        if (isError && !errorText.isNullOrBlank()) {
            Text(
                text = errorText,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GainzOutlinedTextFieldPreview() {
    GainzOutlinedTextField(
        value = "Bench Press",
        onValueChange = {},
        label = "Exercise Name",
        textAlign = TextAlign.Start,
        modifier = Modifier.height(150.dp).padding(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun GainzOutlinedTextFieldEmptyPreview() {
    GainzOutlinedTextField(
        value = "",
        onValueChange = {},
        label = "Workout Name",
        modifier = Modifier.height(150.dp).padding(16.dp)
    )
}
