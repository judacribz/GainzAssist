package ca.gainzassist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R

private val DropdownMenuCornerRadius = 14.dp
private val DropdownMenuElevation = 4.dp
private val DropdownMenuOuterHorizontalPadding = 8.dp
private val DropdownMenuOuterVerticalPadding = 8.dp
private val DropdownMenuItemSpacing = 4.dp
private val DropdownMenuItemMinHeight = 56.dp
private val DropdownMenuItemHorizontalPadding = 20.dp
private val DropdownMenuItemVerticalPadding = 8.dp
private val DropdownMenuItemCornerRadius = 8.dp
private val DropdownMenuItemTextSize = 18.sp
private val DropdownSelectedIconSize = 22.dp
private val DropdownMenuBorderWidth = 1.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GainzMaterialDropdown(
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(R.color.colorLightAccent),
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.dp,
                    color = colorResource(R.color.blueDark),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = enabled),
                textStyle = GainzMaterialFieldDefaults.textStyle(TextAlign.Center),
                shape = GainzMaterialFieldDefaults.Shape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedTextColor = colorResource(R.color.colorDarkText),
                    unfocusedTextColor = colorResource(R.color.colorDarkText),
                    disabledTextColor = colorResource(R.color.colorDarkText).copy(alpha = 0.5f)
                )
            )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .exposedDropdownSize()
                .border(
                    width = DropdownMenuBorderWidth,
                    color = colorResource(R.color.blueDark),
                    shape = RoundedCornerShape(DropdownMenuCornerRadius)
                ),
            shape = RoundedCornerShape(DropdownMenuCornerRadius),
            containerColor = colorResource(R.color.colorLightBg),
            tonalElevation = DropdownMenuElevation,
            shadowElevation = DropdownMenuElevation
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = DropdownMenuOuterHorizontalPadding,
                    vertical = DropdownMenuOuterVerticalPadding
                ),
                verticalArrangement = Arrangement.spacedBy(DropdownMenuItemSpacing)
            ) {
                options.forEach { option ->
                    val selected = option == selectedValue
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontFamily = GainzMaterialFieldDefaults.safeFontFamily(),
                                fontSize = DropdownMenuItemTextSize,
                                color = colorResource(R.color.colorDarkText),
                                maxLines = 1
                            )
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        trailingIcon = {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(DropdownSelectedIconSize),
                                    tint = colorResource(R.color.blueDark)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = DropdownMenuItemMinHeight)
                            .clip(RoundedCornerShape(DropdownMenuItemCornerRadius))
                            .background(
                                color = if (selected) {
                                    colorResource(R.color.colorLightAccent)
                                } else {
                                    Color.Transparent
                                }
                            ),
                        contentPadding = PaddingValues(
                            horizontal = DropdownMenuItemHorizontalPadding,
                            vertical = DropdownMenuItemVerticalPadding
                        )
                    )
                }
            }
        }
        }
        GainzFieldLabelOverlay(label = label)
    }
}

@Preview(showBackground = true)
@Composable
private fun GainzMaterialDropdownPreview() {
    GainzMaterialDropdown(
        selectedValue = "Barbell",
        options = listOf("Barbell", "Dumbbell", "N/A"),
        onOptionSelected = {}
    )
}
