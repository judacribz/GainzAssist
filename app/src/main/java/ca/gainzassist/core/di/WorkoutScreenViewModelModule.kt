package ca.gainzassist.core.di

import ca.gainzassist.activities.start_workout.workout_screen.WorkoutScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val workoutScreenViewModelModule = module {
    viewModel { WorkoutScreenViewModel(get(), get(), get(), get(), get(), get()) }
}
