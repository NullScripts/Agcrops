package com.example.myapplication.models

import com.firebase.geofire.GeoLocation

class DriverGeoModel {
    var key : String? = null
    var geoLocation : GeoLocation? = null
    var tractorInfoModel : TractorInfoModel? = null
    var isDecline:Boolean = false

    constructor(key:String?, geoLocation: GeoLocation)
    {
        this.key=key
        this.geoLocation = geoLocation!!
    }




}