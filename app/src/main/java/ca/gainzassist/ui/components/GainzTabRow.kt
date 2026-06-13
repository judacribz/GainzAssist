@file:Suppress("kotlin:S107", "kotlin:S109", "kotlin:S1192", "kotlin:S138", "kotlin:S3776", "kotlin:S112", "kotlin:S1874", "DEPRECATION", "HardCodedStringLiteral")
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R
import kotlinx.coroutines.launch
import kotlin.math.abs

data class GainzTabItem(
    val title: String,
    val iconResId: Int? = null
)

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
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            edgePadding = 0.dp,
            modifier = modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorResource(id = R.color.blue),
                            colorResource(id = R.color.colorBg),
                            colorResource(id = R.color.colorBg)
                        )
                    )
                ),
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                GainzTabIndicator(pagerState, tabPositions)
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
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorResource(id = R.color.blue),
                            colorResource(id = R.color.colorBg),
                            colorResource(id = R.color.colorBg)
                        )
                    )
                ),
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                GainzTabIndicator(pagerState, tabPositions)
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
private fun GainzTabIndicator(
    pagerState: PagerState,
    tabPositions: List<androidx.compose.material3.TabPosition>
) {
    val page = pagerState.currentPage
    val fraction = pagerState.currentPageOffsetFraction
    val targetPage = if (fraction > 0) page + 1 else page - 1

    val currentTab = tabPositions.getOrNull(page)
    val targetTab = tabPositions.getOrNull(targetPage)

    if (currentTab != null) {
        val indicatorWidth = if (targetTab != null) {
            lerp(currentTab.width, targetTab.width, abs(fraction))
        } else currentTab.width

        val indicatorOffset = if (targetTab != null) {
            lerp(currentTab.left, targetTab.left, abs(fraction))
        } else currentTab.left

        TabRowDefaults.SecondaryIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.BottomStart)
                .offset(x = indicatorOffset)
                .width(indicatorWidth),
            color = colorResource(id = R.color.blue),
            height = 2.dp
        )
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
        val pageOffset = ((pagerState.currentPage - index) + pagerState.currentPageOffsetFraction)
        val distance = abs(pageOffset).coerceIn(0f, 1f)
        val color = androidx.compose.ui.graphics.lerp(
            colorResource(id = R.color.blue),
            Color.White,
            distance
        )

        val isPlusTab = tab.iconResId != null && tab.title.isBlank()
        val tabWidthModifier = if (scrollable) {
            if (isPlusTab) {
                Modifier.wrapContentWidth(Alignment.Start).padding(start = 16.dp)
            } else {
                Modifier.wrapContentWidth()
            }
        } else {
            Modifier.fillMaxWidth()
        }

        Box(
            modifier = tabWidthModifier
                .instantClickable {
                    if (onTabClick != null) {
                        onTabClick(index)
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                }
                .padding(vertical = if (tab.iconResId != null && tab.title.isNotBlank()) 8.dp else 14.dp),
            contentAlignment = Alignment.Center
        ) {
            if (tab.iconResId != null && tab.title.isNotBlank()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = tab.iconResId),
                        contentDescription = tab.title,
                        tint = color,
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .size(24.dp)
                    )
                    Text(
                        text = tab.title.uppercase(),
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = if (distance < 0.5f) FontWeight.Bold else FontWeight.Normal
                    )
                }
            } else if (tab.iconResId != null && tab.title.isBlank()) {
                Icon(
                    painter = painterResource(id = tab.iconResId),
                    contentDescription = "Add",
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = tab.title,
                    color = color
                )
            }
        }
    }
}

@Composable
fun Modifier.instantClickable(onClick: () -> Unit): Modifier {
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
                        kotlinx.coroutines.delay(100) // Minimum ripple visibility duration
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
