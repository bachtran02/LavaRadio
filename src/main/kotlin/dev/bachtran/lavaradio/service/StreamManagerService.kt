package dev.bachtran.lavaradio.service

import dev.bachtran.lavaradio.grpc.service.WebRTCService
import dev.bachtran.lavaradio.lavaplayer.service.LavaplayerService
import org.springframework.stereotype.Service

@Service
class StreamManagerService (
    private val lavaplayerService: LavaplayerService,
    private val grpcWebRTCService: WebRTCService,
) {

    @Synchronized
    fun startStream() {
        grpcWebRTCService.startWebRTCSession()
    }

    fun stopStream() {
        grpcWebRTCService.stopWebRTCSession()
        lavaplayerService.stop()
    }
}
