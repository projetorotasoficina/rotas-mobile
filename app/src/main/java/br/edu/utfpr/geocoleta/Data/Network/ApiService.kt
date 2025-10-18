package br.edu.utfpr.geocoleta.Data.Network

import br.edu.utfpr.geocoleta.Data.Models.Coordinates
import br.edu.utfpr.geocoleta.Data.Models.Route
import br.edu.utfpr.geocoleta.Data.Models.Truck
import br.edu.utfpr.geocoleta.Data.Models.Trucker
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("/motoristas")
    suspend fun getDrivers(): List<Trucker>

    @GET("/caminhoes")
    suspend fun getTrucks(): List<Truck>

    @GET("/rota")
    suspend fun getRoutes(): List<Route>

    @POST("/trajetos")
    suspend fun sendCoordinate(@Body coordinate: Coordinates): Response<Unit>

}