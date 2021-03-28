package com.example.myapplication.individual

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.example.myapplication.Common
import com.example.myapplication.R
import com.example.myapplication.models.EventBus.TractorRequestReciever
import com.example.myapplication.models.EventBus.NotifyBookEvent
import com.example.myapplication.models.BookInfoModel
import com.example.myapplication.models.TripPlanModel
import com.example.myapplication.remote.IGoogleAPI
import com.example.myapplication.remote.RetrofitClient
import com.example.myapplication.utils.UserUtils
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.kusu.library.LoadingButton
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class TractorRentingActivity : AppCompatActivity() , OnMapReadyCallback, GeoQueryEventListener {

    // Views
    private lateinit var chip_decline : Chip
    private lateinit var layout_accept : CardView
    private lateinit var circularProgressBar : CircularProgressBar
    private lateinit var txt_estimate_time : TextView
    private lateinit var txt_estimate_distance : TextView
    private lateinit var root_layout : FrameLayout

    private lateinit var txt_rating : TextView
    private lateinit var txt_type_uber : TextView
    private lateinit var img_round : ImageView
    private lateinit var layout_start_uber : CardView
    private lateinit var txt_book_name : TextView
    private lateinit var txt_start_uber_estimate_distance : TextView
    private lateinit var txt_start_uber_estimate_time : TextView
    private lateinit var img_phone_call : ImageView
    private lateinit var btn_start_uber : LoadingButton


    private lateinit var layout_notify_book: LinearLayout
    private lateinit var txt_notify_book: TextView
    private lateinit var progress_notify: ProgressBar

    private var pickupGeoFire : GeoFire?= null
    private var pickupGeoQuery: GeoQuery?= null

    private var isTripStart = false
    private var onlineSystemAlreadyRegister = false

    private var tripNumberId: String?=""


    // Routes
    private val compositeDisposable = CompositeDisposable()
    private lateinit var iGoogleAPI : IGoogleAPI
    private var blackPolyLine : Polyline?=null
    private var greyPolyline : Polyline?=null
    private var polylineOptions: PolylineOptions?=null
    private var blackPolylineOptions: PolylineOptions?=null
    private var polylineList : ArrayList<LatLng?>?=null

    private lateinit var mMap: GoogleMap

    private lateinit var mapFragment : SupportMapFragment

    // Location
    private var locationRequest : LocationRequest?=null
    private var locationCallback : LocationCallback?=null
    private var fusedLocationProviderClient : FusedLocationProviderClient?=null

    // Online system
    private lateinit var onlineRef : DatabaseReference
    private var currentUserRef : DatabaseReference? = null
    private lateinit var tractorsLocationRef : DatabaseReference
    private lateinit var geoFire : GeoFire

    //Decline
    private var tractorRequestRecieved : TractorRequestReciever?=null
    private var countDownEvent : Disposable?=null

    private val onlineValueEventListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            Snackbar.make(mapFragment.requireView(), error.message, Snackbar.LENGTH_LONG).show()
        }

        override fun onDataChange(snapshot: DataSnapshot) {
            if(snapshot.exists() && currentUserRef != null)
            {
                currentUserRef!!.onDisconnect().removeValue()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
        geoFire.removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
        onlineRef.removeEventListener(onlineValueEventListener)

        compositeDisposable.clear()

        onlineSystemAlreadyRegister = false

        if(EventBus.getDefault().hasSubscriberForEvent(TractorRentingActivity::class.java))
            EventBus.getDefault().removeStickyEvent(TractorRentingActivity::class.java)
        if(EventBus.getDefault().hasSubscriberForEvent(NotifyBookEvent::class.java))
            EventBus.getDefault().removeStickyEvent(NotifyBookEvent::class.java)
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        registerOnlineSystem()
    }

    private fun registerOnlineSystem() {
        if(!onlineSystemAlreadyRegister)
            onlineRef.addValueEventListener(onlineValueEventListener)
        onlineSystemAlreadyRegister = true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tractor_renting)

        initViews()
        init()

        mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    private fun initViews() {
        chip_decline = findViewById(R.id.chip_decline) as Chip
        layout_accept = findViewById(R.id.layout_accept) as CardView
        circularProgressBar = findViewById(R.id.circularProgressBar) as CircularProgressBar
        txt_estimate_distance = findViewById(R.id.txt_estimate_distance) as TextView
        txt_estimate_time = findViewById(R.id.txt_estimate_time) as TextView
        root_layout = findViewById(R.id.root_layout) as FrameLayout


        txt_rating = findViewById(R.id.txt_rating) as TextView
        txt_type_uber = findViewById(R.id.txt_type_uber) as TextView
        img_round = findViewById(R.id.img_round) as ImageView
        layout_start_uber = findViewById(R.id.layout_start_uber) as CardView
        txt_book_name = findViewById(R.id.txt_rider_name) as TextView
        txt_start_uber_estimate_distance = findViewById(R.id.txt_start_uber_estimate_distance) as TextView
        txt_start_uber_estimate_time = findViewById(R.id.txt_start_uber_estimate_time) as TextView
        img_phone_call = findViewById(R.id.img_phone_call) as ImageView
        btn_start_uber = findViewById(R.id.btn_start_uber) as LoadingButton


        layout_notify_book = findViewById(R.id.layout_notify_rider) as LinearLayout
        txt_notify_book = findViewById(R.id.txt_notify_rider) as TextView
        progress_notify = findViewById(R.id.progress_notify) as ProgressBar

        //Event
        chip_decline.setOnClickListener{
            if(tractorRequestRecieved != null)
            {
                if(countDownEvent != null)
                {
                    countDownEvent!!.dispose()
                }
                chip_decline.visibility = View.GONE
                layout_accept.visibility = View.GONE
                mMap.clear()
                circularProgressBar.progress = 0f
                UserUtils.sendDeclineRequest(root_layout, Activity(), tractorRequestRecieved!!.key!!)
                tractorRequestRecieved = null

            }
        }
    }

    private fun init() {

        iGoogleAPI = RetrofitClient.instance!!.create(IGoogleAPI::class.java)

        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected")



        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Snackbar.make(root_layout, getString(R.string.permission_require), Snackbar.LENGTH_LONG).show()
            return
        }


        buildLocationRequest()
        buildLocationCallback()
        updateLocation()


    }

    private fun updateLocation() {
        if(fusedLocationProviderClient == null)
        {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Snackbar.make(root_layout, getString(R.string.permission_require), Snackbar.LENGTH_LONG).show()
                return
            }
            fusedLocationProviderClient!!.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
    }

    private fun buildLocationCallback() {
        if(locationCallback == null)
        {
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    val newPos = LatLng(locationResult!!.lastLocation.latitude, locationResult!!.lastLocation.longitude)


                    if(pickupGeoFire != null)
                    {
                        pickupGeoQuery = pickupGeoFire!!.queryAtLocation(
                            GeoLocation(locationResult.lastLocation.latitude,
                            locationResult.lastLocation.longitude), Common.MIN_RANGE_PICKUP_IN_KM)

                        pickupGeoQuery!!.addGeoQueryEventListener(this@TractorRentingActivity)
                    }

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos,18f))


                    if(!isTripStart) {
                        val geoCoder = Geocoder(this@TractorRentingActivity, Locale.getDefault())
                        val addressList: List<Address>?
                        try {
                            addressList = geoCoder.getFromLocation(
                                locationResult.lastLocation.latitude,
                                locationResult.lastLocation.longitude, 1
                            )
                            val cityName = addressList[0].locality

                            tractorsLocationRef = FirebaseDatabase.getInstance()
                                .getReference(Common.TRACTORS_LOCATION_REFERENCES)
                                .child(cityName)
                            currentUserRef = tractorsLocationRef.child(
                                FirebaseAuth.getInstance().currentUser!!.uid
                            )

                            geoFire = GeoFire(tractorsLocationRef)

                            //Update Location
                            geoFire.setLocation(
                                FirebaseAuth.getInstance().currentUser!!.uid,
                                GeoLocation(
                                    locationResult.lastLocation.latitude,
                                    locationResult.lastLocation.longitude
                                )
                            ) { key: String?, error: DatabaseError? ->
                                if (error != null) {
                                    Snackbar.make(
                                        mapFragment.requireView(),
                                        error.message,
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                }
                            }

                            registerOnlineSystem()

                        } catch (e: IOException) {
                            Snackbar.make(root_layout, e.message!!, Snackbar.LENGTH_SHORT).show()
                        }
                    }else
                    {
                        if(!TextUtils.isEmpty(tripNumberId))
                        {
                            //Update Location
                            val update_data = HashMap<String, Any>()
                            update_data["currentLat"] = locationResult.lastLocation.latitude
                            update_data["currentLng"] = locationResult.lastLocation.longitude

                            FirebaseDatabase.getInstance().getReference(Common.TRIP)
                                .child(tripNumberId!!)
                                .updateChildren(update_data)
                                .addOnFailureListener{e ->
                                    Snackbar.make(mapFragment.requireView(), e.message!!, Snackbar.LENGTH_LONG).show()
                                }
                                .addOnSuccessListener {  }
                        }
                    }


                }
            }

        }
    }

    private fun buildLocationRequest() {
        if(locationRequest == null)
        {
            locationRequest = LocationRequest()
            locationRequest!!.setPriority((LocationRequest.PRIORITY_HIGH_ACCURACY))
            locationRequest!!.setFastestInterval(15000) // 15 sec
            locationRequest!!.interval = 10000 // 10 sec
            locationRequest!!.setSmallestDisplacement(50f) // 50m
        }
    }

    override fun onMapReady(googleMap : GoogleMap?) {
        mMap = googleMap!!

        //Request Permission
        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    mMap.setOnMyLocationClickListener {
                        fusedLocationProviderClient!!.lastLocation
                            .addOnFailureListener{e ->
                                Toast.makeText(this@TractorRentingActivity, e.message, Toast.LENGTH_SHORT).show()
                            }
                            .addOnSuccessListener { location ->
                                val userLatLng = LatLng(location.latitude, location.longitude)
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f))

                            }
                        true
                    }

