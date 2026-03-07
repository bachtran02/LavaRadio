package dev.bachtran.lavaradio.service

import org.springframework.boot.context.properties.ConfigurationProperties
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

@ConfigurationProperties(prefix = "mediamtx")
data class MediaMtxConfig(
    val baseApiUrl : String = "http://127.0.0.1:9997/v3"
)

@Service
class StreamCleanupService (
    mediaMtxConfig: MediaMtxConfig,
    private val streamManagerService: StreamManagerService,
    restClientBuilder: RestClient.Builder,
) {
    companion object {
        const val STOP_UNDEMANDED_ACTIVE_STREAMS_RATE: Long = 60_000 * 10

        const val REMOVE_INACTIVE_STREAMS_RATE: Long = 60_000 * 60

        const val INACTIVITY_THRESHOLD: Long = 60_000 * 30

        const val MUXER_PATH = "/hlsmuxers/list"
    }

    private val logger = Logger.getLogger(StreamCleanupService::class.java.name)

    private val mediaMtxClient = restClientBuilder.baseUrl(
        mediaMtxConfig.baseApiUrl).build()

    @Scheduled(fixedRate = STOP_UNDEMANDED_ACTIVE_STREAMS_RATE)
    fun stopUndemandedActiveStreams() {
        /*
            Get active stream list from StreamManagerService and active muxer list from media-mtx.
            Stop streams that have no active muxer, which means there is no listener.
        */
        logger.info("Stopping undemanded active streams job started.")
        try {
            val activePathSet = getActivePaths()

            for (sessionInfo in streamManagerService.getStreamSessionsInfo(true)) {
                if (sessionInfo.isActive && !activePathSet.contains(sessionInfo.streamId)) {
                    /* No more active listener on this stream */
                    streamManagerService.stopStream(sessionInfo.streamId)
                    logger.info("Stream $sessionInfo stopped")
                }
            }
        } catch (e: Exception) {
            logger.severe("Error during stream cleanup: ${e.message}")
        }
        logger.info("Stopping undemanded active streams job completed.")
    }

    @Scheduled(fixedRate = REMOVE_INACTIVE_STREAMS_RATE)
    fun removeInactiveStreams() {
        /* Remove streams that have been inactive for more than 30 minutes. */
        logger.info("Removing inactive streams job started.")
        try {
            for (sessionInfo in streamManagerService.getStreamSessionsInfo(false)) {
                if (!sessionInfo.isActive &&
                    (System.currentTimeMillis() - sessionInfo.lastStopped) > INACTIVITY_THRESHOLD) {
                    streamManagerService.removeStream(sessionInfo.streamId)
                    logger.info("Stream $sessionInfo.streamId removed")
                }
            }
        } catch (e: Exception) {
            logger.severe("Error during inactive stream removal: ${e.message}")
        }
        logger.info("Removing inactive streams job completed.")
    }

    private fun getActivePaths(): HashSet<String> {
        /*
            Get existing muxers' path.
            Muxer exists as long as there is at least 1 listener
         */

        val pathSet = HashSet<String>()

        try {
            val response = mediaMtxClient.get()
                .uri(MUXER_PATH)
                .retrieve()
                .body<HlsMuxerResponse>()

            response?.items?.forEach { item -> pathSet.add(item.path) }
        } catch (e: Exception) {
            logger.severe("Failed to fetch HLS muxers: ${e.message}")
        }
        return pathSet
    }
}