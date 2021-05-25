package com.maulida.pantaujalanku.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.maulida.pantaujalanku.R
import com.maulida.pantaujalanku.databinding.ActivityHomeBinding
import com.maulida.pantaujalanku.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment(), View.OnClickListener {

    private lateinit var binding : FragmentDashboardBinding
    private lateinit var navbarBinding : ActivityHomeBinding

    private lateinit var reportFragment : Fragment
    private lateinit var profileFragment : Fragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//
//        if (activity != null){
//
////            reportFragment = ReportFragment()
////            profileFragment = ProfileFragment()
//
////            binding.report.setOnClickListener(this)
////            binding.map.setOnClickListener(this)
////            binding.about.setOnClickListener(this)
////            binding.profile.setOnClickListener(this)
//
//        }
    }

    override fun onClick(v: View?) {
//        when(v?.id){
//            R.id.report ->{
//                setFragment(reportFragment)
//                setIconNavbar(navbarBinding.navUpload, R.drawable.ic_baseline_image_24)
//                setIconNavbar(navbarBinding.navHome, R.drawable.ic_baseline_home_24)
//                setIconNavbar(navbarBinding.navProfile, R.drawable.ic_baseline_manage_accounts_white_24)
//            }
//            R.id.map ->{
//                Toast.makeText(v.context, "Coming soon...", Toast.LENGTH_SHORT).show()
//            }
//            R.id.about ->{
//                startActivity(Intent(v.context, AboutActivity::class.java))
//            }
//            R.id.profile -> {
//                setIconNavbar(navbarBinding.navUpload, R.drawable.ic_baseline_image_white_24)
//                setIconNavbar(navbarBinding.navHome, R.drawable.ic_baseline_home_24)
//                setIconNavbar(navbarBinding.navProfile, R.drawable.ic_baseline_manage_accounts_active)
//            }
//        }
    }

//    private fun setFragment(fragment : Fragment){
//        fragmentManager?.beginTransaction()
//                ?.replace(R.id.fragment_container, fragment)
//                ?.commit()
//    }
//
//    private fun setIconNavbar(image : ImageView, int : Int){
//        image.setImageResource(int)
//    }

}
