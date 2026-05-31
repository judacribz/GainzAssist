package ca.gainzassist.activities.start_workout.fragments

import android.view.LayoutInflater
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import ca.gainzassist.databinding.FragmentWorkoutScreenBinding

@Composable
fun WorkoutScreenScreen(
    onBindingReady: (FragmentWorkoutScreenBinding) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            val binding = FragmentWorkoutScreenBinding.inflate(LayoutInflater.from(context))
            onBindingReady(binding)
            binding.root
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun WorkoutScreenScreenPreview() {
    WorkoutScreenScreen(
        onBindingReady = {}
    )
}
