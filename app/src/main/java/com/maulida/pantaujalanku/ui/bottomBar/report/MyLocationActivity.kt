package com.maulida.pantaujalanku.ui.bottomBar.report

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.maulida.pantaujalanku.R
import com.maulida.pantaujalanku.databinding.ActivityMapBinding
import com.maulida.pantaujalanku.databinding.ActivityMyLocationBinding
import com.maulida.pantaujalanku.ui.HomeActivity
import com.maulida.pantaujalanku.ui.bottomBar.map.MapActivity

class MyLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object{
        const val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        const val COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
        const val LOCALE_PERMISSION_REQUEST_CODE = 1234
        const val DEFAULT_ZOOM = 18f
        const val AUTOCOMPLETE_REQUEST_CODE = 1
    }

    private lateinit var mFusedLocationProviderClient : FusedLocationProviderClient
    private lateinit var mMap : GoogleMap
    private lateinit var binding : ActivityMyLocationBinding

    private var isLocatePermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getLocationPermission()
    }

    private fun initMap(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_locate) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getLocationPermission(){
        val permission = arrayOf(FINE_LOCATION, COARSE_LOCATION)

        if (ContextCompat.checkSelfPermission(this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(this, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                isLocatePermission = true
                initMap()
            } else {
                ActivityCompat.requestPermissions(this, permission, MapActivity.LOCALE_PERMISSION_REQUEST_CODE)
            }
        }else {
            ActivityCompat.requestPermissions(this, permission, MapActivity.LOCALE_PERMISSION_REQUEST_CODE)
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
            MapActivity.LOCALE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()){
                    for (i in grantResults.indices){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
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
            if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return
            }
            mMap?.isMyLocationEnabled = true
            mMap?.uiSettings?.isMyLocationButtonEnabled = true

        }
    }

    private fun getDeviceLocation(){
        Log.d("MapActivity","getDeviceLocation : getting the current devices")
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            val location = mFusedLocationProviderClient.lastLocation
            location.addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Log.d("MapActivity", "onComplete : Found location!")
                    val currentLocation = task.result

                    binding.edtAddress.setText(currentLocation.provider)
                    binding.edtLatitude.setText(currentLocation.latitude.toString())
                    binding.edtLongitude.setText(currentLocation.longitude.toString())

                    sendLocation(
                        binding.edtAddress.setText(currentLocation.provider).toString(),
                        binding.edtLatitude.setText(currentLocation.latitude.toString()).toString(),
                        binding.edtLongitude.setText(currentLocation.longitude.toString()).toString())

                    moveCamera(LatLng(currentLocation.latitude, currentLocation.longitude), MapActivity.DEFAULT_ZOOM, "My Location")
                } else {
                    Log.e("MapActivity", "onComplete : Your location is null")
                    Toast.makeText(this, "unable to get current location", Toast.LENGTH_SHORT).show()
                }
            }
        }catch (e : SecurityException){
            Log.e("getDeviceLocation", "SecurityException : " + e.message)
        }
    }

    private fun moveCamera(latLng: LatLng, zoom : Float, title : String){
        Log.d("mapActivity", "moveCamera : Moving camera to: lat: ${latLng.latitude}, long : ${latLng.longitude}")
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))

        if (!title.equals("My Location")){
            val option = MarkerOptions().position(latLng).title(title)
            mMap?.addMarker(option)
        }

    }

    private fun sendLocation(address : String?, latitude : String?, longitude : String?){

        binding.imgShare.setOnClickListener {

            val bundle = Bundle()

            bundle.putString("ADDRESS", address)
            bundle.putString("LATITUDE", latitude)
            bundle.putString("LONGITUDE", longitude)

            val reportFragment = ReportFragment()

            reportFragment.arguments = bundle

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_report, reportFragment)
                .commit()

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}