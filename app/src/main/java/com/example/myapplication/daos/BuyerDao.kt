package com.example.myapplication.daos

import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BuyerDao {
    private val db = FirebaseDatabase.getInstance()
    val reference1 = db.reference.child("buyers")
    private var flag:Boolean=false

    fun addBuyingRequest(address: String , requirements: String):Boolean{
        val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        GlobalScope.launch {
            //val currentTime = System.currentTimeMillis()
            val userMap = HashMap<String, Any>()
            userMap["address"] = address
            userMap["requirements"] = requirements

            reference1.child(currentUser).setValue(userMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        flag=true


                    }
                }
        }
        return flag
    }



}