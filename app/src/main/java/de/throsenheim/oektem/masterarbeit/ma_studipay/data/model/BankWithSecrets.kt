package de.throsenheim.oektem.masterarbeit.ma_studipay.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class BankWithSecrets(
    @Embedded val bank: Bank,
    @Relation(
        parentColumn = "bank_code",
        entityColumn = "bank_code"
    )
    val secrets: List<BankSecrets>
)
