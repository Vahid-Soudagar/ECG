package com.vcreate.ecg.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vcreate.ecg.data.ApiClient
import com.vcreate.ecg.data.model.ApiResultDemo
import com.vcreate.ecg.data.model.EcgRequest
import com.vcreate.ecg.data.model.EcgResponse
import com.vcreate.ecg.data.model.GuestDetailRequest
import com.vcreate.ecg.data.model.GuestDetailResponse
import com.vcreate.ecg.data.repo.ApiRepoImpl
import com.vcreate.ecg.data.service.ApiInterface
import com.vcreate.ecg.util.BlankEntryValidation
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val apiService =
        ApiClient.createService(ApiInterface::class.java)

    private val repository = ApiRepoImpl(apiService)

    private val _ecgResponse =
        MutableLiveData<ApiResultDemo<EcgResponse>>()
    val ecgResponseResult: LiveData<ApiResultDemo<EcgResponse>> get() = _ecgResponse

    fun getEcgResponse(data: EcgRequest) {
        viewModelScope.launch {
            _ecgResponse.postValue(repository.processEcg(data))
        }
    }

    val blankEntryValidation = BlankEntryValidation()
    val mobileValidation = ValidationNumber()

    private val _nameError = MutableLiveData<String?>(null)
    val nameError: LiveData<String?> get() = _nameError

    private val _genderError = MutableLiveData<String?>(null)
    val genderError: LiveData<String?> get() = _genderError
    private val _dobError = MutableLiveData<String?>(null)
    val dobError: LiveData<String?> get() = _dobError

    private val _numberError = MutableLiveData<String?>(null)
    val numberError: LiveData<String?> get() = _numberError

    // MutableLiveData to hold the result of allergy list request.
    private val _guestSignInResult = MutableLiveData<ApiResultDemo<GuestDetailResponse>>()

    // Expose LiveData to observe changes in allergy list request result.
    val guestSignInResult: LiveData<ApiResultDemo<GuestDetailResponse>> get() = _guestSignInResult

    fun validateName(name: String, entryType: String) {

        val isEntryValid = blankEntryValidation.execute(name, entryType)
        if (!isEntryValid.successful) {
            _nameError.value = isEntryValid.errorMessage
        } else {
            _nameError.value = null
        }

    }
    fun validateGender(gender: String, entryType: String) {

        val isEntryValid = blankEntryValidation.execute(gender, entryType)
        if (!isEntryValid.successful) {
            _genderError.value = isEntryValid.errorMessage
        } else {
            _genderError.value = null
        }

    }
    fun validateDob(dob: String, entryType: String) {

        val isEntryValid = blankEntryValidation.execute(dob, entryType)
        if (!isEntryValid.successful) {
            _dobError.value = isEntryValid.errorMessage
        } else {
            _dobError.value = null
        }

    }

    fun validateNumber(number: String) {

        val isNumberValid = mobileValidation.execute(number)
        if (!isNumberValid.successful) {
            _numberError.value = isNumberValid.errorMessage
        } else {
            _numberError.value = null
        }

    }

    fun guestSignIn(guestDetail: GuestDetailRequest) {
        // Launch a coroutine within the scope of the ViewModel
        viewModelScope.launch {
            _guestSignInResult.value = repository.guestSignIn(guestDetail)
        }
    }
}