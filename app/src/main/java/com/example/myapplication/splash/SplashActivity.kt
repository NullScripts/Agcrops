package com.example.myapplication.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.onboarding.OnboardingActivity

class SplashActivity : AppCompatActivity() {
    lateinit var handler: Handler
    lateinit var bottomAnim: Animation
    lateinit var topAnim: Animation
    lateinit var cardView: CardView
    lateinit var textView1: TextView
    lateinit var textView2: TextView
    lateinit var textView3: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_animation)

        cardView = findViewById(R.id.icon)
        textView1 = findViewById(R.id.appname)
        textView2 = findViewById(R.id.middle)
        textView3 = findViewById(R.id.bottom)

        cardView.setAnimation(topAnim)
        textView1.setAnimation(bottomAnim)
        textView2.setAnimation(bottomAnim)
        textView3.setAnimation(bottomAnim)

        handler = Handler()
        handler.postDelayed(
                {

                    // Delay and Start Activity
                    val intent = Intent(this, OnboardingActivity::class.java)
                    startActivity(intent)
                    finish()

                }, 2000)
    }
}