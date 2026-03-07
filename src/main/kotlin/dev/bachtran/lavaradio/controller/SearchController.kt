package dev.bachtran.lavaradio.controller

import dev.bachtran.lavaradio.dto.rest.SearchResultItem
import dev.bachtran.lavaradio.service.StreamManagerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/search/{streamId}")
class SearchController(
    private val streamManagerService: StreamManagerService
) {
    @GetMapping
    fun search(
        @PathVariable streamId: String,
        @RequestParam query: String,
        @RequestParam source: String,
        @RequestParam types: String
    ): List<SearchResultItem> {
        return streamManagerService.withRadio(streamId) {
            it.searchQuery(query, source, types) ?: emptyList()
        }
    }
}