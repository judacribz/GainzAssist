package ca.gainzassist.core.di

import ca.gainzassist.activities.main.MainViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mainViewModelModule = module {
    viewModel {
        MainViewModel(
            observeWorkoutsUseCase = get(),
            getWorkoutWithExercisesByNameUseCase = get(),
            deleteWorkoutUseCase = get(),
            deleteAllWorkoutsUseCase = get(),
            getIncompleteWorkoutNamesUseCase = get()
        )
    }
}
