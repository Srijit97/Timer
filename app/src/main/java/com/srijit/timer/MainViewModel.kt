package com.srijit.timer

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {
    val isTimerRunning= ObservableBoolean(false)


    fun toggleTimerRunning(isRunning: Boolean){
        isTimerRunning.set(isRunning)
    }
}