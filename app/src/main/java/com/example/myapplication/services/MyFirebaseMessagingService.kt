package com.example.myapplication.services

import com.example.myapplication.Common
import com.example.myapplication.models.EventBus.DeclineRequestTractor
import com.example.myapplication.models.EventBus.TractorAcceptTripEvent
import com.example.myapplication.models.EventBus.TractorRequestReciever
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
        if(data != null && data[Common.NOTI_TITLE] != null)
        {

            if(data[Common.NOTI_TITLE].equals(Common.REQUEST_TRACTOR_TITLE)){

                val driverRequestReciever = TractorRequestReciever()
                driverRequestReciever.key = data[Common.BOOK_KEY]
                driverRequestReciever.pickupLocation = data[Common.PICKUP_LOCATION]
                driverRequestReciever.pickupLocationString = data[Common.PICKUP_LOCATION_STRING]
                driverRequestReciever.destinationLocation = data[Common.DESTINATION_LOCATION]
                driverRequestReciever.destinationLocation = data[Common.DESTINATION_LOCATION_STRING]
                EventBus.getDefault().postSticky(driverRequestReciever)
            }
            else if(data[Common.NOTI_TITLE].equals(Common.REQUEST_TRACTOR_DECLINE)){
                    EventBus.getDefault().postSticky(DeclineRequestTractor())
            }else if(data[Common.NOTI_TITLE].equals(Common.REQUEST_TRACTOR_ACCEPT)){
                   EventBus.getDefault().postSticky(TractorAcceptTripEvent(data[Common.TRIP_KEY]!!))
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