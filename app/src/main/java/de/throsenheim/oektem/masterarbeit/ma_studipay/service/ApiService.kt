package de.throsenheim.oektem.masterarbeit.ma_studipay.service

import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("/users")
    suspend fun getAllUsers(): Response<List<User>>
}
