package com.vcreate.ecg.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vcreate.ecg.data.ApiClient
import com.vcreate.ecg.data.model.ApiResultDemo
import com.vcreate.ecg.data.model.EcgResponse
import com.vcreate.ecg.data.repo.ApiRepoImpl
import com.vcreate.ecg.data.service.ApiInterface
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val apiService =
        ApiClient.createService(ApiInterface::class.java)

    private val repository = ApiRepoImpl(apiService)

    private val _ecgResponse =
        MutableLiveData<ApiResultDemo<EcgResponse>>()
    val ecgResponseResult: LiveData<ApiResultDemo<EcgResponse>> get() = _ecgResponse


    fun getEcgResponse(data: String) {
        viewModelScope.launch {
            _ecgResponse.postValue(repository.processEcg(data))
        }
    }
}