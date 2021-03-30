package com.example.myapplication.ui.book
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BookViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Tractor Booking Fragment"
    }
    val text: LiveData<String> = _text
}