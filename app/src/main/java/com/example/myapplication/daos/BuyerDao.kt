package com.example.myapplication.daos

import com.example.myapplication.models.BuyerRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BuyerDao {
    val db = FirebaseDatabase.getInstance()
    val reference = db.getReference("buyers")


    fun addBuyingRequest(address: String , requirements: String){
        val currentUser = FirebaseAuth.getInstance().getCurrentUser()!!.getUid();
        GlobalScope.launch {
            val currentTime = System.currentTimeMillis()
            val buyerRequest = BuyerRequest(address, requirements, currentUser, currentTime)
            reference.child(currentUser).setValue(buyerRequest)
        }
    }
}