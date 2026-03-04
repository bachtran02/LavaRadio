package dev.bachtran.lavaradio.controller

import dev.bachtran.lavaradio.service.StreamManagerService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/mtx")
class MediaMtxController(
    private val streamManagerService: StreamManagerService
) {
    /*
        Endpoint for MediaMtx to call when there is no listener to stop stream.
        This is not exposed to the public.
     */
//    @PostMapping("/start")
//    fun userStreamDemanded(@RequestParam id: String) {
//        println("stream started: streamId=$id")
//    }

    @DeleteMapping("/stop")
    fun userStreamUndemanded(@RequestParam id: String) {
        streamManagerService.removeStream(id)
    }
}