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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R

private val TopBarPaddingBottom = 2.dp
private val TopBarCornerRadiusBg = 3.dp
private val TopBarHeight = 56.dp
private val TopBarCornerRadius = 2.dp
private val TopBarBorderWidth = 1.dp
private val TopBarPaddingHorizontal = 4.dp
private val TopBarSpacerWidth = 12.dp
private val TopBarTitleFontSize = 35.sp
private val TopBarShadowOffset = 1f
private val TopBarShadowBlur = 5f
private val TopBarTitleMaxLines = 2

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
            .padding(bottom = TopBarPaddingBottom)
            .background(
                color = colorResource(R.color.colorLightAccent),
                shape = RoundedCornerShape(TopBarCornerRadiusBg)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(TopBarHeight)
                .background(
                    color = colorResource(id = R.color.colorLightBg),
                    shape = RoundedCornerShape(TopBarCornerRadius)
                )
                .border(
                    width = TopBarBorderWidth,
                    color = colorResource(R.color.colorBg),
                    shape = RoundedCornerShape(TopBarCornerRadius)
                )
                .padding(horizontal = TopBarPaddingHorizontal),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        tint = colorResource(R.color.colorBg)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(TopBarSpacerWidth))
            }

            Text(
                text = title,
                style = TextStyle(
                    fontSize = TopBarTitleFontSize,
                    color = colorResource(R.color.colorBg),
                    fontWeight = FontWeight.Normal,
                    shadow = Shadow(
                        color = colorResource(R.color.colorBg),
                        offset = Offset(TopBarShadowOffset, TopBarShadowOffset),
                        blurRadius = TopBarShadowBlur
                    )
                ),
                maxLines = TopBarTitleMaxLines,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (showBack) 0.dp else TopBarPaddingHorizontal)
            )

            actions()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GainzTopBarPreviewTitleOnly() {
    GainzTopBar(
        title = "Gainz Assist"
    )
}

@Preview(showBackground = true)
@Composable
fun GainzTopBarPreviewWithBack() {
    GainzTopBar(
        title = "Exercises Entry",
        showBack = true
    )
}

@Preview(showBackground = true)
@Composable
fun GainzTopBarPreviewLongTitle() {
    GainzTopBar(
        title = "This is a very long title that should not break the layout",
        showBack = true
    )
}
