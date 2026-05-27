package ca.gainzassist.activities.start_workout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gainzassist.R
import ca.gainzassist.models.Exercise
import ca.gainzassist.models.ExerciseSet

@Composable
fun ExerciseSetsListScreen(exercises: List<Exercise>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(exercises) { exercise ->
            ExerciseSetsCard(exercise = exercise)
        }
    }
}

@Composable
fun ExerciseSetsCard(exercise: Exercise) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = exercise.name ?: "",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(10.dp),
                color = Color.White,
                fontSize = 30.sp,
                textAlign = TextAlign.Start
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.Bottom
            ) {
                // Subtitles
                Column(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .padding(vertical = 5.dp)
                        .width(70.dp), // Approximate width of subtitles
                    verticalArrangement = Arrangement.spacedBy(15.dp) // Spacing to match sets
                ) {
                    Text(
                        text = stringResource(id = R.string.set_num),
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(id = R.string.reps),
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(id = R.string.weight_lbs),
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(
                    color = Color.Gray,
                    modifier = Modifier
                        .width(1.dp)
                        .height(180.dp)
                )

                // Sets
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(exercise.setsList) { set ->
                        ExerciseSetColumn(set = set)
                    }
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
fun ExerciseSetColumn(set: ExerciseSet) {
    Column(
        modifier = Modifier.width(60.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = (set.setNumber + 1).toString(),
            fontSize = 20.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Text(
            text = set.reps.toString(),
            fontSize = 20.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Text(
            text = String.format("%.0f", set.weight),
            fontSize = 20.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(5.dp))
    }
}
