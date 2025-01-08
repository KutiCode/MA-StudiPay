package de.throsenheim.oektem.masterarbeit.ma_studipay.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lastName: String,
    val firstName: String,
    val username: String,
    val password: String,
    val matrikelnumber: String,
    val accountNumber: String,
    val balance: Double

)
