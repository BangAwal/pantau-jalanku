package com.maulida.pantaujalanku.ui.bottomBar.report

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.maulida.pantaujalanku.BuildConfig.MAPS_API_KEY
import com.maulida.pantaujalanku.R
import com.maulida.pantaujalanku.core.data.ReportEntity
import com.maulida.pantaujalanku.core.data.UserEntity
import com.maulida.pantaujalanku.core.preference.SetPreferences
import com.maulida.pantaujalanku.databinding.FragmentReportBinding
import com.maulida.pantaujalanku.ui.bottomBar.map.MapActivity
import okhttp3.internal.canParseAsIpAddress
import java.security.acl.Permission
import java.util.*

class ReportFragment : Fragment() {

    private lateinit var binding: FragmentReportBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var fireStorage: StorageReference
    private lateinit var storage: FirebaseStorage
    private lateinit var document: DocumentReference
    private lateinit var sesi: SetPreferences
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    //atribut
    private var statusAdd = false
    private lateinit var filePathUri: Uri
    private lateinit var user: String
    private lateinit var placesClient: PlacesClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (activity != null) {

            Places.initialize(view.context, "${MAPS_API_KEY}")
            Places.isInitialized()

            placesClient = Places.createClient(requireContext())

            firestore = FirebaseFirestore.getInstance()
            storage = FirebaseStorage.getInstance()
            fireStorage = storage.reference

            sesi = SetPreferences(view.context)

            document = firestore.collection("users").document()

            user = arguments?.getString("ID_USER") as String
            Log.d("ReportActivity", user)

            fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())

//            getDeviceLocation()

            currentLocation()

            binding.imgUpload.setOnClickListener {
                statusAdd = true
                ImagePicker.with(this)
                    .cameraOnly()
                    .start()
            }

            binding.btnUpload.setOnClickListener {
                val location = binding.tvLocation.text.toString()
                if (location.isEmpty()) {
                    binding.tvLocation.error = "Field is empty"
                    binding.tvLocation.requestFocus()
                } else if (filePathUri != null) {
                    val progressDialog = ProgressDialog(view.context)
                    progressDialog.setTitle("Uploading...")
                    progressDialog.show()

                    val ref = fireStorage.child("potholes/" + UUID.randomUUID().toString())
                    ref.putFile(filePathUri)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Toast.makeText(view.context, "Uploaded", Toast.LENGTH_SHORT).show()

                            ref.downloadUrl.addOnSuccessListener {
                                saveToFirebase(it.toString(), location)
                            }
                        }
                        .addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(
                                view.context,
                                "Failed uploaded image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnProgressListener {
                            val progress = 100.0 * it.bytesTransferred / it.totalByteCount
                            progressDialog.setMessage("Upload ${progress.toInt()}%")
                        }
                }
            }
        }

    }


    private fun saveToFirebase(uri: String, location: String) {

        val report = ReportEntity()
        report.image = uri
        report.location = location

        firestore.collection("users/$user/report")
            .add(report)
            .addOnSuccessListener {
                Log.d("ReportActivity", "DocumentSnapshot added with ID: " + it.getId())
            }
            .addOnFailureListener {
                Log.w("ReportActivity", "Error adding document", it);
            }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            statusAdd = true
            filePathUri = data?.data!!

            Glide.with(view?.context!!)
                .load(filePathUri)
                .apply(RequestOptions().transform(RoundedCorners(50)))
                .into(binding.imgUpload)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(view?.context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(view?.context, "Task Cancelled....", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(
                view?.context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                view?.context!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            binding.tvLocation.setText(it.provider)
        }
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
                    binding.tvLocation.setText(response.placeLikelihoods[0].place.address)
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