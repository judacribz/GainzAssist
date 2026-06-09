package ca.gainzassist.core.di

import ca.gainzassist.data.repository.SharedPreferencesSessionRepository
import ca.gainzassist.data.repository.SharedPreferencesUserRepository
import ca.gainzassist.domain.repository.SessionPreferencesRepository
import ca.gainzassist.domain.repository.UserPreferencesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val preferencesDataModule = module {
    single<SessionPreferencesRepository> {
        SharedPreferencesSessionRepository(
            context = androidContext(),
            dispatcherProvider = get()
        )
    }

    single<UserPreferencesRepository> {
        SharedPreferencesUserRepository(
            context = androidContext(),
            dispatcherProvider = get()
        )
    }
}
