package com.release.startcommunity.viewmodel


import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.release.startcommunity.api.ApiClient
import com.release.startcommunity.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// --- ViewModel ---
class PostViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredPosts: StateFlow<List<Post>> = combine(_posts, _searchQuery) { posts, query ->
        if (query.isBlank()) posts
        else posts.filter { it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }


    init {
        fetchPosts()
    }


    fun fetchPosts() {
        viewModelScope.launch {
            try {
                _posts.value = ApiClient.postService.getPosts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun addPost(post: Post) {
        viewModelScope.launch {
            try {
                val response = ApiClient.postService.createPost(post)
                if (response.isSuccessful) {
                    fetchPosts()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}