package dev.bachtran.lavaradio.controller

import dev.bachtran.lavaradio.lavaplayer.service.LavaplayerService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class MoveRequest(val uri: String, val from: Int, val to: Int)

@RestController
@RequestMapping("/api/queue")
class QueueController(
    private val lavaplayerService: LavaplayerService
) {
    @GetMapping
    fun getQueue() = lavaplayerService.getQueue()

    @DeleteMapping("/{index}")
    fun removeQueuedTrack(@PathVariable index: Int) = lavaplayerService.removeQueuedTrack(index)

    @PostMapping("/move")
    fun moveQueuedTrack(@RequestBody request: MoveRequest) {
        lavaplayerService.moveQueuedTrack(request.uri, request.from, request.to)
    }
}