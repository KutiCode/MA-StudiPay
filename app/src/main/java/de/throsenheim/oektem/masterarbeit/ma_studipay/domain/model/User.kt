package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model

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
    @PrimaryKey val matriculationNumber: String,
    @ColumnInfo(name = "lastName") val lastName: String,
    @ColumnInfo(name = "firstName") val firstName: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "accountNumber") val accountNumber: String,
    @ColumnInfo(name = "balance") var balance: Double,
    @ColumnInfo(name = "securePin") val securePin: String,
    @ColumnInfo(name = "bank_code") var bank_code: String? = null,
    @ColumnInfo(name = "dailyTransactionCount") var dailyTransactionCount: Int = 0,
    @ColumnInfo(name = "lastTransactionDate") var lastTransactionDate: String? = null,
    @ColumnInfo(name = "highRiskAbortedCount") var highRiskAbortedCount: Int = 0,
    @ColumnInfo(name = "lastTransaktionRiskValue") var lastTransaktionRiskValue: Int = 0
)
