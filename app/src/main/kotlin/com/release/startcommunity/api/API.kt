package com.release.startcommunity.api

import com.release.startcommunity.model.Comment
import com.release.startcommunity.model.Message
import com.release.startcommunity.model.Post
import com.release.startcommunity.model.User
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface PostApiService {


    //帖子区接口
    @GET("posts/paged")
    suspend fun getPostsPaged(
        @Query("page") page: Int,
        @Query("size") size: Int = 10
    ): PageResponse<Post>   // 用后端 Page<PostDTO>

    @POST("posts")
    suspend fun createPost(@Body request: CreatePostRequest): Post


    // 点赞接口
    @POST("likes/{targetId}")
    suspend fun likePost(@Path("targetId") targetId: Long, @Body request: LikeRequest): Response<Boolean>

    // 取消点赞接口
    @DELETE("likes/{targetId}")
    suspend fun unlikePost(@Path("targetId") targetId: Long, @Body request: LikeRequest): Response<Boolean>

    // 获取点赞数接口
    @GET("likes/{targetId}/count")
    suspend fun getLikeCount(@Path("targetId") targetId: Long): Response<Long>

    // 检查是否已点赞接口
    @GET("likes/{targetId}/status")
    suspend fun hasLiked(@Path("targetId") targetId: Long, @Query("userId") userId: Long): Response<Boolean>



    //用户区接口
    @GET("users")
    suspend fun getUsers(): List<User>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): User

    @POST("users")
    suspend fun registerUser(@Body user: RegisterRequest): Response<User>

    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @Multipart
    @POST("users/{id}/avatar")
    suspend fun uploadAvatar(
        @Path("id") userId: Long,
        @Part file: MultipartBody.Part
    ): Response<AvatarResponse>

    @FormUrlEncoded
    @POST("auth/send")
    suspend fun sendCode(@Field("email") email: String): Response<String>

    @FormUrlEncoded
    @POST("auth/verify")
    suspend fun verifyCode(@Field("email") email: String,
                           @Field("code") code: String): Response<String>




    //评论区接口
    @GET("comments/post/{postId}")
    suspend fun getComments(@Path("postId") postId: Long): List<Comment>

    @POST("comments")
    suspend fun postComment(@Body comment: CreateCommentRequest): Response<Comment>

    @DELETE("comments/{commentId}")
    suspend fun deleteComment(@Path("commentId") commentId: Long): Response<Unit>


    //私信接口
    // 发送私信消息
    @POST("/chat/send")
    suspend fun sendMessage(@Body request: CreateMessageRequest): Response<Unit>

    // 获取历史消息
    @GET("/chat/history")
    suspend fun getHistory(
        @Query("userA") userA: Long,
        @Query("userB") userB: Long
    ): Response<List<Message>>

}
object ApiClient {
    private const val BASE_URL = "http://api.starcommunity.asia:54321/"

    val basicAuthInterceptor = Interceptor { chain ->
        val credentials = Credentials.basic("admin", "123456")
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
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PostApiService::class.java)
}

data class CreatePostRequest(val title: String, val content: String, val userId: Long)
data class CreateCommentRequest(val postId: Long, val user: User, val content: String)
data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(val username: String, val password: String, val email: String)
data class CreateMessageRequest (val chatId: Long, val senderId: Long, val content: String)
data class EmailRequest(val email: String, val code: String)
data class TokenResponse(val token: String, val userId: Long)
data class LikeRequest(val userId: Long)
data class PageResponse<T>(
    val content: List<T>,
    val number: Int,
    val totalPages: Int
)
data class AvatarResponse(val avatarUrl: String)