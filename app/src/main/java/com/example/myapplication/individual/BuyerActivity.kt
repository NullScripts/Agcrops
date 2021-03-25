package com.example.myapplication.individual

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.R
import com.example.myapplication.activities.buyer_infoActivity
import kotlinx.android.synthetic.main.activity_buyer.*

class BuyerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer)

        fab_buyer.setOnClickListener{
            val intent = Intent(this,buyer_infoActivity::class.java)
            startActivity(intent)
        }

    }
}