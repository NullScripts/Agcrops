package com.example.myapplication.services

import com.example.myapplication.Common
import com.example.myapplication.models.EventBus.DeclineRequestFromDriver
import com.example.myapplication.models.EventBus.DriverAcceptTripEvent
import com.example.myapplication.utils.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.greenrobot.eventbus.EventBus
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        if(FirebaseAuth.getInstance().currentUser != null)
        {
            UserUtils.updateToken(this, p0)
        }
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        val data = p0.data
        if(data != null)
        {
            if(data[Common.NOTI_TITLE] != null)
            {
                if(data[Common.NOTI_TITLE].equals(Common.REQUEST_DRIVER_DECLINE)){
                    EventBus.getDefault().postSticky(DeclineRequestFromDriver())
                }else if(data[Common.NOTI_TITLE].equals(Common.REQUEST_DRIVER_ACCEPT)){
                       EventBus.getDefault().postSticky(DriverAcceptTripEvent(data[Common.TRIP_KEY]!!))
                }
                else{
                    Common.showNotification(this, Random.nextInt(),
                            data[Common.NOTI_TITLE],
                            data[Common.NOTI_BODY],
                            null)
                }
            }

        }
    }


}