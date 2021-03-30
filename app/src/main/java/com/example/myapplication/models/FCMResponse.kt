package com.example.myapplication.models

class FCMResponse {
    var multicast_id : Long = 0
    var success = 0
    var failure = 0
    var cannonical_ids = 0
    var results : List<FCMResult>?= null
    var message_id:Long=0
}