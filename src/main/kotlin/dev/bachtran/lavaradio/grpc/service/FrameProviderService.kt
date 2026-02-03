package dev.bachtran.lavaradio.grpc.service

import com.google.protobuf.ByteString
import dev.bachtran.lavaradio.lavaplayer.service.LavaplayerService
import io.grpc.stub.ServerCallStreamObserver
import io.grpc.stub.StreamObserver
import lavaradio.proto.AudioFrame
import lavaradio.proto.AudioProviderGrpc
import lavaradio.proto.StreamRequest
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service
class FrameProviderService(
    private val lavaService: LavaplayerService
) : AudioProviderGrpc.AudioProviderImplBase() {

    private val executor = Executors.newSingleThreadScheduledExecutor()
    private var pushAudioTask: ScheduledFuture<*>? = null

    override fun pullAudioStream(
        request: StreamRequest?,
        responseObserver: StreamObserver<AudioFrame?>
    ) {
        val serverCallObserver = responseObserver as ServerCallStreamObserver

        // 1. Define the unit of work
        val pushNextAudioBuffer = Runnable {
            try {
                if (serverCallObserver.isCancelled) {
                    stopStreaming()
                    return@Runnable
                }

                val frame = lavaService.provideFrame()
                val response = AudioFrame.newBuilder()

                if (frame == null) {
                    response.setIsSilence(true)
                    response.setOpusData(ByteString.EMPTY)
                } else {
                    response.setOpusData(ByteString.copyFrom(frame.data))
                    response.setIsSilence(false)
                }

                responseObserver.onNext(response.build())
            } catch (e: Exception) {
                responseObserver.onError(e)
                stopStreaming()
            }
        }

        // Scheduled task every 20ms
        pushAudioTask = executor.scheduleAtFixedRate(
            pushNextAudioBuffer, 0, 20, TimeUnit.MILLISECONDS
        )

        // Handle client disconnection cleanup
        serverCallObserver.setOnCancelHandler {
            stopStreaming()
        }
    }

    private fun stopStreaming() {
        pushAudioTask?.cancel(false)
        println("Streaming stopped and task cancelled.")
    }
}
