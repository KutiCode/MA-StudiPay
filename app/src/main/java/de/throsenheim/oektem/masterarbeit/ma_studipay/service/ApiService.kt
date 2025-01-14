package de.throsenheim.oektem.masterarbeit.ma_studipay.service

import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.worker.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    // Login-Endpunkt
    @POST("/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<User>

    // Registrierungs-Endpunkt
    @POST("/register")
    suspend fun registerUser(@Body user: User): Response<Unit>

    // Synchronisation-Endpunkt
    @GET("/users")
    suspend fun getAllUsers(): Response<List<User>>
}
