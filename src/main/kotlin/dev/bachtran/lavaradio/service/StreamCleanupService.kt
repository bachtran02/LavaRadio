package dev.bachtran.lavaradio.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.util.logging.Logger

data class HlsMuxerResponse(
    val itemCount: Int,
    val pageCount: Int,
    val items: List<HlsMuxerItem>
)

data class HlsMuxerItem(
    val path: String,
    val created: String,
    val lastRequest: String,
    val bytesSent: Long
)

@Service
class StreamCleanupService (
    private val streamManagerService: StreamManagerService,
    restClientBuilder: RestClient.Builder,
) {
    private val logger = Logger.getLogger(StreamCleanupService::class.java.name)

    private val mediaMtxClient = restClientBuilder.baseUrl("http://localhost:9997/v3").build()

    /* Executes on 5-minute interval */
    @Scheduled(fixedRate = 300_000)
    fun executeTask() {
        try {
            val activePathSet = getActivePaths()

            for ((sessionId, session) in streamManagerService.getActiveSessions()) {
                if (!activePathSet.contains(sessionId)) {
                    /* No more active listener on this stream */
                    session.stopStream()
                }
            }
        } catch (e: Exception) {
            logger.severe("Error during stream cleanup: ${e.message}")
        }
    }

    private fun getActivePaths(): HashSet<String> {
        /*
            Get existing muxers' path.
            Muxer exists as long as there is at least 1 listener
         */

        val pathSet = HashSet<String>()

        try {
            val response = mediaMtxClient.get()
                .uri("/hlsmuxers/list")
                .retrieve()
                .body<HlsMuxerResponse>()

            response?.items?.forEach { item -> pathSet.add(item.path) }
        } catch (e: Exception) {
            logger.severe("Failed to fetch HLS muxers: ${e.message}")
        }
        return pathSet
    }
}