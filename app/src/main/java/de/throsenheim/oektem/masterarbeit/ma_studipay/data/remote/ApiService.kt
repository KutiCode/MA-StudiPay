package de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote

import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.dto.BankResponseDto
import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request.BalanceUpdateRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request.RiskValueUpdateRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request.SecurePinUpdateRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request.TransactionVerificationRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.response.UpdateUserResponse
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request.UserRegistrationRequest
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT


interface ApiService {
    @GET("/api/all_secrets")
    suspend fun getAllBankSecrets(): BankResponseDto


    @GET("/api/users")
    suspend fun getAllUsers(): Response<UserResponse>

    @POST("/api/register")
    suspend fun registerUser(@Body userRegistrationRequest: UserRegistrationRequest): Response<Unit>

    @POST("/api/add_balance")
    suspend fun addBalance(@Body balanceUpdateRequest: BalanceUpdateRequest): Response<Unit>

    @POST("/api/deduct_balance")
    suspend fun deductBalance(@Body balanceUpdateRequest: BalanceUpdateRequest): Response<Unit>

    @POST("/api/update_secure_pin")
    suspend fun updateSecurePin(@Body securePinUpdateRequest: SecurePinUpdateRequest): Response<Unit>

    @PUT("api/update_user")
    suspend fun updateUser(@Body user: User): Response<UpdateUserResponse>

    @POST("/api/verify_transaction")
    suspend fun verifyTransaction(@Body transactionVerificationRequest: TransactionVerificationRequest): Response<Unit>

    @POST("/api/update_risk_params")
    suspend fun updateRiskParams(@Body riskValueUpdateRequest: RiskValueUpdateRequest): Response<Unit>
}

