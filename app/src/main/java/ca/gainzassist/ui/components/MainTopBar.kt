package ca.gainzassist.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import ca.gainzassist.R
import ca.gainzassist.presentation.main.MainTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    selectedTab: MainTab,
    isSearchExpanded: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onCloseSearchClick: () -> Unit,
    onAddWorkoutClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var overflowExpanded by remember { mutableStateOf(false) }

    if (isSearchExpanded && selectedTab != MainTab.SETTINGS) {
        TopAppBar(
            title = {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search workouts...", color = Color.Gray) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { /* just dismiss keyboard or do nothing since filter is reactive */ }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = colorResource(id = R.color.colorBg),
                        unfocusedTextColor = colorResource(id = R.color.colorBg),
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
                        tint = colorResource(id = R.color.colorBg)
                    )
                }
            },
            actions = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search",
                            tint = colorResource(id = R.color.colorBg)
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
            title = stringResource(id = R.string.app_name),
            showBack = false,
            actions = {
                if (selectedTab != MainTab.SETTINGS) {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = colorResource(id = R.color.colorBg)
                        )
                    }
                }
                
                if (selectedTab == MainTab.WORKOUTS) {
                    IconButton(onClick = onAddWorkoutClick) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add workout",
                            tint = colorResource(id = R.color.colorBg)
                        )
                    }
                }
                
                // Overflow menu for Logout
                IconButton(onClick = { overflowExpanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More actions",
                        tint = colorResource(id = R.color.colorBg)
                    )
                }
                
                DropdownMenu(
                    expanded = overflowExpanded,
                    onDismissRequest = { overflowExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = {
                            overflowExpanded = false
                            onLogoutClick()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        )
    }
}

@Preview
@Composable
fun MainTopBarPreview_Workouts() {
    MainTopBar(
        selectedTab = MainTab.WORKOUTS,
        isSearchExpanded = false,
        searchQuery = "",
        onSearchQueryChange = {},
        onSearchClick = {},
        onCloseSearchClick = {},
        onAddWorkoutClick = {},
        onLogoutClick = {}
    )
}

@Preview
@Composable
fun MainTopBarPreview_Resume() {
    MainTopBar(
        selectedTab = MainTab.RESUME,
        isSearchExpanded = false,
        searchQuery = "",
        onSearchQueryChange = {},
        onSearchClick = {},
        onCloseSearchClick = {},
        onAddWorkoutClick = {},
        onLogoutClick = {}
    )
}

@Preview
@Composable
fun MainTopBarPreview_Settings() {
    MainTopBar(
        selectedTab = MainTab.SETTINGS,
        isSearchExpanded = false,
        searchQuery = "",
        onSearchQueryChange = {},
        onSearchClick = {},
        onCloseSearchClick = {},
        onAddWorkoutClick = {},
        onLogoutClick = {}
    )
}

@Preview
@Composable
fun MainTopBarPreview_SearchExpanded() {
    MainTopBar(
        selectedTab = MainTab.WORKOUTS,
        isSearchExpanded = true,
        searchQuery = "Legs",
        onSearchQueryChange = {},
        onSearchClick = {},
        onCloseSearchClick = {},
        onAddWorkoutClick = {},
        onLogoutClick = {}
    )
}
