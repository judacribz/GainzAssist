package ca.gainzassist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R

@Composable
fun GainzDropdown(
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val colorDarkText = colorResource(id = R.color.colorDarkText)
    val colorLightBg = colorResource(id = R.color.colorLightBg)
    val colorAccent = colorResource(id = R.color.colorAccent)
    val colorLightAccent = colorResource(id = R.color.colorLightAccent)

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .background(colorLightAccent, RoundedCornerShape(5.dp))
            .border(2.dp, colorAccent, RoundedCornerShape(5.dp))
            .clickable(enabled = enabled) { expanded = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedValue,
                color = colorDarkText,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_drop_dark),
                contentDescription = "Dropdown",
                modifier = Modifier.size(24.dp),
                tint = colorDarkText
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colorLightBg)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = colorDarkText,
                            fontSize = 22.sp
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GainzDropdownPreview_Barbell() {
    GainzDropdown(
        selectedValue = "Barbell",
        options = listOf("Barbell", "Dumbbell", "N/A"),
        onOptionSelected = {}
    )
}

@Preview(showBackground = true)
@Composable
fun GainzDropdownPreview_Dumbbell() {
    GainzDropdown(
        selectedValue = "Dumbbell",
        options = listOf("Barbell", "Dumbbell", "N/A"),
        onOptionSelected = {}
    )
}

@Preview(showBackground = true)
@Composable
fun GainzDropdownPreview_NA() {
    GainzDropdown(
        selectedValue = "N/A",
        options = listOf("Barbell", "Dumbbell", "N/A"),
        onOptionSelected = {}
    )
}

@Preview(showBackground = true, widthDp = 150)
@Composable
fun GainzDropdownPreview_NarrowWidth() {
    GainzDropdown(
        selectedValue = "Barbell",
        options = listOf("Barbell", "Dumbbell", "N/A"),
        onOptionSelected = {}
    )
}
