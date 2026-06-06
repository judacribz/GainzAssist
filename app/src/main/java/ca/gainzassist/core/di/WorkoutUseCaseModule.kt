package ca.gainzassist.core.di

import ca.gainzassist.domain.usecase.workout.*
import org.koin.dsl.module

val workoutUseCaseModule = module {
    factory { ObserveWorkoutsUseCase(workoutRepository = get()) }
    factory { ObserveUniqueExerciseNamesUseCase(workoutRepository = get()) }
    factory { GetWorkoutByNameUseCase(workoutRepository = get()) }
    factory { GetWorkoutWithExercisesByNameUseCase(workoutRepository = get()) }
    factory { SaveWorkoutUseCase(workoutRepository = get()) }
    factory { AddWorkoutUseCase(workoutRepository = get()) }
    factory { UpdateWorkoutUseCase(workoutRepository = get()) }
    factory { DeleteWorkoutUseCase(workoutRepository = get()) }
    factory { DeleteAllWorkoutsUseCase(workoutRepository = get()) }
    factory { ExerciseExistsUseCase() }
}
