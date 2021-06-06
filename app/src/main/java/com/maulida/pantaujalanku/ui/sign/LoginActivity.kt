package com.maulida.pantaujalanku.ui.sign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.maulida.pantaujalanku.R
import com.maulida.pantaujalanku.core.data.UserEntity
import com.maulida.pantaujalanku.core.preference.SetPreferences
import com.maulida.pantaujalanku.core.preference.UserRepository
import com.maulida.pantaujalanku.databinding.ActivityLoginBinding
import com.maulida.pantaujalanku.ui.HomeActivity
import java.lang.Exception


class LoginActivity : AppCompatActivity(), View.OnClickListener {

    companion object{
        const val FIELD_EMPTY = "This field is empty"
        const val TAG = "LoginActivity"
        private const val ID_SIGN_IN = 100
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firestore : FirebaseFirestore
    private lateinit var sesiPreferences : SetPreferences
    private lateinit var userRepository: UserRepository

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleOptions: GoogleSignInOptions
    private lateinit var googleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        sesiPreferences = SetPreferences(this)
        userRepository = UserRepository.getInstance(sesiPreferences)

        googleOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleOptions)

        firebaseAuth = FirebaseAuth.getInstance()

        if (userRepository.isUserLogin()){
            checkUser()
            finishAffinity()
            startActivity(Intent(this, HomeActivity::class.java))
        }

        binding.btnBack.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.btnGoogle.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }
            R.id.btn_login -> {
                loginUser()
            }
            R.id.btn_google -> {
                Log.d(TAG, "onCreate : begin google sign in")
                val intent = googleSignInClient.signInIntent
                startActivityForResult(intent, ID_SIGN_IN)
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
                            if (document.data.getValue("password").equals(password)
                                && document.data.getValue("email").equals(email)) {
                                userRepository.loginUser("USERNAME_USER", document.data.getValue("username").toString())
                                userRepository.loginUser("ID_USER", document.id)
                                userRepository.loginUser("EMAIL_USER", email)

                                finishAffinity()
                                val intent = Intent(this, HomeActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "Email or Password is wrong", Toast.LENGTH_SHORT).show()
                            }

                        }
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
    }

    private fun checkUser(){
        val googleSignIn = firebaseAuth.currentUser

        if (googleSignIn != null){
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ID_SIGN_IN){
            Log.d(TAG, "onActivityResult : googleSignInFromIntent")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthGoogle(account)
            } catch(e : Exception){
                Log.d(TAG, "onActivityResult : ${e.message}")
            }

        }
    }

    private fun firebaseAuthGoogle(account : GoogleSignInAccount?){
        Log.d(TAG, "firebaseAuthGoogle : Begin firebase auth with google")

        val credential = GoogleAuthProvider.getCredential(account?.idToken!!, null)
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener {  authResult ->
                    Log.d(TAG, "firebaseAuthGoogle : Login")

                    val firebaseUser = firebaseAuth.currentUser
                    val uid = firebaseUser?.uid
                    val email = firebaseUser?.email

                    Log.d(TAG, "FirebaseAuthGoogle : Uid : $uid")
                    Log.d(TAG, "FirebaseAuthGoogle : Email : $email")

                    firestore.collection("users").whereEqualTo("email", firebaseUser?.email)
                            .get()
                            .addOnSuccessListener {
                                if (it.size() > 0){
                                    Toast.makeText(this, "Your account already added", Toast.LENGTH_SHORT).show()
                                } else {
                                    val user = UserEntity()
                                    user.photo = firebaseUser?.photoUrl.toString()
                                    user.email = firebaseUser?.email
                                    user.username = firebaseUser?.displayName
                                    user.password = ""

                                    firestore.collection("users")
                                            .add(user)
                                            .addOnSuccessListener {
                                                if (authResult.additionalUserInfo!!.isNewUser){
                                                    Log.d(TAG, "FirebaseAuthGoogle : Account create \n$email")
                                                    Toast.makeText(this, "Account Create $email", Toast.LENGTH_SHORT).show()
                                                }
                                                userRepository.loginUser("USERNAME_USER", firebaseUser?.displayName.toString())
                                                userRepository.loginUser("EMAIL_USER", firebaseUser?.email.toString())
                                                userRepository.loginUser("ID_USER", it.id)

                                                startActivity(Intent(this, HomeActivity::class.java))
                                                finish()

                                            }
                                }
                            }
                }
                .addOnFailureListener {
                    Log.d(TAG, "firebaseAuthGoogle : Login Failed due to ${it.message}")
                    Toast.makeText(this, "Login failed due to ${it.message}", Toast.LENGTH_SHORT).show()

                }
    }

}