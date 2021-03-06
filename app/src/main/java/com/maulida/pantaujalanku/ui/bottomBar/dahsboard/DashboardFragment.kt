package com.maulida.pantaujalanku.ui.bottomBar.dahsboard

//import com.maulida.pantaujalanku.BuildConfig.MAPS_API_KEY
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.FirebaseFirestore
import com.maulida.pantaujalanku.R
import com.maulida.pantaujalanku.core.preference.SetPreferences
import com.maulida.pantaujalanku.core.preference.UserRepository
import com.maulida.pantaujalanku.databinding.FragmentDashboardBinding
import com.maulida.pantaujalanku.ui.bottomBar.about.AboutActivity

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
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sesi = SetPreferences(view.context)
        userRepository = UserRepository.getInstance(sesi)
        firestore = FirebaseFirestore.getInstance()

        Places.initialize(view.context, "AIzaSyBJP-Tr60idUtUYhF0P8gFQ1Ya6Jd7KFw4")
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

        binding.about.setOnClickListener {
            startActivity(Intent(context, AboutActivity::class.java))
        }

        Toast.makeText(view.context, "Check your profile to update your password", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(view?.context, "Get my current location", Toast.LENGTH_SHORT).show()
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