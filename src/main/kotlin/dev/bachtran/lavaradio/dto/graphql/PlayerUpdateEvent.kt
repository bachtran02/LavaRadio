package dev.bachtran.lavaradio.dto.graphql

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PlaybackUpdateEvent(
    val eventType: String,
    val state: PlaybackState? = null,
    val queue: List<TrackInfo>? = null,
    val history: List<TrackInfo>? = null
) {
    companion object {
        // Playback Events
        const val TRACK_STARTED         = "TRACK_STARTED"
        const val TRACK_ENDED           = "TRACK_ENDED"
        const val TRACK_STUCK           = "TRACK_STUCK"
        const val TRACK_EXCEPTION       = "TRACK_EXCEPTION"
        const val TRACK_SKIPPED         = "TRACK_SKIPPED"
        const val PLAYER_STOPPED        = "PLAYER_STOPPED"

        // State Events
        const val PAUSE_TOGGLED         = "PAUSE_TOGGLED"
        const val POSITION_SEEKED       = "POSITION_SEEKED"
        const val LOOP_MODE_CHANGED     = "LOOP_MODE_CHANGED"

        // Queue Events
        const val QUEUE_UPDATED         = "QUEUE_UPDATED"
        const val QUEUE_SHUFFLED        = "QUEUE_SHUFFLED"
    }
}