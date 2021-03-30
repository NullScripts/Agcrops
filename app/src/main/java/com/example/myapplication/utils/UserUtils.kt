package com.example.myapplication.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import com.example.myapplication.Common
import com.example.myapplication.R
import com.example.myapplication.models.EventBus.NotifyBookEvent
import com.example.myapplication.models.EventBus.SelectedPlaceEvents
import com.example.myapplication.models.FCMSendData
import com.example.myapplication.models.TokenModel
import com.example.myapplication.models.TractorGeoModel
import com.example.myapplication.remote.IFCMService
import com.example.myapplication.remote.RetrofitFCMClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.lang.StringBuilder

object UserUtils {

    fun updateUser(
        view : View?,
        updateData: Map<String, Any>
    ){
        FirebaseDatabase.getInstance()
            .getReference(Common.Book_INFO_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .updateChildren(updateData)
            .addOnFailureListener{e ->
                Snackbar.make(view!!, e.message!!, Snackbar.LENGTH_LONG).show()
            }.addOnSuccessListener {
                Snackbar.make(view!!, "Update Information Successful", Snackbar.LENGTH_LONG).show()
            }
    }

    fun updateToken(context: Context, token: String) {
        val tokenModel = TokenModel()
        tokenModel.token = token

        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .setValue(tokenModel)
            .addOnFailureListener{e ->
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
            .addOnSuccessListener {  }
    }

    fun sendRequestToTractor(context: Context, mainLayout: RelativeLayout?, foundTractor: TractorGeoModel?, selectedPlaceEvents: SelectedPlaceEvents) {

        val compositeDisposable = CompositeDisposable()
        val ifcmService = RetrofitFCMClient.instance!!.create(IFCMService::class.java)

        //Get Token
        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REFERENCE)
            .child(foundTractor!!.key!!)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Snackbar.make(mainLayout!!, error.message, Snackbar.LENGTH_LONG).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists())
                    {
                        val tokenModel = snapshot.getValue(TokenModel::class.java)
                        val notificationData : MutableMap<String, String> = HashMap()
                        notificationData.put(Common.NOTI_TITLE, Common.REQUEST_TRACTOR_TITLE)
                        notificationData.put(Common.NOTI_BODY, "This message represent for Request Driver action")
                        notificationData.put(Common.BOOK_KEY, FirebaseAuth.getInstance().currentUser!!.uid)

                        notificationData.put(Common.PICKUP_LOCATION_STRING, selectedPlaceEvents.originString)
                        notificationData.put(Common.PICKUP_LOCATION, StringBuilder()
                            .append(selectedPlaceEvents!!.origin!!.latitude)
                            .append(",")
                            .append(selectedPlaceEvents!!.origin!!.longitude)
                            .toString())

                        notificationData.put(Common.DESTINATION_LOCATION_STRING, selectedPlaceEvents.originString)
                        notificationData.put(Common.DESTINATION_LOCATION, StringBuilder()
                                .append(selectedPlaceEvents!!.destination!!.latitude)
                                .append(",")
                                .append(selectedPlaceEvents!!.destination!!.longitude)
                                .toString())



                        val fcmSendData = FCMSendData(tokenModel!!.token,notificationData)

                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ fcmResponse ->
                                if(fcmResponse!!.success == 0)
                                {
                                    compositeDisposable.clear()
                                    Snackbar.make(mainLayout!!, context.getString(R.string.send_request_tractor_failed), Snackbar.LENGTH_LONG).show()

                                }
                            },{t : Throwable? ->
                                compositeDisposable.clear()
                                Snackbar.make(mainLayout!!, t!!.message!!, Snackbar.LENGTH_LONG).show()
                            }))

                    }else{
                        Snackbar.make(mainLayout!!, context.getString(R.string.token_not_found), Snackbar.LENGTH_LONG).show()
                    }
                }
            })
    }

    fun sendDeclineRequest(view: View, activity: Activity, key: String) {

        val compositeDisposable = CompositeDisposable()
        val ifcmService = RetrofitFCMClient.instance!!.create(IFCMService::class.java)

        //Get Token
        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REFERENCE)
            .child(key)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Snackbar.make(view, error.message, Snackbar.LENGTH_LONG).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists())
                    {
                        val tokenModel = snapshot.getValue(TokenModel::class.java)
                        val notificationData : MutableMap<String, String> = HashMap()
                        notificationData.put(Common.NOTI_TITLE, Common.REQUEST_TRACTOR_DECLINE)
                        notificationData.put(Common.NOTI_BODY, "This message represent for Request Decline action")
                        notificationData.put(Common.TRACTOR_KEY, FirebaseAuth.getInstance().currentUser!!.uid)


                        val fcmSendData = FCMSendData(tokenModel!!.token,notificationData)

                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ fcmResponse ->
                                if(fcmResponse!!.success == 0)
                                {
                                    compositeDisposable.clear()
                                    Snackbar.make(view, activity.getString(R.string.decline_failed), Snackbar.LENGTH_LONG).show()

                                }
                                else
                                {

                                    Snackbar.make(view, activity.getString(R.string.decline_success), Snackbar.LENGTH_LONG).show()

                                }
                            },{t : Throwable? ->
                                compositeDisposable.clear()
                                Snackbar.make(view, t!!.message!!, Snackbar.LENGTH_LONG).show()
                            }))

                    }else{
                        compositeDisposable.clear()
                        Snackbar.make(view, activity.getString(R.string.token_not_found), Snackbar.LENGTH_LONG).show()
                    }
                }
            })

    }

    fun sendAcceptRequestToBook(
        view: View?,
        requireContext: Context,
        key: String,
        tripNumberId: String?
    ) {
        val compositeDisposable = CompositeDisposable()
        val ifcmService = RetrofitFCMClient.instance!!.create(IFCMService::class.java)



        //Get Token
        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REFERENCE)
            .child(key)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Snackbar.make(view!!, error.message, Snackbar.LENGTH_LONG).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists())
                    {
                        val tokenModel = snapshot.getValue(TokenModel::class.java)
                        val notificationData : MutableMap<String, String> = HashMap()
                        notificationData.put(Common.NOTI_TITLE, Common.REQUEST_TRACTOR_ACCEPT)
                        notificationData.put(Common.NOTI_BODY, "This message represent for Request Accept action")
                        notificationData.put(Common.TRACTOR_KEY, FirebaseAuth.getInstance().currentUser!!.uid)
                        notificationData.put(Common.TRIP_KEY, tripNumberId!!)

                        val fcmSendData = FCMSendData(tokenModel!!.token,notificationData)

                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ fcmResponse ->
                                if(fcmResponse!!.success == 0)
                                {
                                    compositeDisposable.clear()
                                    Snackbar.make(view!!, requireContext.getString(R.string.accept_failed), Snackbar.LENGTH_LONG).show()

                                }
                                else
                                {

                                    Snackbar.make(view!!, requireContext.getString(R.string.accept_success), Snackbar.LENGTH_LONG).show()

                                }
                            },{t : Throwable? ->
                                compositeDisposable.clear()
                                Snackbar.make(view!!, t!!.message!!, Snackbar.LENGTH_LONG).show()
                            }))

                    }else{
                        compositeDisposable.clear()
                        Snackbar.make(view!!, requireContext.getString(R.string.token_not_found), Snackbar.LENGTH_LONG).show()
                    }
                }
            })

    }

    fun sendNotifyToBook(context: Context, view: View, key: String?) {
        val compositeDisposable = CompositeDisposable()
        val ifcmService = RetrofitFCMClient.instance!!.create(IFCMService::class.java)



        //Get Token
        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REFERENCE)
            .child(key!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Snackbar.make(view!!, error.message, Snackbar.LENGTH_LONG).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists())
                    {
                        val tokenModel = snapshot.getValue(TokenModel::class.java)
                        val notificationData : MutableMap<String, String> = HashMap()
                        notificationData.put(Common.NOTI_TITLE, context.getString(R.string.tractor_arrived))
                        notificationData.put(Common.NOTI_BODY, context.getString(R.string.your_tractor_arrived))
                        notificationData.put(Common.TRACTOR_KEY, FirebaseAuth.getInstance().currentUser!!.uid)
                        notificationData.put(Common.BOOK_KEY, key)

                        val fcmSendData = FCMSendData(tokenModel!!.token,notificationData)

                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ fcmResponse ->
                                if(fcmResponse!!.success == 0)
                                {
                                    compositeDisposable.clear()
                                    Snackbar.make(view!!, context.getString(R.string.accept_failed), Snackbar.LENGTH_LONG).show()

                                }
                                else
                                {
                                    EventBus.getDefault().postSticky(NotifyBookEvent::class.java)
                                    Snackbar.make(view!!, context.getString(R.string.accept_success), Snackbar.LENGTH_LONG).show()

                                }
                            },{t : Throwable? ->
                                compositeDisposable.clear()
                                Snackbar.make(view!!, t!!.message!!, Snackbar.LENGTH_LONG).show()
                            }))

                    }else{
                        compositeDisposable.clear()
                        Snackbar.make(view!!, context.getString(R.string.token_not_found), Snackbar.LENGTH_LONG).show()
                    }
                }
            })

    }

}