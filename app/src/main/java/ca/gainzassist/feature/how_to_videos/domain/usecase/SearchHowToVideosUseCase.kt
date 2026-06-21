package ca.gainzassist.feature.how_to_videos.domain.usecase

import ca.gainzassist.feature.how_to_videos.domain.model.HowToVideo
import ca.gainzassist.feature.how_to_videos.domain.repository.HowToVideosRepository

class SearchHowToVideosUseCase(
    private val repository: HowToVideosRepository
) {
    suspend operator fun invoke(query: String): List<HowToVideo> = repository.searchVideos(query)
}
