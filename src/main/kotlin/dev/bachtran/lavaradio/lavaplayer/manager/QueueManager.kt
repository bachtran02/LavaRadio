package dev.bachtran.lavaradio.lavaplayer.manager

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import org.springframework.stereotype.Component
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingDeque

@Component
class QueueManager {
    companion object {
        private const val HISTORY_QUEUE_SIZE = 20
    }

    private val queueLock = Any()

    private val historyLock = Any()

    private val queue = CopyOnWriteArrayList<AudioTrack>();

    private val historyQueue = LinkedBlockingDeque<AudioTrack>(HISTORY_QUEUE_SIZE)

    fun addTrack(track: AudioTrack) {
        queue.add(track)
    }

    fun addTracks(tracks: List<AudioTrack>) {
        if (tracks.isEmpty()) {
            return
        }
        queue.addAll(tracks)
    }

    fun addTrackHistory(track: AudioTrack) {

        synchronized(historyLock) {
            if (historyQueue.size >= HISTORY_QUEUE_SIZE) {
                historyQueue.removeLast()
            }
            historyQueue.addFirst(track)
        }
    }

    fun moveQueuedTrack(trackUri: String, oldIndex: Int, newIndex: Int) : Boolean {

        synchronized(queueLock) {
            if (oldIndex < 0 || newIndex < 0 || oldIndex >= queue.size || newIndex >= queue.size) {
                return false
            }
            val trackToMove = queue[oldIndex]
            if (trackToMove.info.uri != trackUri) {
                /* Sanity check (non-exhaustive) that we are moving the right track */
                return false
            }
            queue.removeAt(oldIndex)
            queue.add(newIndex, trackToMove)
        }
        return true
    }

    fun removeQueuedTrack(index: Int) {

        synchronized(queueLock) {
            if (index >= 0 && index < queue.size) {
                queue.removeAt(index)
            }
        }
    }

    fun popNextTrack() : AudioTrack? {

        synchronized(queueLock) {
            if (queue.isEmpty()) {
                return null
            }
            return queue.removeAt(0)
        }
    }

    fun shuffleQueue() {
        synchronized(queueLock) {
            queue.shuffle()
        }
    }

    fun getQueue() : List<AudioTrackInfo> = queue.map { it.info }

    fun clearQueue() { queue.clear() }

    fun getHistory() : List<AudioTrackInfo> = historyQueue.map { it.info }

    fun clearHistory() { historyQueue.clear() }

}