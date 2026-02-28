package dev.bachtran.lavaradio.controller

import dev.bachtran.lavaradio.dto.graphql.PlaybackUpdateEvent
import dev.bachtran.lavaradio.service.StreamManagerService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class StateController(
    private val streamManagerService: StreamManagerService
) {

    @QueryMapping
    fun getInitialState(@Argument("stream_id") streamId: String): PlaybackUpdateEvent {
        return streamManagerService.withRadio(streamId) {
            it.getPlaybackUpdateEvent("INITIAL_LOAD")
        }
    }

    @SubscriptionMapping
    fun playerUpdates(@Argument("stream_id") streamId: String): Flux<PlaybackUpdateEvent> {
        return streamManagerService.withRadio(streamId) {
            it.getPlaybackUpdateStream()
        }
    }
}