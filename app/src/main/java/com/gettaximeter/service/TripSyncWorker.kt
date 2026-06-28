package com.gettaximeter.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gettaximeter.data.repository.TaxiRepository

class TripSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = TaxiRepository(applicationContext)
        return try {
            repository.syncOfflineData()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
