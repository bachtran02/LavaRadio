package dev.bachtran.lavaradio.controller

import dev.bachtran.lavaradio.dto.graphql.PlaybackUpdateEvent
import dev.bachtran.lavaradio.lavaplayer.service.LavaplayerService
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class StateController(private val lavaplayerService: LavaplayerService) {

    @QueryMapping
    fun getInitialState(): PlaybackUpdateEvent {
        return lavaplayerService.getPlaybackUpdateEvent("INITIAL_LOAD")
    }

    @SubscriptionMapping
    fun playerUpdates(): Flux<PlaybackUpdateEvent> {
        return lavaplayerService.getPlaybackUpdateStream()
    }
}