package de.throsenheim.oektem.masterarbeit.ma_studipay.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "matrikelnumber")
    val matrikelnumber: String, // Ändere zu String, wenn es als TEXT gespeichert werden soll

    @ColumnInfo(name = "lastName")
    val lastName: String,

    @ColumnInfo(name = "firstName")
    val firstName: String,

    @ColumnInfo(name = "password")
    val password: String,

    @ColumnInfo(name = "accountNumber")
    val accountNumber: String,

    @ColumnInfo(name = "balance")
    var balance: Double,

    @ColumnInfo(name = "securePin")
    val securePin: String



) {

}

