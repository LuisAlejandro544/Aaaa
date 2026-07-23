package com.example.ui.viewmodel.delegates

import com.example.ui.viewmodel.PlaylistDetailTarget
import com.example.ui.viewmodel.SpotifyTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavigationDelegate {

    private val _currentTab = MutableStateFlow(SpotifyTab.HOME)
    val currentTab: StateFlow<SpotifyTab> = _currentTab.asStateFlow()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    private val _openedPlaylistDetail = MutableStateFlow<PlaylistDetailTarget?>(null)
    val openedPlaylistDetail: StateFlow<PlaylistDetailTarget?> = _openedPlaylistDetail.asStateFlow()

    fun selectTab(tab: SpotifyTab) {
        _currentTab.value = tab
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    fun openPlaylistDetail(target: PlaylistDetailTarget?) {
        _openedPlaylistDetail.value = target
    }
}
