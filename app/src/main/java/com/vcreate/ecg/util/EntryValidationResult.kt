package com.vcreate.ecg.util

data class EntryValidationResult (
    val successful: Boolean,
    val errorMessage: String? = null
)