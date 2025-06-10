package com.release.startcommunity.viewmodel


import android.util.Log
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


class PostViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(true)

    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    val filteredPosts: StateFlow<List<Post>> = combine(_posts, _searchQuery) { posts, query ->
        if (query.isBlank()) posts
        else posts.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _posts.value = ApiClient.api.getPosts()
            } catch (e: Exception) {
                Log.e("PostViewModel", "加载失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPost(post: Post) {
        viewModelScope.launch {
            try {
                val newPost = ApiClient.api.createPost(CreatePostRequest(title = post.title, content = post.content, userId = UserViewModel().currentUser.value?.id ?: 0))
                _posts.value = _posts.value + newPost
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error adding post: ${e.localizedMessage}")
                _errorMessage.value = "发帖失败: ${e.localizedMessage}"
            }
        }
    }

    fun likePost(postId: Long) {
        viewModelScope.launch {
            try {
                ApiClient.api.likePost(postId)
                fetchPosts()
            } catch (e: Exception) {
                _errorMessage.value = "点赞失败: ${e.localizedMessage}"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun removePost(postId: Long) {
        _posts.value = _posts.value.filterNot {
            it.id == postId.toString()
        }
    }

}