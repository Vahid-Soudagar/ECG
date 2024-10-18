package com.vcreate.ecg.data.service

import com.vcreate.ecg.data.model.EcgResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {

    @GET("ecg")
    suspend fun processEcg(
        @Query("data") data: String
    ): Response<EcgResponse>

}