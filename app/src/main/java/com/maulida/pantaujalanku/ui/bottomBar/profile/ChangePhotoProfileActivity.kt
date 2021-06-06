package com.maulida.pantaujalanku.ui.bottomBar.profile

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.maulida.pantaujalanku.R
import com.maulida.pantaujalanku.databinding.ActivityChangePhotoProfileBinding
import com.maulida.pantaujalanku.ui.HomeActivity
import java.util.*

class ChangePhotoProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityChangePhotoProfileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var fireStorageReference: StorageReference

    //atribut
    private var userId : String? = null
    private var statusAdd = false
    private lateinit var fileUri : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePhotoProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        fireStorageReference = storage.reference

        userId = intent.getStringExtra("ID_USER")
        Log.d("ChangePhotoProfile", "User ID : $userId")

        binding.btnBack.setOnClickListener{
            onBackPressed()
        }

        binding.ivAddImage.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .start()
        }

        showImage()

        binding.btnSaveChanges.setOnClickListener {
            if (fileUri != null) {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Uploading....")
                progressDialog.show()

                val ref = fireStorageReference.child("profile/" + UUID.randomUUID().toString()
                )
                ref.putFile(fileUri)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Change Photo Profile Success", Toast.LENGTH_SHORT).show()

                        ref.downloadUrl.addOnSuccessListener {
                            saveToFirebase(it.toString())
                        }
                    }
                    .addOnFailureListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                        Log.e("ChangeProfileActivity", "Change Failure")
                    }
                    .addOnProgressListener {
                        val progress = 100.0 * it.bytesTransferred/ it.totalByteCount
                        progressDialog.setMessage("Upload ${progress.toInt()}%")
                    }
            }
        }

    }

    private fun saveToFirebase(uri : String) {
        firestore.collection("users").document(userId.toString())
            .update(mapOf(
                "photo" to uri
            ))
            .addOnSuccessListener {
                Log.d("ChangePhoto", "Change Success")
            }
            .addOnFailureListener {
                Log.e("ChangePhoto", it.message.toString())
            }
    }

    private fun showImage(){
        firestore.collection("users").document(userId.toString())
            .get()
            .addOnSuccessListener {
                if (it.exists()){
                    if (it.data?.getValue("photo") == null
                        || it.data?.getValue("photo") == ""){
                        binding.imgProfile.setImageResource(R.drawable.ic_baseline_account_circle_24)
                    } else {
                        Glide.with(this)
                            .load(it.data?.getValue("photo"))
                            .circleCrop()
                            .into(binding.imgProfile)
                    }
                }
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("ID_USER", userId)
        startActivity(intent)
        finish()
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            statusAdd = true
            fileUri = data?.data!!

            Glide.with(this)
                .load(fileUri)
                .circleCrop()
                .into(binding.imgProfile)
        } else if(resultCode == ImagePicker.RESULT_ERROR){
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Failed", Toast.LENGTH_SHORT).show()
        }
    }

}