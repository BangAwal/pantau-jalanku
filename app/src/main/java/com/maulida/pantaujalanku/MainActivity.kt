package com.maulida.pantaujalanku

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.maulida.pantaujalanku.databinding.ActivityMainBinding
import com.maulida.pantaujalanku.ui.sign.LoginActivity
import com.maulida.pantaujalanku.ui.sign.SignUpActivity

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener(this)
        binding.btnSignUp.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_login -> {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            R.id.btn_sign_up -> {
                startActivity(Intent(this, SignUpActivity::class.java))
            }
        }
    }
}