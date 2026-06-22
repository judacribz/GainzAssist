package ca.gainzassist.feature.how_to_videos.data.repository

import android.content.Context
import android.content.pm.Signature
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import ca.gainzassist.BuildConfig
import ca.gainzassist.feature.how_to_videos.domain.model.HowToVideo
import ca.gainzassist.feature.how_to_videos.domain.repository.HowToVideosRepository
import java.security.MessageDigest
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
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
            throw Exception("Missing Google API Key for video search")
        }

        val encodedQuery = java.net.URLEncoder.encode(queryKey, "UTF-8")
        val urlString = "https://www.googleapis.com/youtube/v3/search" +
            "?part=snippet" +
            "&fields=items(id/videoId,snippet/title)" +
            "&maxResults=10" +
            "&q=$encodedQuery" +
            "&type=video" +
            "&videoEmbeddable=true" +
            "&safeSearch=moderate" +
            "&key=${BuildConfig.GOOGLE_API_KEY}"

        val jsonResponse = fetchYouTubeResponse(urlString)
        parseYouTubeResponse(jsonResponse)
    }

    private fun getAppSha1(): String {
        try {
            val signatures: List<Signature> = PackageInfoCompat.getSignatures(
                /* packageManager = */ context.packageManager,
                /* packageName = */ context.packageName
            )
            val signature = signatures.firstOrNull()
            if (signature != null) {
                val md = MessageDigest.getInstance("SHA-1")
                md.update(signature.toByteArray())
                val digest = md.digest()
                val hexString = StringBuilder()
                for (b in digest) {
                    hexString.append(String.format("%02X", b))
                }
                return hexString.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private suspend fun fetchYouTubeResponse(urlString: String): String {
        try {
            val response: HttpResponse = httpClient.get(urlString) {
                header("X-Android-Package", context.packageName)
                header("X-Android-Cert", getAppSha1())
            }

            val responseBody = response.bodyAsText()
            val responseCode = response.status.value

            if (responseCode !in 200..299) {
                val sanitizedUrl = urlString.replace(Regex("&key=[^&]*"), "&key=***")
                Log.e("HowToVideosRepo", "Error $responseCode for $sanitizedUrl\nBody: $responseBody")

                val msg = if (responseBody.contains("quotaExceeded") ||
                    responseBody.contains("dailyLimitExceeded")
                ) {
                    "YouTube video search quota exceeded. Please try again later."
                } else {
                    ERR_YOUTUBE_LOAD
                }
                throw Exception(msg)
            }
            return responseBody
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) {
                throw e
            }
            // Rethrow or wrap if it's already an expected Exception
            if (e.message == ERR_YOUTUBE_LOAD || e.message?.contains("quotaExceeded") == true || e.message?.contains("dailyLimitExceeded") == true) {
                throw e
            }
            Log.e("HowToVideosRepo", "Network Error: ${e.message}", e)
            throw Exception(ERR_YOUTUBE_LOAD)
        }
    }

    private fun parseYouTubeResponse(jsonString: String): List<HowToVideo> = try {
        val jsonObject = JSONObject(jsonString)
        val items = jsonObject.optJSONArray("items")

        val videos = mutableListOf<HowToVideo>()

        if (items != null) {
            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i) ?: continue
                val id = item.optJSONObject("id") ?: continue
                val snippet = item.optJSONObject("snippet") ?: continue

                val videoId = id.optString("videoId")
                val title = snippet.optString("title")

                if (videoId.isNotEmpty()) {
                    videos.add(HowToVideo(videoId, title))
                }
            }
        }
        videos
    } catch (e: JSONException) {
        e.printStackTrace()
        throw Exception("Error parsing response")
    }

    companion object {
        private const val ERR_YOUTUBE_LOAD =
            "Unable to load YouTube videos. Check API key or network."
    }
}
