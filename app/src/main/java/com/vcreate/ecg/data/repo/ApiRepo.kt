package com.vcreate.ecg.data.repo

import com.vcreate.ecg.data.model.ApiResultDemo
import com.vcreate.ecg.data.model.EcgRequest
import com.vcreate.ecg.data.model.EcgResponse

interface ApiRepo {

    suspend fun processEcg(data: EcgRequest): ApiResultDemo<EcgResponse>
}