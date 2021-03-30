package com.example.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.myapplication.R
import com.example.myapplication.daos.BuyerDao
import com.example.myapplication.daos.FertiDao
import com.example.myapplication.individual.FertilizerSellingActivity

import kotlinx.android.synthetic.main.activity_fertilizer_info.*

class fertilizer_infoActivity : AppCompatActivity() {
    private lateinit var fertiDao: FertiDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fertilizer_info)

        fertiDao = FertiDao()

        ferti_req.setOnClickListener{
            showProgressbar()
            val address = fertilizer_address.text.toString()
            val ferti_type = ferti_type.text.toString()
            if (address.isNotEmpty() && ferti_type.isNotEmpty()){
                if(fertiDao.addSellingRequest(address,ferti_type)){
                    hideProgressbar()
                    startActivity(Intent(this, FertilizerSellingActivity::class.java))
                }
                else{
                    hideProgressbar()
                    Toast.makeText(this,"Failed", Toast.LENGTH_SHORT).show()
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