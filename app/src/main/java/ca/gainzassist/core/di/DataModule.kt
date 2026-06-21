package ca.gainzassist.core.di

import ca.gainzassist.data.local.database.WorkoutDatabase
import ca.gainzassist.data.repository.RoomWorkoutRepository
import ca.gainzassist.domain.repository.WorkoutRepository
import ca.gainzassist.feature.how_to_videos.data.repository.YoutubeHowToVideosRepository
import ca.gainzassist.feature.how_to_videos.domain.repository.HowToVideosRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        WorkoutDatabase.getDatabase(androidContext())
    }

    single<WorkoutRepository> {
        RoomWorkoutRepository(
            calculateNextExerciseWeightUseCase = get(),
            dispatcherProvider = get(),
            database = get()
        )
    }

    single<HowToVideosRepository> {
        YoutubeHowToVideosRepository(context = androidContext())
    }
}
