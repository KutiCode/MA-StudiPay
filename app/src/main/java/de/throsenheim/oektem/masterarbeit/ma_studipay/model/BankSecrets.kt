package de.throsenheim.oektem.masterarbeit.ma_studipay.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "bank_secrets",
    foreignKeys = [ForeignKey(
        entity = Bank::class,
        parentColumns = arrayOf("bank_code"),
        childColumns = arrayOf("bank_code"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class BankSecrets(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bank_code: String,         // Fremdschlüssel als String
    val secretCode: String,
    val generatedAt: String
)



