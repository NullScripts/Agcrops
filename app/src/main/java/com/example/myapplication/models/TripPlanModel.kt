package com.example.myapplication.models

class TripPlanModel{
    var rider : String? = null
    var driver : String? = null
    var driverInfoModel : TractorInfoModel? = null
    var riderInfoModel : RiderInfoModel? = null
    var origin : String? = null
    var originString : String? = null
    var destination : String? = null
    var destinationString : String? = null
    var distancePickup : String? = null
    var durationPickup : String? = null
    var distanceDestination : String? = null
    var durationDestination : String? = null
    var currentLat : Double? = -1.0
    var currentLng : Double? = -1.0
    var isDone = false
    var isCancel = false
}