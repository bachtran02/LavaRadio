package dev.bachtran.lavaradio.grpc.service

import com.google.protobuf.ByteString
import dev.bachtran.lavaradio.service.StreamManagerService
import io.grpc.stub.ServerCallStreamObserver
import io.grpc.stub.StreamObserver
import lavaradio.proto.AudioFrame
import lavaradio.proto.AudioProviderGrpc
import lavaradio.proto.StreamRequest
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Service
class FrameProviderService(
    private val streamManagerService: StreamManagerService,
) : AudioProviderGrpc.AudioProviderImplBase() {

    companion object {
        private const val MAX_CONCURRENT_STREAMS = 10
        private const val FRAME_INTERVAL_MS = 20L
    }

    private val activeStreams = ConcurrentHashMap<String, ScheduledExecutorService>()

    override fun pullAudioStream(
        request: StreamRequest,
        responseObserver: StreamObserver<AudioFrame?>
    ) {

        if (!canAllocateStream()) {
            responseObserver.onError(Throwable("Maximum stream limit reached"))
            return
        }

        val streamId = request.streamId
        val service = streamManagerService.getRadioService(streamId)

        if (service == null) {
            responseObserver.onError(Throwable("Stream not found"))
            return
        }

        val player = service.getAudioPlayer()
        val executor = Executors.newSingleThreadScheduledExecutor()
        val serverCallObserver = responseObserver as ServerCallStreamObserver<AudioFrame?>

        /* Store executor to map */
        activeStreams[streamId] = executor

        val pushNextAudioBuffer = Runnable {
            try {
                if (serverCallObserver.isCancelled) {
                    stopStreamInternal(streamId)
                    return@Runnable
                }

                val frame = player.provide()
                val response = AudioFrame.newBuilder().apply {
                    if (frame == null) {
                        setIsSilence(true)
                        setOpusData(ByteString.EMPTY)
                    } else {
                        setOpusData(ByteString.copyFrom(frame.data))
                        setIsSilence(false)
                    }
                }.build()

                /* Push frame to gRPC*/
                responseObserver.onNext(response)

            } catch (e: Exception) {
                responseObserver.onError(e)
                stopStreamInternal(streamId)
            }
        }

        executor.scheduleAtFixedRate(
            pushNextAudioBuffer, 0, FRAME_INTERVAL_MS, TimeUnit.MILLISECONDS
        )

        serverCallObserver.setOnCancelHandler { stopStreamInternal(streamId) }
    }

    private fun canAllocateStream(): Boolean {
        /* TODO: concurrency (right now exceeding 1 stream is fine) */
        return activeStreams.size < MAX_CONCURRENT_STREAMS
    }

    private fun stopStreamInternal(streamId: String) {
        activeStreams.remove(streamId)?.let { executor ->
            executor.shutdownNow()
            println("$streamId stream stopped and executor shutdown")
        }
    }
}
