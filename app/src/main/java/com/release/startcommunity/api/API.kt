package com.release.startcommunity.api

import com.release.startcommunity.model.Post
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface PostApiService {
    @GET("/api/posts")
    suspend fun getPosts(): List<Post>

    @POST("/api/posts")
    suspend fun createPost(@Body post: Post): Response<Unit>
}
object ApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.114.60.110:8080/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val postService: PostApiService = retrofit.create(PostApiService::class.java)
}