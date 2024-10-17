package com.vcreate.ecg



data class Patient(
    val patientId: Long = 0,
    var name: String = "",
    var age: String= "",
    var sex: String = "",
    var height: String = "",
    var weight: String = "",
    val patientType: String = ""
)
