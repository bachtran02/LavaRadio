package dev.bachtran.lavaradio.controller

import dev.bachtran.lavaradio.service.StreamManagerService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/*
    This controller handles starting and stopping streaming sessions.
    They are reserved for hooks from MediaMtx.
*/

@RestController
@RequestMapping("/api/radio")
class StreamController(
    private val streamManagerService: StreamManagerService
) {
    @PostMapping("/start")
    fun startStream() {
        streamManagerService.startStream()
    }

    @PostMapping("/stop")
    fun stopStream() {
        streamManagerService.stopStream()
    }
}