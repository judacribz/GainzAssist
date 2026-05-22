package ca.judacribz.gainzassist.activities.main.fragments.workouts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ca.judacribz.gainzassist.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutsScreen(
    workoutNames: List<String>,
    selectedWorkoutName: String?,
    onWorkoutClick: (String) -> Unit,
    onWorkoutLongClick: (String) -> Unit,
    onDismissDialog: () -> Unit,
    onEditWorkout: (String) -> Unit,
    onDeleteWorkout: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = stringResource(id = R.string.workout_list),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(workoutNames) { workoutName ->
                    WorkoutListItem(
                        workoutName = workoutName,
                        onClick = { onWorkoutClick(workoutName) },
                        onLongClick = { onWorkoutLongClick(workoutName) }
                    )
                }
            }
        }

        if (selectedWorkoutName != null) {
            WorkoutOptionsDialog(
                workoutName = selectedWorkoutName,
                onDismiss = onDismissDialog,
                onEdit = { onEditWorkout(selectedWorkoutName) },
                onDelete = { onDeleteWorkout(selectedWorkoutName) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkoutListItem(
    workoutName: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shadowElevation = 2.dp,
        color = colorResource(id = R.color.blue), // Match basic styling from XML button style, though themes alter this
        contentColor = Color.White
    ) {
        Text(
            text = workoutName,
            modifier = Modifier
                .fillMaxWidth()
                .padding(50.dp),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WorkoutOptionsDialog(
    workoutName: String,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            color = colorResource(id = R.color.colorPrimaryDark), // edit_text_box_blue refers to a blue dark background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = workoutName,
                    fontSize = 30.sp,
                    color = colorResource(id = R.color.colorBg), // From XML tv_workout_name textColor
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(id = R.string.delete))
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(id = R.string.edit))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkoutsScreenEmptyPreview() {
    WorkoutsScreen(
        workoutNames = emptyList(),
        selectedWorkoutName = null,
        onWorkoutClick = {},
        onWorkoutLongClick = {},
        onDismissDialog = {},
        onEditWorkout = {},
        onDeleteWorkout = {}
    )
}

@Preview(showBackground = true)
@Composable
fun WorkoutsScreenPopulatedPreview() {
    WorkoutsScreen(
        workoutNames = listOf("Chest Day", "Leg Day", "Back Day"),
        selectedWorkoutName = null,
        onWorkoutClick = {},
        onWorkoutLongClick = {},
        onDismissDialog = {},
        onEditWorkout = {},
        onDeleteWorkout = {}
    )
}

@Preview(showBackground = true)
@Composable
fun WorkoutsScreenDialogPreview() {
    WorkoutsScreen(
        workoutNames = listOf("Chest Day", "Leg Day", "Back Day"),
        selectedWorkoutName = "Leg Day",
        onWorkoutClick = {},
        onWorkoutLongClick = {},
        onDismissDialog = {},
        onEditWorkout = {},
        onDeleteWorkout = {}
    )
}