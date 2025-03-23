package de.throsenheim.oektem.masterarbeit.ma_studipay.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "banks",
    indices = [Index(value = ["bank_code"], unique = true)]
)
data class Bank(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val bank_code: String
)
