package de.throsenheim.oektem.masterarbeit.ma_studipay.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operation: String,
    val userId: String,
    val timestamp: Long
)
