package com.example.myapplication.ui.book

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.Common
import com.example.myapplication.R
import com.example.myapplication.activities.RequestTractorActivity
import com.example.myapplication.callback.FirebaseTractorInfoListener
import com.example.myapplication.callback.FirebaseFailedListener
import com.example.myapplication.models.AnimationModel
import com.example.myapplication.models.EventBus.SelectedPlaceEvents
import com.example.myapplication.models.GeoQueryModel
import com.example.myapplication.models.TractorGeoModel
import com.example.myapplication.models.TractorInfoModel
import com.example.myapplication.remote.IGoogleAPI
import com.example.myapplication.remote.RetrofitClient
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_book.view.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class BookFragment : Fragment(), OnMapReadyCallback, FirebaseTractorInfoListener {

    private lateinit var bookViewModel: BookViewModel

    private lateinit var mMap: GoogleMap

    private lateinit var mapFragment : SupportMapFragment

    private lateinit var slidingUpPanelLayout : SlidingUpPanelLayout
    private lateinit var txt_welcome: TextView
    private lateinit var autocompleteSupportMapFragment: AutocompleteSupportFragment

    // Location
    var locationRequest : LocationRequest?=null
    var locationCallback : LocationCallback?=null
    var fusedLocationProviderClient : FusedLocationProviderClient?=null

    // Load Tractor
    var distance = 1.0
    val LIMIT_RANGE = 10.0
    var previousLocation : Location? = null
    var currentLocation : Location? = null

    var firstTime = true

    // Listener
    lateinit var iFirebaseTractorInfoListener : FirebaseTractorInfoListener
    lateinit var iFirebaseFailedListener : FirebaseFailedListener

    var cityName = ""

    //
    val compositeDisposable = CompositeDisposable()
    lateinit var iGoogleApi : IGoogleAPI



    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()

    }

    override fun onDestroy() {
        if(fusedLocationProviderClient != null)
            fusedLocationProviderClient!!.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        bookViewModel =
                ViewModelProvider(this).get(BookViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_book, container, false)

        mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        initViews(root)
        init()

        return root
    }


    private fun initViews(root: View?) {
        slidingUpPanelLayout = root!!.findViewById(R.id.activity_main) as SlidingUpPanelLayout
        txt_welcome = root!!.findViewById(R.id.txt_welcome) as TextView

        Common.setWelcomeMessage(txt_welcome)
    }

    private fun init() {

        Places.initialize(requireContext(), getString(R.string.google_api_key))
        autocompleteSupportMapFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteSupportMapFragment.setPlaceFields(Arrays.asList(Place.Field.ID,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.NAME))
        autocompleteSupportMapFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: Place) {
                if (ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Snackbar.make(requireView(), getString(R.string.permission_require), Snackbar.LENGTH_LONG).show()
                    return
                }
                fusedLocationProviderClient!!
                        .lastLocation.addOnSuccessListener { location ->
                            val origin = LatLng(location.latitude, location.longitude)
                            val destination = LatLng(p0.latLng!!.latitude, p0.latLng!!.longitude)

                            startActivity(Intent(requireContext(), RequestTractorActivity::class.java))
                            EventBus.getDefault().postSticky(SelectedPlaceEvents(origin, destination))
                        }
            }

            override fun onError(p0: Status) {
                Snackbar.make(requireView(), ""+p0.statusMessage!!, Snackbar.LENGTH_LONG).show()
            }

        })

        iGoogleApi = RetrofitClient.instance!!.create(IGoogleAPI::class.java)


        iFirebaseTractorInfoListener = this

        if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            Snackbar.make(mapFragment.requireView(), getString(R.string.permission_require), Snackbar.LENGTH_LONG).show()
            return
        }

        buildLocationRequest()
        buildLocationCallback()
        updateLocation()

        loadAvailableTractors()
    }

    private fun updateLocation() {
        if(fusedLocationProviderClient == null)
        {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
            if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
//            Snackbar.make(requireView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show()
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
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos,18f))

                    // if use has change location, calculate and load tractor again
                    if(firstTime)
                    {
                        previousLocation = locationResult.lastLocation
                        currentLocation = locationResult.lastLocation

                        setRestrictPlacesInCountry(locationResult!!.lastLocation)

                        firstTime = false
                    }
                    else{
                        previousLocation = currentLocation
                        currentLocation = locationResult.lastLocation
                    }

                    if(previousLocation!!.distanceTo(currentLocation)/1000 <= LIMIT_RANGE)
                        loadAvailableTractors()

                }
            }

        }
    }

    private fun buildLocationRequest() {
        if(locationRequest == null)
        {
            locationRequest = LocationRequest()
            locationRequest!!.setPriority((LocationRequest.PRIORITY_HIGH_ACCURACY))
            locationRequest!!.setFastestInterval(3000)
            locationRequest!!.interval = 5000
            locationRequest!!.setSmallestDisplacement(10f)
        }
    }

    private fun setRestrictPlacesInCountry(location: Location?) {
        try{
            val geoCoder = Geocoder(requireContext(), Locale.getDefault())
            var addressList  = geoCoder.getFromLocation(location!!.latitude, location!!.longitude, 1)
            if(addressList.size >0)
                autocompleteSupportMapFragment.setCountry(addressList[0].countryCode)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    private fun loadAvailableTractors() {
        if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            Snackbar.make(requireView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show()
            return
        }
        fusedLocationProviderClient!!.lastLocation
                .addOnFailureListener { e ->
                    Snackbar.make(requireView(), e.message!!, Snackbar.LENGTH_SHORT).show()
                }
                .addOnSuccessListener { location ->
                    // Load all tractors in city
                    val geoCoder = Geocoder(requireContext(), Locale.getDefault())
                    var addressList : List<Address> = ArrayList()
                    try {
                        addressList = geoCoder.getFromLocation(location!!.latitude, location!!.longitude, 1)
                        if (addressList.size > 0)
                            cityName = addressList[0].locality

                        //Query
                        if (!TextUtils.isEmpty(cityName))
                        {
                            val tractors_location_ref = FirebaseDatabase.getInstance()
                                    .getReference(Common.TRACTORS_LOCATION_REFERENCES)
                                    .child(cityName)
                            val gf = GeoFire(tractors_location_ref)
                            val geoQuery = gf.queryAtLocation(
                                    GeoLocation(location.latitude, location.longitude),
                                    distance
                            )
                            geoQuery.removeAllListeners()

                            geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
                                override fun onGeoQueryReady() {
                                    if (distance <= LIMIT_RANGE) {
                                        distance++
                                        loadAvailableTractors()
                                    } else {
                                        distance = 0.0
                                        addTractorMarker()
                                    }
                                }

                                override fun onKeyEntered(key: String?, location: GeoLocation?) {

                                    if(!Common.tractorsFound.containsKey(key))
                                        Common.tractorsFound[key!!] = TractorGeoModel(key!!, location!!)
                                }

                                override fun onKeyMoved(key: String?, location: GeoLocation?) {

                                }

                                override fun onKeyExited(key: String?) {

                                }

                                override fun onGeoQueryError(error: DatabaseError?) {
                                    Snackbar.make(requireView(), error!!.message, Snackbar.LENGTH_SHORT)
                                            .show()
                                }
                            })

                            tractors_location_ref.addChildEventListener(object : ChildEventListener {
                                override fun onCancelled(error: DatabaseError) {
                                    Snackbar.make(requireView(), error.message, Snackbar.LENGTH_SHORT)
                                            .show()
                                }

                                override fun onChildMoved(
                                        snapshot: DataSnapshot,
                                        previousChildName: String?
                                ) {

                                }

                                override fun onChildChanged(
                                        snapshot: DataSnapshot,
                                        previousChildName: String?
                                ) {

                                }

                                override fun onChildAdded(
                                        snapshot: DataSnapshot,
                                        previousChildName: String?
                                ) {
                                    // Have a new Tractor
                                    val geoQueryModel = snapshot.getValue(GeoQueryModel::class.java)
                                    val geoLocation =
                                            GeoLocation(geoQueryModel!!.l!![0], geoQueryModel!!.l!![1])
                                    val tractorGeoModel = TractorGeoModel(snapshot.key, geoLocation)
                                    val newTractorLocation = Location("")
                                    newTractorLocation.latitude = geoLocation.latitude
                                    newTractorLocation.longitude = geoLocation.longitude
                                    val newDistance = location.distanceTo(newTractorLocation) / 1000
                                    if (newDistance <= LIMIT_RANGE) {
                                        findTractorByKey(tractorGeoModel)
                                    }
                                }


                                override fun onChildRemoved(snapshot: DataSnapshot) {

                                }

                            })
                        }else{
                            Snackbar.make(requireView(), getString(R.string.city_name_not_found), Snackbar.LENGTH_SHORT).show()
                        }
                    }
                    catch(e: IOException)
                    {
                        Snackbar.make(requireView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show()
                    }

                }
    }

    private fun addTractorMarker() {

        if(Common.tractorsFound.size > 0)
        {
            Observable.fromIterable(Common.tractorsFound.keys)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {key: String? ->
                                findTractorByKey(Common.tractorsFound[key!!])
                            },
                            {
                                t: Throwable? -> Snackbar.make(requireView(), t!!.message!!, Snackbar.LENGTH_SHORT).show()
                            }
                    )
        }
        else
        {
            Snackbar.make(requireView(), getString(R.string.tractors_not_found), Snackbar.LENGTH_SHORT).show()

        }
    }

    private fun findTractorByKey(tractorGeoModel: TractorGeoModel?) {
        FirebaseDatabase.getInstance()
                .getReference(Common.TRACTOR_INFO_REFERENCE)
                .child(tractorGeoModel!!.key!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        iFirebaseFailedListener.onFirebaseFailed(error.message)
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.hasChildren())
                        {
                            tractorGeoModel.tractorInfoModel = (snapshot.getValue(TractorInfoModel::class.java))
                            Common.tractorsFound[tractorGeoModel.key!!]!!.tractorInfoModel = (snapshot.getValue(TractorInfoModel::class.java))
                            iFirebaseTractorInfoListener.onTractorInfoLoadSuccess(tractorGeoModel)
                        }
                        else{
                            iFirebaseFailedListener.onFirebaseFailed(getString(R.string.key_not_found)+tractorGeoModel)
                        }
                    }
                })
    }

    override fun onMapReady(googleMap : GoogleMap?) {
        mMap = googleMap!!

        //Request Permission
        Dexter.withContext(requireContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        if(fusedLocationProviderClient == null)
                        {
                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
                            if (ActivityCompat.checkSelfPermission(
                                            requireContext(),
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                                            requireContext(),
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                    ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                //Snackbar.make(requireView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show()
                            }
                                return
                            }
                        mMap.isMyLocationEnabled = true
                        mMap.uiSettings.isMyLocationButtonEnabled = true
                        mMap.setOnMyLocationClickListener {
                            fusedLocationProviderClient!!.lastLocation
                                    .addOnFailureListener{e ->
                                        Toast.makeText(context!!, e.message, Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnSuccessListener { location ->
                                        val userLatLng = LatLng(location.latitude, location.longitude)
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f))

                                    }
                            true
                        }

                        val locationButton = (mapFragment.requireView()!!
                                .findViewById<View>("1".toInt())!!
                                .parent!! as View).findViewById<View>("2".toInt())
                        val params = locationButton.layoutParams as RelativeLayout.LayoutParams
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                        params.bottomMargin = 250


                        //Update Location
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
                        Toast.makeText(context!!, "Permission" + p0!!.permissionName +" was denied", Toast.LENGTH_SHORT).show()
                    }
                }).check()

        //Enable Zoom
        mMap.uiSettings.isZoomControlsEnabled = true

        try{
            val success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(context,
                            R.raw.uber_maps_style))
            if(!success)
                Log.e("ERROR", "style parsing error")
        }catch (e: Resources.NotFoundException)
        {
            Log.e("ERROR",e.message.toString())
        }
    }

    override fun onTractorInfoLoadSuccess(tractorGeoModel: TractorGeoModel?) {
        // If already have marked with this key, doesnt set again
        if(!Common.markerList.containsKey(tractorGeoModel!!.key))
            Common.markerList.put(tractorGeoModel!!.key!!,
                    mMap.addMarker(MarkerOptions()
                            .position(LatLng(tractorGeoModel!!.geoLocation!!.latitude, tractorGeoModel!!.geoLocation!!.longitude))
                            .flat(true)
                            .title(Common.buildName(tractorGeoModel.tractorInfoModel!!.ownerFirstName, tractorGeoModel.tractorInfoModel!!.ownerLastName))
                            .snippet(tractorGeoModel.tractorInfoModel!!.phoneNumber)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.tractor_small))
                    ))

        if(!TextUtils.isEmpty(cityName))
        {
            val tractorLocation = FirebaseDatabase.getInstance()
                    .getReference(Common.TRACTORS_LOCATION_REFERENCES)
                    .child(cityName)
                    .child(tractorGeoModel!!.key!!)
            tractorLocation.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Snackbar.make(requireView(), error.message, Snackbar.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(!snapshot.hasChildren())
                    {
                        if(Common.markerList.get(tractorGeoModel!!.key!!) != null)
                        {
                            val marker = Common.markerList.get(tractorGeoModel!!.key!!)
                            marker!!.remove()
                            Common.markerList.remove(tractorGeoModel!!.key!!) // Remove Marker Information
                            Common.tractorsSubscribe.remove(tractorGeoModel.key!!) // Remove Tractor information

                            if(Common.tractorsFound != null && Common.tractorsFound[tractorGeoModel!!.key!!] != null)
                                Common.tractorsFound.remove(tractorGeoModel!!.key!!)

                            tractorLocation.removeEventListener(this)
                        }
                    }
                    else{
                        if(Common.markerList.get(tractorGeoModel!!.key!!) != null)
                        {
                            val geoQueryModel = snapshot!!.getValue(GeoQueryModel::class.java)
                            val animationModel = AnimationModel(false, geoQueryModel!!)
                            if(Common.tractorsSubscribe.get(tractorGeoModel.key!!) != null)
                            {
                                val marker = Common.markerList.get(tractorGeoModel!!.key!!)
                                val oldPosition = Common.tractorsSubscribe.get(tractorGeoModel!!.key!!)

                                val from = StringBuilder()
                                        .append(oldPosition!!.geoQueryModel!!.l?.get(0))
                                        .append(",")
                                        .append(oldPosition!!.geoQueryModel!!.l?.get(1))
                                        .toString()

                                val to = StringBuilder()
                                        .append(animationModel.geoQueryModel!!.l?.get(0))
                                        .append(",")
                                        .append(animationModel!!.geoQueryModel!!.l?.get(1))
                                        .toString()


                                moveMarkerAnimation(tractorGeoModel.key!!, animationModel, marker, from, to)
                            }
                            else
                            {
                                Common.tractorsSubscribe.put(tractorGeoModel.key!!, animationModel) // First Location Init


                            }
                        }
                    }

                }

            })
        }

    }

    private fun moveMarkerAnimation(key: String, newData: AnimationModel, marker: Marker?, from: String, to: String) {

        if(!newData.isRun)
        {
            //Request API
            compositeDisposable.add(iGoogleApi.getDirections("driving",
                    "less_driving",
                    from, to,
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
                                newData.polylineList  = Common.decodePoly(polyline)
                            }

                            // Moving
                            newData.index = -1
                            newData.next = 1

                            val runnable = object : Runnable{
                                override fun run() {
                                    if(newData.polylineList != null && newData.polylineList!!.size > 1)
                                    {
                                        if(newData.index < newData.polylineList!!.size -2)
                                        {
                                            newData.index++
                                            newData.next = newData.index+1
                                            newData.start = newData.polylineList!![newData.index]!!
                                            newData.end = newData.polylineList!![newData.next]!!

                                        }

                                        val valueAnimator = ValueAnimator.ofInt(0,1)
                                        valueAnimator.duration = 3000
                                        valueAnimator.interpolator = LinearInterpolator()
                                        valueAnimator.addUpdateListener { value ->
                                            newData.v = value.animatedFraction
                                            newData.lat = newData.v*newData.end!!.latitude + (1-newData.v) *newData.start!!.latitude
                                            newData.lng = newData.v*newData.end!!.longitude + (1-newData.v)*newData.start!!.longitude
                                            val newPos = LatLng(newData.lat, newData.lng)
                                            marker!!.position = newPos
                                            marker!!.setAnchor(0.5f, 0.5f)
                                            marker!!.rotation = Common.getBearing(newData.start!!, newPos)

                                        }
                                        valueAnimator.start()
                                        if(newData.index < newData.polylineList!!.size -2)
                                            newData.handler!!.postDelayed(this, 1500)
                                        else if(newData.index < newData.polylineList!!.size -1)
                                        {
                                            newData.isRun = false
                                            Common.tractorsSubscribe.put(key, newData)
                                        }

                                    }
                                }

                            }

                            newData.handler!!.postDelayed(runnable, 1500)
                        }catch (e: Exception)
                        {
                            Snackbar.make(requireView(), e.message!!, Snackbar.LENGTH_LONG).show()
                        }
                    })
        }
    }
}