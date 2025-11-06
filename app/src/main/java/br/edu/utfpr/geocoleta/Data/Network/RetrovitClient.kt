package br.edu.utfpr.geocoleta.Data.Network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrovitClient {
    private const val BASE_URL = "https://rotas-api-yqsi.onrender.com/api/"
    private const val PREFS_NAME = "GeoColetaPrefs"
    private const val TOKEN_KEY = "auth_token"

    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val authInterceptor = Interceptor { chain ->
        val token = sharedPreferences.getString(TOKEN_KEY, null)
        val requestBuilder = chain.request().newBuilder()

        requestBuilder.addHeader("Content-Type", "application/json")
        token?.let { requestBuilder.addHeader("X-App-Token", it) }

        val request = requestBuilder.build()

        // 🔹 Log simples da requisição
        Log.i("RetrofitDebug", "➡️ REQUEST ${request.method} ${request.url}")
        Log.i("RetrofitDebug", "Headers: ${request.headers}")

        val response = chain.proceed(request)

        // 🔹 Log simples da resposta
        Log.i("RetrofitDebug", "⬅️ RESPONSE ${response.code} ${response.message}")

        response
    }

    private val logging = HttpLoggingInterceptor { message ->
        // Opcional: imprime body da requisição e resposta
        Log.i("RetrofitDebug", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .protocols(listOf(Protocol.HTTP_1_1))
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
