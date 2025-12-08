package br.edu.utfpr.geocoleta.Data.Network

import br.edu.utfpr.geocoleta.Data.Models.ActivationRequest
import br.edu.utfpr.geocoleta.Data.Models.ActivationResponse
import br.edu.utfpr.geocoleta.Data.Models.Coordinates
import br.edu.utfpr.geocoleta.Data.Models.CoordinatesDTO
import br.edu.utfpr.geocoleta.Data.Models.Incident
import br.edu.utfpr.geocoleta.Data.Models.Route
import br.edu.utfpr.geocoleta.Data.Models.Trajeto
import br.edu.utfpr.geocoleta.Data.Models.TrajetoResponse
import br.edu.utfpr.geocoleta.Data.Models.Truck
import br.edu.utfpr.geocoleta.Data.Models.Trucker
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

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
    suspend fun sendCoordinate(@Body coordinate: List<CoordinatesDTO>): Response<Unit>

    @POST("trajetos/iniciar")
    suspend fun registrarTrajeto(@Body trajeto: Trajeto): Response<TrajetoResponse>

    @PUT("trajetos/{id}/finalizar")
    suspend fun finalizarTrajeto(@Path("id") id: Int): Response<TrajetoResponse>

    @POST("pontos-trajeto/registrar")
    suspend fun sendOneLocation(@Body coordinate: CoordinatesDTO): Response<Unit>

    @GET("trajetos/{id}")
    suspend fun getTrajeto(@Path("id") id: Int): Response<Trajeto>

    @PUT("trajetos/{id}")
    suspend fun updateTrajeto(@Path("id") id: Int, @Body trajeto: Trajeto): Response<Trajeto>


    @Multipart
    @POST("incidentes/upload")
    suspend fun criarIncidenteComFoto(
        @Part("incidente") incidenteJson: RequestBody,
        @Part foto: MultipartBody.Part
    ): Incident
}