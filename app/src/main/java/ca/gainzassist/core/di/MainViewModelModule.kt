package ca.gainzassist.core.di

import ca.gainzassist.feature.main.presentation.viewmodel.MainViewModel
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
