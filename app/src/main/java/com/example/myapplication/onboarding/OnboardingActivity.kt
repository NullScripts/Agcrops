package com.example.myapplication.onboarding

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.MainActivity
import com.example.myapplication.R



class OnboardingActivity : AppCompatActivity() {

    lateinit var preferences: SharedPreferences
    val prefShowIntro = "Intro"
    lateinit var activity : Activity
    lateinit var indicatorsContainer :LinearLayout

    private val introSliderAdapter = IntroSliderAdapter(
        listOf(
            IntroSlide(
                "Helix",
                "Welcome to HELIX a platform for launching your start-up",
                R.drawable.connect
            ),
            IntroSlide(
                "Ideas",
                "Pitch your Idea and get Investment",
                R.drawable.investers
            ),
            IntroSlide(
                "Start-up",
                "interaction between Start-ups and Investors",
                R.drawable.userfriendly
            ),
            IntroSlide(
                "Socialize",
                "Connect with your peers",
                R.drawable.socialize
            )
        )
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        val buttonNext=findViewById<Button>(R.id.buttonNext)
        val textSkipIntro =findViewById<TextView>(R.id.textSkipIntro)
        val introSliderViewPager =findViewById<ViewPager2>(R.id.introSliderViewPager)
        indicatorsContainer = findViewById<LinearLayout>(R.id.indicatorsContainer)


                activity = this
        preferences = getSharedPreferences("IntroSlider", Context.MODE_PRIVATE)

        Log.d("OnBoarding", "onCreate: "+preferences.getBoolean(prefShowIntro,false));
        if ( preferences.getBoolean(prefShowIntro,false)) {
            startActivity(Intent(activity, MainActivity::class.java))
            finish()
        }

        introSliderViewPager.adapter = introSliderAdapter
        setupIndicators()
        setCurrentIndicator(0)

        introSliderViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)

                buttonNext.setOnClickListener{
                    if(introSliderViewPager.currentItem + 1 < introSliderAdapter.itemCount){
                        introSliderViewPager.currentItem += 1
                    }else {
                        Intent(applicationContext,  MainActivity::class.java).also {
                            startActivity(it)
                            val editor = preferences.edit()
                            editor.putBoolean(prefShowIntro, true)
                            editor.apply()
                            finish()

                        }
                    }
                }

                textSkipIntro.setOnClickListener{
                    Intent(applicationContext, MainActivity::class.java).also {
                        startActivity(it)
                        val editor = preferences.edit()
                        editor.putBoolean(prefShowIntro, true)
                        editor.apply()
                        finish()

                    }
                }

            }
        })

    }

    private fun setupIndicators(){
        val indicators = arrayOfNulls<ImageView>(introSliderAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        layoutParams.setMargins(8,0,8,0)
        for(i in indicators.indices){
            indicators[i] = ImageView(applicationContext)
            indicators[i].apply {
                this?.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                this?.layoutParams = layoutParams
            }
            indicatorsContainer.addView(indicators[i])
        }
    }
    private fun setCurrentIndicator(index: Int){
        val childCount = indicatorsContainer.childCount
        for(i in 0 until childCount){
            val imageView = indicatorsContainer[i] as ImageView
            if(i == index){
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            }
            else{
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }
}