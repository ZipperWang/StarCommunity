package com.release.startcommunity.api

import com.release.startcommunity.model.Comment
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
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.FormUrlEncoded
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
    suspend fun registerUser(@Body user: User): Response<User>

    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): TokenResponse

    @GET("posts/paged")
    suspend fun getPostsPaged(
        @Query("page") page: Int,
        @Query("size") size: Int = 10
    ): PageResponse<Post>   // 用后端 Page<PostDTO>

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
}
object ApiClient {
    private const val BASE_URL = "http://47.121.204.76:8080/"

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
data class CreateCommentRequest(val postId: Long, val userId: Long, val content: String)
data class LoginRequest(val username: String, val password: String)
data class EmailRequest(val email: String, val code: String)
data class TokenResponse(val token: String, val uid: Long)
data class PageResponse<T>(
    val content: List<T>,
    val number: Int,
    val totalPages: Int
)
data class AvatarResponse(val avatarUrl: String)