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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.release.startcommunity.api.CreateCommentRequest
import com.release.startcommunity.api.LikeRequest
import com.release.startcommunity.model.Comment
import com.release.startcommunity.model.User
import com.release.startcommunity.model.Event

class PostViewModel : ViewModel() {

    /* ---------------- 数据流 ---------------- */
    private val _toastMessage = MutableLiveData<Event<String>>()        //Toast动态数据
    val toastMessage: LiveData<Event<String>> = _toastMessage

    private val _posts = mutableStateListOf<Post>()        // 用 StateList 追加更省事
    val posts: SnapshotStateList<Post> = _posts

    private val _comments = mutableStateListOf<Comment>()
    val comments: SnapshotStateList<Comment> = _comments

    private val postsFlow = snapshotFlow { posts.toList() }

    private val _query  = MutableStateFlow("")
    val query:  StateFlow<String> = _query

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
        if (_loading.value) {        //正在加载中
            _toastMessage.postValue(Event("加载中......"))
            return
        }
        if (_reachEnd.value) {        //加载已到底
            _toastMessage.postValue(Event("已经到底了"))
            return
        }
        viewModelScope.launch {
            _loading.value = true
            try {
                val resp = ApiClient.api.getPostsPaged(page, pageSize)
                _posts.addAll(resp.content)
                _reachEnd.value = resp.number + 1 >= resp.totalPages
                if (!_reachEnd.value) page++
                _toastMessage.postValue(Event("加载成功！当前页${page + 1}"))
            } catch (e: Exception) {
                Log.e("PostVM", "加载失败", e)
                _toastMessage.postValue(Event("加载失败！${e.message}"))
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
                _toastMessage.postValue(Event("用户ID错误！"))
            }
            try {
                val created = ApiClient.api.createPost(
                    CreatePostRequest(title, content, userId)
                )
                _posts.add(0, created) // 直接插到列表顶部
                _toastMessage.postValue(Event("发帖成功！"))
            } catch (e: Exception) {
                Log.e("PostVM", "发帖失败", e)
                _toastMessage.postValue(Event("发帖失败！${e.message}"))
            }
        }
    }

    fun createComment(
        postId: Long,
        content: String,
        user: User?,
        onCommentCreated: (comment: Comment) -> Unit
    ) {
        if (user == null) {
            Log.w("PostVM", "用户未登录，无法发表评论")
            _toastMessage.postValue(Event("用户未登录，无法发表评论！"))
            return
        }

        viewModelScope.launch {
            try {
                val response = ApiClient.api.postComment(
                    CreateCommentRequest(postId, user, content)
                )

                val comment = response.body()
                if (comment != null) {
                    onCommentCreated(comment)
                    Log.d("PostVM", "创建评论成功：$comment")
                    _toastMessage.postValue(Event("发表评论成功！"))
                } else {
                    Log.e("PostVM", "评论创建失败，响应为空")
                    _toastMessage.postValue(Event("不能发表空评论"))
                }
            } catch (e: Exception) {
                Log.e("PostVM", "创建评论异常", e)
                _toastMessage.postValue(Event("发表评论失败！${e.message}"))
            }
        }
    }

    fun loadComments(postId: Long, onPostLoaded: (Post) -> Unit) {
        viewModelScope.launch {
            try {
                val index = _posts.indexOfFirst { it.id == postId }
                if (index != -1) {
                    val comments = ApiClient.api.getComments(postId)
                    Log.d("PostVM", "${index}id:${_posts[index].id}加载评论成功$comments")
                    _toastMessage.postValue(Event("加载评论成功！"))
                    val updatePost = _posts[index].copy(comments = comments)
                    onPostLoaded(updatePost)
                }
            } catch (e: Exception) {
                Log.e("PostVM", "获取评论失败", e)
                _toastMessage.postValue(Event("获取评论失败${e.message}"))
            }
        }
    }

    fun likePost(postId: Long,userId: Long) {
        viewModelScope.launch {
            try {
                Log.d("PostVM", "开始点赞")
                val response = ApiClient.api.likePost(postId, LikeRequest(userId))
                if (response.isSuccessful) {
                    val updatedPost = response.body()
                    Log.d("PostVM", "点赞成功：$updatedPost")
                }
            }catch (e: Exception) {
                Log.e("PostVM", "点赞失败", e)
                _toastMessage.postValue(Event("点赞失败${e.message}"))
            }
        }
    }
}