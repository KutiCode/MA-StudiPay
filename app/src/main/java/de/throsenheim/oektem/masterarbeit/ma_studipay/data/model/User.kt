package de.throsenheim.oektem.masterarbeit.ma_studipay.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val vorname: String,
    val benutzername: String,
    val passwort: String
)
