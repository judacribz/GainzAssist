package ca.gainzassist.feature.how_to_videos.data.repository

import ca.gainzassist.BuildConfig
import ca.gainzassist.feature.how_to_videos.domain.repository.YouTubeApiConfig

class DefaultYouTubeApiConfig : YouTubeApiConfig {
    override val apiKey: String
        get() = BuildConfig.GOOGLE_API_KEY
}
