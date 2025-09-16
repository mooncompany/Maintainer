package com.maintainer.app.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface NhtsaApiService {
    @GET("vehicles/DecodeVin/{vin}?format=json")
    suspend fun decodeVin(@Path("vin") vin: String): Response<VinDecodeResponse>
}

data class VinDecodeResponse(
    val Results: List<VinResult>
)

data class VinResult(
    val Variable: String,
    val Value: String?
)