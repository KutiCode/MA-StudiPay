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

/**
 * ApiService defines all REST API endpoints used by the app.
 *
 * Each function represents a specific endpoint for performing CRUD operations or sending
 * specific requests such as registration, balance updates, and risk parameter updates.
 */
interface ApiService {

    /**
     * Retrieves all bank secrets from the backend.
     *
     * @return A BankResponseDto containing a list of banks with their secrets.
     */
    @GET("/api/all_secrets")
    suspend fun getAllBankSecrets(): BankResponseDto

    /**
     * Retrieves all users from the backend.
     *
     * @return A Response wrapping a UserResponse containing the list of all users.
     */
    @GET("/api/users")
    suspend fun getAllUsers(): Response<UserResponse>

    /**
     * Registers a new user on the backend.
     *
     * @param userRegistrationRequest Contains the registration details for a user.
     * @return A Response<Unit> indicating whether the registration was successful.
     */
    @POST("/api/register")
    suspend fun registerUser(@Body userRegistrationRequest: UserRegistrationRequest): Response<Unit>

    /**
     * Adds a specified amount to a user's balance.
     *
     * @param balanceUpdateRequest Contains the matriculation number and the amount to add.
     * @return A Response<Unit> indicating the success or failure of the operation.
     */
    @POST("/api/add_balance")
    suspend fun addBalance(@Body balanceUpdateRequest: BalanceUpdateRequest): Response<Unit>

    /**
     * Deducts a specified amount from a user's balance.
     *
     * @param balanceUpdateRequest Contains the matriculation number and the amount to deduct.
     * @return A Response<Unit> indicating the success or failure of the deduction.
     */
    @POST("/api/deduct_balance")
    suspend fun deductBalance(@Body balanceUpdateRequest: BalanceUpdateRequest): Response<Unit>

    /**
     * Updates the secure PIN for a user.
     *
     * @param securePinUpdateRequest Contains the matriculation number and the new PIN.
     * @return A Response<Unit> indicating success or failure of the PIN update.
     */
    @POST("/api/update_secure_pin")
    suspend fun updateSecurePin(@Body securePinUpdateRequest: SecurePinUpdateRequest): Response<Unit>

    /**
     * Updates user details on the backend.
     *
     * @param user The updated User object.
     * @return A Response wrapping an UpdateUserResponse with information about the update.
     */
    @PUT("api/update_user")
    suspend fun updateUser(@Body user: User): Response<UpdateUserResponse>

    /**
     * Sends a transaction verification request to the backend.
     *
     * @param transactionVerificationRequest Contains details for verifying a transaction.
     * @return A Response<Unit> indicating whether the verification was successful.
     */
    @POST("/api/verify_transaction")
    suspend fun verifyTransaction(@Body transactionVerificationRequest: TransactionVerificationRequest): Response<Unit>

    /**
     * Updates risk parameters associated with a transaction.
     *
     * @param riskValueUpdateRequest Contains the updated risk parameter values.
     * @return A Response<Unit> indicating the success or failure of the risk parameter update.
     */
    @POST("/api/update_risk_params")
    suspend fun updateRiskParams(@Body riskValueUpdateRequest: RiskValueUpdateRequest): Response<Unit>
}
