package de.throsenheim.oektem.masterarbeit.ma_studipay.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "banks")
data class Bank(
    @PrimaryKey
    @ColumnInfo("bankleitzahl")
    val bankleitzahl: String,

    @ColumnInfo("bankname")
    val bankname: String


)
