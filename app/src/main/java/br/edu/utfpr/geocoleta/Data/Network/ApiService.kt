package br.edu.utfpr.geocoleta.Data.Network

import br.edu.utfpr.geocoleta.Data.Models.ActivationRequest
import br.edu.utfpr.geocoleta.Data.Models.ActivationResponse
import br.edu.utfpr.geocoleta.Data.Models.Coordinates
import br.edu.utfpr.geocoleta.Data.Models.Route
import br.edu.utfpr.geocoleta.Data.Models.Truck
import br.edu.utfpr.geocoleta.Data.Models.Trucker
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("motoristas")
    suspend fun getMotoristas(): List<Trucker>

    @GET("caminhoes")
    suspend fun getCaminhoes(): List<Truck>

    @GET("rota")
    suspend fun getRotas(): List<Route>

    @POST("app/activate")
    suspend fun activate(@Body request: ActivationRequest): ActivationResponse

    @POST("pontos-trajeto/registrar-lote")
    suspend fun sendCoordinate(@Body coordinate: List<Coordinates>): Response<Unit>
}