package com.release.startcommunity.viewmodel

import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.release.startcommunity.api.ApiClient
import com.release.startcommunity.api.CreatePostRequest
import com.release.startcommunity.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class PostViewModel : ViewModel() {

    /* ---------------- 数据流 ---------------- */

    private val _posts = mutableStateListOf<Post>()        // 用 StateList 追加更省事
    val posts: SnapshotStateList<Post> = _posts

    private val postsFlow = snapshotFlow { posts.toList() }

    private val _query  = MutableStateFlow("")
    val   query:  StateFlow<String> = _query

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _reachEnd = MutableStateFlow(false)
    val reachEnd: StateFlow<Boolean> = _reachEnd

    /* ---------------- 分页参数 ---------------- */

    private var page = 0
    private val pageSize = 20

    /* ---------------- 过滤后的列表 ---------------- */

    val filteredPosts: StateFlow<List<Post>> =
        combine(postsFlow, _query) { list, q ->
            if (q.isBlank()) list
            else list.filter {
                it.title.contains(q, true) || it.content.contains(q, true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /* ---------------- 首屏加载 ---------------- */

    init { loadMore() }

    /* ---------------- 搜索 ---------------- */

    fun updateSearchQuery(q: String) { _query.value = q }

    /* ---------------- 加载下一页 ---------------- */

    fun loadMore() {
        if (_loading.value || _reachEnd.value) return      // 已在加载或到底
        viewModelScope.launch {
            _loading.value = true
            try {
                val resp = ApiClient.api.getPostsPaged(page, pageSize)
                _posts.addAll(resp.content)
                _reachEnd.value = resp.number + 1 >= resp.totalPages
                if (!_reachEnd.value) page++
            } catch (e: Exception) {
                Log.e("PostVM", "加载失败", e)
            } finally {
                _loading.value = false
            }
        }
    }

    /* ---------------- 发帖 ---------------- */

    fun addPost(title: String, content: String, userId: Long) {
        viewModelScope.launch {
            if (userId < 0L) {
                Log.e("PostVM", "用户ID错误")
            }
            try {
                val created = ApiClient.api.createPost(
                    CreatePostRequest(title, content, userId)
                )
                _posts.add(0, created)          // 直接插到列表顶部
            } catch (e: Exception) {
                Log.e("PostVM", "发帖失败", e)
            }
        }
    }
}