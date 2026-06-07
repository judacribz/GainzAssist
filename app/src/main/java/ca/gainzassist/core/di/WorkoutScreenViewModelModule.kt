package ca.gainzassist.core.di

import ca.gainzassist.presentation.start_workout.workout.WorkoutScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val workoutScreenViewModelModule = module {
    viewModel { WorkoutScreenViewModel(get(), get(), get(), get(), get()) }
}
