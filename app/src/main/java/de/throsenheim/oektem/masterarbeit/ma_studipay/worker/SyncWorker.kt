package de.throsenheim.oektem.masterarbeit.ma_studipay.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository
import de.throsenheim.oektem.masterarbeit.ma_studipay.service.RetrofitInstance
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.database.AppDatabase

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    val userRepository = UserRepository(
        userDao = AppDatabase.getDatabase(context).userDao(),
        syncQueueDao = AppDatabase.getDatabase(context).syncQueueDao(),
        apiService = RetrofitInstance.api
    )

    override suspend fun doWork(): Result {
        return try {
            userRepository.syncDatabase()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}