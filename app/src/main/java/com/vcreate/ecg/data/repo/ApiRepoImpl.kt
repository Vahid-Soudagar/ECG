package com.vcreate.ecg.data.repo

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.vcreate.ecg.data.model.ApiResultDemo
import com.vcreate.ecg.data.model.EcgRequest
import com.vcreate.ecg.data.model.EcgResponse
import com.vcreate.ecg.data.model.RequestErrorModel
import com.vcreate.ecg.data.service.ApiInterface
import retrofit2.Response

class ApiRepoImpl(
    private val apiService: ApiInterface
) : ApiRepo {


    override suspend fun processEcg(data: EcgRequest): ApiResultDemo<EcgResponse> {
        return try {
            val response = apiService.processEcg(data)
            handleApiResponse(response)
        } catch (e: Exception) {
            val error = "Error in calling api $e"
            return ApiResultDemo.Error(RequestErrorModel(error,null, null))
        }
    }

    private fun <T : Any> handleApiResponse(response: Response<T>) =
        if (response.isSuccessful) {
            ApiResultDemo.Success(response.body()!!)
        } else {
            val errorBody = response.errorBody()?.string()
            val errorResponse = errorBody?.let { parseErrorResponse(it) }
            val error = "error response is null"
            ApiResultDemo.Error(errorResponse ?: RequestErrorModel(error,null, null))
        }

    private fun parseErrorResponse(errorBody: String): RequestErrorModel {
        return try {
            val gson = Gson() // Assuming you're using Gson for JSON parsing
            gson.fromJson(errorBody, RequestErrorModel::class.java)
        } catch (e: JsonSyntaxException) {
            val error = "Error parsing error response"
            RequestErrorModel(error, null,null)
        }
    }
}