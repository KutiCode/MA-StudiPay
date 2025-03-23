package de.throsenheim.oektem.masterarbeit.ma_studipay.service

import de.throsenheim.oektem.masterarbeit.ma_studipay.model.User

data class UpdateUserResponse(
    val message: String,
    val user: User
)