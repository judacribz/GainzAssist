package ca.gainzassist.activities.how_to_videos.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import ca.gainzassist.BuildConfig
import ca.gainzassist.activities.start_workout.view.StartWorkoutActivity.Companion.EXTRA_HOW_TO_VID
import ca.gainzassist.core.util.UI.setInitTheme
import ca.gainzassist.ui.components.HowToVideosTopBar
import ca.gainzassist.ui.components.HowToVideosTopBarActions
import ca.gainzassist.ui.components.HowToVideosTopBarState
import com.google.android.material.snackbar.Snackbar
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
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

class HowToVideosActivity : AppCompatActivity() {

    companion object {
        private const val ERR_YOUTUBE_LOAD = "Unable to load YouTube videos. Check API key or network."
    }

    private var videos by mutableStateOf<List<HowToVideoUiItem>>(emptyList())
    private var isPlayerVisible by mutableStateOf(false)

    private var youTubePlayer: YouTubePlayer? = null
    private var pendingVideoId: String? = null
    private var videoId: String? = null
    private var exerciseName: String? = null

    private var isSearchExpanded by mutableStateOf(false)
    private var searchQuery by mutableStateOf("")

    private val queryCache = HashMap<String, Pair<ArrayList<String>, ArrayList<String>>>()
    private var activeQuery: String? = null

