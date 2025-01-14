package de.throsenheim.oektem.masterarbeit.ma_studipay.service

import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("/register")
    suspend fun registerUser(@Body user: User): Response<Unit>

    @GET("/sync")
    suspend fun getUsers(): Response<List<User>>
}
