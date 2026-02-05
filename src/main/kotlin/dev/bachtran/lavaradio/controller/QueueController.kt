package dev.bachtran.lavaradio.controller

import dev.bachtran.lavaradio.lavaplayer.service.LavaplayerService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/queue")
class QueueController(
    private val lavaplayerService: LavaplayerService
) {
    @GetMapping
    fun getQueue() = lavaplayerService.getQueue()

    @DeleteMapping("/{index}")
    fun removeQueuedTrack(@PathVariable index: Int) = lavaplayerService.removeQueuedTrack(index)
}