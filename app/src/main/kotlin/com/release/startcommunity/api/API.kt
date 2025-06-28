package com.release.startcommunity.api

import com.release.startcommunity.model.Comment
import com.release.startcommunity.model.Post
import com.release.startcommunity.model.User
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import kotlin.getValue


interface PostApiService {
    @GET("posts")
    suspend fun getPosts(): List<Post>

    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequest): Post

    @POST("posts/{id}/like")
    suspend fun likePost(@Path("id") postId: Long): Int

    @POST("comments")
    suspend fun createComment(@Body request: CreateCommentRequest): Comment

    @POST("comments/{id}/like")
    suspend fun likeComment(@Path("id") commentId: Long): Int

    @GET("users")
    suspend fun getUsers(): List<User>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): User

    @POST("users")
    suspend fun registerUser(@Body user: User): User

    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @GET("posts/paged")
    suspend fun getPostsPaged(
        @Query("page") page: Int,
        @Query("size") size: Int = 10
    ): PageResponse<Post>   // 用后端 Page<PostDTO>
}
object ApiClient {
    private const val BASE_URL = "http://47.121.204.76:8080/" // 本地测试用，部署用 IP 替换

    val basicAuthInterceptor = Interceptor { chain ->
        val credentials = Credentials.basic("admin", "123456") // 🔐 你的用户名密码
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", credentials)
            .build()
        chain.proceed(newRequest)
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(basicAuthInterceptor)
        .build()

    val api: PostApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PostApiService::class.java)
}

data class CreatePostRequest(val title: String, val content: String, val userId: Long)
data class CreateCommentRequest(val postId: Long, val userId: Long, val content: String)
data class LoginRequest(val username: String, val password: String)
data class TokenResponse(val token: String, val uid: Long)
data class PageResponse<T>(
    val content: List<T>,
    val number: Int,
    val totalPages: Int
)