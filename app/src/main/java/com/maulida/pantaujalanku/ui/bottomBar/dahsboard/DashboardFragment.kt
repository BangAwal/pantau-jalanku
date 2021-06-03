package com.maulida.pantaujalanku.ui.bottomBar.dahsboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.FirebaseFirestore
import com.maulida.pantaujalanku.BuildConfig.MAPS_API_KEY
import com.maulida.pantaujalanku.R
import com.maulida.pantaujalanku.core.preference.SetPreferences
import com.maulida.pantaujalanku.core.preference.UserRepository
import com.maulida.pantaujalanku.databinding.ActivityHomeBinding
import com.maulida.pantaujalanku.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment(){

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var placesClient : PlacesClient

    private lateinit var sesi: SetPreferences
    private lateinit var userRepository: UserRepository

    private var userId : String? = null

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

        sesi = SetPreferences(view.context)
        userRepository = UserRepository.getInstance(sesi)
        firestore = FirebaseFirestore.getInstance()

        Places.initialize(view.context, "$MAPS_API_KEY")
        Places.isInitialized()

        placesClient = Places.createClient(view.context)

        userId = arguments?.getString("ID_USER")

        firestore.collection("users").document(userId.toString())
                .get()
                .addOnSuccessListener {
                    if (it.exists()){
                        binding.tvUsername.text = it.data?.getValue("username").toString()
                        if (it.data?.getValue("photo") == null
                                || it.data?.getValue("photo") == ""){
                            binding.imgAva.setImageResource(R.drawable.ic_baseline_account_circle_24)
                        } else {
                            Glide.with(view.context)
                                    .load(it.data?.getValue("photo"))
                                    .into(binding.imgAva)
                        }

                        Log.d("DashboardFragment", "username : ${it.data?.getValue("username")}")
                    }
                }
                .addOnFailureListener {
                    Log.e("DashboardFragment", "Failed load data")
                }

        currentLocation()
    }

    private fun currentLocation() {
        val placeFields: List<Place.Field> = listOf(Place.Field.NAME, Place.Field.ADDRESS)
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)

        if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) ==
                PackageManager.PERMISSION_GRANTED
        ) {

            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response = task.result
                    binding.tvAddress.text = response.placeLikelihoods[0].place.address
                    for (placeLikelihood in response?.placeLikelihoods ?: emptyList()) {
                        Log.i(
                                "ReportFragment",
                                "Place '${placeLikelihood.place.name}' has likelihood: ${placeLikelihood.likelihood}"
                        )
                    }
                } else {
                    val exception = task.exception
                    if (exception is ApiException) {
                        Log.e("ReportFragment", "Place not found: ${exception.statusCode}")
                    }
                }
            }
        }
    }
}