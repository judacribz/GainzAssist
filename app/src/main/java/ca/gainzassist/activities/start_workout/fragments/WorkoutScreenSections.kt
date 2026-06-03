package ca.gainzassist.activities.start_workout.fragments

import android.view.LayoutInflater
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ca.gainzassist.databinding.SectionWorkoutEquipmentTimerBinding
import ca.gainzassist.databinding.SectionWorkoutFooterControlsBinding
import ca.gainzassist.databinding.SectionWorkoutProgressHeaderBinding
import ca.gainzassist.databinding.SectionWorkoutRepsWeightControlsBinding

data class WorkoutScreenSectionBindings(
    val progressHeader: SectionWorkoutProgressHeaderBinding,
    val equipmentTimer: SectionWorkoutEquipmentTimerBinding,
    val repsWeightControls: SectionWorkoutRepsWeightControlsBinding,
    val footerControls: SectionWorkoutFooterControlsBinding
)

@Composable
fun WorkoutProgressHeaderAndroidView(
    onBindingReady: (SectionWorkoutProgressHeaderBinding) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val binding = SectionWorkoutProgressHeaderBinding.inflate(LayoutInflater.from(context))
            onBindingReady(binding)
            binding.root
        }
    )
}

@Composable
fun WorkoutEquipmentTimerAndroidView(
    onBindingReady: (SectionWorkoutEquipmentTimerBinding) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val binding = SectionWorkoutEquipmentTimerBinding.inflate(LayoutInflater.from(context))
            onBindingReady(binding)
            binding.root
        }
    )
}

@Composable
fun WorkoutRepsWeightControlsAndroidView(
    onBindingReady: (SectionWorkoutRepsWeightControlsBinding) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val binding = SectionWorkoutRepsWeightControlsBinding.inflate(LayoutInflater.from(context))
            onBindingReady(binding)
            binding.root
        }
    )
}

@Composable
fun WorkoutFooterControlsAndroidView(
    onBindingReady: (SectionWorkoutFooterControlsBinding) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val binding = SectionWorkoutFooterControlsBinding.inflate(LayoutInflater.from(context))
            onBindingReady(binding)
            binding.root
        }
    )
}

@Composable
fun WorkoutScreenSectionedAndroidViewScreen(
    onBindingsReady: (WorkoutScreenSectionBindings) -> Unit,
    modifier: Modifier = Modifier
) {
    var progressHeaderBinding by remember { mutableStateOf<SectionWorkoutProgressHeaderBinding?>(null) }
    var equipmentTimerBinding by remember { mutableStateOf<SectionWorkoutEquipmentTimerBinding?>(null) }
    var repsWeightBinding by remember { mutableStateOf<SectionWorkoutRepsWeightControlsBinding?>(null) }
    var footerBinding by remember { mutableStateOf<SectionWorkoutFooterControlsBinding?>(null) }

    LaunchedEffect(
        progressHeaderBinding,
        equipmentTimerBinding,
        repsWeightBinding,
        footerBinding
    ) {
        val progress = progressHeaderBinding
        val equipment = equipmentTimerBinding
        val repsWeight = repsWeightBinding
        val footer = footerBinding

        if (
            progress != null &&
            equipment != null &&
            repsWeight != null &&
            footer != null
        ) {
            onBindingsReady(
                WorkoutScreenSectionBindings(
                    progressHeader = progress,
                    equipmentTimer = equipment,
                    repsWeightControls = repsWeight,
                    footerControls = footer
                )
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(3.dp)
    ) {
        WorkoutProgressHeaderAndroidView(
            onBindingReady = { progressHeaderBinding = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(5f)
                .padding(3.dp)
        )

        WorkoutEquipmentTimerAndroidView(
            onBindingReady = { equipmentTimerBinding = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(4f)
        )

        WorkoutRepsWeightControlsAndroidView(
            onBindingReady = { repsWeightBinding = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(3f)
                .padding(3.dp)
        )

        WorkoutFooterControlsAndroidView(
            onBindingReady = { footerBinding = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkoutScreenSectionedAndroidViewScreenPreview() {
    WorkoutScreenSectionedAndroidViewScreen(
        onBindingsReady = {}
    )
}
