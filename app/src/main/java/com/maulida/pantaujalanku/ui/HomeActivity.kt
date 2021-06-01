package com.maulida.pantaujalanku.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.maulida.pantaujalanku.R
import com.maulida.pantaujalanku.databinding.ActivityHomeBinding
import com.maulida.pantaujalanku.ui.bottomBar.dahsboard.DashboardFragment
import com.maulida.pantaujalanku.ui.bottomBar.map.MapActivity
import com.maulida.pantaujalanku.ui.bottomBar.profile.ProfileFragment
import com.maulida.pantaujalanku.ui.bottomBar.report.ReportFragment

class HomeActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var dashboardFragment : Fragment
    private lateinit var reportFragment : Fragment
    private lateinit var profileFragment : Fragment

    private var userId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dashboardFragment = DashboardFragment()
        reportFragment = ReportFragment()
        profileFragment = ProfileFragment()

        userId = intent.getStringExtra("ID_USER")

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
                if (isServiceOk()){
                    init()
                }
            }

        }
    }


    private fun setFragment(fragment : Fragment){
        val bundle = Bundle()

        bundle.putString("ID_USER", userId)

        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setIconNavbar(image : ImageView, int : Int){
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
            val dialog : Dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, 9001) as Dialog
            dialog.show()
        } else {
            Toast.makeText(this, "You can't make MAP request", Toast.LENGTH_SHORT).show()
        }
        return false
    }

}