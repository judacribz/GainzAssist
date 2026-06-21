package ca.gainzassist.feature.start_workout.presentation.screen

import ca.gainzassist.R

enum class StartWorkoutTab(val titleResId: Int, val iconResId: Int) {
    WARMUPS(R.string.warmups, R.drawable.ic_warmups),
    WORKOUT(R.string.workout, R.drawable.ic_workout),
    EXERCISES(R.string.exercises, R.drawable.ic_exercises)
}
