package ca.gainzassist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R

private val MaterialFieldLabelCornerRadius = 4.dp
private val MaterialFieldLabelBorderWidth = 1.dp
private val MaterialFieldLabelHorizontalPadding = 4.dp
private val MaterialFieldLabelVerticalPadding = 1.dp
private val MaterialFieldLabelFontSize = 14.sp
private val MaterialFieldLabelStartPadding = 16.dp
private val MaterialFieldLabelVerticalOffset = 8.dp
private val MaterialFieldInputLineHeight = 40.sp
private val MaterialFieldLabelLineHeight = 16.sp

@Composable
fun GainzMaterialOutlinedTextField(
    state: GainzTextFieldState,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    textAlign: TextAlign = TextAlign.Center,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Box(modifier = modifier) {
        val existingTextStyle = GainzMaterialFieldDefaults.textStyle(textAlign)
        OutlinedTextField(
            value = state.value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxSize(),
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            isError = state.isError,
            textStyle = existingTextStyle.copy(
                lineHeight = MaterialFieldInputLineHeight,
                platformStyle = PlatformTextStyle(
                    includeFontPadding = true
                )
            ),
            label = null,
            supportingText = {
                if (state.isError && !state.errorText.isNullOrBlank()) {
                    Text(text = state.errorText)
                }
            },
            shape = GainzMaterialFieldDefaults.Shape,
            colors = GainzMaterialFieldDefaults.colors()
        )

        GainzFieldLabelOverlay(label = state.label)
    }
}

@Composable
fun GainzFieldLabelOverlay(label: String) {
    if (label.isNotEmpty()) {
        Box(
            modifier = Modifier
                .padding(start = MaterialFieldLabelStartPadding)
                .offset(y = -MaterialFieldLabelVerticalOffset)
                .background(
                    color = colorResource(R.color.grey),
                    shape = RoundedCornerShape(MaterialFieldLabelCornerRadius)
                )
                .border(
                    width = MaterialFieldLabelBorderWidth,
                    color = colorResource(R.color.blueDark),
                    shape = RoundedCornerShape(MaterialFieldLabelCornerRadius)
                )
                .padding(
                    horizontal = MaterialFieldLabelHorizontalPadding,
                    vertical = MaterialFieldLabelVerticalPadding
                )
        ) {
            Text(
                text = label,
                fontFamily = GainzMaterialFieldDefaults.safeFontFamily(),
                fontSize = MaterialFieldLabelFontSize,
                lineHeight = MaterialFieldLabelLineHeight,
                color = colorResource(R.color.blueDark)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GainzMaterialOutlinedTextFieldPreview() {
    GainzMaterialOutlinedTextField(
        state = GainzTextFieldState(
            value = "Bench Press",
            label = "Exercise Name"
        ),
        onValueChange = {}
    )
}
