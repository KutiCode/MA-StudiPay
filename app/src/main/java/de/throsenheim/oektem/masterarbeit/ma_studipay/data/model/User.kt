package de.throsenheim.oektem.masterarbeit.ma_studipay.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user",
    foreignKeys = [ForeignKey(
        entity = Bank::class,
        parentColumns = arrayOf("bank_code"),
        childColumns = arrayOf("bank_code"),
        onDelete = ForeignKey.SET_NULL
    )]
)
data class User(
    @PrimaryKey val matrikelnumber: String,

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
    val securePin: String,

    @ColumnInfo(name = "bank_code")
    var bank_code: String?   // Fremdschlüssel, nullable, da evtl. kein Bank zugeordnet
) {

}

