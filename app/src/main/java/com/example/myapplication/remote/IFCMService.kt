package com.example.myapplication.remote

import com.example.myapplication.models.FCMResponse
import com.example.myapplication.models.FCMSendData
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAA67gFnQs:APA91bG1IhErWZauAIbR7s22hUQK5sWUJYzkOVDD-YQ2RCRAicqtO4hmS-Js1bjH1SCjao--0_M0eqbjJFP7qLu1PqUD_ZnX6FtsxCI8fo21T8LjQjvYeeWoDqLgRebOvvRQ4sO6X1RI"
    )

    @POST("fcm/send")
    fun sendNotification(@Body body: FCMSendData?): Observable<FCMResponse?>?
}