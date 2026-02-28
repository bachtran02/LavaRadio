package dev.bachtran.lavaradio.session

import dev.bachtran.lavaradio.grpc.service.WebRTCService
import dev.bachtran.lavaradio.lavaplayer.service.RadioService

class StreamSession (
    private val streamId: String,
    private val userId: String,
    private val radioService: RadioService,
    private val grpcWebRTCService: WebRTCService,
) {
    fun streamId() = streamId

    fun userId() = userId

    fun getRadioService() = radioService

    fun createStream() {
        grpcWebRTCService.startWebRTCSession(streamId)
    }

//    fun removeStream() {
//        grpcWebRTCService.stopWebRTCSession(streamId)
//    }
}