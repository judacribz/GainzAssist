package ca.gainzassist.core.di

import ca.gainzassist.feature.start_workout.presentation.viewmodel.WorkoutScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val workoutScreenViewModelModule = module {
    viewModel {
        WorkoutScreenViewModel(
            getSessionProgressUseCase = get(),
            saveSessionProgressUseCase = get(),
            finishWorkoutSessionUseCase = get()
        )
    }
}
