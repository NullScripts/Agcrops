package com.example.myapplication.ui.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditTextViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is edit Fragment"
    }
    val text: LiveData<String> = _text
}