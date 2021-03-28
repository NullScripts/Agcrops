package com.example.myapplication.activities

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.myapplication.Common
import com.example.myapplication.R
import com.example.myapplication.models.EventBus.DeclineRequestTractor
import com.example.myapplication.models.EventBus.TractorAcceptTripEvent
import com.example.myapplication.models.EventBus.SelectedPlaceEvents
import com.example.myapplication.models.TractorGeoModel
import com.example.myapplication.models.TripPlanModel
import com.example.myapplication.remote.IGoogleAPI
import com.example.myapplication.remote.RetrofitClient
import com.example.myapplication.utils.UserUtils

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.android.ui.IconGenerator
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_request_tractor.*
import kotlinx.android.synthetic.main.layout_confirm_pickup.*
import kotlinx.android.synthetic.main.layout_confirm_uber.*
import kotlinx.android.synthetic.main.layout_driver_info.*
import kotlinx.android.synthetic.main.layout_finding_your_driver.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.lang.Exception
import java.lang.StringBuilder

class RequestTractorActivity : AppCompatActivity(), OnMapReadyCallback {

    private var tractorOldPosition: String = ""
    private var handler: Handler? = null
    private var v = 0f
    private var lat = 0.0
    private var lng = 0.0
    private var index = 0
    private var next = 0
    private var start:LatLng?= null
    private var end:LatLng?=null


    //Spinning Animation
    var animator: ValueAnimator?=null
    private val DESIRED_NUM_OF_SPINS = 5
    private val DESIRED_SECONDS_PER_ONE_FULL_360_SPIN = 40


    //Effect
    var lastUserCircle : Circle?=null
    val duration = 100
    var lastPulseAnimator: ValueAnimator?=null


    private lateinit var mMap: GoogleMap
    private lateinit var txt_origin : TextView

    private var selectedPlaceEvents : SelectedPlaceEvents?=null

    private lateinit var mapFragment : SupportMapFragment

    //Routes
    private val compositeDisposable = CompositeDisposable()
    private lateinit var iGoogleAPI : IGoogleAPI
    private var blackPolyLine : Polyline?=null
    private var greyPolyline : Polyline?=null
    private var polylineOptions: PolylineOptions?=null
    private var blackPolylineOptions: PolylineOptions?=null
    private var polylineList : ArrayList<LatLng?>?=null
    private var originMarker: Marker?=null
    private var destinationMarker: Marker?=null

    private var lastTractorCall: TractorGeoModel?=null



    override fun onStart() {
        super.onStart()
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        compositeDisposable.clear()
        if(EventBus.getDefault().hasSubscriberForEvent(SelectedPlaceEvents::class.java))
            EventBus.getDefault().removeStickyEvent(SelectedPlaceEvents::class.java)
        if(EventBus.getDefault().hasSubscriberForEvent(DeclineRequestTractor::class.java))
            EventBus.getDefault().removeStickyEvent(DeclineRequestTractor::class.java)
        if(EventBus.getDefault().hasSubscriberForEvent(TractorAcceptTripEvent::class.java))
            EventBus.getDefault().removeStickyEvent(TractorAcceptTripEvent::class.java)
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onTractorAcceptTripEvent(event: TractorAcceptTripEvent)
    {
        Snackbar.make(main_layout, "Hello Here", Snackbar.LENGTH_LONG).show()


        FirebaseDatabase.getInstance().getReference(Common.TRIP)
                .child(event.tripId)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Snackbar.make(main_layout, error.message!!, Snackbar.LENGTH_LONG).show()
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists())
                        {
                            val tripPlanModel = snapshot.getValue(TripPlanModel::class.java)
                            mMap.clear()
                            fill_maps.visibility = View.GONE
                            if(animator != null) animator!!.end()
                            val cameraPos = CameraPosition.Builder().target(mMap.cameraPosition.target)
                                    .tilt(0f).zoom(mMap.cameraPosition.zoom).build()
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos))

