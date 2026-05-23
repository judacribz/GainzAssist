package ca.judacribz.gainzassist.activities.how_to_videos

import android.os.AsyncTask
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class SearchVideosTask : AsyncTask<String, Void, List<ArrayList<String>>>() {

    interface YouTubeSearchObserver {
        fun videoSearchDataReceived(videoIds: ArrayList<String>, videoTitles: ArrayList<String>)
    }

    private var youTubeSearchObserver: YouTubeSearchObserver? = null

    fun setYouTubeSearchObserver(youTubeSearchObserver: YouTubeSearchObserver?) {
        this.youTubeSearchObserver = youTubeSearchObserver
    }

    override fun doInBackground(vararg strings: String): List<ArrayList<String>>? {
        val result = ArrayList<ArrayList<String>>()
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null

        try {
            val url = URL(strings[0])
            connection = url.openConnection() as HttpURLConnection
            connection.connect()

            val stream = connection.inputStream
            reader = BufferedReader(InputStreamReader(stream))

            val buffer = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                buffer.append(line).append("\n")
            }

            val jsonObject = JSONObject(buffer.toString())
            val items = jsonObject.getJSONArray("items")

            val videoIds = ArrayList<String>()
            val videoTitles = ArrayList<String>()

            for (i in 0 until items.length()) {
                val item = items.getJSONObject(i)
                val id = item.getJSONObject("id")
                val snippet = item.getJSONObject("snippet")

                videoIds.add(id.getString("videoId"))
                videoTitles.add(snippet.getString("title"))
            }

            result.add(videoIds)
            result.add(videoTitles)

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
            try {
                reader?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return result
    }

    override fun onPostExecute(lists: List<ArrayList<String>>) {
        super.onPostExecute(lists)
        if (lists.size == 2) {
            youTubeSearchObserver?.videoSearchDataReceived(lists[0], lists[1])
        }
    }
}
