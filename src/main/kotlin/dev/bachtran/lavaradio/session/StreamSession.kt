package dev.bachtran.lavaradio.session

import dev.bachtran.lavaradio.grpc.service.WebRTCService
import dev.bachtran.lavaradio.lavaplayer.service.RadioService

data class StreamSessionInfo(
    val streamId: String,
    val userId: String,
    val isActive: Boolean,
    val lastStopped: Long,
)

class StreamSession (

    private val streamId: String,

    private val userId: String,

    private val radioService: RadioService,

    private val grpcWebRTCService: WebRTCService,
) {
    private var lastStopped = System.currentTimeMillis()

    private var isActive: Boolean = false

    fun isActive() = isActive

    fun streamId() = streamId

    fun userId() = userId

    fun getRadioService() = radioService

    fun startStream() {
        grpcWebRTCService.startWebRTCSession(streamId)
        isActive = true
    }

    fun stopStream() {
        grpcWebRTCService.stopWebRTCSession(streamId)
        lastStopped = System.currentTimeMillis()
        isActive = false
    }

    fun cleanup() {
        radioService.cleanup()
    }

    fun getInfo(): StreamSessionInfo {
        return StreamSessionInfo(
            streamId = streamId,
            userId = userId,
            isActive = isActive,
            lastStopped = lastStopped
        )
    }
}