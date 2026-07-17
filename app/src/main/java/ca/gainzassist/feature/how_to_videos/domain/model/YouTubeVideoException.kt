package ca.gainzassist.feature.how_to_videos.domain.model

sealed class YouTubeVideoException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    class MissingApiKey(message: String) : YouTubeVideoException(message)
    class QuotaExceeded(message: String) : YouTubeVideoException(message)
    class InvalidResponse(message: String, cause: Throwable? = null) : YouTubeVideoException(message, cause)
    class RequestFailed(
        message: String,
        val statusCode: Int,
        cause: Throwable? = null
    ) : YouTubeVideoException(message, cause)
    class Network(message: String, cause: Throwable? = null) : YouTubeVideoException(message, cause)
}
