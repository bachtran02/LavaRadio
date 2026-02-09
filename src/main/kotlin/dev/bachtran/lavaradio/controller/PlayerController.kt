package dev.bachtran.lavaradio.controller

import dev.bachtran.lavaradio.lavaplayer.service.LavaplayerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/player")
class PlayerController(
    private val lavaplayerService: LavaplayerService
) {
    @PostMapping("/add")
    fun add(@RequestBody url: String) = lavaplayerService.addTrack(url)

    @PostMapping("/play")
    fun play(@RequestBody url: String) = lavaplayerService.playTrack(url)

    @PostMapping("/skip")
    fun skip() = lavaplayerService.skip()

    @PostMapping("/pause")
    fun pause() = lavaplayerService.pause()

    @PostMapping("/resume")
    fun resume() = lavaplayerService.resume()

    @PostMapping("/seek")
    fun seek(@RequestBody position: Long) = lavaplayerService.seek(position)

    @PostMapping("/stop")
    fun stop() = lavaplayerService.stop()

    @PostMapping("/loop/{mode}")
    fun setLoop(@PathVariable mode: String) = lavaplayerService.setLoop(mode)

    @PostMapping("/shuffle")
    fun shuffle() = lavaplayerService.shuffleQueue()

    @GetMapping("/playback")
    fun getPlaybackState() = lavaplayerService.getPlaybackState()
}