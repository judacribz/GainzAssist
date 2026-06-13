package ca.gainzassist.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import ca.gainzassist.R

data class HowToVideosTopBarState(
    val title: String,
    val isSearchExpanded: Boolean,
    val searchQuery: String
)

data class HowToVideosTopBarActions(
    val onSearchQueryChange: (String) -> Unit,
    val onSearchSubmit: () -> Unit,
    val onSearchClick: () -> Unit,
    val onCloseSearchClick: () -> Unit,
    val onBackClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToVideosTopBar(
    state: HowToVideosTopBarState,
    actions: HowToVideosTopBarActions
) {
    if (state.isSearchExpanded) {
        TopAppBar(
            title = {
                TextField(
                    value = state.searchQuery,
                    onValueChange = actions.onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { androidx.compose.material3.Text("Search videos...", color = Color.Gray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { actions.onSearchSubmit() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = colorResource(R.color.colorBg),
                        unfocusedTextColor = colorResource(R.color.colorBg),
                        cursorColor = colorResource(R.color.colorLightAccent),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = actions.onCloseSearchClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close search",
                        tint = colorResource(R.color.colorBg)
                    )
                }
            },
            actions = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { actions.onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search",
                            tint = colorResource(R.color.colorBg)
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorResource(R.color.colorLightBg)
            )
        )
    } else {
        GainzTopBar(
            title = state.title,
            showBack = true,
            onBackClick = actions.onBackClick,
            actions = {
                IconButton(onClick = actions.onSearchClick) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = colorResource(R.color.colorBg)
                    )
                }
            }
        )
    }
}

@Preview
@Composable
fun HowToVideosTopBarNormal() {
    HowToVideosTopBar(
        state = HowToVideosTopBarState(
            title = "How To Bench Press",
            isSearchExpanded = false,
            searchQuery = ""
        ),
        actions = HowToVideosTopBarActions(
            onSearchQueryChange = {},
            onSearchSubmit = {},
            onSearchClick = {},
            onCloseSearchClick = {},
            onBackClick = {}
        )
    )
}

@Preview
@Composable
fun HowToVideosTopBarSearchExpanded() {
    HowToVideosTopBar(
        state = HowToVideosTopBarState(
            title = "How To Bench Press",
            isSearchExpanded = true,
            searchQuery = "Squat"
        ),
        actions = HowToVideosTopBarActions(
            onSearchQueryChange = {},
            onSearchSubmit = {},
            onSearchClick = {},
            onCloseSearchClick = {},
            onBackClick = {}
        )
    )
}

@Preview
@Composable
fun HowToVideosTopBarLongTitle() {
    HowToVideosTopBar(
        state = HowToVideosTopBarState(
            title = "How To Incline Dumbbell Bench Press with a very very long name",
            isSearchExpanded = false,
            searchQuery = ""
        ),
        actions = HowToVideosTopBarActions(
            onSearchQueryChange = {},
            onSearchSubmit = {},
            onSearchClick = {},
            onCloseSearchClick = {},
            onBackClick = {}
        )
    )
}
