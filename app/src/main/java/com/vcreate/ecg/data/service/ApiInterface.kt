package com.vcreate.ecg.data.service

import com.vcreate.ecg.data.model.EcgRequest
import com.vcreate.ecg.data.model.EcgResponse
import com.vcreate.ecg.data.model.GuestDetailRequest
import com.vcreate.ecg.data.model.GuestDetailResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiInterface {

    @POST("ecg")
    suspend fun processEcg(
        @Body ecgRequest: EcgRequest
    ): Response<EcgResponse>

    //    https://api.bridgehealth.care/v1/guest/register
    @Headers("Content-Type: application/json")
    @POST("guest/register")
    suspend fun guestAuth(@Body guestDetails: GuestDetailRequest): Response<GuestDetailResponse>


}