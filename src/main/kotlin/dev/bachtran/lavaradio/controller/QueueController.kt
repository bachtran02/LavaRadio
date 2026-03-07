package dev.bachtran.lavaradio.controller

import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import dev.bachtran.lavaradio.service.StreamManagerService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class MoveRequest(val uri: String, val from: Int, val to: Int)

@RestController
@RequestMapping("/api/queue/{streamId}")
class QueueController(
    private val streamManagerService: StreamManagerService
) {
    @GetMapping
    fun getQueue(@PathVariable streamId: String): List<AudioTrackInfo> {
        return streamManagerService.withRadio(streamId) { it.getQueue() }
    }

    @GetMapping("/history")
    fun getHistory(@PathVariable streamId: String): List<AudioTrackInfo> {
        return streamManagerService.withRadio(streamId) { it.getHistory() }
    }

    @DeleteMapping("/{index}")
    fun removeQueuedTrack(@PathVariable streamId : String, @PathVariable index: Int) {
        return streamManagerService.withRadio(streamId) { it.removeQueuedTrack(index) }
    }

    @PostMapping("/move")
    fun moveQueuedTrack(@PathVariable streamId : String, @RequestBody request: MoveRequest) {
        return streamManagerService.withRadio(streamId) {
            it.moveQueuedTrack(request.uri, request.from, request.to)
        }
    }
}