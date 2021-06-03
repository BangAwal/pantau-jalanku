package com.maulida.pantaujalanku.ui

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.maulida.pantaujalanku.R
import com.maulida.pantaujalanku.core.preference.SetPreferences
import com.maulida.pantaujalanku.core.preference.UserRepository
import com.maulida.pantaujalanku.databinding.ActivityHomeBinding
import com.maulida.pantaujalanku.ui.bottomBar.dahsboard.DashboardFragment
import com.maulida.pantaujalanku.ui.bottomBar.map.MapActivity
import com.maulida.pantaujalanku.ui.bottomBar.profile.ProfileFragment
import com.maulida.pantaujalanku.ui.bottomBar.report.ReportFragment


class HomeActivity : AppCompatActivity(), View.OnClickListener {

    companion object{
        const val LOCALE_PERMISSION_REQUEST_CODE = 1234
        const val REQUEST_CHECK_SETTINGS = 1
    }

    private lateinit var binding: ActivityHomeBinding

    private lateinit var dashboardFragment : Fragment
    private lateinit var reportFragment : Fragment
    private lateinit var profileFragment : Fragment
    private lateinit var userRepository: UserRepository

    private var userId : String? = null
    private var username : String? = null
    private var email : String? = null
    private var isLocatePermission : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getLocationPermission()

        dashboardFragment = DashboardFragment()
        reportFragment = ReportFragment()
        profileFragment = ProfileFragment()

        userRepository = UserRepository.getInstance(SetPreferences(this))

        createLocationRequest()

        userId = userRepository.getUser("ID_USER")
        email = userRepository.getUser("EMAIL_USER")
        username = userRepository.getUser("USERNAME_USER")
        Log.d("HomeActivity", userId.toString())

        setFragment(dashboardFragment)
        setIconNavbar(binding.navHome, R.drawable.ic_baseline_home__active_24)
        setIconNavbar(binding.navProfile, R.drawable.ic_baseline_manage_accounts_white_24)
        setIconNavbar(binding.navUpload, R.drawable.ic_baseline_image_white_24)
        setIconNavbar(binding.navMaps, R.drawable.ic_baseline_location_on_white_24)


        with(binding){
            binding.navHome.setOnClickListener(this@HomeActivity)
            binding.navUpload.setOnClickListener(this@HomeActivity)
            binding.navMaps.setOnClickListener(this@HomeActivity)
            binding.navProfile.setOnClickListener(this@HomeActivity)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id){
            R.id.nav_home -> {
                setFragment(dashboardFragment)
                setIconNavbar(binding.navHome, R.drawable.ic_baseline_home__active_24)
                setIconNavbar(binding.navProfile, R.drawable.ic_baseline_manage_accounts_white_24)
                setIconNavbar(binding.navUpload, R.drawable.ic_baseline_image_white_24)
            }
            R.id.nav_upload -> {
                setFragment(reportFragment)
                setIconNavbar(binding.navUpload, R.drawable.ic_baseline_image_24)
                setIconNavbar(binding.navHome, R.drawable.ic_baseline_home_24)
                setIconNavbar(binding.navProfile, R.drawable.ic_baseline_manage_accounts_white_24)
            }
            R.id.nav_profile -> {
                setFragment(profileFragment)
                setIconNavbar(binding.navProfile, R.drawable.ic_baseline_manage_accounts_active)
                setIconNavbar(binding.navUpload, R.drawable.ic_baseline_image_white_24)
                setIconNavbar(binding.navHome, R.drawable.ic_baseline_home_24)
            }
            R.id.nav_maps -> {
//                Toast.makeText(this, "Coming soon...", Toast.LENGTH_SHORT).show()
                if (isServiceOk()) {
                    init()
                }
            }

        }
    }


    private fun setFragment(fragment: Fragment){
        val bundle = Bundle()

        bundle.putString("ID_USER", userId)
        bundle.putString("USERNAME_USER", username)
        bundle.putString("EMAIL_USER", email)

        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setIconNavbar(image: ImageView, int: Int){
        image.setImageResource(int)
    }

    private fun init() {
        binding.navMaps.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isServiceOk() : Boolean{
        val available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        if (available == ConnectionResult.SUCCESS){
            return true
        } else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            val dialog : Dialog = GoogleApiAvailability.getInstance().getErrorDialog(
                this,
                available,
                9001
            ) as Dialog
            dialog.show()
        } else {
            Toast.makeText(this, "You can't make MAP request", Toast.LENGTH_SHORT).show()
        }
        return false
    }


    private fun getLocationPermission(){
        val permission = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED){
            } else {
                ActivityCompat.requestPermissions(
                    this, permission,
                    MapActivity.LOCALE_PERMISSION_REQUEST_CODE
                )
            }
        }else {
            ActivityCompat.requestPermissions(
                this, permission,
                MapActivity.LOCALE_PERMISSION_REQUEST_CODE
            )
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
            HomeActivity.LOCALE_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    for (i in grantResults.indices) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            isLocatePermission = false
                            return
                        }
                    }
                    isLocatePermission = true
                }
            }

        }

    }

    private fun createLocationRequest() {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener(this,
            OnSuccessListener<LocationSettingsResponse> { locationSettingsResponse -> // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                Toast.makeText(
                    this, "Gps already open",
                    Toast.LENGTH_LONG
                ).show()
                Log.d("location settings", locationSettingsResponse.toString())
            })
        task.addOnFailureListener(this, OnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(
                        this,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: SendIntentException) {
                    // Ignore the error.
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Gps opened", Toast.LENGTH_SHORT).show()
                //if user allows to open gps
                Log.d("result ok", data.toString())
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(
                    this, "refused to open gps",
                    Toast.LENGTH_SHORT
                ).show()
                // in case user back press or refuses to open gps
                Log.d("result cancelled", data.toString())
            }
        }
    }
}