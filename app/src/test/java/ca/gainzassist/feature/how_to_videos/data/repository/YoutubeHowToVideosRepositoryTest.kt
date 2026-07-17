package ca.gainzassist.feature.how_to_videos.data.repository

import ca.gainzassist.core.coroutines.DispatcherProvider
import ca.gainzassist.feature.how_to_videos.domain.model.YouTubeVideoException
import ca.gainzassist.feature.how_to_videos.domain.repository.AndroidSignatureProvider
import ca.gainzassist.feature.how_to_videos.domain.repository.YouTubeApiConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicReference

@OptIn(ExperimentalCoroutinesApi::class)
class YoutubeHowToVideosRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider = object : DispatcherProvider {
        override val main: CoroutineDispatcher = testDispatcher
        override val io: CoroutineDispatcher = testDispatcher
        override val default: CoroutineDispatcher = testDispatcher
    }

    private val validJson = """
        {
          "items": [
            {
              "id": {
                "videoId": "abc123"
              },
              "snippet": {
                "title": "Bench Press Tutorial"
              }
            }
          ]
        }
    """.trimIndent()

    private val emptyJson = """
        {
          "items": []
        }
    """.trimIndent()

    private val malformedJson = "not-json"

    private class TestApiConfig(override var apiKey: String = "test-api-key") : YouTubeApiConfig

    private class TestSignatureProvider : AndroidSignatureProvider {
        override val packageName: String = "ca.gainzassist.test"
        override val certificateSha1: String = "1234"
    }

    private val apiConfig = TestApiConfig()
    private val signatureProvider = TestSignatureProvider()

    @Before
    fun setUp() {
        // No Android dependencies to set up
    }

    private fun createRepository(engine: MockEngine): YoutubeHowToVideosRepository {
        return YoutubeHowToVideosRepository(
            httpClient = HttpClient(engine),
            dispatcherProvider = dispatcherProvider,
            apiConfig = apiConfig,
            signatureProvider = signatureProvider
        )
    }

    @Test
    fun `searchVideos with blank query returns empty list and performs no request`() = runTest {
        var requestMade = false
        val engine = MockEngine { request ->
            requestMade = true
            respond(validJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val repo = createRepository(engine)

        val result = repo.searchVideos("   ")

        assertTrue(result.isEmpty())
        assertTrue(!requestMade)
    }

    @Test
    fun `searchVideos with missing API key throws MissingApiKey exception`() = runTest {
        apiConfig.apiKey = "   "
        var requestMade = false
        val engine = MockEngine { request ->
            requestMade = true
            respond(validJson, HttpStatusCode.OK)
        }
        val repo = createRepository(engine)

        val exception = runCatching { repo.searchVideos("workout") }.exceptionOrNull()

        assertTrue(exception is YouTubeVideoException.MissingApiKey)
        assertTrue(!requestMade)
    }

    @Test
    fun `searchVideos with successful response parses correctly`() = runTest {
        val engine = MockEngine { request ->
            respond(validJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val repo = createRepository(engine)

        val result = repo.searchVideos("workout")

        assertEquals(1, result.size)
        assertEquals("abc123", result[0].videoId)
        assertEquals("Bench Press Tutorial", result[0].title)
    }

    @Test
    fun `searchVideos with empty successful response returns empty list`() = runTest {
        val engine = MockEngine { request ->
            respond(emptyJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val repo = createRepository(engine)

        val result = repo.searchVideos("workout")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `searchVideos with malformed JSON throws InvalidResponse exception`() = runTest {
        val engine = MockEngine { request ->
            respond(malformedJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val repo = createRepository(engine)

        val exception = runCatching { repo.searchVideos("workout") }.exceptionOrNull()

        assertTrue(exception is YouTubeVideoException.InvalidResponse)
    }

    @Test
    fun `searchVideos with quotaExceeded response throws QuotaExceeded exception`() = runTest {
        val engine = MockEngine { request ->
            respond("error quotaExceeded limit", HttpStatusCode.Forbidden)
        }
        val repo = createRepository(engine)

        val exception = runCatching { repo.searchVideos("workout") }.exceptionOrNull()

        assertTrue(exception is YouTubeVideoException.QuotaExceeded)
    }

    @Test
    fun `searchVideos with dailyLimitExceeded response throws QuotaExceeded exception`() = runTest {
        val engine = MockEngine { request ->
            respond("dailyLimitExceeded reached", HttpStatusCode.Forbidden)
        }
        val repo = createRepository(engine)

        val exception = runCatching { repo.searchVideos("workout") }.exceptionOrNull()

        assertTrue(exception is YouTubeVideoException.QuotaExceeded)
    }

    @Test
    fun `searchVideos with generic non-success HTTP response throws RequestFailed exception`() = runTest {
        val engine = MockEngine { request ->
            respond("Internal Server Error", HttpStatusCode.InternalServerError)
        }
        val repo = createRepository(engine)

        val exception = runCatching { repo.searchVideos("workout") }.exceptionOrNull()

        assertTrue(exception is YouTubeVideoException.RequestFailed)
        assertEquals(500, (exception as YouTubeVideoException.RequestFailed).statusCode)
    }

    @Test
    fun `searchVideos with network failure throws Network exception`() = runTest {
        val engine = MockEngine { request ->
            throw IOException("Network offline")
        }
        val repo = createRepository(engine)

        val exception = runCatching { repo.searchVideos("workout") }.exceptionOrNull()

        assertTrue(exception is YouTubeVideoException.Network)
    }

    @Test
    fun `searchVideos with CancellationException propagates cancellation`() = runTest {
        val engine = MockEngine { request ->
            throw CancellationException("Cancelled by coroutine")
        }
        val repo = createRepository(engine)

        val exception = runCatching { repo.searchVideos("workout") }.exceptionOrNull()

        assertTrue(exception is CancellationException)
    }

    @Test
    fun `searchVideos constructs correct request`() = runTest {
        apiConfig.apiKey = "secret-key-123"
        val requestCapture = AtomicReference<HttpRequestData>()
        val engine = MockEngine { request ->
            requestCapture.set(request)
            respond(validJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val repo = createRepository(engine)

        repo.searchVideos("bench press  ")

        val req = requestCapture.get()
        assertEquals("https://www.googleapis.com/youtube/v3/search", req.url.toString().substringBefore("?"))
        assertEquals("snippet", req.url.parameters["part"])
        assertEquals("items(id/videoId,snippet/title)", req.url.parameters["fields"])
        assertEquals("10", req.url.parameters["maxResults"])
        assertEquals("bench press", req.url.parameters["q"])
        assertEquals("video", req.url.parameters["type"])
        assertEquals("true", req.url.parameters["videoEmbeddable"])
        assertEquals("moderate", req.url.parameters["safeSearch"])
        assertEquals("secret-key-123", req.url.parameters["key"])

        assertEquals("ca.gainzassist.test", req.headers["X-Android-Package"])
    }
}
