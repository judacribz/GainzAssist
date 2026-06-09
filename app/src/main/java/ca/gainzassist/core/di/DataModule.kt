package ca.gainzassist.core.di

import ca.gainzassist.data.repository.RoomWorkoutRepository
import ca.gainzassist.domain.repository.WorkoutRepository
import ca.gainzassist.models.db.WorkoutDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        WorkoutDatabase.getDatabase(androidContext())
    }

    single<WorkoutRepository> {
        RoomWorkoutRepository(
            database = get(),
            dispatcherProvider = get()
        )
    }
}
