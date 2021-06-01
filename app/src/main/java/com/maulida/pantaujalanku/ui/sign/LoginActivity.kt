package com.maulida.pantaujalanku.ui.sign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.maulida.pantaujalanku.R
import com.maulida.pantaujalanku.core.preference.SetPreferences
import com.maulida.pantaujalanku.databinding.ActivityLoginBinding
import com.maulida.pantaujalanku.ui.HomeActivity


class LoginActivity : AppCompatActivity(), View.OnClickListener {

    companion object{
        const val FIELD_EMPTY = "This field is empty"
        const val TAG = "LoginActivity"
        const val RC_SIGN_IN = 1
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firestore : FirebaseFirestore
    private lateinit var sesiPreferences : SetPreferences

    private lateinit var googleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        sesiPreferences = SetPreferences(this)

        binding.btnBack.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }
            R.id.btn_login -> {
                loginUser()
            }
        }
    }

    private fun loginUser() {
        val email = binding.tvEmail.text.toString().trim()
        val password = binding.tvPassword.text.toString().trim()

        if (email.isEmpty()){
            binding.tvEmail.error = FIELD_EMPTY
            binding.tvEmail.requestFocus()
        } else if (password.isEmpty()){
            binding.tvPassword.error = FIELD_EMPTY
            binding.tvPassword.requestFocus()
        } else {
            pushLogin(email, password)
        }
    }

    private fun pushLogin(email : String, password: String) {
        firestore.collection("users")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        if(document.data.isEmpty()){
                            Toast.makeText(this, "Your user cannot be found", Toast.LENGTH_SHORT).show()
                        } else {
                            if (document.data.getValue("email") == email && document.data.getValue("password") == password) {
                                sesiPreferences.saveToPreference(SetPreferences.KEY_USERNAME, document.data.getValue("username").toString())
                                sesiPreferences.saveToPreference(SetPreferences.KEY_ID, document.id)

                                finishAffinity()
                                val intent = Intent(this, HomeActivity::class.java)
                                intent.putExtra("ID_USER", document.id)
                                startActivity(intent)
                            } else{
                                Toast.makeText(this,"Your email or password is wrong", Toast.LENGTH_SHORT).show()
                            }
                        }
                        Log.d(TAG, document.id + " => " + document.data)
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
    }

}