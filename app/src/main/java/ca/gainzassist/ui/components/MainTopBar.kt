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
import ca.gainzassist.feature.main.presentation.screen.MainTab

data class MainTopBarActions(
    val onSearchQueryChange: (String) -> Unit = {},
    val onSearchClick: () -> Unit = {},
    val onCloseSearchClick: () -> Unit = {},
    val onAddWorkoutClick: () -> Unit = {},
    val onLogoutClick: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    selectedTab: MainTab,
    isSearchExpanded: Boolean,
    searchQuery: String,
    actions: MainTopBarActions = MainTopBarActions()
) {
    var overflowExpanded by remember { mutableStateOf(false) }

    if (isSearchExpanded && selectedTab != MainTab.SETTINGS) {
        TopAppBar(
            title = {
                TextField(
                    value = searchQuery,
                    onValueChange = actions.onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.hint_search_workouts),
                            color = Color.Gray
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { /* just dismiss keyboard or do nothing since filter is reactive */ }
                    ),
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
                        contentDescription = stringResource(R.string.cd_close_search),
                        tint = colorResource(R.color.colorBg)
                    )
                }
            },
            actions = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { actions.onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cd_clear_search),
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
            title = stringResource(R.string.app_name),
            showBack = false,
            actions = {
                if (selectedTab != MainTab.SETTINGS) {
                    IconButton(onClick = actions.onSearchClick) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.cd_search),
                            tint = colorResource(R.color.colorBg)
                        )
                    }
                }

                if (selectedTab == MainTab.WORKOUTS) {
                    IconButton(onClick = actions.onAddWorkoutClick) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.cd_add_workout),
                            tint = colorResource(R.color.colorBg)
                        )
                    }
                }

                // Overflow menu for Logout
                IconButton(onClick = { overflowExpanded = true }) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.cd_more_actions),
                        tint = colorResource(R.color.colorBg)
                    )
                }

                DropdownMenu(
                    expanded = overflowExpanded,
                    onDismissRequest = { overflowExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.logout)) },
                        onClick = {
                            overflowExpanded = false
                            actions.onLogoutClick()
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
private fun MainTopBarPreviewWorkouts() {
    MainTopBar(
        selectedTab = MainTab.WORKOUTS,
        isSearchExpanded = false,
        searchQuery = ""
    )
}

@Preview
@Composable
private fun MainTopBarPreviewResume() {
    MainTopBar(
        selectedTab = MainTab.RESUME,
        isSearchExpanded = false,
        searchQuery = ""
    )
}

@Preview
@Composable
private fun MainTopBarPreviewSettings() {
    MainTopBar(
        selectedTab = MainTab.SETTINGS,
        isSearchExpanded = false,
        searchQuery = ""
    )
}

@Preview
@Composable
private fun MainTopBarPreviewSearchExpanded() {
    MainTopBar(
        selectedTab = MainTab.WORKOUTS,
        isSearchExpanded = true,
        searchQuery = "Legs"
    )
}
