package ca.gainzassist.core.di

import ca.gainzassist.presentation.main.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
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
