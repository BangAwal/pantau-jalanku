package com.maulida.pantaujalanku.ui.bottomBar.profile

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.maulida.pantaujalanku.MainActivity
import com.maulida.pantaujalanku.R
import com.maulida.pantaujalanku.core.preference.SetPreferences
import com.maulida.pantaujalanku.core.preference.UserRepository
import com.maulida.pantaujalanku.databinding.FragmentProfileBinding
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var fireStorageReference: StorageReference
    private lateinit var userRepository: UserRepository

    //atribut
    private var userId : String? = null
    private var statusAdd = false
    private lateinit var fileUri : Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        fireStorageReference = storage.reference

        userRepository = UserRepository.getInstance(SetPreferences(view.context))

        userId = arguments?.getString("ID_USER")

        binding.btnLogout.setOnClickListener {
            userRepository.logoutUser()
            startActivity(Intent(view.context, MainActivity::class.java))
            activity?.finishAffinity()
        }

        getData()

        //change photo profile
        binding.ivAddProfile.setOnClickListener {
            val intent = Intent(view.context, ChangePhotoProfileActivity::class.java)
            intent.putExtra("ID_USER", userId)
            startActivity(intent)
        }

        //change data in form
        binding.btnSaveChange.setOnClickListener {
            val username = binding.tvUsername.text.toString()
            val email = binding.tvEmail.text.toString()
            val password = binding.tvChangePass.text.toString()

            changeDataFirebase(username, email, password)
        }

        binding.btnSaveChange.setOnClickListener {
            if (fileUri != null) {
                val progressDialog = ProgressDialog(view?.context).apply {
                    setTitle("Uploading...")
                    show()
                }
                val ref = fireStorageReference.child("profile/${UUID.randomUUID()}")
                ref.putFile(this.fileUri)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(view?.context, "Change Profile Success", Toast.LENGTH_SHORT)
                            .show()

                        ref.downloadUrl.addOnSuccessListener {
                            saveToFirebase(it.toString())
                        }
                    }
                    .addOnFailureListener {
                        progressDialog.dismiss()
                        Toast.makeText(view?.context, "Cancelled...", Toast.LENGTH_SHORT).show()
                    }
                    .addOnProgressListener {
                        val progress = 100.0 * it.bytesTransferred / it.totalByteCount
                        progressDialog.setMessage("Upload ${progress.toInt()}")
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
                Toast.makeText(view?.context, "Change profile success", Toast.LENGTH_SHORT).show()
                Log.d("ProfileFragment", "success data change")
            }
            .addOnFailureListener {
                Toast.makeText(view?.context, "Failure change profile...", Toast.LENGTH_SHORT).show()
                Log.e("ProfileFragment", "Failed change data")
            }
    }

    private fun changeDataFirebase(
        username: String,
        email: String,
        password: String
    ) {
        firestore.collection("users").document(userId.toString())
            .update(mapOf(
                "username" to username,
                "email" to email,
                "password" to password
            ))
            .addOnSuccessListener {
                Toast.makeText(view?.context, "Update data success", Toast.LENGTH_SHORT).show()
                Log.d("profileFragment", "Success update data")
            }
            .addOnFailureListener {
                Toast.makeText(view?.context, "Failed Update data", Toast.LENGTH_SHORT).show()
                Log.d("profileFragment", "Failed update data")
            }
    }

    private fun getData() {
        firestore.collection("users").document(userId.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()){
                    with(binding){
                        tvUsername.setText(document.data?.getValue("username").toString())
                        tvEmail.setText(document.data?.getValue("email").toString())
                        tvChangePass.setText(document.data?.getValue("password").toString())
                        if (document.data?.getValue("photo") == null
                                || document.data?.getValue("photo") == ""){
                            binding.imgLogin.setImageResource(R.drawable.ic_baseline_account_circle_24)
                        } else {
                            Glide.with(view?.context!!)
                                    .load(document.data?.getValue("photo"))
                                    .into(imgLogin)
                        }
                    }
                }
            }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Activity.RESULT_OK){
            statusAdd = true
            fileUri = data?.data!!

            Glide.with(view?.context!!)
                .load(fileUri)
                .into(binding.imgLogin)
        } else if(requestCode == ImagePicker.RESULT_ERROR){
            Toast.makeText(view?.context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(view?.context, "Task failed...", Toast.LENGTH_SHORT).show()
        }
    }

}