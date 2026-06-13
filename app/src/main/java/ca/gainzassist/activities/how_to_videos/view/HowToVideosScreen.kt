package ca.gainzassist.activities.how_to_videos.view

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

data class HowToVideosUiState(
    val videos: List<HowToVideoUiItem> = emptyList(),
    val isPlayerVisible: Boolean = false,
    val message: String? = null
)

data class HowToVideoUiItem(
    val videoId: String,
    val title: String
)

@Composable
fun HowToVideosScreen(
    uiState: HowToVideosUiState,
    onVideoClick: (String) -> Unit,
    playerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .fillMaxSize()
        .background(Color.Black)) {
        if (uiState.isPlayerVisible) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)) {
                playerContent()
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(uiState.videos) { video ->
                VideoItemRow(
                    item = video,
                    onClick = { onVideoClick(video.videoId) }
                )
            }
        }
    }
}

@Composable
private fun YouTubeThumbnail(
    videoId: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
        },
        update = { imageView ->
            val url = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg"
            Glide.with(imageView.context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        }
    )
}

@Composable
fun VideoItemRow(
    item: HowToVideoUiItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        YouTubeThumbnail(
            videoId = item.videoId,
            modifier = Modifier
                .width(120.dp)
                .height(90.dp)
                .padding(end = 16.dp)
        )
        Text(
            text = item.title,
            fontSize = 16.sp,
            color = Color.White
        )
    }
    Divider(color = Color.DarkGray)
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun HowToVideosScreenPreviewEmpty() {
    HowToVideosScreen(
        uiState = HowToVideosUiState(videos = emptyList()),
        onVideoClick = {},
        playerContent = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun HowToVideosScreenPreviewWithResults() {
    HowToVideosScreen(
        uiState = HowToVideosUiState(
            videos = listOf(
                HowToVideoUiItem("1", "How to Squat Properly"),
                HowToVideoUiItem("2", "Squat Mistakes to Avoid"),
                HowToVideoUiItem("3", "Advanced Squat Techniques")
            )
        ),
        onVideoClick = {},
        playerContent = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun HowToVideosScreenPreviewPlayerVisible() {
    HowToVideosScreen(
        uiState = HowToVideosUiState(
            isPlayerVisible = true,
            videos = listOf(
                HowToVideoUiItem("1", "How to Squat Properly"),
                HowToVideoUiItem("2", "Squat Mistakes to Avoid")
            )
        ),
        onVideoClick = {},
        playerContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 100.dp)
            ) {
                Text("YouTube Player Area", color = Color.White)
            }
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun HowToVideosScreenPreviewLongTitles() {
    HowToVideosScreen(
        uiState = HowToVideosUiState(
            videos = listOf(
                HowToVideoUiItem("1", "This is an extremely long title that should wrap to the next line or be handled gracefully in the UI without cutting off awkwardly."),
            )
        ),
        onVideoClick = {},
        playerContent = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 320, heightDp = 480)
@Composable
fun HowToVideosScreenPreviewSmallPhone() {
    HowToVideosScreen(
        uiState = HowToVideosUiState(
            isPlayerVisible = true,
            videos = listOf(
                HowToVideoUiItem("1", "How to Squat Properly"),
                HowToVideoUiItem("2", "Squat Mistakes to Avoid")
            )
        ),
        onVideoClick = {},
        playerContent = {
            Text("YouTube Player Area", color = Color.White)
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, fontScale = 1.5f)
@Composable
fun HowToVideosScreenPreviewLargeFont() {
    HowToVideosScreen(
        uiState = HowToVideosUiState(
            videos = listOf(
                HowToVideoUiItem("1", "How to Squat Properly")
            )
        ),
        onVideoClick = {},
        playerContent = {}
    )
}
