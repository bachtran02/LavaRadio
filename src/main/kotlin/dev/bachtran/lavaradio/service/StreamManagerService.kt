package dev.bachtran.lavaradio.service

import dev.bachtran.lavaradio.service.grpc.FrameProviderService
import dev.bachtran.lavaradio.service.grpc.GrpcWebRTCService
import dev.bachtran.lavaradio.service.lavaplayer.LavaPlayerService
import org.springframework.stereotype.Service

@Service
class StreamManagerService (
    private val lavaplayerService: LavaPlayerService,
    private val grpcWebRTCService: GrpcWebRTCService,
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
