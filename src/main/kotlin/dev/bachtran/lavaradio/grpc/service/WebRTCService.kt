package dev.bachtran.lavaradio.grpc.service

import dev.bachtran.lavaradio.grpc.config.WebRTCGrpcConfig
import io.grpc.ManagedChannelBuilder
import lavaradio.proto.StartSessionRequest
import lavaradio.proto.EndSessionRequest
import lavaradio.proto.WebRTCManagerGrpc
import org.springframework.boot.grpc.server.autoconfigure.GrpcServerProperties
import org.springframework.stereotype.Service

@Service
class WebRTCService(
    private val grpcServerProperties: GrpcServerProperties,
    webRTCGrpcConfig: WebRTCGrpcConfig
) {
    private val channel = ManagedChannelBuilder.forAddress(webRTCGrpcConfig.host, webRTCGrpcConfig.port)
        .usePlaintext()
        .build()

    private val stub: WebRTCManagerGrpc.WebRTCManagerBlockingStub = WebRTCManagerGrpc.newBlockingStub(channel)

    fun startWebRTCSession() {
        val request = StartSessionRequest.newBuilder()
            .setAudioProviderAddress("${grpcServerProperties.address}:${grpcServerProperties.port}")
            .build()

        try {
            val response = stub.startSession(request)
            println("Session Accepted: ${response.accepted}")

        } catch (e: Exception) {
            println("RPC failed: ${e.message}")
        }
    }

    fun stopWebRTCSession() {
        val request = EndSessionRequest.newBuilder().build()

        try {
            val response = stub.stopSession(request)
            println("Session Ended: ${response.accepted}")

        } catch (e: Exception) {
            println("RPC failed: ${e.message}")
        }
    }
}