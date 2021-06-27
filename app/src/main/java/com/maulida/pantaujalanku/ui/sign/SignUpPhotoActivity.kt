package com.maulida.pantaujalanku.ui.sign

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.maulida.pantaujalanku.core.preference.SetPreferences
import com.maulida.pantaujalanku.core.preference.UserRepository
import com.maulida.pantaujalanku.databinding.ActivitySignUpPhotoBinding
import com.maulida.pantaujalanku.ui.HomeActivity
import java.util.*

class SignUpPhotoActivity : AppCompatActivity(){

//    companion object{
//        const val EXTRA_ID = "id"
//        const val EXTRA_EMAIL = "email"
//    }

    private lateinit var binding : ActivitySignUpPhotoBinding

    private lateinit var firestore: FirebaseFirestore
    private lateinit var fireStorage: StorageReference
    private lateinit var storage: FirebaseStorage
    private lateinit var userRepository: UserRepository

    //atribut
    private var statusAdd = false
    private lateinit var fileUri : Uri
    private var id : String? = null
    private var email : String? = null
    private var username : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRepository = UserRepository.getInstance(SetPreferences(this))

        id = userRepository.getUser("ID_USER")
        email = userRepository.getUser("EMAIL_USER")
        username = userRepository.getUser("USERNAME_USER")
        Log.d("SignUpPhoto", id.toString())
        Log.d("SignUpPhoto", email.toString())

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        fireStorage = storage.reference

        binding.tvEmail.text = email

        //skip button
        binding.btnSkip.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            userRepository.loginUser("ID_USER", id.toString())
            userRepository.loginUser("EMAIL_USER", email.toString())
            userRepository.loginUser("USERNAME_USER", username.toString())
            startActivity(intent)
            finish()
        }

        //button add photo profile
        binding.ivAddProfile.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .start()
        }

        //save photo profile
        binding.btnSaveImg.setOnClickListener {
            if (fileUri != null){
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Uploading...")
                progressDialog.show()

                val ref = fireStorage.child("profile/" + UUID.randomUUID().toString())
                ref.putFile(fileUri)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Upload Profile Success", Toast.LENGTH_SHORT).show()

                        ref.downloadUrl.addOnSuccessListener {
                            saveToFirebase(it.toString())
                        }
                    }
                    .addOnFailureListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Failed Upload Image", Toast.LENGTH_SHORT).show()
                    }
                    .addOnProgressListener {
                        val proses = 100.0 * it.bytesTransferred/it.totalByteCount
                        progressDialog.setMessage("Upload ${proses.toInt()}%")
                    }
            }
        }

    }

    private fun saveToFirebase(uri : String) {

        firestore.collection("users").document("$id")
                .update(mapOf(
                    "photo" to uri
                ))
                .addOnSuccessListener {
                    val intent = Intent(this, HomeActivity::class.java)
                    userRepository.loginUser("ID_USER", id.toString())
                    userRepository.loginUser("EMAIL_USER", email.toString())
                    userRepository.loginUser("USERNAME_USER", username.toString())
                    startActivity(intent)

                    finish()

                    Log.d("SignUpPhotoActivity", "photo name : $uri")
                }
                .addOnFailureListener {
                    Log.e("SignUpPhotoActivity", "Failed Photo...")
                }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK){
            statusAdd = true
            fileUri = data?.data!!

            Glide.with(this)
                .load(fileUri)
                .circleCrop()
                .into(binding.ivProfile)

            binding.btnSaveImg.visibility = View.VISIBLE
        } else if (resultCode == ImagePicker.RESULT_ERROR){
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else{
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}