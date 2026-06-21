package ca.gainzassist.core.di

import org.koin.dsl.module

import ca.gainzassist.feature.workout_entry.domain.usecase.ValidateWorkoutEntryUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.ValidateExerciseInputUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.BuildWorkoutFromExercisesUseCase
import ca.gainzassist.feature.start_workout.domain.usecase.StartWorkoutSessionUseCase
import ca.gainzassist.feature.start_workout.domain.usecase.FinishWorkoutSessionUseCase
import ca.gainzassist.feature.start_workout.domain.usecase.SaveIncompleteWorkoutUseCase
import ca.gainzassist.feature.how_to_videos.domain.usecase.SearchHowToVideosUseCase

val domainModule = module {
    factory { ValidateWorkoutEntryUseCase() }
    factory { ValidateExerciseInputUseCase() }
    factory { BuildWorkoutFromExercisesUseCase() }
    factory { StartWorkoutSessionUseCase(get(), get(), get()) }
    factory { FinishWorkoutSessionUseCase(get(), get(), get(), get()) }
    factory { SaveIncompleteWorkoutUseCase(get(), get()) }
    factory { SearchHowToVideosUseCase(repository = get()) }
}