                            //Get routes
                            val tractorLocation = StringBuilder()
                                    .append(tripPlanModel!!.currentLat)
                                    .append(",")
                                    .append(tripPlanModel!!.currentLng)
                                    .toString()

                            compositeDisposable.add(
                                    iGoogleAPI.getDirections("driving",
                                            "less_driving",
                                            tripPlanModel!!.origin,
                                            tractorLocation,
                                            getString(R.string.google_api_key))!!
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe{ returnResult ->
                                                var blackPolylineOptions: PolylineOptions?=null
                                                var polylineList : List<LatLng?>?=null
                                                var blackPolyline : Polyline?=null
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



                                                    blackPolylineOptions = PolylineOptions()
                                                    blackPolylineOptions!!.color(Color.BLACK)
                                                    blackPolylineOptions!!.width(5f)
                                                    blackPolylineOptions!!.startCap(SquareCap())
                                                    blackPolylineOptions!!.jointType(JointType.ROUND)
                                                    blackPolylineOptions!!.addAll(polylineList)
                                                    blackPolyline = mMap.addPolyline(blackPolylineOptions)

                                                    // Add car icon for origin
                                                    val objects = jsonArray.getJSONObject(0)
                                                    val legs = objects.getJSONArray("legs")
                                                    val legsObject = legs.getJSONObject(0)

                                                    val time = legsObject.getJSONObject("duration")
                                                    val duration = time.getString("text")


                                                    val origin = LatLng(
                                                            tripPlanModel!!.origin!!.split(",").get(0).toDouble(),
                                                            tripPlanModel!!.origin!!.split(",").get(1).toDouble()
                                                    )

                                                    val destination = LatLng(tripPlanModel.currentLat!!, tripPlanModel.currentLng!!)

                                                    val latLngBounds = LatLngBounds.Builder()
                                                            .include(origin)
                                                            .include(destination)
                                                            .build()

                                                    addPickupMarkerWithDuration(duration, origin)
                                                    addTractorMarker(destination)

                                                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160))
                                                    mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition!!.zoom-1))

                                                    initTractorForMoving(event.tripId, tripPlanModel)

                                                    confirm_uber_layout.visibility = View.GONE
                                                    confirm_pickup_layout.visibility = View.GONE
                                                    driver_info_layout.visibility = View.VISIBLE

                                                }
                                                catch (e: Exception)
                                                {
                                                    Toast.makeText(this@RequestTractorActivity, e.message!!, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                            )




                        }else{
                            Snackbar.make(main_layout, getString(R.string.trip_not_found)+event.tripId, Snackbar.LENGTH_LONG).show()
                        }
                    }
                })
    }

    private fun initTractorForMoving(tripId: String, tripPlanModel: TripPlanModel) {
        tractorOldPosition = StringBuilder().append(tripPlanModel.currentLat)
                .append(",").append(tripPlanModel.currentLng).toString()

        FirebaseDatabase.getInstance().getReference(Common.TRIP)
                .child(tripId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        Snackbar.make(main_layout, error.message, Snackbar.LENGTH_LONG).show()
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val newData = snapshot.getValue(TripPlanModel::class.java)
                        val tractorNewPosition = StringBuilder().append(newData!!.currentLat)
                                .append(",").append(newData!!.currentLng).toString()
                        if(!tractorOldPosition.equals(tractorNewPosition))
                        {
                            moveMarkerAnimation(destinationMarker!!, tractorOldPosition, tractorNewPosition)
                        }
                    }
                })
    }

    private fun moveMarkerAnimation(marker: Marker, from: String, to: String) {
        compositeDisposable.add(
                iGoogleAPI.getDirections("driving",
                        "less_driving",
                        from,
                        to,
                        getString(R.string.google_api_key))!!
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe{ returnResult ->

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



                                blackPolylineOptions = PolylineOptions()
                                blackPolylineOptions!!.color(Color.BLACK)
                                blackPolylineOptions!!.width(5f)
                                blackPolylineOptions!!.startCap(SquareCap())
                                blackPolylineOptions!!.jointType(JointType.ROUND)
                                blackPolylineOptions!!.addAll(polylineList)
                                blackPolyLine = mMap.addPolyline(blackPolylineOptions)

                                // Add car icon for origin
                                val objects = jsonArray.getJSONObject(0)
                                val legs = objects.getJSONArray("legs")
                                val legsObject = legs.getJSONObject(0)

                                val time = legsObject.getJSONObject("duration")
                                val duration = time.getString("text")


                                val bitmap = Common.createIconWithDuration(this@RequestTractorActivity, duration)
                                originMarker!!.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap))


                                // Moving
                                val runnable = object : Runnable{
                                    override fun run() {
                                        if(index < polylineList!!.size - 2)
                                        {
                                            index++
                                            next = index+1
                                            start = polylineList!![index]
                                            end = polylineList!![next]
                                        }

                                        val valueAnimator = ValueAnimator.ofInt(0,1)
                                        valueAnimator.duration = 1500
                                        valueAnimator.interpolator = LinearInterpolator()
                                        valueAnimator.addUpdateListener { valueAnimatorNew ->
                                            v = valueAnimatorNew.animatedFraction
                                            lat = v*end!!.latitude+(1-v)*start!!.latitude
                                            lng = v*end!!.longitude+(1-v)*end!!.longitude

                                            val newPos = LatLng(lat, lng)
                                            marker.position = newPos
                                            marker.setAnchor(0.5f, 0.5f)
                                            marker.rotation = Common.getBearing(start!!, newPos)
                                            mMap.moveCamera(CameraUpdateFactory.newLatLng(newPos))
                                        }
                                        valueAnimator.start()
                                        if(index < polylineList!!.size - 2) handler!!.postDelayed(this, 1500)
                                    }
                                }
                                handler = Handler()
                                index = -1
                                next = 1
                                handler!!.postDelayed(runnable, 1500)
                                tractorOldPosition = to

                            }
                            catch (e: Exception)
                            {
                                Toast.makeText(this@RequestTractorActivity, e.message!!, Toast.LENGTH_SHORT).show()
                            }
                        })
    }

    private fun addTractorMarker(destination: LatLng) {
        destinationMarker = mMap.addMarker(MarkerOptions().position(destination).flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)))
    }

    private fun addPickupMarkerWithDuration(duration: String, origin: LatLng) {

        val icon = Common.createIconWithDuration(this@RequestTractorActivity, duration)!!
        originMarker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(origin))
    }

    @Subscribe(sticky=true, threadMode = ThreadMode.MAIN)
    fun onDeclineRecieved(event : DeclineRequestTractor)
    {
        if(lastTractorCall != null)
        {
            Common.tractorsFound.get(lastTractorCall!!.key)!!.isDecline = true
            findNearbyTractor(selectedPlaceEvents!!)
        }
    }

    @Subscribe(sticky=true, threadMode = ThreadMode.MAIN)
    fun onSelectPlaceEvent(events: SelectedPlaceEvents)
    {
        selectedPlaceEvents =events
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_tractor)

        init()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun init() {
        iGoogleAPI = RetrofitClient.instance!!.create(IGoogleAPI::class.java)

        //Event
        btn_confirm_uber.setOnClickListener{
            confirm_pickup_layout.visibility = View.VISIBLE
            confirm_uber_layout.visibility = View.GONE

            setDataPickup()
        }

        btn_confirm_pickup.setOnClickListener {
            if(mMap == null) return@setOnClickListener

            // Clear Map
            mMap.clear()

            //Tilt
            val cameraPos = CameraPosition.Builder().target(selectedPlaceEvents!!.origin)
                    .tilt(45f)
                    .zoom(16f)
                    .build()

            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPos))

            // Start Animation
            addMarkerWithPulseAnimation()
        }

    }

    private fun addMarkerWithPulseAnimation() {
        confirm_pickup_layout.visibility = View.GONE
        fill_maps.visibility = View.VISIBLE

        finding_your_ride_layout.visibility = View.VISIBLE

        originMarker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker())
                .position(selectedPlaceEvents!!.origin))

        addPulsatingEffect(selectedPlaceEvents!!)


    }

    private fun addPulsatingEffect(selectedPlaceEvents: SelectedPlaceEvents) {

        if(lastPulseAnimator != null) lastPulseAnimator!!.cancel()
        if(lastUserCircle != null) lastUserCircle!!.center = selectedPlaceEvents.origin
        lastPulseAnimator = Common.valueAnimate(duration, object : ValueAnimator.AnimatorUpdateListener{
            override fun onAnimationUpdate(animation: ValueAnimator?) {
                if(lastUserCircle != null)
                    lastUserCircle!!.radius = animation!!.animatedValue.toString().toDouble()
                else
                {
                    lastUserCircle = mMap.addCircle(CircleOptions()
                            .center(selectedPlaceEvents.origin)
                            .radius(animation!!.animatedValue.toString().toDouble())
                            .strokeColor(Color.WHITE)
                            .fillColor(ContextCompat.getColor(this@RequestTractorActivity, R.color.map_darker)))

                }
            }
        })

        // Start Rotating Camera
        startMapCameraSpinningAnimation(selectedPlaceEvents)

    }

    private fun startMapCameraSpinningAnimation(selectedPlaceEvents: SelectedPlaceEvents?) {
        if(animator != null) animator!!.cancel()
        animator = ValueAnimator.ofFloat(0f, (DESIRED_NUM_OF_SPINS*360).toFloat())
        animator!!.duration = (DESIRED_SECONDS_PER_ONE_FULL_360_SPIN*1000).toLong()
        animator!!.interpolator = LinearInterpolator()
        animator!!.startDelay = (100)
        animator!!.addUpdateListener { valueAnimator ->
            val newBearingValue = valueAnimator.animatedValue as Float
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder()
                    .target(selectedPlaceEvents!!.origin)
                    .zoom(16f)
                    .tilt(45f)
                    .bearing(newBearingValue)
                    .build()
            ))
        }
        animator!!.start()

        findNearbyTractor(selectedPlaceEvents)
    }

    private fun findNearbyTractor(selectedPlaceEvents: SelectedPlaceEvents?) {
        if(Common.tractorsFound.size > 0)
        {
            var min = 0f
            var foundTractor : TractorGeoModel? = null
            val currentBookLocation = Location("")
            currentBookLocation.latitude = selectedPlaceEvents!!.origin!!.latitude
            currentBookLocation.longitude = selectedPlaceEvents!!.origin!!.longitude

            for(key in Common.tractorsFound.keys)
            {
                val tractorLocation = Location("")
                tractorLocation.latitude = Common.tractorsFound[key]!!.geoLocation!!.latitude
                tractorLocation.longitude = Common.tractorsFound[key]!!.geoLocation!!.longitude

                // First, init min value and found if first tractor in list
                if(min == 0f)
                {
                    min = tractorLocation.distanceTo(currentBookLocation)
                    if(!Common.tractorsFound[key]!!.isDecline)
                    {
                        foundTractor = Common.tractorsFound[key]
                        break // Exit from loop
                    }
                    else
                        continue

                }else if(tractorLocation.distanceTo(currentBookLocation) < min)
                {
                    min = tractorLocation.distanceTo(currentBookLocation)
                    if(!Common.tractorsFound[key]!!.isDecline)
                    {
                        foundTractor = Common.tractorsFound[key]
                        break // Exit from loop
                    }
                    else
                        continue
                }
            }



            if(foundTractor != null)
            {
                UserUtils.sendRequestToTractor(this@RequestTractorActivity,
                        main_layout,
                        foundTractor,
                        selectedPlaceEvents!!)
                lastTractorCall = foundTractor
            }
            else
            {
                Toast.makeText(this, getString(R.string.no_tractor_accept), Toast.LENGTH_LONG).show()
                lastTractorCall = null
                finish()
            }

        }else
        {
            Snackbar.make(main_layout, getString(R.string.tractors_not_found), Snackbar.LENGTH_LONG).show()
            lastTractorCall = null
            finish()
        }
    }

    override fun onDestroy() {
        if(animator != null) animator!!.end()
        super.onDestroy()
    }

    private fun setDataPickup() {

        txt_address_pickup.text = if(txt_origin != null) txt_origin.text else "None"
        mMap.clear()
        addPickupMarker()

    }

    private fun addPickupMarker() {
        val view = layoutInflater.inflate(R.layout.pickup_info_windows, null)

        val generator = IconGenerator(this)
        generator.setContentView(view)
        generator.setBackground(ColorDrawable(Color.TRANSPARENT))

        val icon = generator.makeIcon()

        originMarker = mMap.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectedPlaceEvents!!.origin)
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//        mMap.isMyLocationEnabled = true
//        mMap.uiSettings.isMyLocationButtonEnabled=true
//        mMap.setOnMyLocationClickListener {
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedPlaceEvents!!.origin, 10f))
//            true
//        }

        drawPath(selectedPlaceEvents!!)

