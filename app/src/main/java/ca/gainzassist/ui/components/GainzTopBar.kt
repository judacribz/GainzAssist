package ca.gainzassist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R

@Composable
fun GainzTopBar(
    title: String,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp)
            .background(colorResource(id = R.color.colorLightAccent), RoundedCornerShape(3.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(colorResource(id = R.color.colorLightBg), RoundedCornerShape(2.dp))
                .border(1.dp, colorResource(id = R.color.colorBg), RoundedCornerShape(2.dp))
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorResource(id = R.color.colorBg)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = title,
                style = TextStyle(
                    fontSize = 35.sp,
                    color = colorResource(id = R.color.colorBg),
                    fontWeight = FontWeight.Normal,
                    shadow = Shadow(
                        color = colorResource(id = R.color.colorBg),
                        offset = Offset(1f, 1f),
                        blurRadius = 5f
                    )
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (showBack) 0.dp else 4.dp)
            )

            actions()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GainzTopBarPreview_TitleOnly() {
    GainzTopBar(
        title = "Gainz Assist"
    )
}

@Preview(showBackground = true)
@Composable
fun GainzTopBarPreview_WithBack() {
    GainzTopBar(
        title = "Exercises Entry",
        showBack = true
    )
}

@Preview(showBackground = true)
@Composable
fun GainzTopBarPreview_LongTitle() {
    GainzTopBar(
        title = "This is a very long title that should not break the layout",
        showBack = true
    )
}
