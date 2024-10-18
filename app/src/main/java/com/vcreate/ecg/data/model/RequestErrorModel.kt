package com.vcreate.ecg.data.model

data class RequestErrorModel(
    val message: String? = null,
    val messages: List<String>? = null,
    val error: String? = null,
    val statusCode: Long? = null
)