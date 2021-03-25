package com.example.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.daos.BuyerDao
import com.example.myapplication.individual.BuyerActivity
import kotlinx.android.synthetic.main.activity_buyer_info.*

class buyer_infoActivity : AppCompatActivity() {

    private lateinit var buyerDao: BuyerDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buyer_info)

        buyerDao = BuyerDao()

        buyer_req.setOnClickListener{
            showProgressbar()
            val address = buyer_address.text.toString()
            val requirement = buyer_buying_requirements.text.toString()
            if (address.isNotEmpty() && requirement.isNotEmpty()){
                if(buyerDao.addBuyingRequest(address,requirement)){
                    hideProgressbar()
                    startActivity(Intent(this,BuyerActivity::class.java))
                }
                else{
                    hideProgressbar()
                    Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()
                }
            }
        }

    }
    fun showProgressbar(){
        progressBar.show()
        progressBar.visibility= View.VISIBLE
    }

    fun hideProgressbar(){
        if(progressBar.visibility== View.VISIBLE) {
            progressBar.hide()
            progressBar.visibility = View.INVISIBLE
        }
    }
}