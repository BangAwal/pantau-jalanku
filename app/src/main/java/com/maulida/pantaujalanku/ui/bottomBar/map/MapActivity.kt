package com.maulida.pantaujalanku.ui.bottomBar.map

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.constant.TransportMode
import com.akexorcist.googledirection.model.Route
import com.akexorcist.googledirection.util.DirectionConverter
import com.akexorcist.googledirection.util.execute
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.firestore.FirebaseFirestore
//import com.maulida.pantaujalanku.BuildConfig.MAPS_API_KEY
import com.maulida.pantaujalanku.R
import com.maulida.pantaujalanku.databinding.ActivityMapBinding
import java.io.IOException

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object{
        const val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        const val COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
        const val LOCALE_PERMISSION_REQUEST_CODE = 1234
        const val DEFAULT_ZOOM = 18f
        const val AUTOCOMPLETE_REQUEST_CODE = 1
    }

    private lateinit var mFusedLocationProviderClient : FusedLocationProviderClient
    private lateinit var mMap : GoogleMap
    private lateinit var binding : ActivityMapBinding
    private lateinit var firestore : FirebaseFirestore
    private lateinit var polylineOptions: PolylineOptions

    private var isLocatePermission = false
    //private lateinit var polylinelist : List<LatLng>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Places.initialize(this, "AIzaSyBJP-Tr60idUtUYhF0P8gFQ1Ya6Jd7KFw4")
        Places.isInitialized()

        firestore = FirebaseFirestore.getInstance()

        polylineOptions = PolylineOptions()

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getLocationPermission()


    }

    private fun initMap(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getLocationPermission(){
        val permission = arrayOf(FINE_LOCATION, COARSE_LOCATION)

        if (ContextCompat.checkSelfPermission(this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                isLocatePermission = true
                initMap()
            } else {
                ActivityCompat.requestPermissions(this, permission, LOCALE_PERMISSION_REQUEST_CODE)
            }
        }else {
            ActivityCompat.requestPermissions(this, permission, LOCALE_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        isLocatePermission = false

        when(requestCode){
            LOCALE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    for (i in grantResults.indices) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            isLocatePermission = false
                            return
                        }
                    }
                    isLocatePermission = true
                    initMap()
                }
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (isLocatePermission) {
            getDeviceLocation()
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED) {
                return
            }
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = false

            //marker potholes
            firestore.collection("report")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            for (document in task.result){
                                val latitude : Double = document.data.getValue("latitude") as Double
                                val longitude : Double = document.data.getValue("longitude") as Double
                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(latitude, longitude))
                                        .title("Potholes")
                                        .icon(
                                            BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_AZURE
                                            )
                                        )
                                        .alpha(0.8f)
                                )
                            }
                        }

                    }

            init()
        }
    }

    private fun init(){

        binding.icMagnify.setOnClickListener {
            autoCompletePlaces()
        }

        binding.icGps.setOnClickListener {
            getDeviceLocation()
        }
    }

    @Suppress("DEPRECATION")
    private fun autoCompletePlaces() {
        binding.icMagnify.setOnClickListener {
            val fieldList = listOf(Place.Field.ADDRESS, Place.Field.ID, Place.Field.NAME)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(
                this
            )
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE){
            when(resultCode){
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        val geoCoder = Geocoder(this)
                        var list: List<Address> = ArrayList()
                        try {
                            list = geoCoder.getFromLocationName(place.toString(), 1)

                        } catch (e: IOException) {
                            Log.e("MapActivity", "geoLocate : IOException: ${e.message}")
                        }

                        if (list.isNotEmpty()) {
                            val address = list[0]
                            Log.d("MapActivity", "geoLocate : found location: $address")
                            moveCamera(
                                LatLng(address.latitude, address.longitude),
                                DEFAULT_ZOOM,
                                address.getAddressLine(
                                    0
                                )
                            )

                                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    return
                                }
                                val myLocation = mFusedLocationProviderClient.lastLocation
                                myLocation.addOnCompleteListener { task ->
                                    if (task.isSuccessful){
                                        val latitude = task.result.latitude
                                        val longitude = task.result.longitude
                                        GoogleDirection.withServerKey("AIzaSyBJP-Tr60idUtUYhF0P8gFQ1Ya6Jd7KFw4")
                                                .from(LatLng(latitude, longitude))
                                                .to(LatLng(address.latitude,address.longitude))
                                                .transportMode(TransportMode.DRIVING)
                                                .execute(onDirectionSuccess = {direction ->
                                                    if (direction?.isOK!!){
                                                        val route = direction.routeList[0]
                                                        val directionPosition = route.legList[0].directionPoint
                                                        mMap.addPolyline(
                                                                DirectionConverter.createPolyline(
                                                                        this,
                                                                        directionPosition,
                                                                        10,
                                                                        Color.rgb(41, 159, 255),

                                                                ))
                                                        setCameraWithCoordinatorBounds(route)
                                                        Log.d("MapActivity", direction.status)
                                                    } else {
                                                        Log.d("MapActivity", direction.status)
                                                    }
                                                },
                                                onDirectionFailure = { t ->
                                                    Log.e("MapActivity", t.message.toString())
                                                })
                                    }
                                }

                        }
                        Log.d("mapActivity", "${place.address}, ${place.id}")
//                        binding.searchInput.setText(place.address)
//                        moveCamera(LatLng(place.latLng?.latitude!!, place.latLng!!.longitude), DEFAULT_ZOOM, place.address.toString())
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.e("mapActivity", status.statusMessage.toString())
                    }
                }
            }
            return
        }
    }

    private fun setCameraWithCoordinatorBounds(route : Route){
        val southwest = route.bound.southwestCoordination.coordination
        val northeast = route.bound.northeastCoordination.coordination
        val bound = LatLngBounds(southwest, northeast)
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bound, 100))
    }

    private fun getDeviceLocation(){
        Log.d("MapActivity", "getDeviceLocation : getting the current devices")
        try {
            val location = mFusedLocationProviderClient.lastLocation
            location.addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Log.d("MapActivity", "onComplete : Found location!")
                    val currentLocation = task.result
                    moveCamera(
                        LatLng(currentLocation.latitude, currentLocation.longitude),
                        DEFAULT_ZOOM,
                        "My Location"
                    )
                } else {
                    Log.e("MapActivity", "onComplete : Your location is null")
                    Toast.makeText(this, "unable to get current location", Toast.LENGTH_SHORT).show()
                }
            }
        }catch (e: SecurityException){
            Log.e("getDeviceLocation", "SecurityException : " + e.message)
        }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float, title: String){
        Log.d(
            "mapActivity",
            "moveCamera : Moving camera to: lat: ${latLng.latitude}, long : ${latLng.longitude}"
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))

        if (title != "My Location"){
            val option = MarkerOptions().position(latLng).title(title)
            mMap.addMarker(option)
        }

    }
}