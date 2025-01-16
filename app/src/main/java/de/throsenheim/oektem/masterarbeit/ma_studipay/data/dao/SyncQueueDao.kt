package de.throsenheim.oektem.masterarbeit.ma_studipay.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.SyncQueueEntry

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: SyncQueueEntry)

    @Query("SELECT * FROM sync_queue")
    suspend fun getAllEntries(): List<SyncQueueEntry>

    @Delete
    suspend fun delete(entry: SyncQueueEntry)
}
