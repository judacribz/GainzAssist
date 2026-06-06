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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToVideosTopBar(
    title: String,
    isSearchExpanded: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
    onSearchClick: () -> Unit,
    onCloseSearchClick: () -> Unit,
    onBackClick: () -> Unit
) {
    if (isSearchExpanded) {
        TopAppBar(
            title = {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { androidx.compose.material3.Text("Search videos...", color = Color.Gray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = colorResource(id = R.color.colorLightAccent),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onCloseSearchClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close search",
                        tint = Color.White
                    )
                }
            },
            actions = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search",
                            tint = Color.White
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorResource(id = R.color.colorLightBg)
            )
        )
    } else {
        GainzTopBar(
            title = title,
            showBack = true,
            onBackClick = onBackClick,
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            }
        )
    }
}

@Preview
@Composable
fun HowToVideosTopBar_Normal() {
    HowToVideosTopBar(
        title = "How To Bench Press",
        isSearchExpanded = false,
        searchQuery = "",
        onSearchQueryChange = {},
        onSearchSubmit = {},
        onSearchClick = {},
        onCloseSearchClick = {},
        onBackClick = {}
    )
}

@Preview
@Composable
fun HowToVideosTopBar_SearchExpanded() {
    HowToVideosTopBar(
        title = "How To Bench Press",
        isSearchExpanded = true,
        searchQuery = "Squat",
        onSearchQueryChange = {},
        onSearchSubmit = {},
        onSearchClick = {},
        onCloseSearchClick = {},
        onBackClick = {}
    )
}

@Preview
@Composable
fun HowToVideosTopBar_LongTitle() {
    HowToVideosTopBar(
        title = "How To Incline Dumbbell Bench Press with a very very long name",
        isSearchExpanded = false,
        searchQuery = "",
        onSearchQueryChange = {},
        onSearchSubmit = {},
        onSearchClick = {},
        onCloseSearchClick = {},
        onBackClick = {}
    )
}