//        //Layout Button
//        val locationButton = (mapFragment.requireView()!!
//            .findViewById<View>("1".toInt())!!.parent!! as View)
//            .findViewById<View>("2".toInt())
//
//        val params = locationButton.layoutParams as RelativeLayout.LayoutParams
//        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
//        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
//        params.bottomMargin = 250 // Move to see zoom control

        mMap.uiSettings.isZoomControlsEnabled = true

        try{
            val sucess = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,
                    R.raw.uber_maps_style))
            if(!sucess)
                Snackbar.make(mapFragment.requireView(), "Load Map Style Failed", Snackbar.LENGTH_LONG).show()
        }catch(e:Exception)
        {
            Snackbar.make(mapFragment.requireView(), e.message!!, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun drawPath(selectedPlaceEvents: SelectedPlaceEvents) {
        //Request API
        compositeDisposable.add(iGoogleAPI.getDirections("driving",
                "less_driving",
                selectedPlaceEvents.originString, selectedPlaceEvents.destinationString,
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

                        val latLngBounds = LatLngBounds.Builder().include(selectedPlaceEvents.origin)
                                .include(selectedPlaceEvents.destination)
                                .build()

                        // Add car icon for origin
                        val objects = jsonArray.getJSONObject(0)
                        val legs = objects.getJSONArray("legs")
                        val legsObject = legs.getJSONObject(0)

                        val time = legsObject.getJSONObject("duration")
                        val duration = time.getString("text")

                        val start_address = legsObject.getString("start_address")
                        val end_address = legsObject.getString("end_address")

                        addOriginMarker(duration, start_address)

                        addDestinationMarker(end_address)

                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160))
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.cameraPosition!!.zoom-1))




                    }
                    catch (e: Exception)
                    {
                        Toast.makeText(this, e.message!!, Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun addDestinationMarker(endAddress: String) {
        val view = layoutInflater.inflate(R.layout.destination_info_windows, null)

        val txt_destination = view.findViewById<View>(R.id.txt_destination) as TextView
        txt_destination.text = Common.formatAddress(endAddress)

        val generator = IconGenerator(this)
        generator.setContentView(view)
        generator.setBackground(ColorDrawable(Color.TRANSPARENT))

        val icon = generator.makeIcon()

        destinationMarker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectedPlaceEvents!!.destination))

    }

    private fun addOriginMarker(duration: String, startAddress: String) {
        val view = layoutInflater.inflate(R.layout.origin_info_windows, null)

        val txt_time = view.findViewById<View>(R.id.txt_time) as TextView
        txt_origin = view.findViewById<View>(R.id.txt_origin) as TextView

        txt_time.text = Common.formatDuration(duration)
        txt_origin.text = Common.formatAddress(startAddress)

        val generator = IconGenerator(this)
        generator.setContentView(view)
        generator.setBackground(ColorDrawable(Color.TRANSPARENT))

        val icon = generator.makeIcon()

        originMarker = mMap.addMarker(MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectedPlaceEvents!!.origin))
    }
}