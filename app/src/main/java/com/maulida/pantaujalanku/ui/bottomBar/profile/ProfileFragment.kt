package com.maulida.pantaujalanku.ui.bottomBar.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.maulida.pantaujalanku.MainActivity
import com.maulida.pantaujalanku.R
import com.maulida.pantaujalanku.core.preference.SetPreferences
import com.maulida.pantaujalanku.core.preference.UserRepository
import com.maulida.pantaujalanku.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var fireStorageReference: StorageReference
    private lateinit var userRepository: UserRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleOptions: GoogleSignInOptions ///tambah ini
    private lateinit var googleSignInClient : GoogleSignInClient ///tambah ini

    //atribut
    private var userId : String? = null
    private var statusAdd = false
    private lateinit var fileUri : Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        fireStorageReference = storage.reference

        ///tambah ini
        googleOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .build()
        googleSignInClient = GoogleSignIn.getClient(view.context, googleOptions)
        ///

        firebaseAuth = FirebaseAuth.getInstance()

        userRepository = UserRepository.getInstance(SetPreferences(view.context))

        userId = arguments?.getString("ID_USER")

        binding.btnLogout.setOnClickListener {

            /////tambahan fix
            val alertDialogBuilder = context?.let { it1 -> AlertDialog.Builder(it1) }
            alertDialogBuilder?.setTitle("SignOut Alert!")
            alertDialogBuilder?.setMessage("Do you really want to SignOut ?")
            alertDialogBuilder?.setCancelable(false)
            alertDialogBuilder?.setPositiveButton("Yes") { _, _ ->
                googleSignInClient.signOut().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseAuth.signOut()
                        userRepository.logoutUser()
                        startActivity(Intent(view.context, MainActivity::class.java))
                        activity?.finishAffinity()
                    }
                }
            }
            alertDialogBuilder?.setNegativeButton("No") { _, _ ->
                // Do nothing
            }
            val alertDialog = alertDialogBuilder?.create()
            alertDialog?.show()
            /////

//            firebaseAuth.signOut()
//            userRepository.logoutUser()
//            startActivity(Intent(view.context, MainActivity::class.java))
//            activity?.finishAffinity()
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