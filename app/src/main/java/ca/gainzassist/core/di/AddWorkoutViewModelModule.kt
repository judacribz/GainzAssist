package ca.gainzassist.core.di

import ca.gainzassist.presentation.add_workout.ExercisesEntryViewModel
import ca.gainzassist.presentation.add_workout.SummaryViewModel
import ca.gainzassist.presentation.add_workout.WorkoutEntryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val addWorkoutViewModelModule = module {
    viewModel { WorkoutEntryViewModel() }
    viewModel { ExercisesEntryViewModel(exerciseExistsUseCase = get()) }
    viewModel {
        SummaryViewModel(
            saveWorkoutUseCase = get()
        )
    }
}
