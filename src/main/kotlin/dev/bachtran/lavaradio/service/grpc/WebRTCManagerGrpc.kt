package dev.bachtran.lavaradio.service.grpc
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver
import lavaradio.proto.SessionRequest
import lavaradio.proto.WebRTCManagerGrpc
import org.springframework.stereotype.Service

@Service
class GrpcWebRTCService {
    private val channel = ManagedChannelBuilder.forAddress("127.0.0.1", 50051)
        .usePlaintext()
        .build()

    private val stub: WebRTCManagerGrpc.WebRTCManagerBlockingStub = WebRTCManagerGrpc.newBlockingStub(channel)

    fun startWebRTCSession() {
        val request = SessionRequest.newBuilder()
            .setStreamId("whatever-stream-id")
            .setKotlinProviderAddress("127.0.0.1:9090")
            .build()

        try {
            val response = stub.startSession(request)
            println("Session Accepted: ${response.accepted}")

        } catch (e: Exception) {
            println("RPC failed: ${e.message}")
        }
    }
}