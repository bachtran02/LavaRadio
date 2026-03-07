package dev.bachtran.lavaradio.lavaplayer.manager

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo
import dev.bachtran.lavaradio.exception.InvalidQueueIndexException
import dev.bachtran.lavaradio.exception.MoveItemUnmatchedException
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingDeque

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class QueueManager {
    companion object {
        private const val HISTORY_QUEUE_SIZE = 20
    }

    private val queueLock = Any()

    private val historyLock = Any()

    private val queue = CopyOnWriteArrayList<AudioTrack>();

    private val historyQueue = LinkedBlockingDeque<AudioTrack>(HISTORY_QUEUE_SIZE)

    fun addTrack(track: AudioTrack, index: Int = -1) {
        if (index == -1 || index >= queue.size || index < 0) {
            queue.add(track)
        } else {
            queue.add(index, track)
        }
    }

    fun addTracks(tracks: List<AudioTrack>, index: Int = -1) {
        if (tracks.isEmpty()) {
            return
        }

        if (index == -1 || index >= queue.size || index < 0) {
            queue.addAll(tracks)
        } else {
            queue.addAll(index, tracks)
        }
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
                throw InvalidQueueIndexException()
            }
            val trackToMove = queue[oldIndex]
            if (trackToMove.info.uri != trackUri) {
                /* Sanity check (non-exhaustive) that we are moving the right track */
                throw MoveItemUnmatchedException()
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
            } else {
                throw InvalidQueueIndexException()
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