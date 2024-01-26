package edu.put.grooveglider.internet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

// Used for updating connection state
class LoadingViewModel : ViewModel() {
    val connectionState = MutableLiveData<Boolean>()

    fun connect() {
        viewModelScope.launch {
            val isConnected = TCPHandler.connect()
            connectionState.postValue(isConnected)
        }
    }
}