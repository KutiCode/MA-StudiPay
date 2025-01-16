package de.throsenheim.oektem.masterarbeit.ma_studipay.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.repository.UserRepository

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val userRepository: UserRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            userRepository.syncDatabase() // Synchronisiere die Datenbank
            Result.success()
        } catch (e: Exception) {
            Result.retry() // Bei Fehlern erneut versuchen
        }
    }
}
