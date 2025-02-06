package de.throsenheim.oektem.masterarbeit.ma_studipay.worker

import android.content.Context
import android.util.Log
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
            Log.d("MainActivity", "SyncWorker erfolgreich")
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}