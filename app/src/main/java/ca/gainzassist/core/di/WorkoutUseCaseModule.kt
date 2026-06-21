package ca.gainzassist.core.di

import ca.gainzassist.domain.usecase.workout.AddWorkoutUseCase
import ca.gainzassist.domain.usecase.workout.CalculateNextExerciseWeightUseCase
import ca.gainzassist.domain.usecase.workout.DeleteAllWorkoutsUseCase
import ca.gainzassist.domain.usecase.workout.DeleteWorkoutUseCase
import ca.gainzassist.domain.usecase.workout.ExerciseExistsUseCase
import ca.gainzassist.domain.usecase.workout.GetWorkoutByNameUseCase
import ca.gainzassist.domain.usecase.workout.GetWorkoutWithExercisesByNameUseCase
import ca.gainzassist.domain.usecase.workout.InsertCompletedSessionUseCase
import ca.gainzassist.domain.usecase.workout.ObserveUniqueExerciseNamesUseCase
import ca.gainzassist.domain.usecase.workout.ObserveWorkoutsUseCase
import ca.gainzassist.feature.summary.domain.usecase.SaveWorkoutUseCase
import ca.gainzassist.feature.summary.domain.usecase.UpdateWorkoutUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.BuildExerciseUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.BuildWorkoutFromExerciseEntriesUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.CheckDuplicateExerciseUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.DeleteExerciseUseCase
import ca.gainzassist.feature.exercises_entry.domain.usecase.ValidateExerciseInputUseCase
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
    factory { InsertCompletedSessionUseCase(workoutRepository = get()) }
    factory { ExerciseExistsUseCase() }
    factory { CalculateNextExerciseWeightUseCase() }
    factory { ValidateExerciseInputUseCase() }
    factory { CheckDuplicateExerciseUseCase() }
    factory { BuildExerciseUseCase() }
    factory { DeleteExerciseUseCase() }
    factory { BuildWorkoutFromExerciseEntriesUseCase() }
}
