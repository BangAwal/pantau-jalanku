package com.maulida.pantaujalanku.ui.bottomBar.report

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.maulida.pantaujalanku.BuildConfig.MAPS_API_KEY
import com.maulida.pantaujalanku.core.data.ReportEntity
import com.maulida.pantaujalanku.databinding.FragmentReportBinding
//import com.maulida.pantaujalanku.ml.Model
import org.checkerframework.checker.nullness.qual.NonNull
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.nio.ByteBuffer
import java.util.*

class ReportFragment : Fragment() {

    companion object{
        const val TAG = "potholeResult"
    }

    private lateinit var binding: FragmentReportBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var fireStorage: StorageReference
    private lateinit var storage: FirebaseStorage
    private lateinit var document: DocumentReference
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
//    private lateinit var model : Model

    //atribut
    private var statusAdd = false
    private lateinit var filePathUri: Uri
    private lateinit var user: String
    private lateinit var username: String
    private lateinit var placesClient: PlacesClient
    private lateinit var bitmap : Bitmap


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

//            model = Model.newInstance(view.context)

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(view.context)

            Places.initialize(view.context, "${MAPS_API_KEY}")
            Places.isInitialized()

            placesClient = Places.createClient(requireContext())

            firestore = FirebaseFirestore.getInstance()
            storage = FirebaseStorage.getInstance()
            fireStorage = storage.reference


            document = firestore.collection("users").document()

            user = arguments?.getString("ID_USER") as String
            username = arguments?.getString("USERNAME_USER") as String
            Log.d("ReportActivity", user)

            binding.tvHai.text = "Hai, $username"
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

                    val resized = Bitmap.createScaledBitmap(bitmap, 448, 448, true)
                    val tBuffer = TensorImage.fromBitmap(resized)
                    var byteBuffer = tBuffer.buffer

//                    val image = TensorBuffer.createFixedSize(intArrayOf(1, 448, 448, 3), DataType.UINT8)
//                    image.loadBuffer(byteBuffer)
//
//                    val options = ObjectDetector.ObjectDetectorOptions.builder()
//                        .setMaxResults(5)
//                        .setScoreThreshold(0.5f)
//                        .build()
//                    val detector = ObjectDetector.createFromFileAndOptions(
//                        view.context, // the application context
//                        "model.tflite", // must be same as the filename in assets folder
//                        options
//                    )
//
//                    val result = detector.detect(tBuffer)

                    for ((i, obj) in result.withIndex()) {
                        val box = obj.boundingBox

                        Log.d(TAG, "Detected object: ${i} ")
                        Log.d(TAG, "boundingBox: (${box.left}, ${box.top}) - (${box.right},${box.bottom})")

                        for ((j, category) in obj.categories.withIndex()) {
                            Log.d(TAG, "    Label $j: ${category.label}")
                            val confidence: Int = category.score.times(100).toInt()
                            Log.d(TAG, "    Confidence: ${confidence}%")
                        }
                    }


//                    val outputs = model.process(image)
//                    val score = outputs.scoreAsTensorBuffer
//                    val category = outputs.categoryAsTensorBuffer
//                    val loca = outputs.locationAsTensorBuffer
//                    val number = outputs.numberOfDetectionsAsTensorBuffer
//                    Log.d("resultPothole", "number : ${number.floatArray[0]}, location : ${loca.floatArray[0]}, score : ${score.floatArray[0]}, ${category.floatArray[0]}")
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

    private fun runObjectDetection(bitmap: Bitmap){
//        val image = TensorBuffer.createFixedSize(intArrayOf(1, 448, 448, 3), DataType.UINT8)
        val image = TensorImage.fromBitmap(bitmap)
//        image.loadBuffer()

        val options = ObjectDetector.ObjectDetectorOptions.builder()
            .setMaxResults(5)
            .setScoreThreshold(0.5f)
            .build()
        val detector = ObjectDetector.createFromFileAndOptions(
            view?.context, // the application context
            "model.tflite", // must be same as the filename in assets folder
            options
        )

        val result = detector.detect(image)
    }

    private fun debugPrint(results : List<Detection>) {
        for ((i, obj) in results.withIndex()) {
            val box = obj.boundingBox

            Log.d(TAG, "Detected object: ${i} ")
            Log.d(TAG, "  boundingBox: (${box.left}, ${box.top}) - (${box.right},${box.bottom})")

            for ((j, category) in obj.categories.withIndex()) {
                Log.d(TAG, "    Label $j: ${category.label}")
                val confidence: Int = category.score.times(100).toInt()
                Log.d(TAG, "    Confidence: ${confidence}%")
            }
        }
    }
//
//    private fun getMax(arr : FloatArray) : Int{
//        var ind = 0
//        var min = 0.0f
//
//        for (i in 0..100){
//            if (arr[i] > min){
//                min = arr[i]
//                ind = i
//            }
//        }
//        return ind
//    }


    private fun saveToFirebase(uri: String, location: String) {

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
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { task ->

                val report = ReportEntity()
                report.id = user
                report.image = uri
                report.location = location
                report.latitude = task.latitude
                report.longitude = task.longitude

                firestore.collection("report")
                    .add(report)
                    .addOnSuccessListener {
                        Log.d("ReportFragment", "get latitude : ${task.latitude}, longitude : ${task.longitude}")
                    }
                    .addOnFailureListener {
                        Log.e("ReportFragment", "Failed update data")
                    }
            }
    }

    @Suppress("DEPRECATION", "CAST_NEVER_SUCCEEDS")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            statusAdd = true
            filePathUri = data?.data!!


            bitmap = MediaStore.Images.Media.getBitmap(view?.context?.contentResolver, filePathUri)

            Glide.with(view?.context!!)
                .load(filePathUri)
                .apply(RequestOptions().transform(RoundedCorners(100)))
                .into(binding.imgUpload)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(view?.context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(view?.context, "Task Cancelled....", Toast.LENGTH_SHORT).show()
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