package de.throsenheim.oektem.masterarbeit.ma_studipay.service

import de.throsenheim.oektem.masterarbeit.ma_studipay.data.model.User

data class UpdateUserResponse(
    val message: String,
    val user: User
)