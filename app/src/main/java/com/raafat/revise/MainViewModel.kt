package com.raafat.revise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _ayaListFlow = MutableStateFlow<List<Aya>>(emptyList())
    val ayaListFlow: StateFlow<List<Aya>> = _ayaListFlow.asStateFlow()

    private val _sliderValue = MutableLiveData<Float>()
    val sliderValue: LiveData<Float> get() = _sliderValue

    private val _isTouching = MutableLiveData<Boolean>()
    val isTouching: LiveData<Boolean> get() = _isTouching

    init {
        loadAyaList()
    }

    private fun loadAyaList() {
        viewModelScope.launch {
            AppSingleton.loadAyaList(getApplication()).collect { ayaList ->
                _ayaListFlow.value = ayaList
            }
        }
    }

    fun onSliderValueChanged(value: Float) {
        _sliderValue.value = value
    }

    fun onStartTouch() {
        _isTouching.value = true
    }

    fun onStopTouch() {
        _isTouching.value = false
    }
}