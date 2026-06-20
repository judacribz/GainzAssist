package ca.gainzassist.activities.main.view.tab_screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ca.gainzassist.R

data class WorkoutsScreenActions(
    val onWorkoutClick: (String) -> Unit = {},
    val onWorkoutLongClick: (String) -> Unit = {},
    val onDismissDialog: () -> Unit = {},
    val onEditWorkout: (String) -> Unit = {},
    val onDeleteWorkout: (String) -> Unit = {}
)

private const val DialogWidthFraction = 0.9f
private val DialogBorderWidth = 2.5.dp
private val DialogCornerRadius = 20.dp
private val DialogPadding = 20.dp
private val DialogTitleFontSize = 30.sp
private val DialogTitleBottomPadding = 20.dp
private const val DialogButtonWeight = 1f
private val DialogButtonSpacing = 20.dp
private const val LEG_DAY = "Leg Day"

@Composable
fun WorkoutsScreen(
    workoutNames: List<String>,
    selectedWorkoutName: String?,
    actions: WorkoutsScreenActions = WorkoutsScreenActions()
) {
    SharedWorkoutList(
        titleResId = R.string.workout_list,
        workoutNames = workoutNames,
        onWorkoutClick = actions.onWorkoutClick,
        onWorkoutLongClick = actions.onWorkoutLongClick
    )

    if (selectedWorkoutName != null) {
        WorkoutOptionsDialog(
            workoutName = selectedWorkoutName,
            onDismiss = actions.onDismissDialog,
            onEdit = { actions.onEditWorkout(selectedWorkoutName) },
            onDelete = { actions.onDeleteWorkout(selectedWorkoutName) }
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
                .fillMaxWidth(DialogWidthFraction)
                .wrapContentHeight()
                .border(
                    width = DialogBorderWidth,
                    color = colorResource(R.color.blue),
                    shape = RoundedCornerShape(DialogCornerRadius)
                ),
            color = colorResource(R.color.grey),
            shape = RoundedCornerShape(DialogCornerRadius)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DialogPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = workoutName,
                    fontSize = DialogTitleFontSize,
                    color = colorResource(R.color.colorBg),
                    modifier = Modifier.padding(bottom = DialogTitleBottomPadding)
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
                        modifier = Modifier.weight(DialogButtonWeight)
                    ) {
                        Text(text = stringResource(R.string.delete))
                    }

                    Spacer(modifier = Modifier.width(DialogButtonSpacing))

                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.weight(DialogButtonWeight)
                    ) {
                        Text(text = stringResource(R.string.edit))
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
        selectedWorkoutName = null
    )
}

@Preview(showBackground = true)
@Composable
fun WorkoutsScreenPopulatedPreview() {
    WorkoutsScreen(
        workoutNames = listOf("Chest Day", LEG_DAY, "Back Day"),
        selectedWorkoutName = null
    )
}

@Preview(showBackground = true)
@Composable
fun WorkoutsScreenDialogPreview() {
    WorkoutsScreen(
        workoutNames = listOf("Chest Day", LEG_DAY, "Back Day"),
        selectedWorkoutName = LEG_DAY
    )
}
