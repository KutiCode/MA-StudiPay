package de.throsenheim.oektem.masterarbeit.ma_studipay.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class BalanceUpdateRequest(val matrikelnumber: String, val amount: Double)
