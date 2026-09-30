package com.nextbrowser

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nextbrowser.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BrowserTab(
    val id: Int,
    var url: String = "https://www.google.com",
    var title: String = "New tab",
    var incognito: Boolean = false
)

class BrowserViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = BrowserDatabase.get(app).dao()

    val history = dao.history().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val bookmarks = dao.bookmarks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun recordVisit(title: String, url: String, incognito: Boolean) {
        if (incognito || url.isBlank()) return
        viewModelScope.launch { dao.addHistory(HistoryEntity(title = title, url = url)) }
    }

    fun toggleBookmark(title: String, url: String) {
        viewModelScope.launch {
            if (dao.isBookmarked(url)) dao.removeBookmark(url)
            else dao.addBookmark(BookmarkEntity(title = title, url = url))
        }
    }

    fun clearHistory() {
        viewModelScope.launch { dao.clearHistory() }
    }

    fun clearBookmarks() {
        viewModelScope.launch { dao.clearBookmarks() }
    }

    suspend fun isBookmarked(url: String) = dao.isBookmarked(url)
}
