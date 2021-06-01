package com.maulida.pantaujalanku.ui.sign

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.maulida.pantaujalanku.databinding.ActivitySignUpPhotoBinding
import com.maulida.pantaujalanku.ui.HomeActivity

class SignUpPhotoActivity : AppCompatActivity() {

    companion object{
        const val EXTRA_EMAIL = "email"
    }

    private lateinit var binding : ActivitySignUpPhotoBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val id = intent.extras?.getString(EXTRA_EMAIL)
        Log.d("SignUpPhoto", id.toString())

        firestore = FirebaseFirestore.getInstance()

        binding.btnSkip.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

    }
}