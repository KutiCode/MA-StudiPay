package de.throsenheim.oektem.masterarbeit.ma_studipay.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operation: String, // "INSERT", "UPDATE", "DELETE"
    val userId: String,    // Referenz auf den Nutzer (z. B. matrikelnumber)
    val timestamp: Long    // Zeitstempel der Änderung
)
