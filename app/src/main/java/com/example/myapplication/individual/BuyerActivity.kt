package com.example.myapplication.individual

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.R
import com.example.myapplication.activities.buyer_infoActivity
import com.example.myapplication.daos.BuyerDao
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_buyer.*

class BuyerActivity : AppCompatActivity() {
    lateinit var buyerDao: BuyerDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer)

        fab_buyer.setOnClickListener{
            buyerDao.addAvailableBuyers(FirebaseAuth.getInstance().currentUser!!.uid)
        }

    }
}