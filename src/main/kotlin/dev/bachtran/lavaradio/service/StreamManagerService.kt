package dev.bachtran.lavaradio.service

import dev.bachtran.lavaradio.dto.rest.StreamState
import dev.bachtran.lavaradio.exception.NoStreamPermissionException
import dev.bachtran.lavaradio.exception.StreamAlreadyActiveException
import dev.bachtran.lavaradio.exception.StreamInactiveException
import dev.bachtran.lavaradio.exception.StreamNotFoundException
import dev.bachtran.lavaradio.grpc.service.WebRTCService
import dev.bachtran.lavaradio.lavaplayer.service.RadioService
import dev.bachtran.lavaradio.session.StreamSession
import dev.bachtran.lavaradio.session.StreamSessionInfo
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import jakarta.annotation.PostConstruct
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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

    private val sessionLockMap = ConcurrentHashMap<String, ReentrantLock>()

    private val streamLocks = ConcurrentHashMap<String, ReentrantLock>()

    private val userLocks = ConcurrentHashMap<String, ReentrantLock>()

    @PostConstruct
    fun init() {
        val guestSession = StreamSession(
            GUEST_STREAM_ID,
            GUEST_STREAM_USER,
            radioProvider.getObject(),
            webrtcProvider.getObject()
        )
        userToStreamMap[GUEST_STREAM_USER] = GUEST_STREAM_ID
        activeSessions[GUEST_STREAM_ID] = guestSession
    }

    fun <T> execute(
        streamId: String,
        currentUserId: String? = null,
        action: (StreamSession) -> T
    ): T {
        val sessionLock = sessionLockMap.computeIfAbsent(streamId) { ReentrantLock() }
        return sessionLock.withLock {
            val session = activeSessions[streamId]
                ?: throw StreamNotFoundException(streamId)
            if (!session.isActive()) {
                throw StreamInactiveException(streamId)
            }
            if (currentUserId != null && session.userId() != currentUserId) {
                throw NoStreamPermissionException(streamId, currentUserId)
            }
            action(session)
        }
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

        val lock = userLocks.computeIfAbsent(userId) { ReentrantLock() }
        return lock.withLock {
            /* Check if user has active stream */
            val existingStreamId = userToStreamMap[userId]
            if (existingStreamId != null && activeSessions.containsKey(existingStreamId)) {
                /* Stream existed */
                return@withLock StreamState(existingStreamId, existed = true, active = false)
            }
            if (existingStreamId != null) {
                /* Remove stale session (ID still exists but no corresponding session */
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

            StreamState(newStreamId, existed = true, active = false)
        }
    }

    fun removeStream(streamId: String) {
        streamLock(streamId).withLock {
            val session = activeSessions[streamId] ?: throw StreamNotFoundException(streamId)
            if (session.isActive()) {
                /* Stop if stream is active and streaming */
                session.stopStream()
            }
            if (streamId != GUEST_STREAM_ID) {
                /* Don't remove guest stream */
                activeSessions.remove(streamId)
                userToStreamMap.remove(session.userId())
            }
            session.cleanup()
        }
    }

    fun startStream(streamId: String) {
        streamLock(streamId).withLock {
            val session = activeSessions[streamId] ?: throw StreamNotFoundException(streamId)
            if (session.isActive()) {
                throw StreamAlreadyActiveException(streamId)
            }
            session.startStream()
        }
    }

    fun stopStream(streamId: String) {
        streamLock(streamId).withLock {
            val session = activeSessions[streamId] ?: throw StreamNotFoundException(streamId)
            if (!session.isActive()) {
                throw StreamInactiveException(streamId)
            }
            session.stopStream()
        }
    }

    fun getRadioService(streamId: String): RadioService? {
        val session = activeSessions[streamId] ?: return null
        return session.getRadioService()
    }

    fun getStreamSessionsInfo(includeGuest: Boolean): List<StreamSessionInfo> {
        return activeSessions.values
            .filter { includeGuest || it.streamId() != GUEST_STREAM_ID }
            .map { it.getInfo() }
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

    private fun streamLock(streamId: String): ReentrantLock {
        return streamLocks.computeIfAbsent(streamId) { ReentrantLock() }
    }
}
