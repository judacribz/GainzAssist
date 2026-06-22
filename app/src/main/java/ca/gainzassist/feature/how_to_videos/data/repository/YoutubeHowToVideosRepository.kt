package ca.gainzassist.feature.how_to_videos.data.repository

import android.content.Context
import android.content.pm.Signature
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import ca.gainzassist.BuildConfig
import ca.gainzassist.feature.how_to_videos.domain.model.HowToVideo
import ca.gainzassist.feature.how_to_videos.domain.repository.HowToVideosRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import java.security.MessageDigest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class YoutubeHowToVideosRepository(private val context: Context) : HowToVideosRepository {

    private val httpClient = HttpClient(OkHttp)

    override suspend fun searchVideos(query: String): List<HowToVideo> = withContext(Dispatchers.IO) {
        val queryKey = query.trim()
        if (queryKey.isEmpty()) {
            return@withContext emptyList()
        }
        if (BuildConfig.GOOGLE_API_KEY.isBlank()) {
            throw Exception(ERR_MISSING_API_KEY)
        }
        val jsonResponse = fetchYouTubeResponse(queryKey)
        parseYouTubeResponse(jsonResponse)
    }

    private fun getAppSha1(): String = runCatching {
        val signatures: List<Signature> = PackageInfoCompat.getSignatures(
            /* packageManager = */ context.packageManager,
            /* packageName = */ context.packageName
        )
        val signature = signatures.firstOrNull()
        if (signature != null) {
            val md = MessageDigest.getInstance(DIGEST_ALGORITHM_SHA1)
            md.update(signature.toByteArray())
            val digest = md.digest()
            val hexString = StringBuilder()
            for (b in digest) {
                hexString.append(String.format(HEX_FORMAT, b))
            }
            hexString.toString()
        } else {
            ""
        }
    }.getOrElse { e ->
        e.printStackTrace()
        ""
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
            parameter(PARAM_KEY, BuildConfig.GOOGLE_API_KEY)
            header(HEADER_ANDROID_PACKAGE, context.packageName)
            header(HEADER_ANDROID_CERT, getAppSha1())
        }
        val responseBody = response.bodyAsText()
        val responseCode = response.status.value
        if (responseCode !in HTTP_OK_START..HTTP_OK_END) {
            val fullUrl = response.call.request.url.toString()
            val sanitizedUrl = fullUrl.replace(Regex(REGEX_KEY_MASK), REPLACEMENT_KEY_MASK)
            Log.e("HowToVideosRepo", "Error $responseCode for $sanitizedUrl\nBody: $responseBody")

            val msg = if (responseBody.contains(QUOTA_EXCEEDED) || responseBody.contains(DAILY_LIMIT_EXCEEDED)) {
                ERR_QUOTA_EXCEEDED
            } else {
                ERR_YOUTUBE_LOAD
            }
            throw Exception(msg)
        }
        responseBody
    }.getOrElse { e ->
        if (e is CancellationException) throw e
        // Rethrow or wrap if it's already an expected Exception
        if (
            e.message == ERR_YOUTUBE_LOAD ||
            e.message?.contains(QUOTA_EXCEEDED) == true ||
            e.message?.contains(DAILY_LIMIT_EXCEEDED) == true
        ) {
            throw e
        }
        Log.e("HowToVideosRepo", "Network Error: ${e.message}", e)
        throw Exception(ERR_YOUTUBE_LOAD)
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
        if (e is JSONException) {
            e.printStackTrace()
            throw Exception(ERR_PARSING_RESPONSE)
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
        private const val DIGEST_ALGORITHM_SHA1 = "SHA-1"
        private const val HEX_FORMAT = "%02X"
        private const val JSON_KEY_ITEMS = "items"
        private const val JSON_KEY_ID = "id"
        private const val JSON_KEY_SNIPPET = "snippet"
        private const val JSON_KEY_VIDEO_ID = "videoId"
        private const val JSON_KEY_TITLE = "title"
        private const val QUOTA_EXCEEDED = "quotaExceeded"
        private const val DAILY_LIMIT_EXCEEDED = "dailyLimitExceeded"
        private const val REGEX_KEY_MASK = "key=[^&]*"
        private const val REPLACEMENT_KEY_MASK = "key=***"
        private const val HTTP_OK_START = 200
        private const val HTTP_OK_END = 299
        private const val ERR_YOUTUBE_LOAD = "Unable to load YouTube videos. Check API key or network."
        private const val ERR_QUOTA_EXCEEDED = "YouTube video search quota exceeded. Please try again later."
        private const val ERR_MISSING_API_KEY = "Missing Google API Key for video search"
        private const val ERR_PARSING_RESPONSE = "Error parsing response"
    }
}
