package ca.gainzassist.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R

private val ExerciseButtonWidth = 112.dp
private val ExerciseButtonMinHeight = 84.dp
private val ExerciseButtonPadding = 8.dp
private val ExerciseButtonCornerRadius = 10.dp
private val ExerciseButtonMaxFontSize = 20.sp
private val ExerciseButtonMinFontSize = 14.sp
private val ExerciseButtonFontStep = 1.sp
private const val ExerciseButtonMaxLines = 3

@Composable
fun ExerciseSelectionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val staatlichesFont = FontFamily(Font(R.font.staatliches))
    val safeFontFamily = if (androidx.compose.ui.platform.LocalInspectionMode.current) {
        FontFamily.Default
    } else {
        try {
            staatlichesFont
        } catch (_: Exception) {
            FontFamily.Default
        }
    }

    Surface(
        modifier = modifier
            .width(ExerciseButtonWidth)
            .defaultMinSize(minHeight = ExerciseButtonMinHeight)
            .semantics(mergeDescendants = true) {
                this.text = AnnotatedString(text)
            },
        color = colorResource(R.color.blueDark),
        contentColor = colorResource(R.color.colorText),
        shape = RoundedCornerShape(ExerciseButtonCornerRadius),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.padding(ExerciseButtonPadding),
            contentAlignment = Alignment.Center
        ) {
            var fontSize by remember(text) { mutableStateOf(ExerciseButtonMaxFontSize) }
            
            Text(
                text = text,
                fontFamily = safeFontFamily,
                fontSize = fontSize,
                color = colorResource(R.color.colorText),
                textAlign = TextAlign.Center,
                maxLines = ExerciseButtonMaxLines,
                overflow = TextOverflow.Ellipsis,
                lineHeight = fontSize * 1.2f,
                onTextLayout = { textLayoutResult ->
                    if (textLayoutResult.hasVisualOverflow && fontSize > ExerciseButtonMinFontSize) {
                        fontSize = (fontSize.value - ExerciseButtonFontStep.value).sp
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExerciseSelectionButtonPreviewShort() {
    ExerciseSelectionButton(
        text = "SQUATS",
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ExerciseSelectionButtonPreviewLong() {
    ExerciseSelectionButton(
        text = "SEATED LEG EXTENSION",
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun ExerciseSelectionButtonPreviewVeryLong() {
    ExerciseSelectionButton(
        text = "SINGLE ARM DUMBBELL OVERHEAD TRICEP EXTENSION",
        onClick = {}
    )
}
