package com.example.myapplication.callback

import com.example.myapplication.models.DriverGeoModel

interface FirebaseDriverInfoListener {

    fun onDriverInfoLoadSuccess(driverGeoModel: DriverGeoModel?)
}