    // Create the YouTubePlayerView once to pass to AndroidView
    private val youTubePlayerView by lazy { YouTubePlayerView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseName = intent.getStringExtra(EXTRA_HOW_TO_VID)
        setInitTheme(this)
        
        onBackPressedDispatcher.addCallback(this) {
            navigateBack()
        }
        
        setContent {
            Column(Modifier.fillMaxSize()) {
                HowToVideosTopBar(
                    state = HowToVideosTopBarState(
                        title = "How To ${exerciseName ?: ""}",
                        isSearchExpanded = isSearchExpanded,
                        searchQuery = searchQuery
                    ),
                    actions = HowToVideosTopBarActions(
                        onSearchQueryChange = { searchQuery = it },
                        onSearchSubmit = {
                            val query = searchQuery.trim()
                            if (query.isNotEmpty()) {
                                isSearchExpanded = false
                                executeSearch(query)
                            } else {
                                Snackbar.make(
                                    findViewById(android.R.id.content),
                                    "Please enter a search term",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onSearchClick = {
                            isSearchExpanded = true
                        },
                        onCloseSearchClick = {
                            isSearchExpanded = false
                            searchQuery = ""
                            executeSearch(exerciseName ?: "")
                        },
                        onBackClick = { navigateBack() }
                    )
                )
                
                HowToVideosScreen(
                    uiState = HowToVideosUiState(
                        videos = videos,
                        isPlayerVisible = isPlayerVisible
                    ),
                    onVideoClick = { onVideoClick(it) },
                    playerContent = {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { youTubePlayerView }
                        )
                    }
                )
            }
        }

        lifecycle.addObserver(youTubePlayerView)

        if (exerciseName.isNullOrBlank()) {
            val v = window.decorView.rootView
            Snackbar.make(v, "Search for an exercise video.", Snackbar.LENGTH_LONG).show()
        } else {
            executeSearch("how to $exerciseName")
        }

        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                this@HowToVideosActivity.youTubePlayer = youTubePlayer
                pendingVideoId?.let {
                    youTubePlayer.loadVideo(it, 0f)
                    pendingVideoId = null
                }
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                super.onError(youTubePlayer, error)
                val currentVideoId = videoId
                val v = window.decorView.rootView
                if (currentVideoId != null) {
                    Snackbar.make(v, "Unable to play this video", Snackbar.LENGTH_LONG)
                        .setAction("Open in YouTube") {
                            val intent = Intent(Intent.ACTION_VIEW,
                                "https://www.youtube.com/watch?v=$currentVideoId".toUri())
                            startActivity(intent)
                        }
                        .show()
                } else {
                    Snackbar.make(v, "Unable to play this video", Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun navigateBack() {
        if (isPlayerVisible) {
            closePlayer()
        } else {
            finish()
        }
    }

    private fun onVideoClick(videoId: String) {
        this.videoId = videoId
        isPlayerVisible = true
        if (youTubePlayer != null) {
            youTubePlayer?.loadVideo(videoId, 0f)
        } else {
            pendingVideoId = videoId
        }
    }

    private fun closePlayer() {
        youTubePlayer?.pause()
        isPlayerVisible = false
    }

    private fun executeSearch(query: String) {
        val queryKey = query.trim()

        if (queryKey == activeQuery) {
            return
        }

        if (queryCache.containsKey(queryKey)) {
            val cached = queryCache[queryKey]
            if (cached != null) {
                displaySearchResults(cached.first, cached.second)
            }
            return
        }

        if (BuildConfig.GOOGLE_API_KEY.isBlank()) {
            val v = window.decorView.rootView
            Snackbar.make(v, "Missing Google API Key for video search", Snackbar.LENGTH_LONG).show()
            return
        }

        activeQuery = queryKey

        val uri = Uri.Builder()
            .scheme("https")
            .authority("www.googleapis.com")
            .appendPath("youtube")
            .appendPath("v3")
            .appendPath("search")
            .appendQueryParameter("part", "snippet")
            .appendQueryParameter("fields", "items(id/videoId,snippet/title)")
            .appendQueryParameter("maxResults", "10")
            .appendQueryParameter("q", queryKey)
            .appendQueryParameter("type", "video")
            .appendQueryParameter("videoEmbeddable", "true")
            .appendQueryParameter("safeSearch", "moderate")
            .appendQueryParameter("key", BuildConfig.GOOGLE_API_KEY)
            .build()

        searchYouTube(queryKey, uri.toString())
    }

    private fun getAppSha1(): String {
        try {
            val signatures = PackageInfoCompat.getSignatures(packageManager, packageName)
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

    private fun searchYouTube(queryKey: String, urlString: String) {
        lifecycleScope.launch {
            try {
                val (videoIds, videoTitles) = performYouTubeSearch(urlString)
                videoSearchDataReceived(queryKey, videoIds, videoTitles)
            } catch (e: Exception) {
                videoSearchFailed(queryKey, e.message ?: ERR_YOUTUBE_LOAD)
            }
        }
    }

    private suspend fun performYouTubeSearch(
        urlString: String,
        ioDispatcher: kotlinx.coroutines.CoroutineDispatcher = Dispatchers.IO
    ): Pair<ArrayList<String>, ArrayList<String>> = withContext(ioDispatcher) {
        var connection: HttpURLConnection? = null
        var reader: BufferedReader? = null

        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("X-Android-Package", packageName)
            connection.setRequestProperty("X-Android-Cert", getAppSha1())
            connection.connect()

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream

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
                Log.e("HowToVideos", "Error $responseCode for $sanitizedUrl\nBody: $errorBody")

                val msg = if (errorBody.contains("quotaExceeded") || errorBody.contains("dailyLimitExceeded")) {
                    "YouTube video search quota exceeded. Please try again later."
                } else {
                    ERR_YOUTUBE_LOAD
                }
                throw Exception(msg)
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
            Pair(videoIds, videoTitles)
        } catch (e: JSONException) {
            e.printStackTrace()
            throw Exception("Error parsing response")
        } finally {
            connection?.disconnect()
            try {
                reader?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun videoSearchDataReceived(
        queryKey: String,
        videoIds: ArrayList<String>,
        videoTitles: ArrayList<String>
    ) {
        queryCache[queryKey] = Pair(videoIds, videoTitles)
        if (activeQuery == queryKey) activeQuery = null

        displaySearchResults(videoIds, videoTitles)
    }

    private fun videoSearchFailed(queryKey: String, message: String) {
        if (activeQuery == queryKey) activeQuery = null
        val v = window.decorView.rootView
        Snackbar.make(v, message, Snackbar.LENGTH_LONG).show()
    }

    private fun displaySearchResults(videoIds: ArrayList<String>, videoTitles: ArrayList<String>) {
        if (videoIds.isNotEmpty()) {
            val newList = mutableListOf<HowToVideoUiItem>()
            for (i in 0 until videoIds.size) {
                newList.add(HowToVideoUiItem(videoIds[i], videoTitles[i]))
            }
            videos = newList
        } else {
            val v = window.decorView.rootView
            Snackbar.make(v, "No video results", Snackbar.LENGTH_SHORT).show()
            videos = emptyList()
        }
    }
}
