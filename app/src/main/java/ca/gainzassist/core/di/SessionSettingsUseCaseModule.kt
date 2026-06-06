package ca.gainzassist.core.di

import ca.gainzassist.domain.usecase.session.AddIncompleteWorkoutUseCase
import ca.gainzassist.domain.usecase.session.GetIncompleteSessionUseCase
import ca.gainzassist.domain.usecase.session.GetIncompleteWorkoutNamesUseCase
import ca.gainzassist.domain.usecase.session.GetSessionProgressUseCase
import ca.gainzassist.domain.usecase.session.RemoveIncompleteSessionUseCase
import ca.gainzassist.domain.usecase.session.RemoveIncompleteWorkoutUseCase
import ca.gainzassist.domain.usecase.session.RemoveSessionProgressUseCase
import ca.gainzassist.domain.usecase.session.SaveIncompleteSessionUseCase
import ca.gainzassist.domain.usecase.session.SaveSessionProgressUseCase
import ca.gainzassist.domain.usecase.user.GetThemeUseCase
import ca.gainzassist.domain.usecase.user.GetUserEmailUseCase
import ca.gainzassist.domain.usecase.user.SaveUserInfoUseCase
import ca.gainzassist.domain.usecase.user.SetThemeUseCase
import org.koin.dsl.module

val sessionSettingsUseCaseModule = module {
    factory { GetIncompleteWorkoutNamesUseCase(sessionPreferencesRepository = get()) }
    factory { AddIncompleteWorkoutUseCase(sessionPreferencesRepository = get()) }
    factory { RemoveIncompleteWorkoutUseCase(sessionPreferencesRepository = get()) }

    factory { SaveIncompleteSessionUseCase(sessionPreferencesRepository = get()) }
    factory { GetIncompleteSessionUseCase(sessionPreferencesRepository = get()) }
    factory { RemoveIncompleteSessionUseCase(sessionPreferencesRepository = get()) }

    factory { SaveSessionProgressUseCase(sessionPreferencesRepository = get()) }
    factory { GetSessionProgressUseCase(sessionPreferencesRepository = get()) }
    factory { RemoveSessionProgressUseCase(sessionPreferencesRepository = get()) }

    factory { GetUserEmailUseCase(userPreferencesRepository = get()) }
    factory { SaveUserInfoUseCase(userPreferencesRepository = get()) }
    factory { GetThemeUseCase(userPreferencesRepository = get()) }
    factory { SetThemeUseCase(userPreferencesRepository = get()) }
}
