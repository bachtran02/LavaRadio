package dev.bachtran.lavaradio.grpc.service

import com.google.protobuf.ByteString
import dev.bachtran.lavaradio.service.StreamManagerService
import io.grpc.stub.ServerCallStreamObserver
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import lavaradio.proto.AudioFrame
import lavaradio.proto.AudioProviderGrpc
import lavaradio.proto.StreamRequest
import org.springframework.stereotype.Service
import kotlin.coroutines.cancellation.CancellationException

@Service
class FrameProviderService(
    private val streamManagerService: StreamManagerService,
) : AudioProviderGrpc.AudioProviderImplBase() {

    override fun pullAudioStream(
        request: StreamRequest,
        responseObserver: StreamObserver<AudioFrame?>
    ) {
        val service = streamManagerService.getRadioService(request.streamId)
        if (service == null) {
            responseObserver.onError(Throwable("Stream not found"))
            return
        }
        val serverObserver = responseObserver as ServerCallStreamObserver<*>
        val streamJob = Job()
        val serviceScope = CoroutineScope(Dispatchers.Default + streamJob)

        /* 5-frame (200ms) buffer */
        val audioChannel = Channel<AudioFrame>(capacity = 10, onBufferOverflow = BufferOverflow.SUSPEND)

        serverObserver.setOnCancelHandler { streamJob.cancel() }

        // --- PRODUCER: Fetches audio frames from Lavaplayer ---
        serviceScope.launch(Dispatchers.Default) {
            try {
                while (isActive) {
                    val frame = service.provideFrame()
                    val response = AudioFrame.newBuilder().apply {
                        if (frame == null) isSilence = true
                        else opusData = ByteString.copyFrom(frame.data)
                    }.build()
                    /* Blocks if buffer is full */
                    audioChannel.send(response)
                    delay(20)
                }
            } catch (e: Exception) {
                audioChannel.close(e)
            }
        }

        // --- CONSUMER: Sends frames to the Client ---
        serviceScope.launch(Dispatchers.IO) {
            try {
                for (frame in audioChannel) {
                    while (!serverObserver.isReady && isActive) {
                        delay(5)
                    }
                    if (!isActive) {
                        break
                    }
                    responseObserver.onNext(frame)
                }
                responseObserver.onCompleted()
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    responseObserver.onError(Throwable(e))
                }
            } finally {
                streamJob.cancel()
            }
        }
    }
}
