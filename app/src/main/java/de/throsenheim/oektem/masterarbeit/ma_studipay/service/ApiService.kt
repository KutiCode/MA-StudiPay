package de.throsenheim.oektem.masterarbeit.ma_studipay.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface ApiService {
    @GET("/api/users")
    suspend fun getAllUsers(): Response<UserResponse>

    @POST("/api/register")
    suspend fun registerUser(@Body userRegistrationRequest: UserRegistrationRequest): Response<Unit>

    @POST("/api/add_balance")
    suspend fun addBalance(@Body balanceUpdateRequest: BalanceUpdateRequest): Response<Unit>

    @POST("/api/deduct_balance")
    suspend fun deductBalance(@Body balanceUpdateRequest: BalanceUpdateRequest): Response<Unit>


}

