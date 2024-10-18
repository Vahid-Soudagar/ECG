package com.vcreate.ecg.data.service

import com.vcreate.ecg.data.model.EcgResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiInterface {

    @POST("ecg")
    suspend fun processEcg(
        @Query("data") data: String
    ): Response<EcgResponse>

}