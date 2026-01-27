package dev.bachtran.lavaradio.service

import dev.bachtran.lavaradio.service.grpc.FrameProviderService
import dev.bachtran.lavaradio.service.grpc.GrpcWebRTCService
import dev.bachtran.lavaradio.service.lavaplayer.LavaPlayerService
import org.springframework.stereotype.Service

@Service
class StreamManagerService (
    private val lavaService: LavaPlayerService,
    private val grpcWebRTCService: GrpcWebRTCService,
    private val frameProviderService: FrameProviderService,
) {

    @Synchronized
    fun startStream() {

        grpcWebRTCService.startWebRTCSession()


    }

    fun stopStream() {
        // TODO: Implement logic to handle the end of a stream
    }
}
