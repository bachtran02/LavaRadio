package dev.bachtran.lavaradio.controller

import dev.bachtran.lavaradio.service.lavaplayer.LavaPlayerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/player")
class PlayerController(
    private val lavaPlayerService: LavaPlayerService
) {
    @PostMapping("/add")
    fun add(@RequestBody url: String) = lavaPlayerService.play(url)

    @PostMapping("/skip")
    fun skip() = lavaPlayerService.skip()

    @PostMapping("/pause")
    fun pause() = lavaPlayerService.pause()

    @PostMapping("/resume")
    fun resume() = lavaPlayerService.resume()

    @PostMapping("/stop")
    fun stop() = lavaPlayerService.stop()

    @GetMapping("/playback")
    fun getPlaybackState() = lavaPlayerService.getPlaybackState()

    @GetMapping("/queue")
    fun getQueue() = lavaPlayerService.getQueue()
}