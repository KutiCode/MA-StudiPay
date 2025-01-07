package de.throsenheim.oektem.masterarbeit.ma_studipay.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val university: String,
    val bankCode: String,
    val balance: Double
)
