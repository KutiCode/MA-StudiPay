package de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Represents the secrets (sensitive information) associated with a bank.
 *
 * This entity is stored in the "bank_secrets" table.
 * It is linked to the Bank entity via a foreign key on the bank_code column.
 * When a Bank record is deleted, the corresponding bank secrets are also removed (CASCADE delete).
 */
@Entity(
    tableName = "bank_secrets",
    foreignKeys = [ForeignKey(
        entity = Bank::class,                 // The Bank entity this record is associated with.
        parentColumns = arrayOf("bank_code"),   // The column in the Bank entity that acts as the key.
        childColumns = arrayOf("bank_code"),    // The column in this entity that refers to the bank.
        onDelete = ForeignKey.CASCADE           // Cascade deletion of bank secrets when the bank is deleted.
    )]
)
data class BankSecrets(
    // The primary key for the bank_secrets table, auto-generated.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // Bank code that acts as a foreign key linking to the associated Bank entity.
    val bank_code: String,         // Foreign key as a string.
    // The secret code associated with the bank.
    val secretCode: String,
    // The timestamp indicating when this secret was generated.
    val generatedAt: String
)