//                    val view = mapFragment.requireView()!!
//                        .findViewById<View>("1".toInt())!!
//                        .parent!! as View

                    val locationButton = (mapFragment.requireView()!!
                        .findViewById<View>("1".toInt())!!
                        .parent!! as View).findViewById<View>("2".toInt())
                    val params = locationButton.layoutParams as RelativeLayout.LayoutParams
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                    params.bottomMargin = 50


                    // Location
                    buildLocationRequest()
                    buildLocationCallback()
                    updateLocation()


                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {

                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(this@TractorRentingActivity, "Permission" + p0!!.permissionName +" was denied", Toast.LENGTH_SHORT).show()
                }
            })
            .check()

        try{
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this,
                R.raw.uber_maps_style))
            if(!success)
                Log.e("ERROR", "style parsing error")
        }catch (e: Resources.NotFoundException)
        {
            Log.e("ERROR",e.message.toString())
        }

        Snackbar.make(mapFragment.requireView(), "You're Online!", Snackbar.LENGTH_LONG).show()

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public fun onTractorRequestRecieved(event : TractorRequestReciever)
    {

        tractorRequestRecieved = event
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Snackbar.make(root_layout, getString(R.string.permission_require), Snackbar.LENGTH_LONG).show()
            return
        }
        fusedLocationProviderClient!!.lastLocation
            .addOnFailureListener { e ->
                Snackbar.make(root_layout, e.message!!, Snackbar.LENGTH_LONG).show()
            }
            .addOnSuccessListener { location ->
                compositeDisposable.add(iGoogleAPI.getDirections("driving",
                    "less_driving",
                    StringBuilder()
                        .append(location.latitude)
                        .append(",")
                        .append(location.longitude)
                        .toString(),
                    event.pickupLocation,
                    getString(R.string.google_api_key))!!
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { returnResult ->
                        Log.d("API_RETURN", returnResult)
                        try{
                            val jsonObject = JSONObject(returnResult)
                            val jsonArray = jsonObject.getJSONArray("routes")
                            for(i in 0 until jsonArray.length())
                            {
                                val route = jsonArray.getJSONObject(i)
                                val poly = route.getJSONObject("overview_polyline")
                                val polyline = poly.getString("points")
                                polylineList  = Common.decodePoly(polyline)
                            }


                            polylineOptions = PolylineOptions()
                            polylineOptions!!.color(Color.GRAY)
                            polylineOptions!!.width(12f)
                            polylineOptions!!.startCap(SquareCap())
                            polylineOptions!!.jointType(JointType.ROUND)
                            polylineOptions!!.addAll(polylineList)
                            greyPolyline = mMap.addPolyline(polylineOptions)

                            blackPolylineOptions = PolylineOptions()
                            blackPolylineOptions!!.color(Color.BLACK)
                            blackPolylineOptions!!.width(5f)
                            blackPolylineOptions!!.startCap(SquareCap())
                            blackPolylineOptions!!.jointType(JointType.ROUND)
                            blackPolylineOptions!!.addAll(polylineList)
                            blackPolyLine = mMap.addPolyline(blackPolylineOptions)


                            //Animator
                            val valueAnimator = ValueAnimator.ofInt(0,100)
                            valueAnimator.duration = 1100
                            valueAnimator.repeatCount = ValueAnimator.INFINITE
                            valueAnimator.interpolator = LinearInterpolator()
                            valueAnimator.addUpdateListener { value ->
                                val points = greyPolyline!!.points
                                val percentValue = value.animatedValue.toString().toInt()
                                val size = points.size
                                val newPoints = (size * (percentValue/100.0f)).toInt()
                                val p = points.subList(0, newPoints)
                                blackPolyLine!!.points = (p)
                            }

                            valueAnimator.start()

                            val origin = LatLng(location.latitude, location.longitude)
                            val destination = LatLng(event.pickupLocation!!.split(",")[0].toDouble(),
                                event.pickupLocation!!.split(",")[1].toDouble())

                            val latLngBounds = LatLngBounds.Builder().include(origin)
                                .include(destination)
                                .build()

                            // Add car icon for origin
                            val objects = jsonArray.getJSONObject(0)
                            val legs = objects.getJSONArray("legs")
                            val legsObject = legs.getJSONObject(0)

                            val time = legsObject.getJSONObject("duration")
                            val duration = time.getString("text")

                            val distanceEstimate = legsObject.getJSONObject("distance")
                            val distance = distanceEstimate.getString("text")

                            txt_estimate_time.setText(duration)
                            txt_estimate_distance.setText(distance)

                            mMap.addMarker(
                                MarkerOptions().position(destination).icon(BitmapDescriptorFactory.defaultMarker())
                                .title("Pickup Location"))

                            createGeoFirePickupLocation(event.key, destination)

                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160))
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition!!.zoom-1))

                            // Display Layout
                            chip_decline.visibility = View.VISIBLE
                            layout_accept.visibility = View.VISIBLE

                            //Countdown
                            countDownEvent = Observable.interval(100, TimeUnit.MILLISECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnNext { x ->
                                    circularProgressBar.progress += 1f
                                }
                                .takeUntil{aLong ->
                                    aLong == "100".toLong()
                                }
                                .doOnComplete{
                                    createTripPlan(event, duration, distance)
                                }.subscribe()




                        }
                        catch (e: Exception)
                        {
                            Toast.makeText(this, e.message!!, Toast.LENGTH_SHORT).show()
                        }
                    })

            }
    }

    private fun createGeoFirePickupLocation(key: String?, destination: LatLng) {
        val ref = FirebaseDatabase.getInstance()
            .getReference(Common.TRIP_PICKUP_REF)
        pickupGeoFire = GeoFire(ref)
        pickupGeoFire!!.setLocation(key, GeoLocation(destination.latitude, destination.longitude),
            {key, error ->
                if(error != null)
                    Snackbar.make(root_layout, error.message, Snackbar.LENGTH_LONG).show()
                else
                    Log.d("UBER", key + " was create success")
            })
    }

    private fun createTripPlan(event: TractorRequestReciever, duration: String, distance: String) {
        setLayoutProcess(true)

        // Sync server time with device
        FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    Snackbar.make(mapFragment.requireView()!!, error.message, Snackbar.LENGTH_LONG).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val timeOffset = snapshot.getValue(Long::class.java)

                    //Load booking information
                    FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(event!!.key!!)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onCancelled(error: DatabaseError) {
                                Snackbar.make(mapFragment.requireView()!!, error.message, Snackbar.LENGTH_LONG).show()
                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.exists())
                                {
                                    val bookInfoModel = snapshot.getValue(BookInfoModel::class.java)

                                    // Get Location
                                    if (ActivityCompat.checkSelfPermission(
                                            this@TractorRentingActivity,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                            this@TractorRentingActivity,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        Snackbar.make(mapFragment.requireView()!!, getString(R.string.permission_require)+" "+event!!.key!!, Snackbar.LENGTH_LONG).show()
                                        return
                                    }
                                    fusedLocationProviderClient!!.lastLocation
                                        .addOnFailureListener{e ->
                                            Snackbar.make(mapFragment.requireView()!!, e.message!!, Snackbar.LENGTH_LONG).show()
                                        }
                                        .addOnSuccessListener { location ->

                                            // Create Trip Planner
                                            val tripPlanModel = TripPlanModel()
                                            tripPlanModel.tractor = FirebaseAuth.getInstance().currentUser!!.uid
                                            tripPlanModel.book = event!!.key
                                            tripPlanModel.tractorInfoModel = Common.currentTractor
                                            tripPlanModel.bookInfoModel = bookInfoModel
                                            tripPlanModel.origin = event.pickupLocation
                                            tripPlanModel.originString = event.pickupLocationString
                                            tripPlanModel.destination = event.destinationLocation
                                            tripPlanModel.destinationString = event.destinationLocationString
                                            tripPlanModel.distancePickup = distance
                                            tripPlanModel.durationPickup = duration
                                            tripPlanModel.currentLat = location.latitude
                                            tripPlanModel.currentLng = location.longitude

                                            tripNumberId = Common.createUniqueTripIdNumber(timeOffset)

                                            // submit
                                            FirebaseDatabase.getInstance().getReference(Common.TRIP)
                                                .child(tripNumberId!!)
                                                .setValue(tripPlanModel)
                                                .addOnFailureListener{e ->
                                                    Snackbar.make(mapFragment.requireView()!!, e.message!!, Snackbar.LENGTH_LONG).show()
                                                }
                                                .addOnSuccessListener { aVoid ->
                                                    txt_book_name.setText(bookInfoModel!!.firstName)
                                                    txt_start_uber_estimate_distance.setText(distance)
                                                    txt_start_uber_estimate_time.setText(duration)

                                                    setOfflineModeForTractor(event, duration, distance)
                                                }
                                        }
                                }
                                else
                                {
                                    Snackbar.make(mapFragment.requireView()!!, getString(R.string.book_not_found)+" "+event!!.key!!, Snackbar.LENGTH_LONG).show()
                                }
                            }

                        })
                }
            })
    }

    private fun setOfflineModeForTractor(event: TractorRequestReciever, duration: String, distance: String) {


        UserUtils.sendAcceptRequestToBook(mapFragment.view, this, event.key!!, tripNumberId)

        // Go to offline
        if(currentUserRef != null) currentUserRef!!.removeValue()

        setLayoutProcess(false)
        layout_accept.visibility = View.GONE
        layout_start_uber.visibility = View.VISIBLE

        isTripStart = true
    }


    private fun setLayoutProcess(process: Boolean) {
        var color = -1
        if(process)
        {
            color = ContextCompat.getColor(this,R.color.dark_gray)
            circularProgressBar.indeterminateMode = true
            txt_rating.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_baseline_star_24_dark_gray, 0)
        }
        else
        {
            color = ContextCompat.getColor(this,R.color.white)
            circularProgressBar.indeterminateMode = false
            circularProgressBar.progress = 0f
            txt_rating.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_baseline_star_24, 0)
        }

        txt_estimate_time.setTextColor(color)
        txt_estimate_distance.setTextColor(color)
        txt_rating.setTextColor(color)
        txt_type_uber.setTextColor(color)
        ImageViewCompat.setImageTintList(img_round, ColorStateList.valueOf(color))
    }

    override fun onGeoQueryReady() {

    }

    override fun onKeyEntered(key: String?, location: GeoLocation?) {
        btn_start_uber.isEnabled = true
        UserUtils.sendNotifyToBook(this, root_layout, key)
        if(pickupGeoQuery != null)
        {
            // Remove
            pickupGeoFire!!.removeLocation(key)
            pickupGeoFire = null
            pickupGeoQuery!!.removeAllListeners()
        }
    }

    override fun onKeyMoved(key: String?, location: GeoLocation?) {

    }

    override fun onKeyExited(key: String?) {
        btn_start_uber.isEnabled = false
    }

    override fun onGeoQueryError(error: DatabaseError?) {

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onNotifyBook(event: NotifyBookEvent)
    {
        layout_notify_book!!.visibility = View.VISIBLE
        progress_notify.max = Common.WAIT_TIME_IN_MIN * 60
        val countDownTimer = object : CountDownTimer((progress_notify.max * 1000).toLong(), 1000){
            override fun onFinish() {
                Snackbar.make(root_layout, getString(R.string.time_over), Snackbar.LENGTH_LONG).show()
            }

            override fun onTick(l: Long) {
                progress_notify.progress += 1

                txt_notify_book.text = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(1) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(1)),
                    TimeUnit.MILLISECONDS.toSeconds(1) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(1)))
            }
        }.start()
    }
}