package de.throsenheim.oektem.masterarbeit.ma_studipay.service

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://your-backend-url.com/" // Deine Backend-URL

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // JSON-Konvertierung
            .build()
            .create(ApiService::class.java)
    }
}
