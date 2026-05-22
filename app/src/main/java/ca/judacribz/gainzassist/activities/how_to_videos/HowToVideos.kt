package ca.judacribz.gainzassist.activities.how_to_videos

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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

class HowToVideos : AppCompatActivity(),
    SearchView.OnQueryTextListener,
    SearchVideosTask.YouTubeSearchObserver,
    ThumbnailAdapter.VideoClickObserver {

    companion object {
        const val EXTRA_VIDEO_ID = "ca.judacribz.gainzassist.act_how_to_videos.EXTRA_VIDEO_ID"
        const val SEARCH_SPACE_STR = "%20"
    }

    var URL = ("https://www.googleapis.com/youtube/v3/search?" + // default youtube search url
            "part=snippet&" + // search resource
            "fields=items(id/videoId,snippet/title)&" + // needed fields
            "maxResults=10&" + // number of results to show
            "q=") // search text

    private var thumbnailAdapter: ThumbnailAdapter? = null
    private var task: SearchVideosTask? = null

    private var youTubePlayer: YouTubePlayer? = null
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
                videoId?.let { player.cueVideo(it, 0f) }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        if (binding.ypvPlayer.visibility == View.GONE) {
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
        binding.ypvPlayer.visibility = View.GONE
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
        task?.setYouTubeSearchObserver(null)
        task = SearchVideosTask()
        task?.setYouTubeSearchObserver(this)
        task?.execute(
            URL + query.replace("\\s+".toRegex(), SEARCH_SPACE_STR) +
                    "&key=" +
                    BuildConfig.GOOGLE_API_KEY
        )
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

    override fun onVideoClick(videoId: String) {
        this.videoId = videoId
        binding.ypvPlayer.visibility = View.VISIBLE
        youTubePlayer?.loadVideo(videoId, 0f)
    }
}
