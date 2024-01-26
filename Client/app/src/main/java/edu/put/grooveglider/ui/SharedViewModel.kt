package edu.put.grooveglider.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// Share data between all activities
class SharedViewModel : ViewModel() {
    val jsonDataQueue = MutableLiveData<String>()
    val jsonDataServer = MutableLiveData<String>()
    val currentSong = MutableLiveData<Int>()
}