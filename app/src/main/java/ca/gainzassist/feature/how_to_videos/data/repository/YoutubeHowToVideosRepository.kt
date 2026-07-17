package ca.gainzassist.feature.how_to_videos.data.repository

import android.util.Log
import ca.gainzassist.core.coroutines.DispatcherProvider
import ca.gainzassist.feature.how_to_videos.domain.model.HowToVideo
import ca.gainzassist.feature.how_to_videos.domain.model.YouTubeVideoException
import ca.gainzassist.feature.how_to_videos.domain.repository.AndroidSignatureProvider
import ca.gainzassist.feature.how_to_videos.domain.repository.HowToVideosRepository
import ca.gainzassist.feature.how_to_videos.domain.repository.YouTubeApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class YoutubeHowToVideosRepository(
    private val httpClient: HttpClient,
    private val dispatcherProvider: DispatcherProvider,
    private val apiConfig: YouTubeApiConfig,
    private val signatureProvider: AndroidSignatureProvider
) : HowToVideosRepository {

    override suspend fun searchVideos(query: String): List<HowToVideo> = withContext(dispatcherProvider.io) {
        val queryKey = query.trim()
        if (queryKey.isEmpty()) {
            return@withContext emptyList()
        }
        if (apiConfig.apiKey.isBlank()) {
            throw YouTubeVideoException.MissingApiKey(ERR_MISSING_API_KEY)
        }
        val jsonResponse = fetchYouTubeResponse(queryKey)
        parseYouTubeResponse(jsonResponse)
    }

    private suspend fun fetchYouTubeResponse(query: String): String = runCatching {
        val response: HttpResponse = httpClient.get(BASE_URL) {
            parameter(PARAM_PART, VALUE_PART)
            parameter(PARAM_FIELDS, VALUE_FIELDS)
            parameter(PARAM_MAX_RESULTS, MAX_RESULTS)
            parameter(PARAM_QUERY, query)
            parameter(PARAM_TYPE, VALUE_TYPE)
            parameter(PARAM_EMBEDDABLE, VALUE_EMBEDDABLE)
            parameter(PARAM_SAFE_SEARCH, VALUE_SAFE_SEARCH)
            parameter(PARAM_KEY, apiConfig.apiKey)
            header(HEADER_ANDROID_PACKAGE, signatureProvider.packageName)
            header(HEADER_ANDROID_CERT, signatureProvider.certificateSha1)
        }
        val responseBody = response.bodyAsText()
        val responseCode = response.status.value
        if (responseCode !in HTTP_OK_START..HTTP_OK_END) {
            Log.e("HowToVideosRepo", "Error $responseCode")

            if (responseBody.contains(QUOTA_EXCEEDED) || responseBody.contains(DAILY_LIMIT_EXCEEDED)) {
                throw YouTubeVideoException.QuotaExceeded(ERR_QUOTA_EXCEEDED)
            } else {
                throw YouTubeVideoException.RequestFailed(ERR_YOUTUBE_LOAD, responseCode)
            }
        }
        responseBody
    }.getOrElse { e ->
        if (e is CancellationException) throw e
        if (e is YouTubeVideoException) throw e
        
        Log.e("HowToVideosRepo", "Network Error: ${e.message}", e)
        throw YouTubeVideoException.Network(ERR_YOUTUBE_LOAD, e)
    }

    private fun parseYouTubeResponse(jsonString: String): List<HowToVideo> = runCatching {
        val jsonObject = JSONObject(jsonString)
        val items = jsonObject.optJSONArray(JSON_KEY_ITEMS)

        val videos = mutableListOf<HowToVideo>()

        if (items != null) {
            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val id = item.optJSONObject(JSON_KEY_ID) ?: continue
                val snippet = item.optJSONObject(JSON_KEY_SNIPPET) ?: continue
                val videoId = id.optString(JSON_KEY_VIDEO_ID)
                val title = snippet.optString(JSON_KEY_TITLE)
                if (videoId.isNotEmpty()) {
                    videos.add(HowToVideo(videoId, title))
                }
            }
        }
        videos
    }.getOrElse { e ->
        if (e is CancellationException) throw e
        if (e is JSONException) {
            Log.e("HowToVideosRepo", "Parse Error", e)
            throw YouTubeVideoException.InvalidResponse(ERR_PARSING_RESPONSE, e)
        }
        throw e
    }

    companion object {
        private const val BASE_URL = "https://www.googleapis.com/youtube/v3/search"
        private const val PARAM_PART = "part"
        private const val VALUE_PART = "snippet"
        private const val PARAM_FIELDS = "fields"
        private const val VALUE_FIELDS = "items(id/videoId,snippet/title)"
        private const val PARAM_MAX_RESULTS = "maxResults"
        private const val MAX_RESULTS = 10
        private const val PARAM_QUERY = "q"
        private const val PARAM_TYPE = "type"
        private const val VALUE_TYPE = "video"
        private const val PARAM_EMBEDDABLE = "videoEmbeddable"
        private const val VALUE_EMBEDDABLE = "true"
        private const val PARAM_SAFE_SEARCH = "safeSearch"
        private const val VALUE_SAFE_SEARCH = "moderate"
        private const val PARAM_KEY = "key"
        private const val HEADER_ANDROID_PACKAGE = "X-Android-Package"
        private const val HEADER_ANDROID_CERT = "X-Android-Cert"
        private const val JSON_KEY_ITEMS = "items"
        private const val JSON_KEY_ID = "id"
        private const val JSON_KEY_SNIPPET = "snippet"
        private const val JSON_KEY_VIDEO_ID = "videoId"
        private const val JSON_KEY_TITLE = "title"
        private const val QUOTA_EXCEEDED = "quotaExceeded"
        private const val DAILY_LIMIT_EXCEEDED = "dailyLimitExceeded"
        private const val HTTP_OK_START = 200
        private const val HTTP_OK_END = 299
        private const val ERR_YOUTUBE_LOAD = "Unable to load YouTube videos. Check API key or network."
        private const val ERR_QUOTA_EXCEEDED = "YouTube video search quota exceeded. Please try again later."
        private const val ERR_MISSING_API_KEY = "Missing Google API Key for video search"
        private const val ERR_PARSING_RESPONSE = "Error parsing response"
    }
}
