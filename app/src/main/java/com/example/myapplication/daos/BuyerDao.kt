package com.example.myapplication.daos

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.myapplication.activities.buyer_infoActivity
import com.example.myapplication.individual.BuyerActivity
import com.example.myapplication.models.BuyerRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BuyerDao {
    val db = FirebaseDatabase.getInstance()
    val reference = db.reference.child("buyers")
     var flag:Boolean=false

    fun addBuyingRequest(address: String , requirements: String):Boolean{
        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        GlobalScope.launch {
            //val currentTime = System.currentTimeMillis()

            val userMap = HashMap<String, Any>()
            userMap["address"] = address
            userMap["requirements"] = requirements


            reference.child(currentUser).setValue(userMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        flag=true


                    }
                }
        }
        return flag
    }


    fun addAvailableBuyers(curruserID: String ){

        val reference = db.reference.child("availablebuyers")

        GlobalScope.launch {
            val currentTime = System.currentTimeMillis()

            val userMap = HashMap<String, Any>()


            userMap["currentTime"] = currentTime

            reference.child(curruserID).setValue(userMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {


                    }
                }
        }

    }



}