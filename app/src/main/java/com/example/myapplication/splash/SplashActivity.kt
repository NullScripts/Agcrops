package com.example.myapplication.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.onboarding.OnboardingActivity

class SplashActivity : AppCompatActivity() {
    lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        handler = Handler()
        handler.postDelayed(
                {

                    // Delay and Start Activity
                    val intent = Intent(this, OnboardingActivity::class.java)
                    startActivity(intent)
                    finish()

                } , 2000)
    }
}