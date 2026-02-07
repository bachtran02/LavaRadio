package dev.bachtran.lavaradio.controller

import dev.bachtran.lavaradio.dto.SearchResultItem
import dev.bachtran.lavaradio.lavaplayer.service.LavaplayerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/search")
class SearchController(
    private val lavaplayerService: LavaplayerService
) {
    @GetMapping
    fun search(
        @RequestParam query: String, @RequestParam source: String, @RequestParam types: String
    ): List<SearchResultItem> = lavaplayerService.searchQuery(query, source, types) ?: emptyList()
}