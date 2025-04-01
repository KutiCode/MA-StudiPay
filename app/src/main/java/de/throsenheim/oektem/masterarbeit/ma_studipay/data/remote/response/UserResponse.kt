package de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote.response

import de.throsenheim.oektem.masterarbeit.ma_studipay.domain.model.User

data class UserResponse(
    val users: List<User>
)