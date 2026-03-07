package dev.bachtran.lavaradio.controller

import dev.bachtran.lavaradio.dto.rest.PlaybackState
import dev.bachtran.lavaradio.service.StreamManagerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AddRequest(
    val url: String,
    val next: Boolean? = false,
    val shuffle: Boolean? = false
)

@RestController
@RequestMapping("/api/player/{streamId}")
class PlayerController(
    private val streamManagerService: StreamManagerService
) {
    @PostMapping("/add")
    fun add(@PathVariable streamId : String, @RequestBody request: AddRequest) {
        streamManagerService.withRadio(streamId) {
            it.addTrack(request.url,
                request.next ?: false,
                request.shuffle ?: false
            )
        }
    }

    @PostMapping("/play")
    fun play(@PathVariable streamId : String, @RequestBody url: String) {
        streamManagerService.withRadio(streamId) { it.playTrack(url) }
    }

    @PostMapping("/skip")
    fun skip(@PathVariable streamId : String) {
        streamManagerService.withRadio(streamId) { it.skip() }
    }

    @PostMapping("/pause")
    fun pause(@PathVariable streamId: String) {
        streamManagerService.withRadio(streamId) { it.pause() }
    }

    @PostMapping("/resume")
    fun resume(@PathVariable streamId: String) {
        streamManagerService.withRadio(streamId) { it.resume() }
    }

    @PostMapping("/seek")
    fun seek(@PathVariable streamId: String, @RequestBody position: Long) {
        streamManagerService.withRadio(streamId) { it.seek(position) }
    }

    @PostMapping("/stop")
    fun stop(@PathVariable streamId: String) {
        streamManagerService.withRadio(streamId) { it.stop() }
    }

    @PostMapping("/loop/{mode}")
    fun setLoop(@PathVariable streamId: String, @PathVariable mode: String) {
        streamManagerService.withRadio(streamId) { it.setLoop(mode) }
    }

    @PostMapping("/shuffle")
    fun shuffle(@PathVariable streamId: String) {
        streamManagerService.withRadio(streamId) { it.shuffleQueue() }
    }

    @GetMapping("/state")
    fun getPlaybackState(@PathVariable streamId: String): PlaybackState {
        return streamManagerService.withRadio(streamId) { it.getPlaybackState() }
    }
}