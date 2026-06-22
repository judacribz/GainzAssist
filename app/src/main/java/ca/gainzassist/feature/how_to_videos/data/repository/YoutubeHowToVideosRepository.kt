package ca.gainzassist.feature.how_to_videos.data.repository

import android.content.Context
import android.content.pm.Signature
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import ca.gainzassist.BuildConfig
import ca.gainzassist.feature.how_to_videos.domain.model.HowToVideo
import ca.gainzassist.feature.how_to_videos.domain.repository.HowToVideosRepository
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject

class YoutubeHowToVideosRepository(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : HowToVideosRepository {

    override suspend fun searchVideos(query: String): List<HowToVideo> = withContext(ioDispatcher) {
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

    private fun fetchYouTubeResponse(urlString: String): String {
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null

        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("X-Android-Package", context.packageName)
            connection.setRequestProperty("X-Android-Cert", getAppSha1())
            connection.connect()

            val responseCode = connection.responseCode
            val stream =
                if (responseCode in 200..299) connection.inputStream else connection.errorStream

            if (stream == null) {
                throw Exception(ERR_YOUTUBE_LOAD)
            }

            reader = BufferedReader(InputStreamReader(stream))
            val buffer = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                buffer.append(line).append("\n")
            }

            if (responseCode !in 200..299) {
                val errorBody = buffer.toString()
                val sanitizedUrl = urlString.replace(Regex("&key=[^&]*"), "&key=***")
                Log.e("HowToVideosRepo", "Error $responseCode for $sanitizedUrl\nBody: $errorBody")

                val msg = if (errorBody.contains("quotaExceeded") ||
                    errorBody.contains("dailyLimitExceeded")
                ) {
                    "YouTube video search quota exceeded. Please try again later."
                } else {
                    ERR_YOUTUBE_LOAD
                }
                throw Exception(msg)
            }
            return buffer.toString()
        } finally {
            connection?.disconnect()
            try {
                reader?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
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
