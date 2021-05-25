package com.maulida.pantaujalanku

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.maulida.pantaujalanku.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener(this)
        binding.btnCreate.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_back -> {
                finish()
            }
            R.id.btn_create -> {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }
}