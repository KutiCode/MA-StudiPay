package de.throsenheim.oektem.masterarbeit.ma_studipay.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * RetrofitInstance is a singleton object that provides a configured instance of Retrofit,
 * allowing for network API calls throughout the app.
 */
object RetrofitInstance {
    // Base URL for the API; all endpoint paths will be relative to this URL.
    private const val BASE_URL = "http://192.168.0.10:5000/"

    // Lazily initialized property of type ApiService.
    // The 'by lazy' delegate ensures that the Retrofit instance is created only once when first accessed.
    val api: ApiService by lazy {
        Retrofit.Builder()                                 // Create a new Retrofit Builder.
            .baseUrl(BASE_URL)                             // Set the base URL for the API.
            .addConverterFactory(GsonConverterFactory.create()) // Add a converter factory to serialize/deserialize JSON.
            .build()                                       // Build the Retrofit instance.
            .create(ApiService::class.java)               // Create the implementation of the API endpoints defined in ApiService.
    }
}
