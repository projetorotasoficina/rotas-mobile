package br.edu.utfpr.geocoleta.Data.Network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrovitClient {
    private const val BASE_URL = "https://rotas-api-yqsi.onrender.com"
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}