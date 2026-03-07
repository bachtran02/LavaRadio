package dev.bachtran.lavaradio.grpc.service

import dev.bachtran.lavaradio.grpc.config.WebRTCGrpcConfig
import io.grpc.ManagedChannelBuilder
import lavaradio.proto.StartSessionRequest
import lavaradio.proto.EndSessionRequest
import lavaradio.proto.WebRTCManagerGrpc
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class WebRTCService(
    webRTCGrpcConfig: WebRTCGrpcConfig
) {
    private val logger = Logger.getLogger(WebRTCService::class.java.name)

    private val channel = ManagedChannelBuilder.forAddress(webRTCGrpcConfig.host, webRTCGrpcConfig.port)
        .usePlaintext()
        .build()

    private val stub: WebRTCManagerGrpc.WebRTCManagerBlockingStub = WebRTCManagerGrpc.newBlockingStub(channel)

    fun startWebRTCSession(streamId: String): Boolean {
        val request = StartSessionRequest.newBuilder()
            .setStreamId(streamId)
            .build()

        try {
            stub.startSession(request)
            logger.info("Stream \"$streamId\" started successfully.")
            return true

        } catch (e: Exception) {
            logger.severe("Stream \"$streamId\" failed to start: ${e.message}")
        }
        return false
    }

    fun stopWebRTCSession(streamId: String): Boolean {
        val request = EndSessionRequest.newBuilder()
            .setStreamId(streamId)
            .build()

        try {
            stub.stopSession(request)
            logger.info("Stream $streamId ended successfully.")
            return true
        } catch (e: Exception) {
            logger.severe("Stream $streamId failed to end: ${e.message}")
        }
        return false
    }
}