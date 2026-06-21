package ca.gainzassist.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val EdgePadding = 0.dp
private val IndicatorHeight = 2.dp
private val TabPaddingStart = 16.dp
private val TabPaddingVerticalIcon = 8.dp
private val TabPaddingVerticalNoIcon = 14.dp
private val TabIconSize = 24.dp
private val TabIconPaddingBottom = 4.dp
private val TabFontSize = 10.sp
private val PlusIconSize = 16.dp
private const val RippleDelayMs = 100L
private const val FontWeightThreshold = 0.5f

data class GainzTabItem(val title: String, val iconResId: Int? = null)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GainzTabRow(
    pagerState: PagerState,
    tabs: List<GainzTabItem>,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    onTabClick: ((Int) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()

    if (scrollable) {
        SecondaryScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = EdgePadding,
            modifier = modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorResource(R.color.blue),
                            colorResource(R.color.colorBg),
                            colorResource(R.color.colorBg)
                        )
                    )
                ),
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                    color = colorResource(R.color.blue),
                    height = IndicatorHeight
                )
            },
            divider = {
                androidx.compose.material3.HorizontalDivider(color = Color.Black)
            }
        ) {
            GainzTabItems(
                tabs = tabs,
                pagerState = pagerState,
                coroutineScope = coroutineScope,
                onTabClick = onTabClick,
                scrollable = true
            )
        }
    } else {
        SecondaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorResource(R.color.blue),
                            colorResource(R.color.colorBg),
                            colorResource(R.color.colorBg)
                        )
                    )
                ),
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(pagerState.currentPage),
                    color = colorResource(R.color.blue),
                    height = IndicatorHeight
                )
            },
            divider = {
                androidx.compose.material3.HorizontalDivider(color = Color.Black)
            }
        ) {
            GainzTabItems(
                tabs = tabs,
                pagerState = pagerState,
                coroutineScope = coroutineScope,
                onTabClick = onTabClick,
                scrollable = false
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GainzTabItems(
    tabs: List<GainzTabItem>,
    pagerState: PagerState,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onTabClick: ((Int) -> Unit)?,
    scrollable: Boolean
) {
    tabs.forEachIndexed { index, tab ->
        val isSelected = pagerState.currentPage == index
        GainzTabItemView(
            tab = tab,
            index = index,
            isSelected = isSelected,
            scrollable = scrollable,
            onTabClick = { clickedIndex ->
                if (onTabClick != null) {
                    onTabClick(clickedIndex)
                } else {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(clickedIndex)
                    }
                }
            }
        )
    }
}

@Composable
private fun GainzTabItemView(
    tab: GainzTabItem,
    index: Int,
    isSelected: Boolean,
    scrollable: Boolean,
    onTabClick: (Int) -> Unit
) {
    val color by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) colorResource(R.color.blue) else Color.White,
        label = "tabColorAnim"
    )
    val distance by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSelected) 0f else 1f,
        label = "tabDistanceAnim"
    )

    val isPlusTab = tab.iconResId != null && tab.title.isBlank()
    val tabWidthModifier = if (scrollable) {
        if (isPlusTab) {
            Modifier
                .wrapContentWidth(Alignment.Start)
                .padding(start = TabPaddingStart)
        } else {
            Modifier.wrapContentWidth()
        }
    } else {
        Modifier.fillMaxWidth()
    }

    val verticalPadding = if (tab.iconResId != null && tab.title.isNotBlank()) {
        TabPaddingVerticalIcon
    } else {
        TabPaddingVerticalNoIcon
    }

    Box(
        modifier = tabWidthModifier
            .instantClickable { onTabClick(index) }
            .padding(vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        GainzTabItemContent(tab, distance, color)
    }
}

@Composable
private fun GainzTabItemContent(tab: GainzTabItem, distance: Float, color: Color) {
    if (tab.iconResId != null && tab.title.isNotBlank()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(tab.iconResId),
                contentDescription = tab.title,
                tint = color,
                modifier = Modifier
                    .padding(bottom = TabIconPaddingBottom)
                    .size(TabIconSize)
            )
            Text(
                text = tab.title.uppercase(),
                color = color,
                fontSize = TabFontSize,
                fontWeight = if (distance < FontWeightThreshold) FontWeight.Bold else FontWeight.Normal
            )
        }
    } else if (tab.iconResId != null && tab.title.isBlank()) {
        Icon(
            painter = painterResource(tab.iconResId),
            contentDescription = stringResource(R.string.cd_add),
            tint = color,
            modifier = Modifier.size(PlusIconSize)
        )
    } else {
        Text(
            text = tab.title,
            color = color
        )
    }
}

@Composable
private fun Modifier.instantClickable(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = LocalIndication.current
    val coroutineScope = rememberCoroutineScope()

    return this
        .indication(interactionSource, indication)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { offset ->
                    val press = PressInteraction.Press(offset)
                    val rippleJob = coroutineScope.launch {
                        interactionSource.emit(press)
                        delay(RippleDelayMs.milliseconds) // Minimum ripple visibility duration
                    }

                    val released = tryAwaitRelease()
                    if (released) {
                        coroutineScope.launch {
                            rippleJob.join()
                            interactionSource.emit(PressInteraction.Release(press))
                        }
                    } else {
                        coroutineScope.launch {
                            rippleJob.join()
                            interactionSource.emit(PressInteraction.Cancel(press))
                        }
                    }
                },
                onTap = {
                    onClick()
                }
            )
        }
}
