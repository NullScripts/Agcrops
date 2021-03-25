package com.example.myapplication.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myapplication.R
import com.example.myapplication.daos.BuyerDao
import kotlinx.android.synthetic.main.activity_buyer_info.*

class buyer_infoActivity : AppCompatActivity() {

    private lateinit var buyerDao: BuyerDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_info)

        buyerDao = BuyerDao()

        buyer_req.setOnClickListener{
            val address = buyer_address.text.toString()
            val requirement = buyer_buying_requirements.text.toString()
            if (address.isNotEmpty() && requirement.isNotEmpty()){
                buyerDao.addBuyingRequest(address,requirement)
            }
        }

    }
}