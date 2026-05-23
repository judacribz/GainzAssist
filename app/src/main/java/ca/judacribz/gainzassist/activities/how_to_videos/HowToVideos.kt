package ca.judacribz.gainzassist.activities.how_to_videos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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

class HowToVideos : AppCompatActivity(),
    SearchView.OnQueryTextListener,
    SearchVideosTask.YouTubeSearchObserver,
    ThumbnailAdapter.VideoClickObserver {

    companion object {
        const val EXTRA_VIDEO_ID = "ca.judacribz.gainzassist.act_how_to_videos.EXTRA_VIDEO_ID"
    }

    private var thumbnailAdapter: ThumbnailAdapter? = null
    private var task: SearchVideosTask? = null

    private var youTubePlayer: YouTubePlayer? = null
    private var pendingVideoId: String? = null
    private var videoId: String? = null
    private var exerciseName: String? = null

    private lateinit var binding: ActivityHowToVideosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseName = intent.getStringExtra(EXTRA_HOW_TO_VID)
        setInitTheme(this)
        binding = ActivityHowToVideosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar(this, "How To $exerciseName", true)

        lifecycle.addObserver(binding.ypvPlayer)

        executeSearch("how to $exerciseName")

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

    private fun closePlayer() {
        youTubePlayer?.pause()
        binding.ypvPlayer.visibility = View.INVISIBLE
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

    private fun executeSearch(query: String) {
        if (BuildConfig.GOOGLE_API_KEY.isBlank()) {
            Snackbar.make(binding.rvVideoList, "Missing Google API Key for video search", Snackbar.LENGTH_LONG).show()
            return
        }

        val uri = Uri.Builder()
            .scheme("https")
            .authority("www.googleapis.com")
            .appendPath("youtube")
            .appendPath("v3")
            .appendPath("search")
            .appendQueryParameter("part", "snippet")
            .appendQueryParameter("fields", "items(id/videoId,snippet/title)")
            .appendQueryParameter("maxResults", "10")
            .appendQueryParameter("q", query)
            .appendQueryParameter("type", "video")
            .appendQueryParameter("videoEmbeddable", "true")
            .appendQueryParameter("safeSearch", "moderate")
            .appendQueryParameter("key", BuildConfig.GOOGLE_API_KEY)
            .build()

        task?.setYouTubeSearchObserver(null)
        task = SearchVideosTask()
        task?.setYouTubeSearchObserver(this)
        task?.execute(uri.toString())
    }

    override fun videoSearchDataReceived(videoIds: ArrayList<String>, videoTitles: ArrayList<String>) {
        if (videoIds.size > 0) {
            thumbnailAdapter = ThumbnailAdapter(videoIds, videoTitles)
            binding.rvVideoList.adapter = thumbnailAdapter
            thumbnailAdapter?.setVideoClickObserver(this)
        } else {
            Snackbar.make(binding.rvVideoList, "No video results", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun videoSearchFailed(message: String) {
        Snackbar.make(binding.rvVideoList, message, Snackbar.LENGTH_LONG).show()
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
}
