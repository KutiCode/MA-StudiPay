package de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.response

import de.throsenheim.oektem.masterarbeit.ma_studipay.model.User

data class UpdateUserResponse(
    val message: String,
    val user: User
)