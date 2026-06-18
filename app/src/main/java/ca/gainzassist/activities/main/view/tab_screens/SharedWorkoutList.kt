package ca.gainzassist.activities.main.view.tab_screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.ui.components.GainzButton

@Composable
fun SharedWorkoutList(
    titleResId: Int,
    workoutNames: List<String>,
    emptyStateTextResId: Int? = null,
    onWorkoutClick: (String) -> Unit,
    onWorkoutLongClick: ((String) -> Unit)? = null,
    buttonFontFamily: FontFamily = FontFamily.Default
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(15.dp)
    ) {
        item {
            Text(
                text = stringResource(titleResId),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                fontSize = 16.sp
            )
        }

        if (workoutNames.isEmpty() && emptyStateTextResId != null) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(emptyStateTextResId),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            items(workoutNames) { workoutName ->
                GainzButton(
                    text = workoutName,
                    onClick = { onWorkoutClick(workoutName) },
                    onLongClick = { onWorkoutLongClick?.invoke(workoutName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .height(120.dp),
                    fontFamily = buttonFontFamily
                )
            }
        }
    }
}
