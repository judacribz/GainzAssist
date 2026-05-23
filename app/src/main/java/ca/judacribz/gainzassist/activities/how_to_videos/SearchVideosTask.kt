package ca.judacribz.gainzassist.activities.how_to_videos

import android.os.AsyncTask
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class SearchVideosTask : AsyncTask<String, Void, SearchVideosTask.SearchResult>() {

    class SearchResult(
        val videoIds: ArrayList<String>? = null,
        val videoTitles: ArrayList<String>? = null,
        val errorMessage: String? = null
    )

    interface YouTubeSearchObserver {
        fun videoSearchDataReceived(videoIds: ArrayList<String>, videoTitles: ArrayList<String>)
        fun videoSearchFailed(message: String)
    }

    private var youTubeSearchObserver: YouTubeSearchObserver? = null

    fun setYouTubeSearchObserver(youTubeSearchObserver: YouTubeSearchObserver?) {
        this.youTubeSearchObserver = youTubeSearchObserver
    }

    override fun doInBackground(vararg strings: String): SearchResult {
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null

        try {
            val urlString = strings[0]
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.connect()

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            reader = BufferedReader(InputStreamReader(stream))
            val buffer = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                buffer.append(line).append("\n")
            }

            if (responseCode !in 200..299) {
                val sanitizedUrl = urlString.replace(Regex("&key=[^&]*"), "&key=***")
                Log.e("SearchVideosTask", "Error $responseCode for $sanitizedUrl\nBody: ${buffer.toString()}")
                return SearchResult(errorMessage = "Unable to load YouTube videos. Check API key or network.")
            }

            val jsonObject = JSONObject(buffer.toString())
            val items = jsonObject.optJSONArray("items")

            val videoIds = ArrayList<String>()
            val videoTitles = ArrayList<String>()

            if (items != null) {
                for (i in 0 until items.length()) {
                    val item = items.optJSONObject(i) ?: continue
                    val id = item.optJSONObject("id") ?: continue
                    val snippet = item.optJSONObject("snippet") ?: continue

                    val videoId = id.optString("videoId")
                    val title = snippet.optString("title")

                    if (videoId.isNotEmpty()) {
                        videoIds.add(videoId)
                        videoTitles.add(title)
                    }
                }
            }

            return SearchResult(videoIds, videoTitles)

        } catch (e: IOException) {
            e.printStackTrace()
            return SearchResult(errorMessage = "Unable to load YouTube videos. Check API key or network.")
        } catch (e: JSONException) {
            e.printStackTrace()
            return SearchResult(errorMessage = "Error parsing response")
        } finally {
            connection?.disconnect()
            try {
                reader?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onPostExecute(result: SearchResult) {
        super.onPostExecute(result)
        if (result.errorMessage != null) {
            youTubeSearchObserver?.videoSearchFailed(result.errorMessage)
        } else if (result.videoIds != null && result.videoTitles != null) {
            youTubeSearchObserver?.videoSearchDataReceived(result.videoIds, result.videoTitles)
        }
    }
}
