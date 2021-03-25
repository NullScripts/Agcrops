package com.example.myapplication.models


data class BuyerRequest (
    val address: String = "",
    val requirements: String = "",

    val createdby: String = "",
    val createddate: Long = 0L
)