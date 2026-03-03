package dev.bachtran.lavaradio.grpc.service

import dev.bachtran.lavaradio.grpc.config.WebRTCGrpcConfig
import io.grpc.ManagedChannelBuilder
import lavaradio.proto.StartSessionRequest
import lavaradio.proto.EndSessionRequest
import lavaradio.proto.WebRTCManagerGrpc
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class WebRTCService(
    webRTCGrpcConfig: WebRTCGrpcConfig
) {
    private val channel = ManagedChannelBuilder.forAddress(webRTCGrpcConfig.host, webRTCGrpcConfig.port)
        .usePlaintext()
        .build()

    private val stub: WebRTCManagerGrpc.WebRTCManagerBlockingStub = WebRTCManagerGrpc.newBlockingStub(channel)

    fun startWebRTCSession(streamId: String) {
        val request = StartSessionRequest.newBuilder()
            .setStreamId(streamId)
            .build()

        try {
            val response = stub.startSession(request)
            println("Session Accepted: ${response.accepted}")

        } catch (e: Exception) {
            println("RPC failed: ${e.message}")
        }
    }

    fun stopWebRTCSession(streamId: String) {
        val request = EndSessionRequest.newBuilder()
            .setStreamId(streamId)
            .build()

        try {
            val response = stub.stopSession(request)
            println("Session Ended: ${response.accepted}")

        } catch (e: Exception) {
            println("RPC failed: ${e.message}")
        }
    }
}