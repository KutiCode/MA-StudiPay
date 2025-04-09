package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.services

import android.content.Context
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.request.BalanceUpdateRequest
import kotlinx.coroutines.runBlocking
import retrofit2.Response

object BalanceService {


    fun addBalanceService(context: Context, matriculationNumber: String, amount: Double): Boolean {
        return runBlocking {
            val addRequest = BalanceUpdateRequest(matriculationNumber, amount)
            val addResponse: Response<Unit> = RetrofitInstance.api.addBalance(addRequest)
            if (addResponse.isSuccessful) {
                val userDao = AppDatabase.getDatabase(context).userDao()
                val user = userDao.getUserByMatriculationNumber(matriculationNumber)
                user?.let {
                    it.balance += amount
                    userDao.updateUserBalance(matriculationNumber, it.balance)

                }
                true
            } else {
                false
            }
        }
    }


    fun reduceBalanceService(
        context: Context,
        matriculationNumber: String,
        amount: Double
    ): Boolean {
        return runBlocking {
            val deductRequest = BalanceUpdateRequest(matriculationNumber, amount)
            val deductResponse: Response<Unit> = RetrofitInstance.api.deductBalance(deductRequest)
            if (deductResponse.isSuccessful) {
                val userDao = AppDatabase.getDatabase(context).userDao()
                val user = userDao.getUserByMatriculationNumber(matriculationNumber)
                user?.let {
                    it.balance -= amount
                    userDao.updateUserBalance(matriculationNumber, it.balance)
                }
                true
            } else {
                false
            }
        }
    }


}