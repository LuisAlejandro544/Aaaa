package com.example.player.controllers

import com.example.data.db.TrackEntity
import com.example.player.RepeatMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QueueController {

    private val _queue = MutableStateFlow<List<TrackEntity>>(emptyList())
    val queue: StateFlow<List<TrackEntity>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.NONE)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    /**
     * Devuelve la canción que se reproducirá A CONTINUACIÓN sin alterar el índice de la cola.
     */
    fun peekNextTrack(): TrackEntity? {
        val currentQ = _queue.value
        if (currentQ.isEmpty()) return null

        if (_repeatMode.value == RepeatMode.ONE) {
            return currentQ.getOrNull(_currentIndex.value)
        }

        if (_isShuffle.value) {
            val validIndices = currentQ.indices.filter { it != _currentIndex.value }
            if (validIndices.isNotEmpty()) {
                val nextRandomIdx = validIndices.first()
                return currentQ.getOrNull(nextRandomIdx)
            }
        }

        val nextIdx = _currentIndex.value + 1
        return if (nextIdx < currentQ.size) {
            currentQ[nextIdx]
        } else if (_repeatMode.value == RepeatMode.ALL) {
            currentQ.firstOrNull()
        } else {
            null
        }
    }

    fun setQueue(tracks: List<TrackEntity>, startIndex: Int = 0): TrackEntity? {
        if (tracks.isEmpty()) return null
        _queue.value = tracks
        val safeIndex = startIndex.coerceIn(0, tracks.lastIndex)
        _currentIndex.value = safeIndex
        return tracks[safeIndex]
    }

    fun playQueueIndex(index: Int): TrackEntity? {
        val currentQ = _queue.value
        if (index !in currentQ.indices) return null
        _currentIndex.value = index
        return currentQ[index]
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val list = _queue.value.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            _queue.value = list

            if (_currentIndex.value == fromIndex) {
                _currentIndex.value = toIndex
            } else if (fromIndex < _currentIndex.value && toIndex >= _currentIndex.value) {
                _currentIndex.value -= 1
            } else if (fromIndex > _currentIndex.value && toIndex <= _currentIndex.value) {
                _currentIndex.value += 1
            }
        }
    }

    fun removeQueueItem(index: Int) {
        val list = _queue.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _queue.value = list
            if (_currentIndex.value >= list.size) {
                _currentIndex.value = (list.size - 1).coerceAtLeast(0)
            }
        }
    }

    fun addTrackToNext(track: TrackEntity) {
        val list = _queue.value.toMutableList()
        val insertPos = (_currentIndex.value + 1).coerceAtMost(list.size)
        list.add(insertPos, track)
        _queue.value = list
    }

    fun addTrackToQueue(track: TrackEntity) {
        val list = _queue.value.toMutableList()
        list.add(track)
        _queue.value = list
    }

    fun getNextTrack(currentPosMs: Long): TrackEntity? {
        val currentQ = _queue.value
        if (currentQ.isEmpty()) return null

        if (_repeatMode.value == RepeatMode.ONE) {
            return currentQ.getOrNull(_currentIndex.value)
        }

        var nextIdx = _currentIndex.value + 1
        if (_isShuffle.value) {
            nextIdx = (0 until currentQ.size).random()
        }

        return if (nextIdx < currentQ.size) {
            _currentIndex.value = nextIdx
            currentQ[nextIdx]
        } else if (_repeatMode.value == RepeatMode.ALL) {
            _currentIndex.value = 0
            currentQ[0]
        } else {
            null
        }
    }

    fun getPreviousTrack(currentPosMs: Long): Pair<Boolean, TrackEntity?> {
        val currentQ = _queue.value
        if (currentQ.isEmpty()) return Pair(false, null)

        if (currentPosMs > 3000) {
            return Pair(true, currentQ.getOrNull(_currentIndex.value))
        }

        var prevIdx = _currentIndex.value - 1
        if (prevIdx < 0) {
            prevIdx = if (_repeatMode.value == RepeatMode.ALL) currentQ.lastIndex else 0
        }

        _currentIndex.value = prevIdx
        return Pair(false, currentQ.getOrNull(prevIdx))
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.NONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.NONE
        }
    }
}
