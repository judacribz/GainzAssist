package ca.gainzassist.core.di

import ca.gainzassist.feature.start_workout.presentation.viewmodel.StartWorkoutViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val startWorkoutViewModelModule = module {
    viewModel {
        StartWorkoutViewModel(
            startWorkoutSessionUseCase = get(),
            saveIncompleteWorkoutUseCase = get()
        )
    }
}
