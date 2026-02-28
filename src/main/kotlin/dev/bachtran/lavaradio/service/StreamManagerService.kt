package dev.bachtran.lavaradio.service

import dev.bachtran.lavaradio.dto.rest.StreamState
import dev.bachtran.lavaradio.grpc.service.WebRTCService
import dev.bachtran.lavaradio.lavaplayer.service.RadioService
import dev.bachtran.lavaradio.session.StreamSession
import org.springframework.beans.factory.ObjectProvider
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Service
class StreamManagerService (
    private val radioProvider: ObjectProvider<RadioService>,
    private val webrtcProvider: ObjectProvider<WebRTCService>
) {
    private val userToStreamMap = ConcurrentHashMap<String, String>()

    private val activeSessions = ConcurrentHashMap<String, StreamSession>()

    fun <T> execute(
        streamId: String,
        currentUserId: String? = null,
        action: (StreamSession) -> T
    ): T {
        val session = activeSessions[streamId]
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Stream not found")

        if (currentUserId != null && session.userId() != currentUserId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this stream")
        }
        return action(session)
    }

    fun <T> withRadio(
        streamId: String,
        currentUserId: String? = null,
        block: (RadioService) -> T
    ): T {
        return execute(streamId, currentUserId) { session -> block(session.getRadioService()) }
    }

    fun createStream(userId: String) : String {

        /* Check if user has active stream */
        val existingStreamId = userToStreamMap[userId]
        if (existingStreamId != null && activeSessions.containsKey(existingStreamId)) {
            return existingStreamId
        }
        if (existingStreamId != null) {
            /* remove stale session */
            userToStreamMap.remove(userId)
        }
        val newStreamId = generateUniqueId()
        val newSession = StreamSession(
            newStreamId,
            userId,
            radioProvider.getObject(),
            webrtcProvider.getObject()
        )

        /* Update maps */
        activeSessions[newStreamId] = newSession
        userToStreamMap[userId] = newStreamId

        /* Create stream */
        newSession.createStream()

        return newStreamId
    }

    fun getStreamState(streamId: String): StreamState {
        val session = activeSessions[streamId]
        if (session != null) {
            return StreamState(active = true)
        }
        return StreamState(active = false)
    }

    fun getRadioService(streamId: String): RadioService? {
        val session = activeSessions[streamId] ?: return null
        return session.getRadioService()
    }

    private fun generateUniqueId(): String {
        /* Generate 8-digit random ID */
        // NOTE: improve on this later
        var id: String
        do {
            id = UUID.randomUUID().toString().substring(0, 8)
        } while (activeSessions.containsKey(id))
        return id
    }
}
