package dev.bachtran.lavaradio.lavaplayer.broadcaster

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import dev.bachtran.lavaradio.dto.graphql.PlaybackState
import dev.bachtran.lavaradio.dto.graphql.PlaybackUpdateEvent
import dev.bachtran.lavaradio.dto.graphql.TrackInfo
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

class PlaybackBroadcaster() : AudioEventAdapter() {

    private val sink = Sinks.many().multicast().directBestEffort<PlaybackUpdateEvent>()

    fun stream(): Flux<PlaybackUpdateEvent> = sink.asFlux()

    fun emit(event: PlaybackUpdateEvent) { sink.tryEmitNext(event) }

    fun broadcastPlaybackStateChange(reason: String, state: PlaybackState) {
        val event = PlaybackUpdateEvent(
            eventType = reason,
            state = state
        )
        emit(event)
    }

    fun broadcastQueueChange(reason: String, queue: List<TrackInfo>) {
        val event = PlaybackUpdateEvent(
            eventType = reason,
            queue = queue
        )
        emit(event)
    }

    fun broadcastTrackChange(reason: String, state: PlaybackState, queue: List<TrackInfo>, history: List<TrackInfo>) {
        val event = PlaybackUpdateEvent(
            eventType = reason,
            state = state,
            queue = queue,
            history = history
        )
        emit(event)
    }

    fun cleanup() {
        sink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST)
    }
}