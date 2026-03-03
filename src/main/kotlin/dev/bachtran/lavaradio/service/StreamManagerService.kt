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
import jakarta.annotation.PostConstruct

@Service
class StreamManagerService (
    private val radioProvider: ObjectProvider<RadioService>,

    private val webrtcProvider: ObjectProvider<WebRTCService>
) {
    companion object {
        const val GUEST_STREAM_ID = "guest"
        const val GUEST_STREAM_USER = "guest"
    }

    private val userToStreamMap = ConcurrentHashMap<String, String>()

    private val activeSessions = ConcurrentHashMap<String, StreamSession>()

    @PostConstruct
    fun init() {
        val guestSession = StreamSession(
            GUEST_STREAM_ID,
            GUEST_STREAM_USER,
            radioProvider.getObject(),
            webrtcProvider.getObject()
        )
        activeSessions[GUEST_STREAM_ID] = guestSession
    }

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

    fun getStreamState(streamId: String): StreamState {
        val session = activeSessions[streamId] ?: return StreamState(streamId, existed = false, active = false)
        return StreamState(streamId, existed = true, active = session.isActive())
    }

    fun getOrCreateStream(userId: String) : StreamState {

        /* Check if user has active stream */
        val existingStreamId = userToStreamMap[userId]
        if (existingStreamId != null && activeSessions.containsKey(existingStreamId)) {
            return StreamState(existingStreamId, existed = true, active = false)
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

        return StreamState(newStreamId, existed = true, active = false)
    }

    fun removeStream(streamId: String) {
        val session = activeSessions.remove(streamId) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Stream not found")
        if (session.isActive()) {
            /* Stop if stream is active and streaming */
            session.stopStream()
        }
        userToStreamMap.remove(session.userId())
        session.cleanup()
    }

    fun startStream(streamId: String) {
        val session = activeSessions[streamId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Stream not found")
        if (session.isActive()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Stream is already active")
        }
        session.startStream()
    }

    fun stopStream(streamId: String) {
        val session = activeSessions[streamId] ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Stream not found")
        if (!session.isActive()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Stream is not active")
        }
        session.stopStream()
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
