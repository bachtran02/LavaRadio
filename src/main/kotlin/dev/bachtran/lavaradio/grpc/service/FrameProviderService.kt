package dev.bachtran.lavaradio.grpc.service

import com.google.protobuf.ByteString
import dev.bachtran.lavaradio.service.StreamManagerService
import io.grpc.stub.ServerCallStreamObserver
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
        val streamScope = CoroutineScope(Dispatchers.Default + Job())
        val audioBuffer = Channel<AudioFrame>(capacity = 5)
        val service = streamManagerService.getRadioService(request.streamId) ?: return

        streamScope.launch(Dispatchers.IO) {
            try {
                while (true) {
                    val frame = service.provideFrame()
                    val response = AudioFrame.newBuilder().apply {
                        if (frame == null) {
                            isSilence = true
                        } else {
                            opusData = ByteString.copyFrom(frame.data)
                            isSilence = false
                        }
                    }.build()

                    audioBuffer.send(response)
                }
            } catch (e: Exception) {
                println("error: ${e.message}")
                if (e !is CancellationException) {
                    streamScope.cancel()
                }
            }
        }

        streamScope.launch {
            try {
                var nextTickTime = System.currentTimeMillis()

                while (true) {
                    // Pull the next ready frame from the buffer
                    val response = audioBuffer.receive()

                    // gRPC observers are NOT thread-safe; sync to be safe
                    responseObserver.onNext(response)

                    // Strict Timing Logic:
                    // Instead of delay(20), we calculate the next point in time
                    // to prevent "drift" where execution time adds up.
                    nextTickTime += 20
                    val delayTime = nextTickTime - System.currentTimeMillis()

                    if (delayTime > 0) {
                        delay(delayTime)
                    }
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    responseObserver.onError(e)
                }
            }
        }

        // Handle client disconnection
        (responseObserver as ServerCallStreamObserver).setOnCancelHandler {
            streamScope.cancel()
        }
    }
}
