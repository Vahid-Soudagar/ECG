package com.vcreate.ecg.data.model

sealed class ApiResultDemo<out T : Any> {
    data class Success<out T : Any>(val data: T) : ApiResultDemo<T>()
    data class Error(val error: RequestErrorModel) : ApiResultDemo<Nothing>()
}