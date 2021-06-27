package com.maulida.pantaujalanku.ui.sign

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.maulida.pantaujalanku.R
import com.maulida.pantaujalanku.core.data.UserEntity
import com.maulida.pantaujalanku.core.preference.SetPreferences
import com.maulida.pantaujalanku.core.preference.UserRepository
import com.maulida.pantaujalanku.databinding.ActivitySignUpBinding
import com.maulida.pantaujalanku.ui.HomeActivity


class SignUpActivity : AppCompatActivity(), View.OnClickListener {


    companion object{
        const val FIELD_EMPTY = "This field is empty"
        //const val TAG = "SingUpActivity"
    }

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var fireStore : FirebaseFirestore
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fireStore = FirebaseFirestore.getInstance()

        userRepository = UserRepository.getInstance(SetPreferences(this))

        if (userRepository.isUserLogin()){
            finishAffinity()
            startActivity(Intent(this, HomeActivity::class.java))
        }

        binding.btnBack.setOnClickListener(this)
        binding.btnSaveChanges.setOnClickListener(this)
        binding.checkpass.setOnCheckedChangeListener { _, value ->
            if (value) {
                // Show Password
                binding.tvPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.tvPassverif.transformationMethod = HideReturnsTransformationMethod.getInstance()
            } else {
                // Hide Password
                binding.tvPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.tvPassverif.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }
            R.id.btn_save_changes -> {
                signUp()
            }
        }
    }

    private fun signUp() {
        val username = binding.tvUsername.text.toString()
        val email = binding.tvEmail.text.toString()
        val password = binding.tvPassword.text.toString()
        val passwordVerify = binding.tvPassverif.text.toString()

        with(binding){
            if(username.isEmpty()){
                tvUsername.error = FIELD_EMPTY
                tvUsername.requestFocus()
            } else if (email == ""){
                tvEmail.error = FIELD_EMPTY
                tvEmail.requestFocus()
            } else if (password == "" && password.length < 6){
                tvPassword.error = FIELD_EMPTY
                tvPassword.requestFocus()
            } else if (password.length < 6){
                tvPassword.error = "Minimum 6 character"
                tvPassword.requestFocus()
            } else if (passwordVerify != tvPassword.text.toString()){
                tvPassverif.error = "Your password is different"
                tvPassverif.requestFocus()
            } else {
                saveUser(username, email, password)
            }
        }

    }

    private fun saveUser(username: String, email: String, password: String){

        val userRef = fireStore.collection("users")

        userRef.whereEqualTo("email", email)
                .get()
                .addOnSuccessListener {
                    if (it.size() > 0){
                        Toast.makeText(this, "Your account already added", Toast.LENGTH_SHORT).show()
                    } else {
                        val user = UserEntity()
                        user.photo = ""
                        user.password = password
                        user.username = username
                        user.email = email

                        fireStore.collection("users")
                                .add(user)
                                .addOnSuccessListener{ documentReference ->
                                    val intent = Intent(this, SignUpPhotoActivity::class.java)
                                    userRepository.loginUser("ID_USER", documentReference.id)
                                    userRepository.loginUser("EMAIL_USER", email)
                                    userRepository.loginUser("USERNAME_USER", username)

                                    startActivity(intent)
                                    finish()

                                    Toast.makeText(this, "Success Added", Toast.LENGTH_SHORT).show()
                                    Log.d(
                                        "SignUpActivity",
                                        "document added with Id : ${documentReference.id}"
                                    )
                                }
                                .addOnFailureListener {
                                    Log.w("SignUpActivity", "Something wrong")
                                }
                    }
                }
    }
}