package ca.gainzassist.core.di

import ca.gainzassist.feature.how_to_videos.presentation.viewmodel.HowToVideosViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    viewModel {
        HowToVideosViewModel(
            searchHowToVideosUseCase = get()
        )
    }
}
