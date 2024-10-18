package com.vcreate.ecg.data.model

data class GuestDetailRequest (
    val allergiesList: List<String>,
    val chronicDiseasesList: List<String>,
    val countryCode: String,
    val number: String,
    val dateOfBirth: String,
    val fullName: String,
    val gender: String,
    val machineId: String,
    val operatorId: String,
    var height: Int = 0,
    var weight: Double = 0.0,
    val age: Int = 0
)