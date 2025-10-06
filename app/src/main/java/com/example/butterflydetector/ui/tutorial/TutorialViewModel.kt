package com.example.butterflydetector.ui.tutorial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TutorialViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is the Tutorial Fragment"
    }
    val text: LiveData<String> = _text
}
