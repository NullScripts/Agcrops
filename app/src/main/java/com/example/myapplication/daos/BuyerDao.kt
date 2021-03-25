package com.example.myapplication.daos

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BuyerDao {
    private val db = FirebaseDatabase.getInstance()
    val reference = db.reference.child("buyers")
    private var flag:Boolean=false

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