package ca.judacribz.gainzassist.activities.how_to_videos

import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import ca.judacribz.gainzassist.BuildConfig
import ca.judacribz.gainzassist.R
import ca.judacribz.gainzassist.activities.start_workout.StartWorkout.Companion.EXTRA_HOW_TO_VID
import ca.judacribz.gainzassist.databinding.ActivityHowToVideosBinding
import ca.judacribz.gainzassist.util.UI.setInitTheme
import ca.judacribz.gainzassist.util.UI.setToolbar
import com.google.android.material.snackbar.Snackbar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.ArrayList
import java.util.HashMap

class HowToVideos : AppCompatActivity(),
    SearchView.OnQueryTextListener,
    ThumbnailAdapter.VideoClickObserver {

    companion object {
        const val EXTRA_VIDEO_ID = "ca.judacribz.gainzassist.act_how_to_videos.EXTRA_VIDEO_ID"
    }

    private var thumbnailAdapter: ThumbnailAdapter? = null

    private var youTubePlayer: YouTubePlayer? = null
    private var pendingVideoId: String? = null
    private var videoId: String? = null
    private var exerciseName: String? = null

    private val queryCache = HashMap<String, Pair<ArrayList<String>, ArrayList<String>>>()
    private var activeQuery: String? = null

    private lateinit var binding: ActivityHowToVideosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseName = intent.getStringExtra(EXTRA_HOW_TO_VID)
        setInitTheme(this)
        binding = ActivityHowToVideosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar(this, "How To ${exerciseName ?: ""}", true)

        lifecycle.addObserver(binding.ypvPlayer)

        if (exerciseName.isNullOrBlank()) {
            Snackbar.make(binding.rvVideoList, "Search for an exercise video.", Snackbar.LENGTH_LONG).show()
        } else {
            executeSearch("how to $exerciseName")
        }

        binding.rvVideoList.layoutManager = LinearLayoutManager(this)
        binding.rvVideoList.setHasFixedSize(true)

        binding.ypvPlayer.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(player: YouTubePlayer) {
                youTubePlayer = player
                pendingVideoId?.let {
                    player.loadVideo(it, 0f)
                    pendingVideoId = null
                }
            }

            override fun onError(player: YouTubePlayer, error: PlayerConstants.PlayerError) {
                super.onError(player, error)
                val currentVideoId = videoId
                if (currentVideoId != null) {
                    Snackbar.make(binding.rvVideoList, "Unable to play this video", Snackbar.LENGTH_LONG)
                        .setAction("Open in YouTube") {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$currentVideoId"))
                            startActivity(intent)
                        }
                        .show()
                } else {
                    Snackbar.make(binding.rvVideoList, "Unable to play this video", Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        if (binding.ypvPlayer.visibility == View.INVISIBLE || binding.ypvPlayer.visibility == View.GONE) {
            finish()
        } else {
            closePlayer()
        }
        return true
    }

    override fun onBackPressed() {
        if (binding.ypvPlayer.visibility == View.VISIBLE) {
            closePlayer()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(mainMenu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_how_to, mainMenu)
        val searchItem = mainMenu.findItem(R.id.act_search)
        val searchView = searchItem.actionView as? SearchView
        searchView?.setOnQueryTextListener(this)
        return super.onCreateOptionsMenu(mainMenu)
    }

    override fun onQueryTextChange(query: String): Boolean {
        return false
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        executeSearch(query)
        return false
    }

    override fun onVideoClick(videoId: String) {
        this.videoId = videoId
        binding.ypvPlayer.visibility = View.VISIBLE
        if (youTubePlayer != null) {
            youTubePlayer?.loadVideo(videoId, 0f)
        } else {
            pendingVideoId = videoId
        }
    }

    private fun closePlayer() {
        youTubePlayer?.pause()
        binding.ypvPlayer.visibility = View.INVISIBLE
    }

    private fun executeSearch(query: String) {
        val trimmedQuery = query.trim()

        if (trimmedQuery == activeQuery) {
            return
        }

        if (queryCache.containsKey(trimmedQuery)) {
            val cached = queryCache[trimmedQuery]
            if (cached != null) {
                videoSearchDataReceived(cached.first, cached.second)
            }
            return
        }

        if (BuildConfig.GOOGLE_API_KEY.isBlank()) {
            Snackbar.make(binding.rvVideoList, "Missing Google API Key for video search", Snackbar.LENGTH_LONG).show()
            return
        }

        activeQuery = trimmedQuery

        val uri = Uri.Builder()
            .scheme("https")
            .authority("www.googleapis.com")
            .appendPath("youtube")
            .appendPath("v3")
            .appendPath("search")
            .appendQueryParameter("part", "snippet")
            .appendQueryParameter("fields", "items(id/videoId,snippet/title)")
            .appendQueryParameter("maxResults", "10")
            .appendQueryParameter("q", trimmedQuery)
            .appendQueryParameter("type", "video")
            .appendQueryParameter("videoEmbeddable", "true")
            .appendQueryParameter("safeSearch", "moderate")
            .appendQueryParameter("key", BuildConfig.GOOGLE_API_KEY)
            .build()

        searchYouTube(uri.toString())
    }

    private fun getAppSha1(): String {
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            val signatures = packageInfo.signatures
            if (signatures != null) {
                for (signature in signatures) {
                    val md = MessageDigest.getInstance("SHA-1")
                    md.update(signature.toByteArray())
                    val digest = md.digest()
                    val hexString = StringBuilder()
                    for (b in digest) {
                        hexString.append(String.format("%02X", b))
                    }
                    return hexString.toString()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun searchYouTube(urlString: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            var reader: BufferedReader? = null
            var errorMessage: String? = null
            var videoIds: ArrayList<String>? = null
            var videoTitles: ArrayList<String>? = null

            try {
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("X-Android-Package", packageName)
                connection.setRequestProperty("X-Android-Cert", getAppSha1())
                connection.connect()

                val responseCode = connection.responseCode
                val stream = if (responseCode in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

                if (stream != null) {
                    reader = BufferedReader(InputStreamReader(stream))
                    val buffer = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        buffer.append(line).append("\n")
                    }

                    if (responseCode !in 200..299) {
                        val errorBody = buffer.toString()
                        val sanitizedUrl = urlString.replace(Regex("&key=[^&]*"), "&key=***")
                        Log.e("HowToVideos", "Error $responseCode for $sanitizedUrl\nBody: $errorBody")

                        errorMessage = if (errorBody.contains("quotaExceeded") || errorBody.contains("dailyLimitExceeded")) {
                            "YouTube video search quota exceeded. Please try again later."
                        } else {
                            "Unable to load YouTube videos. Check API key or network."
                        }
                    } else {
                        val jsonObject = JSONObject(buffer.toString())
                        val items = jsonObject.optJSONArray("items")

                        videoIds = ArrayList()
                        videoTitles = ArrayList()

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
                    }
                } else {
                    errorMessage = "Unable to load YouTube videos. Check API key or network."
                }
            } catch (e: IOException) {
                e.printStackTrace()
                errorMessage = "Unable to load YouTube videos. Check API key or network."
            } catch (e: JSONException) {
                e.printStackTrace()
                errorMessage = "Error parsing response"
            } finally {
                connection?.disconnect()
                try {
                    reader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            withContext(Dispatchers.Main) {
                if (errorMessage != null) {
                    videoSearchFailed(errorMessage)
                } else if (videoIds != null && videoTitles != null) {
                    videoSearchDataReceived(videoIds, videoTitles)
                }
            }
        }
    }

    private fun videoSearchDataReceived(videoIds: ArrayList<String>, videoTitles: ArrayList<String>) {
        val currentQuery = activeQuery
        if (currentQuery != null) {
            queryCache[currentQuery] = Pair(videoIds, videoTitles)
        }
        activeQuery = null

        if (videoIds.size > 0) {
            thumbnailAdapter = ThumbnailAdapter(videoIds, videoTitles)
            binding.rvVideoList.adapter = thumbnailAdapter
            thumbnailAdapter?.setVideoClickObserver(this)
        } else {
            Snackbar.make(binding.rvVideoList, "No video results", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun videoSearchFailed(message: String) {
        activeQuery = null
        Snackbar.make(binding.rvVideoList, message, Snackbar.LENGTH_LONG).show()
    }
}
