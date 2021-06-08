package com.maulida.pantaujalanku

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.maulida.pantaujalanku.databinding.ActivitySplashScreenBinding

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var tvAnim: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tvAnim = AnimationUtils.loadAnimation(this, R.anim.pj_anim)
        binding.tvPJ.animation = tvAnim

        Handler(mainLooper).postDelayed({

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }, 4000)
    }
}