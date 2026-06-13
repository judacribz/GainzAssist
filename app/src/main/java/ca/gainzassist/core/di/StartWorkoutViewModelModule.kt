package ca.gainzassist.core.di

import ca.gainzassist.activities.start_workout.StartWorkoutViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val startWorkoutViewModelModule = module {
    viewModel {
        StartWorkoutViewModel(
            getWorkoutWithExercisesByNameUseCase = get(),
            addIncompleteWorkoutUseCase = get(),
            saveIncompleteSessionUseCase = get(),
            getIncompleteSessionUseCase = get(),
            saveSessionProgressUseCase = get(),
            removeIncompleteWorkoutUseCase = get(),
            removeIncompleteSessionUseCase = get(),
            removeSessionProgressUseCase = get()
        )
    }
}
