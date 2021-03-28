package com.example.myapplication.callback

import com.example.myapplication.models.TractorGeoModel

interface FirebaseTractorInfoListener {

    fun onTractorInfoLoadSuccess(tractorGeoModel: TractorGeoModel?)
}