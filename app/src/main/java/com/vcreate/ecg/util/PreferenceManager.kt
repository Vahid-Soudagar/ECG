package com.vcreate.ecg.util

import android.content.Context
import android.content.SharedPreferences
import com.vcreate.ecg.R
import com.vcreate.ecg.data.model.GuestDetailRequest

class PreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(context.getString(
        R.string.app_name), Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()

    fun saveGuestDetails(guestDetailRequest: GuestDetailRequest) {
        editor.putStringSet("allergiestListGuestDetail", guestDetailRequest.allergiesList.toHashSet())
        editor.putStringSet("chronicDiseasesListGuestDetail", guestDetailRequest.chronicDiseasesList.toHashSet())
        editor.putString("dateOfBirthGuestDetail", guestDetailRequest.dateOfBirth)
        editor.putString("fullNameGuestDetail", guestDetailRequest.fullName)
        editor.putString("genderGuestDetail", guestDetailRequest.gender)
        editor.putString("countryCodeGuestDetail", guestDetailRequest.countryCode)
        editor.putString("phoneNumberGuestDetail", guestDetailRequest.number)
        editor.putString("machineIdGuestDetail", guestDetailRequest.machineId)
        editor.putString("operatorIdGuestDetail", guestDetailRequest.operatorId)
        editor.apply()
    }

    fun retrieveGuestDetails() : GuestDetailRequest {
        return GuestDetailRequest(
            allergiesList = sharedPreferences.getStringSet("allergiestListGuestDetail", emptySet())!!.toList(),
            chronicDiseasesList = sharedPreferences.getStringSet("chronicDiseasesListGuestDetail", emptySet())!!.toList(),
            dateOfBirth = sharedPreferences.getString("dateOfBirthGuestDetail", "").toString(),
            fullName = sharedPreferences.getString("fullNameGuestDetail", "").toString(),
            gender = sharedPreferences.getString("genderGuestDetail", "").toString(),
            machineId = sharedPreferences.getString("machineIdGuestDetail", "").toString(),
            operatorId = sharedPreferences.getString("operatorIdGuestDetail", "").toString(),
            countryCode = sharedPreferences.getString("countryCodeGuestDetail", "").toString(),
            number = sharedPreferences.getString("phoneNumberGuestDetail", "").toString()
        )
    }

    fun saveData(data: String) {
        editor.putString("data", data)
        editor.apply()
    }

    fun getData(): String? {
        return sharedPreferences.getString("data", null)
    }

    fun saveParameterData(parameterData: ParameterData) {
        editor.putString("p", parameterData.p)
        editor.putString("rr", parameterData.rr)
        editor.putString("pr", parameterData.pr)
        editor.putString("qt", parameterData.qt)
        editor.putString("qtr", parameterData.qtr)
        editor.putString("stLevel", parameterData.stLevel)
        editor.apply()
    }

    fun getParameterData() : ParameterData {
        return ParameterData(
            p = sharedPreferences.getString("p", "").toString(),
            rr = sharedPreferences.getString("rr", "").toString(),
            pr = sharedPreferences.getString("pr", "").toString(),
            qt = sharedPreferences.getString("qt", "").toString(),
            qtr = sharedPreferences.getString("qtr", "").toString(),
            stLevel = sharedPreferences.getString("stLevel", "").toString()
        )
    }
}