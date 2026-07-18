package ca.gainzassist.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R

internal object GainzMaterialFieldDefaults {
    val CornerRadius = 20.dp
    val Shape = RoundedCornerShape(CornerRadius)
    
    val InputFontSize = 30.sp
    
    val StaatlichesFont = FontFamily(
        Font(R.font.staatliches, FontWeight.Normal)
    )

    @Composable
    fun safeFontFamily(): FontFamily {
        return if (LocalInspectionMode.current) {
            FontFamily.Default
        } else {
            try {
                StaatlichesFont
            } catch (_: Exception) {
                FontFamily.Default
            }
        }
    }

    @Composable
    fun textStyle(textAlign: TextAlign = TextAlign.Center): TextStyle {
        return TextStyle(
            fontFamily = safeFontFamily(),
            fontSize = InputFontSize,
            textAlign = textAlign,
            color = colorResource(R.color.colorDarkText)
        )
    }

    @Composable
    fun colors() = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colorResource(R.color.grey),
        unfocusedContainerColor = colorResource(R.color.grey),
        disabledContainerColor = colorResource(R.color.grey),
        errorContainerColor = colorResource(R.color.grey),
        
        focusedBorderColor = colorResource(R.color.blue),
        unfocusedBorderColor = colorResource(R.color.blue),
        disabledBorderColor = colorResource(R.color.blue).copy(alpha = 0.5f),
        errorBorderColor = Color.Red,
        
        focusedTextColor = colorResource(R.color.colorDarkText),
        unfocusedTextColor = colorResource(R.color.colorDarkText),
        disabledTextColor = colorResource(R.color.colorDarkText).copy(alpha = 0.5f),
        errorTextColor = colorResource(R.color.colorDarkText),
        
        focusedLabelColor = colorResource(R.color.colorBg).copy(alpha = 0.7f),
        unfocusedLabelColor = colorResource(R.color.colorDarkText).copy(alpha = 0.5f),
        errorLabelColor = Color.Red
    )
}
