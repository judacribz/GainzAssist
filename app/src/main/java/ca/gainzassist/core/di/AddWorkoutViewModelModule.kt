package ca.gainzassist.core.di

import ca.gainzassist.activities.add_workout.exercises_entry.ExercisesEntryViewModel
import ca.gainzassist.activities.add_workout.summary.SummaryViewModel
import ca.gainzassist.activities.add_workout.workout_entry.WorkoutEntryViewModel
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
