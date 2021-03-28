package com.example.myapplication

import android.animation.ValueAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.myapplication.models.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ui.IconGenerator
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


object Common {

    fun buildWelcomeMessage(): String {
        return java.lang.StringBuilder("Welcome, ")
                .append(currentUser!!.name)
                .toString()
    }

    fun showNotification(context: Context, id: Int, title: String?, body: String?, intent: Intent?) {
        var pendingIntent: PendingIntent? = null
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val NOTIFICATION_CHANNEL_ID = "tractor_booking"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, "Tractor Booking",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description = "Tractor Book"
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)
        }

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.ic_baseline_directions_car_24
                )
            )

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent!!)
        }

        val notification = builder.build()
        notificationManager.notify(id, notification)
    }

    fun buildName(firstName: String?, lastName: String?): String? {
        return StringBuilder(firstName!!).append(" ").append(lastName).toString()
    }

    fun decodePoly(encoded: String):  ArrayList<LatLng?>? {
        val poly = ArrayList<LatLng?>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(lat.toDouble() / 1E5,
                    lng.toDouble() / 1E5)
            poly.add(p)
        }
        return poly!!
    }

    fun getBearing(begin: LatLng, end: LatLng): Float {
        val lat = Math.abs(begin.latitude - end.latitude)
        val lng = Math.abs(begin.longitude - end.longitude)
        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return Math.toDegrees(Math.atan(lng / lat)).toFloat()
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 90).toFloat()
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (Math.toDegrees(Math.atan(lng / lat)) + 180).toFloat()
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (90 - Math.toDegrees(Math.atan(lng / lat)) + 270).toFloat()
        return (-1).toFloat()
    }

    fun setWelcomeMessage(txtWelcome: TextView) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if(hour >= 1 && hour <= 12)
        {
            txtWelcome.setText(java.lang.StringBuilder("Good Morning"))
        }
        else if(hour > 12 && hour <= 17)
        {
            txtWelcome.setText(java.lang.StringBuilder("Good Afternoon"))
        }else{
            txtWelcome.setText(java.lang.StringBuilder("Good Evening"))
        }
    }

    fun formatDuration(duration: String): CharSequence? {
        if(duration.contains("mins"))
            return duration.substring(0, duration.length-1) // Remove letter "s
        else
            return duration
    }

    fun formatAddress(startAddress: String): CharSequence? {
        val firstIndexComma = startAddress.indexOf(",")
        return startAddress.substring(0, firstIndexComma)
    }

    fun valueAnimate(duration: Int, listener: ValueAnimator.AnimatorUpdateListener): ValueAnimator {
        val va = ValueAnimator.ofFloat(0f, 100f)
        va.duration = duration.toLong()
        va.addUpdateListener(listener)
        va.repeatCount = ValueAnimator.INFINITE
        va.repeatMode = ValueAnimator.RESTART
        va.start()
        return va
    }

    fun createIconWithDuration(context: Context, duration: String): Bitmap? {
        val view = LayoutInflater.from(context).inflate(R.layout.pickup_info_with_duration_windows, null)
        val txt_time = view.findViewById<View>(R.id.txt_duration) as TextView
        txt_time.setText(getNumberFromText(duration!!))
        val generator = IconGenerator(context)
        generator.setContentView(view)
        generator.setBackground(ColorDrawable(Color.TRANSPARENT))
        return generator.makeIcon()
    }

    private fun getNumberFromText(s: String): String {
        return s.substring(0, s.indexOf(" "))
    }

    fun createUniqueTripIdNumber(timeOffset: Long?): String? {
        val rd = Random()
        var current = System.currentTimeMillis()+timeOffset!!
        var unique = current+rd.nextLong()
        if(unique < 0) unique *= -1
        return unique.toString()
    }

    val TRIP: String = "Trips"
    val TRIP_KEY: String = "TripKey"
    val REQUEST_TRACTOR_ACCEPT: String = "Accept"
    val DESTINATION_LOCATION: String="DestinationLocation"
    val DESTINATION_LOCATION_STRING: String="DestinationLocationString"
    val PICKUP_LOCATION_STRING: String="PickupLocationString"
    val REQUEST_TRACTOR_DECLINE: String = "Decline"
    val BOOK_KEY: String = "BookKey"
    val PICKUP_LOCATION: String="PickupLocation"
    val REQUEST_TRACTOR_TITLE: String="RequestTractor"
    val tractorsSubscribe: MutableMap<String, AnimationModel> = HashMap<String, AnimationModel>()
    val markerList: MutableMap<String, Marker> = HashMap<String,Marker>()
    val TRACTOR_INFO_REFERENCE: String = "TractorInfo"
    val tractorsFound: MutableMap<String, TractorGeoModel> = HashMap<String, TractorGeoModel>()
    val TRACTORS_LOCATION_REFERENCES: String = "TractorsLocation"
    val NOTI_BODY: String = "body"
    val NOTI_TITLE: String = "title"
    val TOKEN_REFERENCE: String = "Token"
    var currentUser: UserInfoModel? = null
    val USER_INFO_REFERENCE: String = "users"
    val WAIT_TIME_IN_MIN: Int = 1
    val MIN_RANGE_PICKUP_IN_KM: Double = 0.05
    val TRIP_PICKUP_REF: String = "TripPickupLocation"
    val BOOK_INFO: String = "Bookings"
    val TRACTOR_KEY: String="TractorKey"
    val TRACTOR_LOCATION_REFERENCE: String = "TractorsLocation"
    var currentTractor: TractorInfoModel? = null
}