package com.vcreate.ecg.data.repo

import com.vcreate.ecg.data.model.ApiResultDemo
import com.vcreate.ecg.data.model.EcgRequest
import com.vcreate.ecg.data.model.EcgResponse
import com.vcreate.ecg.data.model.GuestDetailRequest
import com.vcreate.ecg.data.model.GuestDetailResponse

interface ApiRepo {

    suspend fun processEcg(data: EcgRequest): ApiResultDemo<EcgResponse>

    suspend fun guestSignIn(guestDetails: GuestDetailRequest): ApiResultDemo<GuestDetailResponse>

}