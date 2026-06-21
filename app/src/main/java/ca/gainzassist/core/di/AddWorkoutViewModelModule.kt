package ca.gainzassist.core.di

import ca.gainzassist.feature.exercises_entry.presentation.viewmodel.ExercisesEntryViewModel
import ca.gainzassist.feature.summary.presentation.viewmodel.SummaryViewModel
import ca.gainzassist.feature.workout_entry.presentation.viewmodel.WorkoutEntryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val addWorkoutViewModelModule = module {
    viewModel { WorkoutEntryViewModel(validateWorkoutEntryUseCase = get()) }
    viewModel {
        ExercisesEntryViewModel(
            validateExerciseInputUseCase = get(),
            checkDuplicateExerciseUseCase = get(),
            buildExerciseUseCase = get(),
            deleteExerciseUseCase = get(),
            buildWorkoutFromExerciseEntriesUseCase = get()
        )
    }
    viewModel {
        SummaryViewModel(
            saveWorkoutUseCase = get()
        )
    }
}
