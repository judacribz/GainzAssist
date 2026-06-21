package ca.gainzassist.feature.how_to_videos.domain.repository

import ca.gainzassist.feature.how_to_videos.domain.model.HowToVideo

interface HowToVideosRepository {
    suspend fun searchVideos(query: String): List<HowToVideo>
}
